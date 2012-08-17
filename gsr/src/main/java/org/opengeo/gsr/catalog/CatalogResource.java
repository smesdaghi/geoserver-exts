package org.opengeo.gsr.catalog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CoverageStoreInfo;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.WMSStoreInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.rest.AbstractCatalogListResource;
import org.geoserver.catalog.rest.AbstractCatalogResource;
import org.geoserver.catalog.rest.CatalogFreemarkerHTMLFormat;
import org.geoserver.config.util.XStreamPersister;
import org.geoserver.rest.format.DataFormat;
import org.geoserver.rest.format.ReflectiveJSONFormat;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;

import com.thoughtworks.xstream.XStream;
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
                p.save( data, output );
            }
            
            @Override
            protected Object read(InputStream input)
                    throws IOException {
                XStreamPersister p = xpf.createJSONPersister();
                p.setCatalog(catalog);
                
                configurePersister(p,this);
                return p.load( input, clazz );
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
