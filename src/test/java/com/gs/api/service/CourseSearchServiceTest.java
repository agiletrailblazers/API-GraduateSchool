package com.gs.api.service;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gs.api.domain.CourseSearchResponse;

public class CourseSearchServiceTest {

    private CourseSearchService courseSearchService;

    @Before
    public void setUp() throws Exception {
        courseSearchService = new CourseSearchServiceImpl();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSearch() throws Exception {

        //TODO:
        //CourseSearchResponse response = courseSearchService.searchCourses("stuff");

    }

}
