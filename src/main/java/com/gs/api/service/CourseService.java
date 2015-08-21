package com.gs.api.service;

import java.util.List;

import com.gs.api.domain.Course;
import com.gs.api.domain.CourseSession;

public interface CourseService {

    /**
     * Get list of courses.
     * @param filter
     * @return List of courses
     */
    public List<Course> getCourses() throws Exception;
    
    /**
     * Get a single course
     * @param id
     * @return Course
     */
    public Course getCourse(String id) throws Exception;

    /**
     * Get sessions from database id
     * @param id
     * @return List of sessions
     */
    public List<CourseSession> getSessions(String id) throws Exception;
    
}
