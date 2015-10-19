package com.gs.api.domain;

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

