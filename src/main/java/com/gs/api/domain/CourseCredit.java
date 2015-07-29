package com.gs.api.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.ALWAYS)
public class CourseCredit {

    private String value;
    private CourseCreditType type;

    public CourseCredit() {
        //public constructor
    }
    
    public CourseCredit(String value, CourseCreditType type) {
        this.value = value;
        this.type = type;
    }
    
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public CourseCreditType getType() {
        return type;
    }

    public void setType(CourseCreditType type) {
        this.type = type;
    }

}
