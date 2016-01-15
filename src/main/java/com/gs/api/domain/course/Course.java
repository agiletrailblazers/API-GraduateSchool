package com.gs.api.domain.course;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.ALWAYS)
public class Course {

    private String id;
    private String code;
    private String title;
    private String type;
    private String objective;
    private String prerequisites;
    private List<String> outcomes;
    private CourseCredit credit;
    private CourseLength length;
    private String segment;
    private CourseDescription description;
    private String[] category;
    private String[] categorySubject;

    public Course() {
    }

    /**
     * Constructor
     *
     * @param courseId The CourseID
     * @param courseTitle The title of the course represented by courseId
     * @param courseDescription the description of the course represented by courseId
     */
    public Course(String courseId, String code, String courseTitle, String courseDescription) {
        this.id = courseId;
        this.code = code;
        this.title = courseTitle;
        this.description = new CourseDescription(courseDescription, null);
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

    public CourseDescription getDescription() {
        return description;
    }

    public void setDescription(CourseDescription description) {
        this.description = description;
    }

    public String[] getCategory() {
        return category;
    }

    public void setCategory(String[] category) {
        this.category = category;
    }

    public String[] getCategorySubject() {
        return categorySubject;
    }

    public void setCategorySubject(String[] categorySubject) {
        this.categorySubject = categorySubject;
    }

}
