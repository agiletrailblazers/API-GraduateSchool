package com.gs.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestOperations;

import com.gs.api.domain.CourseSearchResponse;
import com.gs.api.exception.NotFoundException;
import com.gs.api.rest.object.CourseSearchContainer;
import com.gs.api.rest.object.CourseSearchDoc;
import com.gs.api.rest.object.CourseSearchDocList;
import com.gs.api.rest.object.CourseSearchGroup;
import com.gs.api.rest.object.CourseSearchGrouped;

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
                createCourseContainer("ABC123", 0, 224, 100), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
            .thenReturn(responseEntity);
        CourseSearchResponse response = courseSearchService.searchCourses("stuff", 0, 100,"");
        assertNotNull(response);
        assertEquals(224, response.getNumFound());
        assertEquals(0,  response.getStart());
        assertEquals(100,  response.getStartNext());
        assertEquals("ABC123", response.getCourses()[0].getId());
        assertFalse(response.isExactMatch());
        assertEquals(3, response.getTotalPages());
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearch_ExactMatch() throws Exception {

        ResponseEntity<CourseSearchContainer> responseEntity = new ResponseEntity<CourseSearchContainer>(
                createCourseContainer("ABC123", 0, 1, 1), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
            .thenReturn(responseEntity);
        CourseSearchResponse response = courseSearchService.searchCourses("ABC123", 0, 100,"");
        assertNotNull(response);
        assertEquals(1, response.getNumFound());
        assertEquals("ABC123", response.getCourses()[0].getId());
        assertTrue(response.isExactMatch());
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearch_ContainsExactMatch() throws Exception {

        ResponseEntity<CourseSearchContainer> responseEntity = new ResponseEntity<CourseSearchContainer>(
                createCourseContainer("ABC123001", 0, 1, 1), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
            .thenReturn(responseEntity);
        CourseSearchResponse response = courseSearchService.searchCourses("ABC123", 0, 100,"");
        assertNotNull(response);
        assertEquals(1, response.getNumFound());
        assertEquals("ABC123001", response.getCourses()[0].getId());
        assertTrue(response.isExactMatch());
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));

    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testSearch_MultipleResults() throws Exception {

        ResponseEntity<CourseSearchContainer> responseEntity = new ResponseEntity<CourseSearchContainer>(
                createCourseContainer("XYZ", 0, 2, 2), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
            .thenReturn(responseEntity);
        CourseSearchResponse response = courseSearchService.searchCourses("ABC123", 0, 100,"");
        assertNotNull(response);
        assertEquals(2, response.getNumFound());
        assertEquals("XYZ", response.getCourses()[0].getId());
        assertFalse(response.isExactMatch());
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearch_ContainsMixedCaseExactMatch() throws Exception {

        ResponseEntity<CourseSearchContainer> responseEntity = new ResponseEntity<CourseSearchContainer>(
                createCourseContainer("ABC123001", 0, 1, 1), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
            .thenReturn(responseEntity);
        CourseSearchResponse response = courseSearchService.searchCourses("abc123", 0, 100,"");
        assertNotNull(response);
        assertEquals(1, response.getNumFound());
        assertEquals("ABC123001", response.getCourses()[0].getId());
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
        CourseSearchResponse response = courseSearchService.searchCourses("find-nothing", 0, 100,"");
        assertNotNull(response);
        assertEquals(0, response.getNumFound());
        assertEquals(0, response.getStart());
        assertEquals(-1, response.getStartNext());
        assertNull(response.getCourses());
        assertFalse(response.isExactMatch());
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearch_Exception() throws Exception {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
            .thenThrow(new RuntimeException("I didn't expect this to happen"));
        try {
            courseSearchService.searchCourses("find-nothing", 0, 100,"");
            assertTrue(false);   //fail test as we should not get here
        } catch (Exception e) {
            assertTrue(e instanceof NotFoundException);
            NotFoundException nfe = (NotFoundException) e;
            assertEquals("No search results found", nfe.getMessage());
        }
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearch_LastPage() throws Exception {

        ResponseEntity<CourseSearchContainer> responseEntity = new ResponseEntity<CourseSearchContainer>(
                createCourseContainer("ABC123", 100, 162, 100), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
            .thenReturn(responseEntity);
        CourseSearchResponse response = courseSearchService.searchCourses("stuff", 100, 100,"");
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
        final String SINGLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:8983/solr/courses/select?q=(course_name:(*fraud*))^3 OR (course_id:(*fraud*))^9 OR (course_code:(*fraud*))^6 OR (course_description:(*fraud*)) OR (course_abstract:(*fraud*)) OR (course_prerequisites:(*fraud*))&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true";
        
    	String endpoint = courseSearchService.buildSearchString("fraud", 0, 100,"");
        assertEquals(SINGLE_TERM_RESULT, endpoint);
    
        //two terms
        final String DOUBLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:8983/solr/courses/select?q=(course_name:(*Project Management*))^3 OR (course_id:(*Project Management*))^9 OR (course_code:(*Project Management*))^6 OR (course_description:(*Project Management*)) OR (course_abstract:(*Project Management*)) OR (course_prerequisites:(*Project Management*))&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true";

        endpoint = courseSearchService.buildSearchString("Project Management",0, 100,"");

        assertEquals(DOUBLE_TERM_RESULT, endpoint);

    }

    @Test
    public void testStripAndEncode() {

        String result = courseSearchService.stripAndEncode("#&^%+-||!(){}[]\"~*?:\\");
        assertEquals("\\+\\-\\||\\!\\(\\)\\{\\}\\[\\]\\\"\\~\\*\\?\\:\\\\", result);

    }

    private CourseSearchContainer createCourseContainerNothing() {
        final CourseSearchContainer container = new CourseSearchContainer();
        final CourseSearchGrouped grouped = new CourseSearchGrouped();
        final CourseSearchGroup group = new CourseSearchGroup();
        final CourseSearchDocList docList = new CourseSearchDocList();
        docList.setNumFound(0);
        docList.setStart(0);
        group.setDoclist(docList);
        group.setMatches(0);
        group.setNgroups(0);
        grouped.setGroup(group);
        container.setGrouped(grouped);
        return container;
    }

    private CourseSearchContainer createCourseContainer(String id, int start, int numFound, int pageSize) {
        final CourseSearchContainer container = new CourseSearchContainer();
        final CourseSearchGrouped grouped = new CourseSearchGrouped();
        final CourseSearchGroup group = new CourseSearchGroup();
        final CourseSearchDocList docList = new CourseSearchDocList();
        List<CourseSearchDoc> docs = new ArrayList<CourseSearchDoc>();
        for (int i = 0; i < pageSize; i++) {
            CourseSearchDoc doc = new CourseSearchDoc();
            doc.setCourse_id(id);
            doc.setCourse_name("Course Name for " + id);
            docs.add(doc);            
        }
        docList.setDocs(docs);
        docList.setNumFound(numFound);
        docList.setStart(start);
        group.setDoclist(docList);
        group.setMatches(numFound);
        group.setNgroups(numFound);
        grouped.setGroup(group);
        container.setGrouped(grouped);
        return container;
    }

}
