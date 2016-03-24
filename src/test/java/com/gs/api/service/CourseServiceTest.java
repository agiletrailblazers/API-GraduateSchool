package com.gs.api.service;

import com.gs.api.dao.CourseCompetencyDAO;
import com.gs.api.dao.CourseDAO;
import com.gs.api.dao.CourseSessionDAO;
import com.gs.api.domain.course.Course;
import com.gs.api.domain.course.CourseSession;
import com.gs.api.helper.CourseTestHelper;

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

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class CourseServiceTest {
    
    @InjectMocks
    @Autowired
    private CourseServiceImpl courseService;

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
        when(courseDAO.getCourse(anyString())).thenReturn(CourseTestHelper.createCourse("12345"));
        when(competencyDAO.getCompetency(anyString())).thenReturn(CourseTestHelper.createCompetencyList());
        Course course = courseService.getCourse("12345");
        assertNotNull(course);
        assertEquals("12345001", course.getId());
        assertNotNull(course.getCode());
        assertNotNull(course.getTitle());
        assertNotNull(course.getDescription());
        verify(courseDAO, times(1)).getCourse(anyString());
        verify(competencyDAO, times(1)).getCompetency(anyString());
    }
    
    @Test
    public void testDetail_NoResult() throws Exception {
        when(courseDAO.getCourse(anyString())).thenReturn(null);
        Course course = courseService.getCourse("12345");
        assertNull(course);
        verify(courseDAO, times(1)).getCourse(anyString());
        verify(competencyDAO, times(0)).getCompetency(anyString());
    }
    
    @Test
    public void testGetCourses() throws Exception {
        when(courseDAO.getCourses()).thenReturn(CourseTestHelper.createCourseList());
        List<Course> courses = courseService.getCourses();
        assertNotNull(courses);
        assertEquals(2, courses.size());
        assertEquals("12345001", courses.get(0).getId());
        verify(courseDAO, times(1)).getCourses();
    }
    
    @Test
    public void testGetCourses_NoResult() throws Exception {
        when(courseDAO.getCourses()).thenReturn(null);
        List<Course> courses = courseService.getCourses();
        assertNull(courses);
        verify(courseDAO, times(1)).getCourses();
    }
    
    @Test
    public void testGetSessionsByCourseId() throws Exception {
        when(sessionDAO.getSessionsByCourseId(anyString())).thenReturn(CourseTestHelper.createSessions());
        List<CourseSession> sessions = courseService.getSessionsByCourseId("12345");
        assertNotNull(sessions);
        assertEquals(2, sessions.size());
        assertEquals("1", sessions.get(0).getClassNumber());
        verify(sessionDAO, times(1)).getSessionsByCourseId(anyString());
    }
    
    @Test
    public void testGetSessionById() throws Exception {
        String sessionId = "55555";

        when(sessionDAO.getSessionById(sessionId)).thenReturn(CourseTestHelper.createSession(sessionId));

        CourseSession session = courseService.getSessionById(sessionId);
        assertNotNull("Expected a session to be found", session);
        assertEquals("Wrong session found", sessionId, session.getClassNumber());
    }

    @Test
    public void testGetSessions() throws Exception {
        when(sessionDAO.getSessions(anyString(),anyString())).thenReturn(CourseTestHelper.createSessions());
        List<CourseSession> sessions = courseService.getSessions("C","12345");
        assertNotNull(sessions);
        assertEquals(2, sessions.size());
        assertEquals("1", sessions.get(0).getClassNumber());
        verify(sessionDAO, times(1)).getSessions(anyString(), anyString());
    }





}
