package com.gs.api.service;

import java.util.List;

import com.gs.api.domain.CourseCategory;
import com.gs.api.domain.CourseSearchResponse;
import com.gs.api.exception.NotFoundException;

public interface CourseSearchService {

    CourseSearchResponse searchCourses(String search, int page, int numRequested,String[] filter) throws NotFoundException;
    
    /**
     * To get the list of categories from the category subject filters list
     * @param categorySubjectFilter filter by this subject list
     * @return CourseCategory
     */
    CourseCategory[] getCategorySubjectFacets(List<String> categorySubjectFilter);

}
