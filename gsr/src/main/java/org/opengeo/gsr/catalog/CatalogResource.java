package org.opengeo.gsr.catalog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.eel.kitchen.jsonschema.keyword.draftv4.RequiredKeywordValidator;
import org.eel.kitchen.jsonschema.main.JsonValidationFailureException;
import org.eel.kitchen.jsonschema.main.JsonValidator;
import org.eel.kitchen.jsonschema.main.ValidationReport;
import org.eel.kitchen.jsonschema.syntax.draftv3.PropertiesSyntaxValidator;
import org.eel.kitchen.jsonschema.syntax.draftv3.RequiredSyntaxValidator;
import org.eel.kitchen.util.JsonLoader;
import org.eel.kitchen.util.NodeType;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CoverageStoreInfo;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.WMSStoreInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.rest.AbstractCatalogResource;
import org.geoserver.catalog.rest.CatalogFreemarkerHTMLFormat;
import org.geoserver.config.util.XStreamPersister;
import org.geoserver.platform.ServiceException;
import org.geoserver.rest.format.DataFormat;
import org.geoserver.rest.format.ReflectiveJSONFormat;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;

import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.json.JsonWriter;

import freemarker.ext.beans.CollectionModel;
import freemarker.template.Configuration;
import freemarker.template.SimpleHash;

public class CatalogResource extends AbstractCatalogResource {

    public CatalogResource(Context context, Request request, Response response, 
            Catalog catalog) {
        super(context, request, response, org.opengeo.gsr.types.Catalog.class, catalog);
    }
    
    @Override
    protected ReflectiveJSONFormat createJSONFormat(Request request,Response response) {
        return new ReflectiveJSONFormat() {
            @Override
            protected void write(Object data, OutputStream output)
                    throws IOException {
                XStreamPersister p = new XStreamPersister(new JsonHierarchicalStreamDriver() {
                    public HierarchicalStreamWriter createWriter(Writer writer) {
                        return new JsonWriter(writer, JsonWriter.DROP_ROOT_MODE);
                    }
                });
                p.setCatalog(catalog);
                p.setReferenceByName(true);
                p.setExcludeIds();
                
                configurePersister(p,this);
                
                //Json Validator
                String jsonString = p.getXStream().toXML(data);
                //TODO: Find a better way to resolve the path to the schema(s).
                // - gsr should be in the extension folder
                File CatalogSchema = new File("../../extension/gsr/src/main/resources/schemas/gsr-cs/1.0/catalog.json");

                JsonNode schema = null;
        		schema = JsonLoader.fromPath(CatalogSchema.getCanonicalPath());

        			JsonValidator validator = null;
					try {
						validator = new JsonValidator(schema);
					} catch (JsonValidationFailureException e) {
						// TODO Auto-generated catch block
						throw new ServiceException("JsonValidator: Error reading schema.");
					}
        			
        			//
        	        // It is necessary to unregister keywords before re-registering them.
        	        // Otherwise, an IllegalArgumentException is thrown.
        	        //
        	        validator.unregisterValidator("properties");
        	        validator.unregisterValidator("required");
        			
        	        //
        	        // Note that you MUST provide the type of nodes handled by this keyword,
        	        // even if you don't provide a KeywordValidator: this is for caching reasons.
        	        //
        	        validator.registerValidator("properties",
        	            new PropertiesSyntaxValidator(), null, NodeType.OBJECT);
        	        validator.registerValidator("required",
        	            new RequiredSyntaxValidator(), new RequiredKeywordValidator(),
        	            NodeType.OBJECT);
        			
        	        ValidationReport report = null;
        			StringReader myStringReader = new StringReader(jsonString);

        			JsonNode node = JsonLoader.fromReader((Reader)myStringReader);
                    try {
						report = validator.validate(node);
					} catch (JsonValidationFailureException e) {
						// TODO Auto-generated catch block
						throw new ServiceException("JsonValidator: Error validating json.");
					}
                    
                    if(!report.isSuccess()) {
                    	String error = "";
                        for (final String msg: report.getMessages()) {
                            error += msg + "\n";
                        }
                    	
                    	throw new ServiceException("Invalid json: " + error);
                    }
                
                //TODO: What do we return if the validation fails?
                p.save( data, output );
            }
            
            @Override
            protected Object read(InputStream input)
                    throws IOException {
                XStreamPersister p = xpf.createJSONPersister();
                p.setCatalog(catalog);
                
                configurePersister(p,this);
                return p.load(input, clazz);
            }
        };
    }
//    @Override
//    protected DataFormat createHTMLFormat(Request request, Response response) {
//        return new CatalogHTMLFormat(request,response,this,catalog);
//    }
    
    @Override
    protected Object handleObjectGet() {      
        String ws = getAttribute("folder");
        if(ws != null)
            return new org.opengeo.gsr.types.Catalog(catalog.getWorkspaceByName(ws));
        
       return new org.opengeo.gsr.types.Catalog(catalog);
    }
    
    @Override
    protected void configurePersister(XStreamPersister persister, DataFormat format) {
        persister.getXStream().processAnnotations(org.opengeo.gsr.types.Catalog.class);
    }
    
    
    static class CatalogHTMLFormat extends CatalogFreemarkerHTMLFormat {
        
        Catalog catalog;
        
        public CatalogHTMLFormat(Request request, Response response, Resource resource, Catalog catalog ) {
            super(WorkspaceInfo.class, request, response, resource);
            this.catalog = catalog;
        }
        
        @Override
        protected Configuration createConfiguration(Object data, Class clazz) {
            Configuration cfg = 
                super.createConfiguration(data, clazz);
            cfg.setObjectWrapper(new ObjectToMapWrapper<WorkspaceInfo>(WorkspaceInfo.class) {
                @Override
                protected void wrapInternal(Map properties, SimpleHash model, WorkspaceInfo object) {
                    List<DataStoreInfo> dataStores = catalog.getStoresByWorkspace(object, DataStoreInfo.class);
                    properties.put( "dataStores", new CollectionModel( dataStores, new ObjectToMapWrapper(DataStoreInfo.class) ) );
                    
                    List<CoverageStoreInfo> coverageStores = catalog.getStoresByWorkspace(object, CoverageStoreInfo.class);
                    properties.put( "coverageStores", new CollectionModel( coverageStores, new ObjectToMapWrapper(CoverageStoreInfo.class) ) );
                    
                    List<WMSStoreInfo> wmsStores = catalog.getStoresByWorkspace(object, WMSStoreInfo.class);
                    properties.put( "wmsStores", new CollectionModel( wmsStores, new ObjectToMapWrapper(WMSStoreInfo.class) ) );
                    
                    properties.put( "isDefault",  object.equals( catalog.getDefaultWorkspace() ) );
                }
            });
            
            return cfg;
        }
    };
}
