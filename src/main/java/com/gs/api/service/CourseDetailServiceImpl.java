package com.gs.api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gs.api.dao.CourseCompetencyDAO;
import com.gs.api.dao.CourseDAO;
import com.gs.api.dao.CourseSessionDAO;
import com.gs.api.domain.Course;
import com.gs.api.domain.CourseSession;

@Service
public class CourseDetailServiceImpl implements CourseDetailService {

    @Autowired
    private CourseDAO courseDao;
    
    @Autowired 
    private CourseCompetencyDAO competencyDao;
    
    @Autowired
    private CourseSessionDAO sessionDao;
    
    /**
     * Get Course object from database, Need to make a separate call to get
     * course competencies.
     */
    @Override
    public Course getCourse(String id) {
        //get course object and outcomes
        final Course course = courseDao.getCourse(id);
        if (null != course) {
            course.setOutcomes(competencyDao.getCompetency(id));
        }
        return course;
    }

    /**
     * Get sessions from database id
     */
    @Override
    public List<CourseSession> getSessions(String id) {
        return sessionDao.getSessions(id);
    }

}
