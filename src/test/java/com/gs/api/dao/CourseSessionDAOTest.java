package com.gs.api.dao;

import com.gs.api.dao.CourseSessionDAO.SessionsRowMapper;
import com.gs.api.domain.course.CourseSession;
import com.gs.api.helper.CourseTestHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.ResultSet;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class CourseSessionDAOTest {

    @InjectMocks
    @Autowired
    private CourseSessionDAO sessionDAO;
    
    @Mock
    private JdbcTemplate jdbcTemplate;
    
    private CourseSessionDAO.SessionsRowMapper rowMapper;

    @Value("${sql.course.session.single.query}")
    private String sqlForSingleSession;

    @Captor
    private ArgumentCaptor<Object[]> singleSessionQueryParamsCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        rowMapper = sessionDAO.new SessionsRowMapper();
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testCourseSessionDAO_GetResult() throws Exception {

        when(jdbcTemplate.query(anyString(), any(Object[].class), any(SessionsRowMapper.class)))
            .thenAnswer(new Answer<List<CourseSession>>() {
                @Override
                public List<CourseSession> answer(InvocationOnMock invocation) throws Throwable {
                    return CourseTestHelper.createSessions();
                }
            });
        List<CourseSession> list = sessionDAO.getSessions("12345");
        assertNotNull(list);
        assertEquals(2, list.size());
        
    }
    
    @Test
    public void testCourseSessionDAO_EmptyResultException() throws Exception {

        when(jdbcTemplate.query(anyString(), any(Object[].class), any(SessionsRowMapper.class)))
            .thenThrow(new EmptyResultDataAccessException(1));
        List<CourseSession> list = sessionDAO.getSessions("12345");
        assertNull(list);
        
    }
    
    @Test
    public void testCourseSessionDAO_RuntimeException() throws Exception {

        when(jdbcTemplate.query(anyString(), any(Object[].class), any(SessionsRowMapper.class)))
            .thenThrow(new RuntimeException("random exception"));
        try {
            sessionDAO.getSessions("12345");
            assertTrue(false);   //should not get here
        } catch( Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof Exception);
        }
        
    }
    
    @Test
    public void testSessionDAO_RowMapper_WithPerson() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("CLASS_NO")).thenReturn("12345");
        when(rs.getString("PERSON_NO")).thenReturn("55555");
        CourseSession session = rowMapper.mapRow(rs, 0);
        assertNotNull(session);
        assertEquals("12345", session.getClassNumber());
        assertEquals("55555", session.getInstructor().getId());
    }
    
    @Test
    public void testSessionDAO_RowMapper_WithoutPerson() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("CLASS_NO")).thenReturn("12345");
        when(rs.getString("PERSON_NO")).thenReturn(null);
        CourseSession session = rowMapper.mapRow(rs, 0);
        assertNotNull(session);
        assertEquals("12345", session.getClassNumber());
        assertEquals(null, session.getInstructor());
    }

    @Test
    public void testGetSession() throws Exception {

        String courseId = "4444";
        String sessionId = "55555";
        Object[] expectedQueryParams = new Object[] {courseId, courseId, sessionId};

        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), any(SessionsRowMapper.class))).thenReturn(CourseTestHelper.createSession(sessionId));

        CourseSession session = sessionDAO.getSession(courseId, sessionId);
        assertNotNull("Expected a session to be found", session);
        assertTrue("Wrong session", sessionId.equals(session.getClassNumber()));

        verify(jdbcTemplate).queryForObject(eq(sqlForSingleSession), singleSessionQueryParamsCaptor.capture(), any(SessionsRowMapper.class));
        Object[] capturedQueryParams = singleSessionQueryParamsCaptor.getValue();
        assertNotNull("Expected parameters", capturedQueryParams);
        assertArrayEquals("wrong parameters", expectedQueryParams, capturedQueryParams);
    }

    @Test
    public void testGetSession_NoSessionFound() throws Exception {

        String courseId = "4444";
        String sessionId = "55555";
        Object[] expectedQueryParams = new Object[] {courseId, courseId, sessionId};

        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), any(SessionsRowMapper.class))).thenReturn(null);

        CourseSession session = sessionDAO.getSession(courseId, sessionId);
        assertNull("No session should be found", session);

        verify(jdbcTemplate).queryForObject(eq(sqlForSingleSession), singleSessionQueryParamsCaptor.capture(), any(SessionsRowMapper.class));
        Object[] capturedQueryParams = singleSessionQueryParamsCaptor.getValue();
        assertNotNull("Expected parameters", capturedQueryParams);
        assertArrayEquals("wrong parameters", expectedQueryParams, capturedQueryParams);
    }

    @Test
    public void testGetSession_IncorrectResultSize() throws Exception {

        String courseId = "4444";
        String sessionId = "55555";
        Object[] expectedQueryParams = new Object[] {courseId, courseId, sessionId};

        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), any(SessionsRowMapper.class))).thenThrow(new IncorrectResultSizeDataAccessException(1, 2));

        CourseSession session = sessionDAO.getSession(courseId, sessionId);
        assertNull("No session should be returned", session);

        verify(jdbcTemplate).queryForObject(eq(sqlForSingleSession), singleSessionQueryParamsCaptor.capture(), any(SessionsRowMapper.class));
        Object[] capturedQueryParams = singleSessionQueryParamsCaptor.getValue();
        assertNotNull("Expected parameters", capturedQueryParams);
        assertArrayEquals("wrong parameters", expectedQueryParams, capturedQueryParams);
    }

}
