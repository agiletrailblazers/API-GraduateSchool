package com.gs.api.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.gs.api.dao.CourseCompetencyDAO.CompetencyRowMapper;
import com.gs.api.dao.CourseSessionDAO.SessionsRowMapper;
import com.gs.api.domain.Course;
import com.gs.api.domain.CourseSession;
import com.gs.api.helper.CourseTestHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class CourseSessionDAOTest {

    @InjectMocks
    @Autowired
    private CourseSessionDAO sessionDAO;
    
    @Mock
    private JdbcTemplate jdbcTemplate;
    
    private CourseSessionDAO.SessionsRowMapper rowMapper;
    
    
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
    public void testSessionDAO_RowMapper() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("CLASS_NO")).thenReturn("12345");
        CourseSession session = rowMapper.mapRow(rs, 0);
        assertNotNull(session);
        assertEquals("12345", session.getClassNumber());
    }
    
}
