package com.gs.api.service;

import com.gs.api.domain.course.CourseSearchResponse;

public interface CourseSearchService {

    CourseSearchResponse searchCourses(String search, int page, int numRequested,String[] filter) throws Exception;

}
