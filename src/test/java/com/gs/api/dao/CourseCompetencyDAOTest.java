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
import com.gs.api.helper.CourseTestHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class CourseCompetencyDAOTest {

    @InjectMocks
    @Autowired
    private CourseCompetencyDAO competencyDAO;
    
    @Mock
    private JdbcTemplate jdbcTemplate;
    
    private CourseCompetencyDAO.CompetencyRowMapper rowMapper;
    
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        rowMapper = competencyDAO.new CompetencyRowMapper();
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testCourseCompetencyDAO_GetResult() throws Exception {

        when(jdbcTemplate.query(anyString(), any(Object[].class), any(CompetencyRowMapper.class)))
            .thenAnswer(new Answer<List<String>>() {
                @Override
                public List<String> answer(InvocationOnMock invocation) throws Throwable {
                    return CourseTestHelper.createCompetencyList();
                }
            });
        List<String> list = competencyDAO.getCompetency("12345");
        assertNotNull(list);
        assertEquals(2, list.size());
        
    }
    
    @Test
    public void testCourseCompetencyDAO_EmptyResultException() throws Exception {

        when(jdbcTemplate.query(anyString(), any(Object[].class), any(CompetencyRowMapper.class)))
            .thenThrow(new EmptyResultDataAccessException(1));
        List<String> list = competencyDAO.getCompetency("12345");
        assertNull(list);
        
    }
    
    @Test
    public void testCourseCompetencyDAO_RuntimeException() throws Exception {

        when(jdbcTemplate.query(anyString(), any(Object[].class), any(CompetencyRowMapper.class)))
            .thenThrow(new RuntimeException("random exception"));
        try {
            competencyDAO.getCompetency("12345");
            assertTrue(false);   //should not get here
        } catch( Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof Exception);
        }
        
    }
    
    @Test
    public void testCompetencyDAO_RowMapper() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("NAME")).thenReturn("12345000");
        String value = rowMapper.mapRow(rs, 0);
        assertNotNull(value);
        assertEquals("12345000", value);
    }
    
}
