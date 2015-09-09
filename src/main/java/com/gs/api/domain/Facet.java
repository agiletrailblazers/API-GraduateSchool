package com.gs.api.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class Facet {
    
    private Map<String, String> location;

    public Facet(Map<String, String> location) {
        this.location = location;
    }

    public Map<String, String> getLocation() {
        return location;
    }

    public void setLocation(Map<String, String> location) {
        this.location = location;
    }

}
