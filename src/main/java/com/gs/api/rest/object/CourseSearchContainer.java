package com.gs.api.rest.object;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseSearchContainer {

    @JsonProperty("response")
    private CourseSearchRestResponse response;

    public CourseSearchRestResponse getResponse() {
        return response;
    }

    public void setResponse(CourseSearchRestResponse response) {
        this.response = response;
    }
    
    
}
