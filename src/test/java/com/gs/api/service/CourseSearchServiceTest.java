package com.gs.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Value("${course.search.solr.endpoint}")
    private String courseSearchSolrEndpoint;
    
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

    @SuppressWarnings("unchecked")
    @Test
    public void testSearch_ResultsFound() throws Exception {

        ResponseEntity<CourseSearchContainer> responseEntity = new ResponseEntity<CourseSearchContainer>(
                createCourseContainer(0, 224), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
            .thenReturn(responseEntity);
        CourseSearchResponse response = courseSearchService.searchCourses("stuff", 0, 100);
        assertNotNull(response);
        assertEquals(224, response.getNumFound());
        assertEquals(0,  response.getStart());
        assertEquals(100,  response.getStartNext());
        assertEquals("ABC123", response.getCourses()[0].getId());
        assertFalse(response.isExactMatch());
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));

    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testSearch_ExactMatch() throws Exception {

        ResponseEntity<CourseSearchContainer> responseEntity = new ResponseEntity<CourseSearchContainer>(
                createCourseContainer(0, 1), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
            .thenReturn(responseEntity);
        CourseSearchResponse response = courseSearchService.searchCourses("ABC123", 0, 100);
        assertNotNull(response);
        assertEquals(1, response.getNumFound());
        assertEquals("ABC123", response.getCourses()[0].getId());
        assertTrue(response.isExactMatch());
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));

    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testSearch_NoMatch() throws Exception {

        ResponseEntity<CourseSearchContainer> responseEntity = new ResponseEntity<CourseSearchContainer>(
                createCourseContainerNothing(), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
            .thenReturn(responseEntity);
        CourseSearchResponse response = courseSearchService.searchCourses("find-nothing", 0, 100);
        assertNotNull(response);
        assertEquals(1, response.getNumFound());
        assertEquals(0, response.getStart());
        assertEquals(-1, response.getStartNext());
        assertNull(response.getCourses());
        assertFalse(response.isExactMatch());
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));

    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testSearch_LastPage() throws Exception {

        ResponseEntity<CourseSearchContainer> responseEntity = new ResponseEntity<CourseSearchContainer>(
                createCourseContainer(100, 162), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
            .thenReturn(responseEntity);
        CourseSearchResponse response = courseSearchService.searchCourses("stuff", 100, 100);
        assertNotNull(response);
        assertEquals(162, response.getNumFound());
        assertEquals(100, response.getStart());
        assertEquals(-1, response.getStartNext());
        assertEquals("ABC123", response.getCourses()[0].getId());
        assertFalse(response.isExactMatch());
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));

    }
    
    @Test
    public void buildSearchString() {
        
        //single term
        final String SINGLE_TERM_RESULT = "http://ec2-54-175-112-131.compute-1.amazonaws.com:8983/solr/gs_solr/select?q=(course_name:(*fraud*))^5 OR (course_id:(*fraud*)) OR (course_description:(*fraud*)) OR (course_desc_obj:(*fraud*))&start=0&rows=100&wt=json&indent=true";            
        String endpoint = courseSearchService.buildSearchString(courseSearchSolrEndpoint, "fraud", 0, 100);
        System.out.println(endpoint);
        assertEquals(SINGLE_TERM_RESULT, endpoint);
    
        //two terms
        final String DOUBLE_TERM_RESULT = "http://ec2-54-175-112-131.compute-1.amazonaws.com:8983/solr/gs_solr/select?q=(course_name:(*Project* AND *Management*))^5 OR (course_id:(*Project* AND *Management*)) OR (course_description:(*Project* AND *Management*)) OR (course_desc_obj:(*Project* AND *Management*))&start=0&rows=100&wt=json&indent=true";
        endpoint = courseSearchService.buildSearchString(courseSearchSolrEndpoint, "Project Management", 0, 100);
        System.out.println(endpoint);
        assertEquals(DOUBLE_TERM_RESULT, endpoint);
        
    }
    
    @Test
    public void testStripAndEncode() {
        
        String result = courseSearchService.stripAndEncode("#&^%+-||!(){}[]\"~*?:\\");
        assertEquals("\\+\\-\\||\\!\\(\\)\\{\\}\\[\\]\\\"\\~\\*\\?\\:\\\\", result);
        
    }

    private CourseSearchContainer createCourseContainerNothing() {
        final CourseSearchContainer container = new CourseSearchContainer();
        final CourseSearchRestResponse response = new CourseSearchRestResponse();
        response.setDocs(null);
        response.setNumFound(1);
        response.setStart(0);
        container.setResponse(response);
        return container;
    }

    private CourseSearchContainer createCourseContainer(int start, int numFound) {
        final CourseSearchContainer container = new CourseSearchContainer();
        final CourseSearchRestResponse response = new CourseSearchRestResponse();
        List<CourseSearchDoc> docs = new ArrayList<CourseSearchDoc>();
        CourseSearchDoc doc = new CourseSearchDoc();
        doc.setCourse_id("ABC123");
        doc.setCourse_name("Course Name for ABC123");
        docs.add(doc);
        response.setDocs(docs);
        response.setNumFound(numFound);
        response.setStart(start);
        container.setResponse(response);
        return container;
    }
  
}
