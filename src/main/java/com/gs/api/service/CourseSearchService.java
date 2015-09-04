package com.gs.api.service;

import com.gs.api.domain.CourseSearchResponse;
import com.gs.api.exception.NotFoundException;

public interface CourseSearchService {

    public CourseSearchResponse searchCourses(String search, int start, int numRequested,String requestFilters) throws NotFoundException;
    

    public String buildSearchString(String search, int start, int numRequested,String requestFilters);

    
    public String stripAndEncode(String search);


}
