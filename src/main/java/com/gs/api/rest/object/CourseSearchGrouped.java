package com.gs.api.rest.object;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CourseSearchGrouped {
    
    @JsonProperty("course_id")
    private CourseSearchGroup group;

    public CourseSearchGroup getGroup() {
        return group;
    }

    public void setGroup(CourseSearchGroup group) {
        this.group = group;
    }

}
