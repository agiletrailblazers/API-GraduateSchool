package com.gs.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.googlecode.ehcache.annotations.Cacheable;
import com.gs.api.domain.course.CourseCategory;
import com.gs.api.domain.course.CourseSearchResponse;

@Service
public class CategoryServiceImpl implements CategoryService {
    
    //@Autowired
    //private CategoryDAO categoryDAO;
    
    @Autowired
    private CourseSearchService courseSearchService;
    

    /*
     * (non-Javadoc)
     * @see com.gs.api.service.CategoryService#getCategories()
     */
    @Cacheable(cacheName="subjectCategoryCache")
    public CourseCategory[] getCategories() throws Exception {
        CourseCategory[] categories = new CourseCategory[0]; 
        CourseSearchResponse response = courseSearchService.searchCourses("", 1, 10, null);
        if (null != response) {
            categories = response.getCategorySubjectFacets();
        }
        return categories;
    }

}
