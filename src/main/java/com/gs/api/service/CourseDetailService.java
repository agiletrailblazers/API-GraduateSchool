package com.gs.api.service;

import java.util.List;

import com.gs.api.domain.Course;
import com.gs.api.domain.CourseSession;

public interface CourseDetailService {

    public Course getCourse(String id);

    public List<CourseSession> getSessions(String id);
    
}
