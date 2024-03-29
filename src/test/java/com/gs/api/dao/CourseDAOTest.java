package com.gs.api.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.sql.ResultSet;
import java.sql.SQLException;
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

import com.gs.api.dao.CourseDAO.CourseRowMapper;
import com.gs.api.domain.course.Course;
import com.gs.api.domain.course.CourseCreditType;
import com.gs.api.helper.CourseTestHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class CourseDAOTest {

    @InjectMocks
    @Autowired
    private CourseDAO courseDAO;
    
    //private CourseDAO.CourseRowMapper courseRowMapper;
    
    private CourseDAO.CourseDetailRowMapper courseDetailRowMapper;
    
    @Mock
    private JdbcTemplate jdbcTemplate;
    
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        courseDetailRowMapper = courseDAO.new CourseDetailRowMapper();
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testGetCourse_GetResult() throws Exception {

        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), any(CourseRowMapper.class)))
            .thenAnswer(new Answer<Course>() {
                @Override
                public Course answer(InvocationOnMock invocation) throws Throwable {
                    return CourseTestHelper.createCourse("12345");
                }
            });
        Course course = courseDAO.getCourse("12345");
        assertNotNull(course);
        assertEquals("12345001", course.getId());
        assertNotNull(course.getCode());
        assertNotNull(course.getTitle());
        assertNotNull(course.getDescription());
    }
    
    @Test
    public void testGetCourse_EmptyResultException() throws Exception {

        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), any(CourseRowMapper.class)))
            .thenThrow(new EmptyResultDataAccessException(1));
        Course course = courseDAO.getCourse("12345");
        assertNull(course);
        
    }
    
    @Test
    public void testGetCourse_RuntimeException() throws Exception {

        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), any(CourseRowMapper.class)))
            .thenThrow(new RuntimeException("random exception"));
        try {
            courseDAO.getCourse("12345");
            assertTrue(false);   //should not get here
        } catch( Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof Exception);
        }
        
    }
    
    @Test
    public void testGetCourses_GetResult() throws Exception {

        when(jdbcTemplate.query(anyString(), any(CourseRowMapper.class)))
            .thenAnswer(new Answer<List<Course>>() {
                @Override
                public List<Course> answer(InvocationOnMock invocation) throws Throwable {
                    return CourseTestHelper.createCourseList();
                }
            });
        List<Course> courses = courseDAO.getCourses();
        assertNotNull(courses);
        assertEquals(2, courses.size());
        assertEquals("12345001", courses.get(0).getId());
        assertEquals("12345", courses.get(0).getCode());
    }
    
    @Test
    public void testGetCourses_EmptyResultException() throws Exception {

        when(jdbcTemplate.query(anyString(), any(CourseRowMapper.class)))
            .thenThrow(new EmptyResultDataAccessException(1));
        List<Course> courses = courseDAO.getCourses();
        assertNull(courses);
        
    }
    
    @Test
    public void testGetCourses_RuntimeException() throws Exception {

        when(jdbcTemplate.query(anyString(), any(CourseRowMapper.class)))
            .thenThrow(new RuntimeException("random exception"));
        try {
            courseDAO.getCourses();
            assertTrue(false);   //should not get here
        } catch( Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof Exception);
        }
        
    }
    
    @Test
    public void testCourseDAO_RowMapper_TypeACE() throws Exception {
        ResultSet rs = prepareResultSet(CourseCreditType.ACE, false);     
        Course course = courseDetailRowMapper.mapRow(rs, 0);
        assertResultSet(course, CourseCreditType.ACE, false);
    }
    
    @Test
    public void testCourseDAO_RowMapper_TypeCEU() throws Exception {
        ResultSet rs = prepareResultSet(CourseCreditType.CEU, false);     
        Course course = courseDetailRowMapper.mapRow(rs, 0);
        assertResultSet(course, CourseCreditType.CEU, false);
    }
    
    @Test
    public void testCourseDAO_RowMapper_TypeCPE() throws Exception {
        ResultSet rs = prepareResultSet(CourseCreditType.CPE, false);     
        Course course = courseDetailRowMapper.mapRow(rs, 0);
        assertResultSet(course, CourseCreditType.CPE, false);
    }
    
    @Test
    public void testCourseDAO_RowMapper_TypeCPE_Null() throws Exception {
        ResultSet rs = prepareResultSet(CourseCreditType.CPE, false);
        when(rs.getString("CEU_CREDIT")).thenReturn(null);
        when(rs.getString("CPE_CREDIT")).thenReturn(null);
        when(rs.getString("ACE_CREDIT")).thenReturn(null);
        Course course = courseDetailRowMapper.mapRow(rs, 0);
        assertResultSet(course, CourseCreditType.CPE, false);
    }
    
    @Test
    public void testCourseDAO_RowMapper_TypeNone_DefaultInterval() throws Exception {
        ResultSet rs = prepareResultSet(null, true);     
        Course course = courseDetailRowMapper.mapRow(rs, 0);
        assertResultSet(course, null, true);
    }
  
    //make it easier to test assertions on a course result for multiple cases
    private void assertResultSet(Course course, 
            CourseCreditType expectedCreditType, boolean expectDefaultInterval) {
        assertNotNull(course);
        assertEquals("12345000", course.getId());
        assertEquals("12345", course.getCode());
        assertEquals("title", course.getTitle());
        assertEquals(null, course.getDescription().getText());
        assertEquals("formatted-description", course.getDescription().getFormatted());
        assertEquals("type", course.getType());
        assertEquals(new Integer(3), course.getLength().getValue());
        assertEquals((expectDefaultInterval ? "Variable" : "Day"), course.getLength().getInterval());
        if (null != expectedCreditType && null != course.getCredit()) {
            assertEquals("3", course.getCredit().getValue());
            assertEquals(expectedCreditType, course.getCredit().getType());
        } else {
            assertNull(course.getCredit());
        }
    }

    //make it easier to prep a mock result set for multiple test cases
    private ResultSet prepareResultSet(CourseCreditType creditType, boolean expectDefaultInterval) throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("CD_CRS")).thenReturn("12345000");
        when(rs.getString("CD_CRS_COURSE")).thenReturn("12345");
        when(rs.getString("NM_CRS")).thenReturn("title");
        when(rs.getString("DESC_TEXT")).thenReturn("text-description");
        when(rs.getString("DESC_FORMAT")).thenReturn("formatted-description");
        when(rs.getString("TYPE")).thenReturn("type");
        when(rs.getInt("TM_CD_DUR")).thenReturn(3);
        when(rs.getString("TX_CRS_INTERVAL")).thenReturn(expectDefaultInterval ? null : "DY");
        when(rs.getString("CEU_CREDIT")).thenReturn(creditType==CourseCreditType.CEU ? "3":"0");
        when(rs.getString("CPE_CREDIT")).thenReturn(creditType==CourseCreditType.CPE ? "3":"0");
        when(rs.getString("ACE_CREDIT")).thenReturn(creditType==CourseCreditType.ACE ? "3":"0");
        when(rs.getString("COURSE_TYPE")).thenReturn("type");
        return rs;
    }
    
}
