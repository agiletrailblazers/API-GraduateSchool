package com.gs.api.domain;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseCategory {

    private String category;
    private CourseSubject[] CourseSubject;

    //default constructor
    public CourseCategory() {
    }
    
    //convenience constructor
    public CourseCategory(CourseSubject[] courseSubject) {
        this.CourseSubject = courseSubject;
    }

    public CourseSubject[] getCourseSubject() {
        return CourseSubject;
    }

    public void setCourseSubject(CourseSubject[] courseSubject) {
        CourseSubject = courseSubject;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
    
}

