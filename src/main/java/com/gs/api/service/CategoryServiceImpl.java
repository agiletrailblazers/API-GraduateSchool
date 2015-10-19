package com.gs.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gs.api.dao.CategoryDAO;
import com.gs.api.domain.CourseCategory;

@Service
public class CategoryServiceImpl implements CategoryService {
    
    @Autowired
    private CategoryDAO categoryDAO;

    /*
     * (non-Javadoc)
     * @see com.gs.api.service.CategoryService#getCategories()
     */
    public CourseCategory[] getCategories() throws Exception {
        return categoryDAO.getCategories();
    }

}
