package com.gs.api.helper;

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
    
}
