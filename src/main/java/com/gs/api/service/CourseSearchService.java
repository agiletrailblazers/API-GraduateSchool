package com.gs.api.service;

import com.gs.api.domain.CourseSearchResponse;
import com.gs.api.exception.NotFoundException;

public interface CourseSearchService {

    public CourseSearchResponse searchCourses(String search, int page, int numRequested,String[] filter) throws NotFoundException;

    public String buildSearchString(String search, int start, int numRequested,String filter);

    public String stripAndEncode(String search);
    
    public int[] createNavRange(int currentPage, int totalPages);

}
