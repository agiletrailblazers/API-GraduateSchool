package com.gs.api.search.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.gs.api.domain.course.CourseCategory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class FacetBuilderTest {

    @Autowired
    @InjectMocks
    private FacetBuilder facetBuilder;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testCategorySubjectFacet() throws Exception {
        List<String> categorySubjectFilter = new ArrayList<String>();
        categorySubjectFilter.add("CategoryA~SubjectA");
        categorySubjectFilter.add("12");
        categorySubjectFilter.add("CategoryA~SubjectB");
        categorySubjectFilter.add("4");
        categorySubjectFilter.add("CategoryA~SubjectA|CategoryA~CategoryB");
        categorySubjectFilter.add("12");
        categorySubjectFilter.add("CategoryB~SubjectA");
        categorySubjectFilter.add("101");
        CourseCategory[] category = facetBuilder.buildCategorySubjectFacets(categorySubjectFilter);
        assertEquals(2, category.length);
        assertEquals("CategoryA", category[0].getCategory());
        assertEquals(2, category[0].getSubjectCount());
        assertEquals("CategoryB", category[1].getCategory());
        assertEquals(1, category[1].getSubjectCount());
    }
    
    @Test
    public void testBuildLocationFacets() throws Exception {
        Map<String, Integer> map = new HashMap<>();
        map.put("Washington, DC", 10);
        map.put("Philadelphia, PA", 13);
        map.put("Hazelton, PA", 0);
        map.put("Exclude,PA", 999);
        Map<String, Integer> result = facetBuilder.buildLocationFacets(map);
        assertNotNull(result);
        assertEquals(2, result.size());
    }  
    
    @Test
    public void testRemoveInvalidEntryAndDups() throws Exception {      
        String[] categorySubject = new String[] {"Dog","Cat","Dog","Dog|Cat", null};
        String[] result = facetBuilder.removeInvalidEntryAndDups(categorySubject);
        assertNotNull(result);
        assertEquals(3, result.length);
    }
    
    @Test
    public void testRemoveInvalidEntryAndDups_Null() throws Exception {
        String[] categorySubject = null;
        String[] result = facetBuilder.removeInvalidEntryAndDups(categorySubject);
        assertNotNull(result);
        assertEquals(0, result.length);
    }
    
    @Test
    public void testRemoveInvalidEntryAndDups_EmptyArray() throws Exception {
        String[] categorySubject = new String[0];
        String[] result = facetBuilder.removeInvalidEntryAndDups(categorySubject);
        assertNotNull(result);
        assertEquals(0, result.length);
    }
    
}
