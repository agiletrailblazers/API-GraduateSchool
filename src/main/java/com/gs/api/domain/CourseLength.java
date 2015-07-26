package com.gs.api.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.ALWAYS)
public class CourseLength {
    
    private String value;
    private String interval;
    
    public CourseLength() {
        //public constructor
    }
    
    public CourseLength(String value, String interval) {
        this.value = value;
        this.interval = interval;
    }
    
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

}
