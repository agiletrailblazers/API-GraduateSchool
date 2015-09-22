package com.gs.api.service;

import com.gs.api.domain.CourseSearchResponse;
import com.gs.api.exception.NotFoundException;

public interface CourseSearchService {

    public CourseSearchResponse searchCourses(String search, int page, int numRequested,String[] filter) throws NotFoundException;

}
