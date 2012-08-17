package org.opengeo.gsr.types;

import java.util.ArrayList;
import java.util.List;

import org.geoserver.catalog.WorkspaceInfo;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class Catalog {
    final double currentVersion = 10.01;
    
//    @XStreamImplicit(itemFieldName="folders")
    protected List<String> folders = new ArrayList<String>();
   
//    @XStreamImplicit(itemFieldName="services")
    protected List<Service> services = new ArrayList<Service>();
    
    public Catalog() {

    }
    
    public Catalog(org.geoserver.catalog.Catalog catalog) {
        
        for (WorkspaceInfo wi : catalog.getWorkspaces()) {
            folders.add(wi.getName());
        }
        
        services.add(new Service(Service.Type.MapServer, "allLayers"));
    }
    
    public Catalog(WorkspaceInfo ws) {
        services.add(new Service(Service.Type.MapServer, ws.getName()));
    }
    
    public List<String> getFolders() {
        return folders;
    }
    
    public void setFolders(List<String> folders) {
        this.folders = folders;
    }
}
