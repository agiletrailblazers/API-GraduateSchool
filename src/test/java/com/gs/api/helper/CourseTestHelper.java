package com.gs.api.helper;

import java.util.ArrayList;
import java.util.List;

import com.gs.api.domain.Course;
import com.gs.api.domain.CourseCredit;
import com.gs.api.domain.CourseCreditType;
import com.gs.api.domain.CourseLength;

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
        course.setDescription("This is the description of a course and is typically very long");
        CourseCredit credit = new CourseCredit();
        credit.setValue("3");
        credit.setType(CourseCreditType.CPE);
        course.setCredit(credit);
        CourseLength length = new CourseLength();
        length.setValue("30");
        length.setInterval("Days");
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
    
}
