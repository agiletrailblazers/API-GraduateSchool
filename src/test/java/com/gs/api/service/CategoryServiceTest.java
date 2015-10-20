package com.gs.api.service;

import static org.junit.Assert.assertEquals;
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

import com.gs.api.dao.CategoryDAO;
import com.gs.api.domain.CourseCategory;
import com.gs.api.helper.CourseTestHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class CategoryServiceTest {

    @InjectMocks
    @Autowired
    private CategoryService categoryService;
    
    @Mock
    private CategoryDAO categoryDAO;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetCategory_ResultsFound() throws Exception {
        when(categoryDAO.getCategories()).thenReturn(CourseTestHelper.createCategoryResponse());
        CourseCategory[] actual = categoryService.getCategories();
        assertEquals(1, actual.length);
        assertEquals(actual[0].getCategory(), "Math");
        assertEquals(actual[0].getCourseSubject()[0].getSubject(), "Addition");
        verify(categoryDAO, times(1)).getCategories();

    }

}
