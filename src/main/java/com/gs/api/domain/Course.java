package com.gs.api.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.ALWAYS)
public class Course {

    private String id;
    private String code;
    private String title;
    private String description;
    private String type;
    private String objective;
    private String prerequisites;
    private List<String> outcomes;
    private CourseCredit credit;
    private CourseLength length;
    private String segment;
    
    public Course() {
    }

    /**
     * Constructor
     * 
     * @param courseId
     * @param courseTitle
     * @param courseDescription
     */
    public Course(String id, String code, String title, String description) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public String getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(String prerequisites) {
        this.prerequisites = prerequisites;
    }
 
    public CourseCredit getCredit() {
        return credit;
    }

    public void setCredit(CourseCredit credit) {
        this.credit = credit;
    }

    public CourseLength getLength() {
        return length;
    }

    public void setLength(CourseLength length) {
        this.length = length;
    }

    public List<String> getOutcomes() {
        return outcomes;
    }

    public void setOutcomes(List<String> outcomes) {
        this.outcomes = outcomes;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

}
