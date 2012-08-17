package org.opengeo.gsr.types;

import java.util.ArrayList;
import java.util.List;

import org.geoserver.catalog.WorkspaceInfo;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class Catalog {
    final double currentVersion = 10.01;
    
//    @XStreamImplicit(itemFieldName="folders")
    protected List<String> folders;
   
//    @XStreamImplicit(itemFieldName="services")
    protected List<String> services;
    
    public Catalog(org.geoserver.catalog.Catalog catalog) {
        folders = new ArrayList<String>();
        services = new ArrayList<String>();
        
        for (WorkspaceInfo wi : catalog.getWorkspaces()) {
            folders.add(wi.getName());
        }
    }
    
    public List<String> getFolders() {
        return folders;
    }
    
    public void setFolders(List<String> folders) {
        this.folders = folders;
    }
}
