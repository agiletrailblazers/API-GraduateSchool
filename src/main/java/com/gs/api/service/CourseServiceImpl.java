package com.gs.api.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gs.api.dao.CourseCompetencyDAO;
import com.gs.api.dao.CourseDAO;
import com.gs.api.dao.CourseSessionDAO;
import com.gs.api.domain.Course;
import com.gs.api.domain.CourseSession;

@Service
public class CourseServiceImpl implements CourseService {

    static final Logger logger = LoggerFactory.getLogger(CourseService.class);
    
    @Autowired
    private CourseDAO courseDao;
    
    @Autowired 
    private CourseCompetencyDAO competencyDao;
    
    @Autowired
    private CourseSessionDAO sessionDao;
    
    /*
     * (non-Javadoc)
     * @see com.gs.api.service.CourseDetailService#getCourse(java.lang.String)
     */
    @Override
    public Course getCourse(String id) throws Exception {
        //get course object and outcomes
        final Course course = courseDao.getCourse(id);
        if (null != course) {
            course.setOutcomes(competencyDao.getCompetency(id));
        }
        return course;
    }

    /*
     * (non-Javadoc)
     * @see com.gs.api.service.CourseDetailService#getSessions(java.lang.String)
     */
    @Override
    public List<CourseSession> getSessions(String id) throws Exception {
        return sessionDao.getSessions(id);
    }

    /*
     * (non-Javadoc)
     * @see com.gs.api.service.CourseDetailService#getCourses(java.lang.String)
     */
    @Override
    public List<Course> getCourses() throws Exception {
        return courseDao.getCourses();
    }

}
