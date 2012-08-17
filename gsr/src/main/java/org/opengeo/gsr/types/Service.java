package org.opengeo.gsr.types;

public class Service {
    public enum Type {
        MapServer,
        GeocodeServer,
        GPServer,
        FeatureServer,
        GeometryServer
    }
    
    private String name;
    private Type type;
    
    public Service() {
        
    }
    
    public Service(Type type, String name) {
        this.type = type;
        this.name = name;
    }

}
