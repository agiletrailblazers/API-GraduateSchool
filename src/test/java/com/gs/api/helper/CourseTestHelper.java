package com.gs.api.helper;

import java.util.ArrayList;
import java.util.List;

import com.gs.api.domain.Course;
import com.gs.api.domain.CourseCredit;
import com.gs.api.domain.CourseCreditType;
import com.gs.api.domain.CourseDescription;
import com.gs.api.domain.CourseLength;
import com.gs.api.domain.CourseSession;

public class CourseTestHelper {

    /**
     * Create a course object for testing.
     * @return Course
     */
    public static Course createCourse() { 
        final Course course = new Course();
        course.setId("12345");
        course.setCode("12345");
        course.setTitle("This is the title of a Course");
        course.setDescription(new CourseDescription("This is the description of a course and is typically very long", ""));
        CourseCredit credit = new CourseCredit();
        credit.setValue("3");
        credit.setType(CourseCreditType.CPE);
        course.setCredit(credit);
        CourseLength length = new CourseLength();
        length.setValue(4320);
        length.setInterval("Day");
        course.setLength(length);
        course.setType("Classroom-Day");
        course.setObjective("--- objective ---");
        return course;
    }
    
    /**
     * Create a list of strings
     * return List
     */
    public static List<String> createCompetencyList() {
        List<String> list = new ArrayList<String>();
        list.add("Line number 1");
        list.add("Line number 2");
        return list;
    }

    /**
     * Get a list of sessions
     * @return List
     */
    public static List<CourseSession> createSessions() {
        List<CourseSession> list = new ArrayList<CourseSession>();
        list.add(createSession("1"));
        list.add(createSession("2"));
        return list;
    }
    
    /**
     * Get a single session object
     * @param classNumber
     * @return Session
     */
    private static CourseSession createSession(String classNumber) {
        CourseSession session = new CourseSession();
        session.setClassNumber(classNumber);
        return session;
    }
    
}
