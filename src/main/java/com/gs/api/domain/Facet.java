package com.gs.api.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Created by kaiprout on 9/4/15.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Facet {
    private Map<String, Integer> location;

    public Facet(Map<String, Integer> location) {
        this.location = location;
    }

    public Map<String, Integer> getLocation() {
        return location;
    }

    public void setLocation(Map<String, Integer> location) {
        this.location = location;
    }
}
