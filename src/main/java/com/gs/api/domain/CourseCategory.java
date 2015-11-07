package com.gs.api.domain;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseCategory {

    private String category;
    private CourseSubject[] courseSubject;

    //default constructor
    public CourseCategory() {
    }
    
    //convenience constructor
    public CourseCategory(CourseSubject[] courseSubject) {
        this.courseSubject = courseSubject;
    }

    public CourseSubject[] getCourseSubject() {
        return courseSubject;
    }

    public void setCourseSubject(CourseSubject[] courseSubject) {
        this.courseSubject = courseSubject;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
    
    /**
     * Convenience getter for the count of the number of subjects (not to be confused with count)
     * @return int subject count
     */
    public int getSubjectCount() {
        if (null != courseSubject) {
            return courseSubject.length;
        }
        else {
            return 0;
        }
    }
    
    /**
     * Get a total count of the number of courses for all subjects in this category
     * @return int count
     */
    public int getCount() {
        if (null != courseSubject) {
            int count = 0;
            for (CourseSubject subject : courseSubject) {
                count = count + subject.getCount();
            }
            return count;
        }
        else {
            return 0;
        }
    }
    
}

