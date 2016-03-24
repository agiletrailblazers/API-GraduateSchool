package com.gs.api.dao;

import com.gs.api.dao.CourseSessionDAO.SessionsRowMapper;
import com.gs.api.domain.course.CourseSession;
import com.gs.api.domain.course.CourseSessionDomain;
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
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    
    private CourseSessionDAO.SessionsRowMapper rowMapper;

    @Value("${sql.course.session.query}")
    private String sql;

    @Value("${sql.sessions.query}")
    private String sqlForSessions;

    @Value("${sql.course.session.id.query}")
    private String sqlForSessionById;

    @Value("${sql.sessions.sessiondomain.query}")
    private String sqlForSessionsByDomain;

    @Captor
    private ArgumentCaptor<Object[]> singleSessionQueryParamsCaptor;

    @Captor
    private ArgumentCaptor<HashMap<String,Object>> singleNamedSessionQueryParamsCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        rowMapper = sessionDAO.new SessionsRowMapper();
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testCourseSessionsByCourseIdDAO_GetResult() throws Exception {

        when(jdbcTemplate.query(anyString(), any(Object[].class), any(SessionsRowMapper.class)))
            .thenAnswer(new Answer<List<CourseSession>>() {
                @Override
                public List<CourseSession> answer(InvocationOnMock invocation) throws Throwable {
                    return CourseTestHelper.createSessions();
                }
            });
        List<CourseSession> list = sessionDAO.getSessionsByCourseId("12345");
        assertNotNull(list);
        assertEquals(2, list.size());
        
    }
    
    @Test
    public void testCourseSessionsByDAO_EmptyResultException() throws Exception {

        when(jdbcTemplate.query(anyString(), any(Object[].class), any(SessionsRowMapper.class)))
            .thenThrow(new EmptyResultDataAccessException(1));
        List<CourseSession> list = sessionDAO.getSessionsByCourseId("12345");
        assertNull(list);
        
    }
    
    @Test
    public void testCourseSessionsByCourseIdDAO_RuntimeException() throws Exception {

        when(jdbcTemplate.query(anyString(), any(Object[].class), any(SessionsRowMapper.class)))
            .thenThrow(new RuntimeException("random exception"));
        try {
            sessionDAO.getSessionsByCourseId("12345");
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
    public void testGetSessionById() throws Exception {
        String sessionId ="1123";
        List<String> courseSessionStatus = new ArrayList<String>();
        List<String> courseSessionId = new ArrayList<String>();
        courseSessionStatus.add("C");
        courseSessionStatus.add("S");
        courseSessionId.add(sessionId);
        Map<String,Object> params = new HashMap<String, Object>();
        Map<String,Object> expectedQueryParams = new HashMap<String, Object>();
        expectedQueryParams.put("courseSessionStatus",courseSessionStatus);
        expectedQueryParams.put("courseSessionId",courseSessionId);

        params.put("courseSessionStatus",courseSessionStatus);
        params.put("courseSessionId",courseSessionId);
        when(namedParameterJdbcTemplate.queryForObject(anyString(), any(HashMap.class), any(SessionsRowMapper.class))).thenReturn(CourseTestHelper.createSession(sessionId));

        CourseSession session = sessionDAO.getSessionById(sessionId);
        assertNotNull("Expected a session to be found", session);
        assertTrue("Wrong session", sessionId.equals(session.getClassNumber()));

        verify(namedParameterJdbcTemplate).queryForObject(eq(sqlForSessionById), singleNamedSessionQueryParamsCaptor.capture(), any(SessionsRowMapper.class));
        HashMap<String, Object> capturedQueryParams = singleNamedSessionQueryParamsCaptor.getValue();
        assertNotNull("Expected parameters", capturedQueryParams);
        assertEquals(expectedQueryParams.size(),capturedQueryParams.size());
    }



    @Test
    public void testGetSessionById_NoSessionFound() throws Exception {
        String sessionId ="1123";
        List<String> courseSessionStatus = new ArrayList<String>();
        List<String> courseSessionId = new ArrayList<String>();
        courseSessionStatus.add("C");
        courseSessionStatus.add("S");
        courseSessionId.add(sessionId);
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("courseSessionStatus",courseSessionStatus);
        params.put("courseSessionId",courseSessionId);

        when(namedParameterJdbcTemplate.queryForObject(anyString(), any(HashMap.class), any(SessionsRowMapper.class))).thenReturn(null);

        CourseSession session = sessionDAO.getSessionById(sessionId);
        assertNull("No session should be found", session);

        verify(namedParameterJdbcTemplate).queryForObject(eq(sqlForSessionById), singleNamedSessionQueryParamsCaptor.capture(), any(SessionsRowMapper.class));
        HashMap<String, Object> capturedQueryParams = singleNamedSessionQueryParamsCaptor.getValue();
        assertNotNull("Expected parameters", capturedQueryParams);

    }

    @Test
    public void testGetSessionById_IncorrectResultSize() throws Exception {

        String sessionId ="5555";
        List<String> courseSessionId = new ArrayList<String>();
        courseSessionId.add(sessionId);
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("courseSessionId",courseSessionId);
        Map<String,Object> expectedQueryParams =  new HashMap<String, Object>();

        when(namedParameterJdbcTemplate.queryForObject(anyString(), any(HashMap.class), any(SessionsRowMapper.class))).thenThrow(new IncorrectResultSizeDataAccessException(1, 2));

        CourseSession session = sessionDAO.getSessionById(sessionId);
        assertNull("No session should be returned", session);

        verify(namedParameterJdbcTemplate).queryForObject(eq(sqlForSessionById), singleNamedSessionQueryParamsCaptor.capture(), any(SessionsRowMapper.class));
        HashMap<String,Object> capturedQueryParams = singleNamedSessionQueryParamsCaptor.getValue();
        assertNotNull("Expected parameters", capturedQueryParams);

    }

    @Test
    public void testgetSessionDAO_GetResult() throws Exception {
        when(namedParameterJdbcTemplate.query(anyString(), any(HashMap.class), any(SessionsRowMapper.class))).thenAnswer(new Answer<List<CourseSession>>() {
                    @Override
                    public List<CourseSession> answer(InvocationOnMock invocation) throws Throwable {
                        return CourseTestHelper.createSessions();
                    }
                });
        List<CourseSession> list = sessionDAO.getSessions("C","domin000000000001085");
        assertNotNull(list);
        assertEquals(2, list.size());

    }

    @Test
    public void testgetAllSessionDAO_EmptyResultException() throws Exception {
        when(namedParameterJdbcTemplate.query(anyString(), any(HashMap.class), any(SessionsRowMapper.class)))
                .thenThrow(new EmptyResultDataAccessException(1));
        List<CourseSession> list = sessionDAO.getSessions("C","domin000000000001085");
        assertNull(list);

    }

    @Test
    public void testgetSessionDAO_RuntimeException() throws Exception {
        when(namedParameterJdbcTemplate.query(anyString(), any(HashMap.class), any(SessionsRowMapper.class)))       .thenThrow(new RuntimeException("random exception"));
        try {
            sessionDAO.getSessions("C","123");
            assertTrue(false);   //should not get here
        } catch( Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof Exception);
        }

    }

    @Test
    public void testgetSessionStatusDAO_GetResult() throws Exception {
        when(namedParameterJdbcTemplate.query(anyString(), any(HashMap.class), any(SessionsRowMapper.class))).thenAnswer(new Answer<List<CourseSession>>() {
            @Override
            public List<CourseSession> answer(InvocationOnMock invocation) throws Throwable {
                return CourseTestHelper.createSessions();
            }
        });
        List<CourseSession> list = sessionDAO.getSessions("C",null);
        assertNotNull(list);
        assertEquals(2, list.size());

    }

}
