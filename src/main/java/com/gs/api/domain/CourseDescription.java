package com.gs.api.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.ALWAYS)
public class CourseDescription {

    private String text;
    private String formatted;

    public CourseDescription() {
        //default constructor
    }

    //for ease of creation
    public CourseDescription(String formatted) {
        this.formatted = formatted;
    }

    //for ease of creation
    public CourseDescription(String text, String formatted) {
        this.text = text;
        this.formatted = formatted;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFormatted() {
        return formatted;
    }

    public void setFormatted(String formatted) {
        this.formatted = formatted;
    }

}
