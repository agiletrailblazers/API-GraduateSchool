package com.gs.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.gs.api.domain.CourseCategory;
import com.gs.api.domain.CourseSearchResponse;
import com.gs.api.helper.CourseTestHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class CategoryServiceTest {

    @InjectMocks
    @Autowired
    private CategoryService categoryService;
    
    @Mock
    private CourseSearchService courseSearchService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetCategory_ResultsFound() throws Exception {
        CourseSearchResponse response = new CourseSearchResponse();
        response.setCategorySubjectFacets(CourseTestHelper.createCategoryResponse());
        when(courseSearchService.searchCourses(anyString(), anyInt(), anyInt(), any())).thenReturn(response);
        CourseCategory[] actual = categoryService.getCategories();
        assertEquals(1, actual.length);
        assertEquals(actual[0].getCategory(), "Math");
        assertEquals(actual[0].getCourseSubject()[0].getSubject(), "Addition");
        verify(courseSearchService, times(1)).searchCourses(anyString(), anyInt(), anyInt(), any());
    }
    
    @Test
    public void testGetCategory_NoResultsFound() throws Exception {
        when(courseSearchService.searchCourses(anyString(), anyInt(), anyInt(), any())).thenReturn(null);
        CourseCategory[] actual = categoryService.getCategories();
        assertNotNull(actual);
        assertEquals(0, actual.length);
        verify(courseSearchService, times(1)).searchCourses(anyString(), anyInt(), anyInt(), any());
    }

}
