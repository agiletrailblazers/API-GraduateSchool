package com.gs.api.rest.object;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseSearchContainer {

    @JsonProperty("grouped")
    private CourseSearchGrouped grouped;
    
    public CourseSearchGrouped getGrouped() {
        return grouped;
    }

    public void setGrouped(CourseSearchGrouped grouped) {
        this.grouped = grouped;
    }
    
}
