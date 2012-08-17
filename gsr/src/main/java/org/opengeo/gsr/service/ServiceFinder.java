package org.opengeo.gsr.service;

import org.geoserver.catalog.rest.AbstractCatalogFinder;
import org.geoserver.config.GeoServer;
import org.geoserver.ows.Dispatcher;
import org.geoserver.wms.WMS;
import org.opengeo.gsr.catalog.CatalogListResource;
import org.opengeo.gsr.catalog.CatalogResource;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;

public class ServiceFinder extends AbstractCatalogFinder {

    private GeoServer geoServer;

    private WMS wms;

    private Dispatcher dispatcher;

    protected ServiceFinder(GeoServer geoServer, WMS wms, Dispatcher dispatcher) {
        super(geoServer.getCatalog());
        this.geoServer = geoServer;
        this.wms = wms;
        this.dispatcher = dispatcher;
    }

    public Resource findTarget(Request request, Response response) {
        Resource resource = null;
        
        try {
            String folder = getAttribute(request, "folder");

            if (folder == null && request.getMethod() == Method.GET) {
                resource = new CatalogResource(null, request, response, catalog);
            }
            
            if (folder != null) { //check that folder(workspace) exists
                if (catalog.getWorkspaceByName(folder) == null) {
                    throw new Exception("Workspace Does Not Exist");
                }
                
                resource = new CatalogResource(null, request, response, catalog);
            }
        } catch (Exception e) {
            response.setEntity("NOT IMPLEMENTED", MediaType.TEXT_HTML);
            resource = new Resource(getContext(), request, response);
        }
        return resource;
    }

}
