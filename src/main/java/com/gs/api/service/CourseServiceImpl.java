package com.gs.api.service;

import com.gs.api.dao.CourseCompetencyDAO;
import com.gs.api.dao.CourseDAO;
import com.gs.api.dao.CourseSessionDAO;
import com.gs.api.domain.course.Course;
import com.gs.api.domain.course.CourseSession;

import com.gs.api.search.util.SessionQueryParamsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    static final Logger logger = LoggerFactory.getLogger(CourseService.class);
    
    @Autowired
    private CourseDAO courseDao;
    
    @Autowired 
    private CourseCompetencyDAO competencyDao;
    
    @Autowired
    private CourseSessionDAO sessionDao;

    @Autowired
    private SessionQueryParamsBuilder sessionQueryParamsBuilder;
    
    /*
     * (non-Javadoc)
     * @see com.gs.api.service.CourseService#getCourse(java.lang.String)
     */
    @Override
    public Course getCourse(String idOrCode) throws Exception {
        //get course object and outcomes
        final Course course = courseDao.getCourse(idOrCode);
        if (null != course) {
            course.setOutcomes(competencyDao.getCompetency(course.getId()));
        }
        return course;
    }

    /*
     * (non-Javadoc)
     * @see com.gs.api.service.CourseService#getSessions(java.lang.String)
     */
    @Override
    public List<CourseSession> getSessions(String id) throws Exception {
        return sessionDao.getSessions(id);
    }

    /*
     * (non-Javadoc)
     * @see com.gs.api.service.CourseService#getSession(java.lang.String)
     */
    @Override
    public CourseSession getSession(String sessionId) throws Exception {
        return sessionDao.getSession(sessionId);
    }

    /*
     * (non-Javadoc)
     * @see com.gs.api.service.CourseService#getCourses()
     */
    @Override
    public List<Course> getCourses() throws Exception {
        return courseDao.getCourses();
    }

    /*
     * (non-Javadoc)
     * @see com.gs.api.service.CourseService#getCourses()
    */
    @Override
    public List<CourseSession> getAllSessions(String courseStatus, String sessionDomain ) throws Exception {
        Map<String,Object> params = sessionQueryParamsBuilder.buildCourseSessionsQueryParams(courseStatus, sessionDomain);
        return sessionDao.getAllSessions(params, courseStatus, sessionDomain);
    }

}
