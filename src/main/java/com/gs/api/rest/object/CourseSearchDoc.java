package com.gs.api.rest.object;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseSearchDoc {

    private String course_name;
    private String course_id;
    private String course_code;
    private String course_desc_obj;
    private String course_description;
    private String course_notes;

    public String getCourse_name() {
        return course_name;
    }

    public void setCourse_name(String course_name) {
        this.course_name = course_name;
    }

    public String getCourse_id() {
        return course_id;
    }

    public void setCourse_id(String course_id) {
        this.course_id = course_id;
    }

    public String getCourse_desc_obj() {
        return course_desc_obj;
    }

    public void setCourse_desc_obj(String course_desc_obj) {
        this.course_desc_obj = course_desc_obj;
    }

    public String getCourse_description() {
        return course_description;
    }

    public void setCourse_description(String course_description) {
        this.course_description = course_description;
    }

    public String getCourse_notes() {
        return course_notes;
    }

    public void setCourse_notes(String course_notes) {
        this.course_notes = course_notes;
    }

    public String getCourse_code() {
        return course_code;
    }

    public void setCourse_code(String course_code) {
        this.course_code = course_code;
    }

}
