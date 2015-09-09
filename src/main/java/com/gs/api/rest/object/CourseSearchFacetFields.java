package com.gs.api.rest.object;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CourseSearchFacetFields {

    @JsonProperty("city_state")
    private List<String> cityState;

    public List<String> getCityState() {
        return cityState;
    }

    public void setCityState(List<String> cityState) {
        this.cityState = cityState;
    }
    
}