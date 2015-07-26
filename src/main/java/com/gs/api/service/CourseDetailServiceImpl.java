package com.gs.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gs.api.dao.CourseDAO;
import com.gs.api.domain.Course;

@Service
public class CourseDetailServiceImpl implements CourseDetailService {

    @Autowired
    private CourseDAO dao;
    
    /**
     * Get Course object from database calls and populate
     */
    @Override
    public Course getCourse(String id) {
        //get course object
        final Course course = dao.getCourse(id); 
        //TODO: get schedule
        return course;
    }

}
