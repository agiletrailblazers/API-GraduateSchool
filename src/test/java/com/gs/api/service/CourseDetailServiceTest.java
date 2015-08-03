package com.gs.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.gs.api.dao.CourseCompetencyDAO;
import com.gs.api.dao.CourseDAO;
import com.gs.api.dao.CourseSessionDAO;
import com.gs.api.domain.Course;
import com.gs.api.domain.CourseSession;
import com.gs.api.helper.CourseTestHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class CourseDetailServiceTest {
    
    @InjectMocks
    @Autowired
    private CourseDetailServiceImpl courseDetailService;

    @Mock
    private CourseDAO courseDAO;
    
    @Mock
    private CourseCompetencyDAO competencyDAO;
    
    @Mock
    private CourseSessionDAO sessionDAO;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testDetail_ResultsFound() throws Exception {
        when(courseDAO.getCourse(anyString())).thenReturn(CourseTestHelper.createCourse());
        when(competencyDAO.getCompetency(anyString())).thenReturn(CourseTestHelper.createCompetencyList());
        Course course = courseDetailService.getCourse("12345");
        assertNotNull(course);
        assertEquals("12345", course.getId());
        assertNotNull(course.getCode());
        assertNotNull(course.getTitle());
        assertNotNull(course.getDescription());
        verify(courseDAO, times(1)).getCourse(anyString());
        verify(competencyDAO, times(1)).getCompetency(anyString());
    }
    
    @Test
    public void testDetail_NoResult() throws Exception {
        when(courseDAO.getCourse(anyString())).thenReturn(null);
        Course course = courseDetailService.getCourse("12345");
        assertNull(course);
        verify(courseDAO, times(1)).getCourse(anyString());
        verify(competencyDAO, times(0)).getCompetency(anyString());
    }
    
    @Test
    public void testSessions() throws Exception {
        when(sessionDAO.getSessions(anyString())).thenReturn(CourseTestHelper.createSessions());
        List<CourseSession> sessions = courseDetailService.getSessions("12345");
        assertNotNull(sessions);
        assertEquals(2, sessions.size());
        assertEquals("1", sessions.get(0).getClassNumber());
        verify(sessionDAO, times(1)).getSessions(anyString());
    }
    
    
    
}
