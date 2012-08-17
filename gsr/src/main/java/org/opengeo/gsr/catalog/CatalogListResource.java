package org.opengeo.gsr.catalog;

import java.util.Collection;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.rest.AbstractCatalogListResource;
import org.geoserver.rest.format.DataFormat;
import org.opengeo.gsr.catalog.CatalogResource.CatalogHTMLFormat;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class CatalogListResource extends AbstractCatalogListResource {

    public CatalogListResource(Context context, Request request, Response response, 
            Catalog catalog) {
        super(context, request, response, WorkspaceInfo.class, catalog);
    }

    @Override
    protected Collection handleListGet() throws Exception {
        return catalog.getWorkspaces();
    }

//    @Override
//    protected DataFormat createHTMLFormat(Request request, Response response) {
//        return new CatalogHTMLFormat(request,response,this,catalog);
//    }

}
