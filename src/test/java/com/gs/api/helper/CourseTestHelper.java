package com.gs.api.helper;

import com.gs.api.domain.course.Course;
import com.gs.api.domain.course.CourseCategory;
import com.gs.api.domain.course.CourseCredit;
import com.gs.api.domain.course.CourseCreditType;
import com.gs.api.domain.course.CourseDescription;
import com.gs.api.domain.course.CourseInstructor;
import com.gs.api.domain.course.CourseLength;
import com.gs.api.domain.course.CourseSession;
import com.gs.api.domain.course.CourseSubject;

import java.util.ArrayList;
import java.util.List;

public class CourseTestHelper {

    /**
     * Create a course object for testing.
     * @return Course
     */
    public static Course createCourse(String code) { 
        final Course course = new Course();
        course.setId(code + "001");
        course.setCode(code);
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
     * Create a list of courses
     */
    public static List<Course> createCourseList() {
        List<Course> courses = new ArrayList<Course>();
        courses.add(createCourse("12345"));
        courses.add(createCourse("67890"));
        return courses;
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
    public static CourseSession createSession(String classNumber) {
        CourseSession session = new CourseSession();
        session.setClassNumber(classNumber);
        CourseInstructor instructor = new CourseInstructor();
        instructor.setId("1");
        instructor.setFirstName("Steve");
        instructor.setLastName("Jobs");
        session.setInstructor(instructor);
        return session;
    }
    
    /** 
     * Create category for mocks 
     * @return CourseCategory array
     */
    public static CourseCategory[] createCategoryResponse() {
        CourseCategory[] categories = new CourseCategory[1];
        categories[0] = new CourseCategory();
        categories[0].setCategory("Math");
        CourseSubject[] subjects = new CourseSubject[2];
        subjects[0] = new CourseSubject("Addition", "Math/Addition", 1);
        subjects[1] = new CourseSubject("Subtration", "Math/Subtration", 2);
        categories[0].setCourseSubject(subjects);
        return categories;
    }

    
}
