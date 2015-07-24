package com.gs.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

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
import org.springframework.web.client.RestOperations;

import com.gs.api.domain.CourseSearchResponse;
import com.gs.api.rest.object.CourseSearchContainer;
import com.gs.api.rest.object.CourseSearchDoc;
import com.gs.api.rest.object.CourseSearchRestResponse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class CourseSearchServiceTest {

    @InjectMocks
    @Autowired
    private CourseSearchService courseSearchService;

    @Mock
    private RestOperations restTemplate;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSearch_ResultsFound() throws Exception {

        when(restTemplate.getForObject(anyString(), any())).thenReturn(createCourseContainer());
        CourseSearchResponse response = courseSearchService.searchCourses("stuff");
        assertNotNull(response);
        assertEquals(1, response.getNumFound());
        assertEquals("ABC123", response.getCourses()[0].getId());
        assertFalse(response.isExactMatch());

    }
    
    @Test
    public void testSearch_ExactMatch() throws Exception {

        when(restTemplate.getForObject(anyString(), any())).thenReturn(createCourseContainer());
        CourseSearchResponse response = courseSearchService.searchCourses("ABC123");
        assertNotNull(response);
        assertEquals(1, response.getNumFound());
        assertEquals("ABC123", response.getCourses()[0].getId());
        assertTrue(response.isExactMatch());

    }
    
    @Test
    public void testSearch_NoMatch() throws Exception {

        when(restTemplate.getForObject(anyString(), any())).thenReturn(createCourseContainerNothing());
        CourseSearchResponse response = courseSearchService.searchCourses("find-nothing");
        assertNotNull(response);
        assertEquals(0, response.getNumFound());
        assertNull(response.getCourses());
        assertFalse(response.isExactMatch());

    }

    private Object createCourseContainerNothing() {
        final CourseSearchContainer container = new CourseSearchContainer();
        final CourseSearchRestResponse response = new CourseSearchRestResponse();
        response.setDocs(null);
        response.setNumFound(1);
        response.setStart(1);
        container.setResponse(response);
        return container;
    }

    private Object createCourseContainer() {
        final CourseSearchContainer container = new CourseSearchContainer();
        final CourseSearchRestResponse response = new CourseSearchRestResponse();
        List<CourseSearchDoc> docs = new ArrayList<CourseSearchDoc>();
        CourseSearchDoc doc = new CourseSearchDoc();
        doc.setCourse_id("ABC123");
        doc.setCourse_name("Course Name for ABC123");
        docs.add(doc);
        response.setDocs(docs);
        response.setNumFound(1);
        response.setStart(1);
        container.setResponse(response);
        return container;
    }

}
