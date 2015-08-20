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
import com.gs.api.domain.Course;
import com.gs.api.domain.CourseCreditType;
import com.gs.api.helper.CourseTestHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class CourseDAOTest {

    @InjectMocks
    @Autowired
    private CourseDAO courseDAO;
    
    private CourseDAO.CourseRowMapper courseRowMapper;
    
    @Mock
    private JdbcTemplate jdbcTemplate;
    
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        courseRowMapper = courseDAO.new CourseRowMapper();
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testCourseDAO_GetResult() throws Exception {

        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), any(CourseRowMapper.class)))
            .thenAnswer(new Answer<Course>() {
                @Override
                public Course answer(InvocationOnMock invocation) throws Throwable {
                    return CourseTestHelper.createCourse();
                }
            });
        Course course = courseDAO.getCourse("12345");
        assertNotNull(course);
        assertEquals("12345", course.getId());
        assertNotNull(course.getCode());
        assertNotNull(course.getTitle());
        assertNotNull(course.getDescription());
    }
    
    @Test
    public void testCourseDAO_EmptyResultException() throws Exception {

        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), any(CourseRowMapper.class)))
            .thenThrow(new EmptyResultDataAccessException(1));
        Course course = courseDAO.getCourse("12345");
        assertNull(course);
        
    }
    
    @Test
    public void testCourseDAO_RuntimeException() throws Exception {

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
    public void testCourseDAO_RowMapper_TypeACE() throws Exception {
        ResultSet rs = prepareResultSet(CourseCreditType.ACE, false);     
        Course course = courseRowMapper.mapRow(rs, 0);
        assertResultSet(course, CourseCreditType.ACE, false);
    }
    
    @Test
    public void testCourseDAO_RowMapper_TypeCEU() throws Exception {
        ResultSet rs = prepareResultSet(CourseCreditType.CEU, false);     
        Course course = courseRowMapper.mapRow(rs, 0);
        assertResultSet(course, CourseCreditType.CEU, false);
    }
    
    @Test
    public void testCourseDAO_RowMapper_TypeCPE() throws Exception {
        ResultSet rs = prepareResultSet(CourseCreditType.CPE, false);     
        Course course = courseRowMapper.mapRow(rs, 0);
        assertResultSet(course, CourseCreditType.CPE, false);
    }
    
    @Test
    public void testCourseDAO_RowMapper_TypeNone_DefaultInterval() throws Exception {
        ResultSet rs = prepareResultSet(null, true);     
        Course course = courseRowMapper.mapRow(rs, 0);
        assertResultSet(course, null, true);
    }
    
    @Test
    public void testCalculateDuration_Day() throws Exception {
        Integer duration = courseRowMapper.calculateCourseDuration("Day", 4320);
        assertEquals(new Integer(3), duration);
    }
    
    @Test
    public void testCalculateDuration_Yr() throws Exception {
        Integer duration = courseRowMapper.calculateCourseDuration("Yr", 525600);
        assertEquals(new Integer(1), duration);
    }
    
    @Test
    public void testCalculateDuration_Wk() throws Exception {
        Integer duration = courseRowMapper.calculateCourseDuration("Wk", 10080);
        assertEquals(new Integer(1), duration);
    }
    
    @Test
    public void testCalculateDuration_Mth() throws Exception {
        Integer duration = courseRowMapper.calculateCourseDuration("Mth", 43200);
        assertEquals(new Integer(1), duration);
    }
    
    @Test
    public void testCalculateDuration_Hr() throws Exception {
        Integer duration = courseRowMapper.calculateCourseDuration("Hr", 60);
        assertEquals(new Integer(1), duration);
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
        assertEquals(new Integer((expectDefaultInterval ? 4320 : 3)), course.getLength().getValue());
        assertEquals((expectDefaultInterval ? "Variable" : "Day"), course.getLength().getInterval());
        if (null != expectedCreditType) {
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
        when(rs.getString("COURSE_TYPE")).thenReturn("type");
        when(rs.getInt("TM_CD_DUR")).thenReturn(4320);
        when(rs.getString("TX_CRS_INTERVAL")).thenReturn(expectDefaultInterval ? null : "Day");
        when(rs.getString("CEU_CREDIT")).thenReturn(creditType==CourseCreditType.CEU ? "3":"0");
        when(rs.getString("CPE_CREDIT")).thenReturn(creditType==CourseCreditType.CPE ? "3":"0");
        when(rs.getString("ACE_CREDIT")).thenReturn(creditType==CourseCreditType.ACE ? "3":"0");
        when(rs.getString("COURSE_TYPE")).thenReturn("type");
        return rs;
    }
    
}
