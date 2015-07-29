package com.gs.api.service;

import com.gs.api.domain.CourseSearchResponse;

public interface CourseSearchService {

    public CourseSearchResponse searchCourses(String search, int start, int numRequested);
    
    public String buildSearchString(String endpoint, String search, int start, int numRequested);
    
    public String stripAndEncode(String search);

}
