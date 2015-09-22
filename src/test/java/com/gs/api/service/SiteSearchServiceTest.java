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
import java.util.Map;

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
import com.gs.api.rest.object.SiteSearchContainer;
import com.gs.api.rest.object.SiteSearchDoc;
import com.gs.api.rest.object.SiteSearchResponse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class SiteSearchServiceTest {
    @Value("${course.search.solr.endpoint}")
    private String courseSearchSolrEndpoint;

    @Value("${course.nutchsearch.solr.query}")
    private String courseSearchSolrQuery;

    @Mock
    private RestOperations restTemplate;

    @InjectMocks
    @Autowired
    private SiteSearchService siteSearchService;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSearch_ResultsFound() throws Exception {

        ResponseEntity<SiteSearchContainer> responseEntity = new ResponseEntity<SiteSearchContainer>(
                createSiteContainer("ABC123", 0, 224, 100), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(responseEntity);
        com.gs.api.domain.SiteSearchResponse response = siteSearchService.searchSite("stuff", 1, 100);
        assertNotNull(response);
        assertEquals(224, response.getNumFound());
        assertEquals(1, response.getCurrentPage());
        assertEquals(2, response.getNextPage());
        assertEquals(3, response.getTotalPages());
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));

    }

    @Test
    public void testSearch_NoMatch() throws Exception {

        ResponseEntity<SiteSearchContainer> responseEntity = new ResponseEntity<SiteSearchContainer>(
                createSiteContainerNothing(), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(responseEntity);
        com.gs.api.domain.SiteSearchResponse response = siteSearchService.searchSite("find-nothing", 0, 0);
        assertNotNull(response);
        assertEquals(0, response.getNumFound());
        assertEquals(0, response.getCurrentPage());
        assertEquals(0, response.getNextPage());
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));

    }
    @SuppressWarnings("unchecked")
    @Test
    public void testSearch_MultipleResults() throws Exception {

        ResponseEntity<SiteSearchContainer> responseEntity = new ResponseEntity<SiteSearchContainer>(
                createSiteContainer("XYZ", 0, 2, 2), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(responseEntity);
        com.gs.api.domain.SiteSearchResponse response = siteSearchService.searchSite("ABC123", 0, 100);
        assertNotNull(response);
        assertEquals(2, response.getNumFound());
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));

    }
    @Test
    public void testSearch_LastPage() throws Exception {

        ResponseEntity<SiteSearchContainer> responseEntity = new ResponseEntity<SiteSearchContainer>(
                createSiteContainer("ABC123", 100, 162, 100), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(responseEntity);
        com.gs.api.domain.SiteSearchResponse response = siteSearchService.searchSite("stuff", 2, 100);
        assertNotNull(response);
        assertEquals(162, response.getNumFound());
        assertEquals(2, response.getCurrentPage());
        assertEquals(0, response.getNextPage());
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));

    }


    @SuppressWarnings("unchecked")
    @Test
    public void testSearch_Exception() throws Exception {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenThrow(new RuntimeException("I didn't expect this to happen"));
        try {
            com.gs.api.domain.SiteSearchResponse response = siteSearchService.searchSite("find-nothing", 0, 100);
            assertTrue(false);   //fail test as we should not get here
        } catch (Exception e) {
            assertTrue(e instanceof NotFoundException);
            NotFoundException nfe = (NotFoundException) e;
            assertEquals("No search results found", nfe.getMessage());
        }
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
    }

    @Test
    public void buildSearchString() {
        //single term
        final String SINGLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/nutch-core/select?q=title:*&wt=json&indent=true";
        String endpoint = siteSearchService.buildSearchString("governnment", 1, 100);
        assertEquals(SINGLE_TERM_RESULT, endpoint);

        //two terms
        final String DOUBLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/nutch-core/select?q=title:*&wt=json&indent=true";

        endpoint = siteSearchService.buildSearchString("governnment",1, 100);

        assertEquals(DOUBLE_TERM_RESULT, endpoint);

    }

    @Test
    public void testStripAndEncode() {

        String result = siteSearchService.stripAndEncode("#&^%+-||!(){}[]\"~*?:\\");
        assertEquals("\\+\\-\\||\\!\\(\\)\\{\\}\\[\\]\\\"\\~\\*\\?\\:\\\\", result);

    }

    @Test
    public void createNavRange_LessThanFive() {
        int[] out = siteSearchService.createNavRange(2, 3);
        assertEquals(1, out[0]);
        assertEquals(2, out[1]);
        assertEquals(3, out[2]);
    }

    @Test
    public void createNavRange_MoreThanFive_BeginRange() {
        int[] out = siteSearchService.createNavRange(2, 10);
        assertEquals(1, out[0]);
        assertEquals(2, out[1]);
        assertEquals(3, out[2]);
        assertEquals(4, out[3]);
        assertEquals(5, out[4]);
    }

    @Test
    public void createNavRange_MoreThanFive_MidRange() {
        int[] out = siteSearchService.createNavRange(5, 10);
        assertEquals(3, out[0]);
        assertEquals(4, out[1]);
        assertEquals(5, out[2]);
        assertEquals(6, out[3]);
        assertEquals(7, out[4]);
    }

    @Test
    public void createNavRange_MoreThanFive_EndRange() {
        int[] out = siteSearchService.createNavRange(9, 10);
        assertEquals(6, out[0]);
        assertEquals(7, out[1]);
        assertEquals(8, out[2]);
        assertEquals(9, out[3]);
        assertEquals(10, out[4]);
    }


    private SiteSearchContainer createSiteContainer(String id, int start, int numFound, int pageSize) {
        final SiteSearchContainer container = new SiteSearchContainer();
        final SiteSearchResponse response = new SiteSearchResponse();
        response.setNumFound(numFound);
        List<SiteSearchDoc> docs = new ArrayList<SiteSearchDoc>();
        for (int i = 0; i < pageSize; i++) {
            SiteSearchDoc doc = new SiteSearchDoc();
            doc.setId("http://ec2-52-3-249-243.compute-1.amazonaws.com/");
            doc.setTitle("Graduate School");
            doc.setUrl("http://ec2-52-3-249-243.compute-1.amazonaws.com/");
            doc.setContent("Graduate School Current Students Prospective");
            docs.add(doc);
        }
        response.setDocs(docs);
        container.setResponse(response);
        return container;
    }


    private SiteSearchContainer createSiteContainerNothing() {
        final SiteSearchContainer container = new SiteSearchContainer();
        final SiteSearchResponse response = new SiteSearchResponse();
        response.setNumFound(0);
        response.setStart(0);
        container.setResponse(response);
        return container;
    }



}
