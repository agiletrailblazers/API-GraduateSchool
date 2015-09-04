package com.gs.api.rest.object;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by kaiprout on 9/4/15.
 */
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