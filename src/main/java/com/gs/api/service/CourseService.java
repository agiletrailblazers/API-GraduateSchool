package com.gs.api.service;

import com.gs.api.domain.course.Course;
import com.gs.api.domain.course.CourseSession;

import java.util.List;

public interface CourseService {

    /**
     * Get list of courses.
     * @return List of courses
     */
    List<Course> getCourses() throws Exception;

    /**
     * Get a single course
     * @param id get a course by this id
     * @return Course
     */
    Course getCourse(String id) throws Exception;

    /**
     * Get sessions from database id
     * @param id get a session by this id
     * @return List of sessions
     */
    List<CourseSession> getSessions(String id) throws Exception;

    /**
     * Get session from database by session id
     * @param sessionId the session id
     * @return the session
     */
    CourseSession getSession(String sessionId) throws Exception;

}
