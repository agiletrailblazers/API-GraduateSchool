package com.gs.api.service;

import com.gs.api.domain.CourseCategory;

public interface CategoryService {

    /**
     * Get categories and subjects
     * @return List of course categories and subjects
     * @throws Exception
     */
    CourseCategory[] getCategories() throws Exception;
}