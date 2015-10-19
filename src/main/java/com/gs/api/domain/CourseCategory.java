package com.gs.api.domain;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseCategory {

    private CourseSubject[] CourseSubject;

    public CourseCategory(CourseSubject[] courseSubject) {
        this.CourseSubject = courseSubject;
    }

    public CourseSubject[] getCourseSubject() {
        return CourseSubject;
    }

    public void setCourseSubject(CourseSubject[] courseSubject) {
        CourseSubject = courseSubject;
    }
}

