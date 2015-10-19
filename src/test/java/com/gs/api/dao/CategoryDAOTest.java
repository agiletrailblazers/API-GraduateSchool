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
import java.util.ArrayList;
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
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.gs.api.dao.LocationDAO.LocationRowMapper;
import com.gs.api.domain.CourseCategory;
import com.gs.api.domain.Location;
import com.gs.api.helper.CourseTestHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class CategoryDAOTest {

    @InjectMocks
    @Autowired
    private CategoryDAO categoryDAO;

    @Mock
    private JdbcTemplate jdbcTemplate;
    
    private CategoryDAO.CategoryExtractor categoryExtractor;    
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        categoryExtractor = categoryDAO.new CategoryExtractor();
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testCategoryDAO_GetResult() throws Exception {
        when(jdbcTemplate.query(anyString(), any(ResultSetExtractor.class)))
            .thenAnswer(new Answer<CourseCategory[]>() {
                @Override
                public CourseCategory[] answer(InvocationOnMock invocation) throws Throwable {
                    return CourseTestHelper.createCategoryResponse();
                }
            });
        CourseCategory[] categories = categoryDAO.getCategories();
        assertNotNull(categories);
        assertEquals(1, categories.length);
    }
    
    @Test
    public void testCategoryDAO_EmptyResultException() throws Exception {
        when(jdbcTemplate.query(anyString(), any(ResultSetExtractor.class)))
            .thenThrow(new EmptyResultDataAccessException(1));
        CourseCategory[] categories = categoryDAO.getCategories();
        assertNull(categories);
    }

    @Test
    public void testCategoryDAO_RuntimeException() throws Exception {
        when(jdbcTemplate.query(anyString(), any(ResultSetExtractor.class)))
            .thenThrow(new RuntimeException("random exception"));
        try {
            categoryDAO.getCategories();
            assertTrue(false);   //should not get here
        } catch( Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof Exception);
        }
    }
    
    @Test
    public void testLocationDAO_RowMapper_NoRows() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(false);
        CourseCategory[] categories = categoryExtractor.extractData(rs);
        assertNotNull(categories);
        assertEquals(0, categories.length);
    }
    
    @Test
    public void testLocationDAO_RowMapper_OneRow() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true).thenReturn(false);
        when(rs.getString("CATEGORY")).thenReturn("Math");
        when(rs.getString("SUBJECT")).thenReturn("Addition");
        when(rs.getString("SUBJECT_CATEGORY")).thenReturn("Math/Addition");
        CourseCategory[] categories = categoryExtractor.extractData(rs);
        assertNotNull(categories);
        assertEquals(1, categories.length);
        assertEquals("Math", categories[0].getCategory());
        assertEquals("Addition", categories[0].getCourseSubject()[0].getSubject());
    }
    
    @Test
    public void testLocationDAO_RowMapper_TwoRows() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(rs.getString("CATEGORY")).thenReturn("Math").thenReturn("Cooking");
        when(rs.getString("SUBJECT")).thenReturn("Addition").thenReturn("Grilling");
        when(rs.getString("SUBJECT_CATEGORY")).thenReturn("Math/Addition").thenReturn("Cooking/Grilling");
        CourseCategory[] categories = categoryExtractor.extractData(rs);
        assertNotNull(categories);
        assertEquals(2, categories.length);
        assertEquals("Math", categories[0].getCategory());
        assertEquals("Addition", categories[0].getCourseSubject()[0].getSubject());
        assertEquals("Cooking", categories[1].getCategory());
        assertEquals("Grilling", categories[1].getCourseSubject()[0].getSubject());
    }
    
    @Test
    public void testLocationDAO_RowMapper_TwoCategories() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(rs.getString("CATEGORY")).thenReturn("Math").thenReturn("Math");
        when(rs.getString("SUBJECT")).thenReturn("Addition").thenReturn("Subtration");
        when(rs.getString("SUBJECT_CATEGORY")).thenReturn("Math/Addition").thenReturn("Math/Subtration");
        CourseCategory[] categories = categoryExtractor.extractData(rs);
        assertNotNull(categories);
        assertEquals(1, categories.length);
        assertEquals("Math", categories[0].getCategory());
        assertEquals("Addition", categories[0].getCourseSubject()[0].getSubject());
        assertEquals("Subtration", categories[0].getCourseSubject()[1].getSubject());
    }
    
}
