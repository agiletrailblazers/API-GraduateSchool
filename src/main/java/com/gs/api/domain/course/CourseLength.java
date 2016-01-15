package com.gs.api.domain.course;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.ALWAYS)
public class CourseLength {
    
    private Integer value;
    private String interval;
    
    public CourseLength() {
        //public constructor
    }
    
    public CourseLength(Integer value, String interval) {
        this.value = value;
        this.interval = interval;
    }
    
    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

}
