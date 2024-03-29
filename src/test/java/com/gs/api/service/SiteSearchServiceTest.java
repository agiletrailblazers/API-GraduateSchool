package com.gs.api.service;

import com.gs.api.domain.Page;
import com.gs.api.domain.SitePagesSearchResponse;
import com.gs.api.rest.object.SiteSearchContainer;
import com.gs.api.rest.object.SiteSearchDoc;
import com.gs.api.rest.object.SiteSearchGrouped;
import com.gs.api.rest.object.SiteSearchGroup;
import com.gs.api.rest.object.SiteSearchResponse;
import com.gs.api.rest.object.SiteSearchDocList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestOperations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class SiteSearchServiceTest {

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

    @SuppressWarnings("unchecked")
    @Test
    public void testSearch_ResultsFound() throws Exception {

        ResponseEntity<SiteSearchContainer> responseEntity = new ResponseEntity<SiteSearchContainer>(
                createSiteContainer("ABC123", 0, 224, 100), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(responseEntity);
        SitePagesSearchResponse response = siteSearchService.searchSite("", 1, 100, new String());
        assertNotNull(response);
        assertEquals(224, response.getNumFound());
        assertEquals(1, response.getCurrentPage());
        assertEquals(2, response.getNextPage());
        assertEquals(3, response.getTotalPages());
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                any(Class.class));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearch_NoMatch() throws Exception {

        ResponseEntity<SiteSearchContainer> responseEntity = new ResponseEntity<SiteSearchContainer>(
                createSiteContainerNothing(), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(responseEntity);
        SitePagesSearchResponse response = siteSearchService.searchSite("find-nothing", 0, 0, new String());
        assertNotNull(response);
        assertEquals(0, response.getNumFound());
        assertEquals(0, response.getCurrentPage());
        assertEquals(0, response.getNextPage());
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                any(Class.class));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearch_MultipleResults() throws Exception {

        ResponseEntity<SiteSearchContainer> responseEntity = new ResponseEntity<SiteSearchContainer>(
                createSiteContainer("XYZ", 0, 2, 2), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(responseEntity);
        SitePagesSearchResponse response = siteSearchService.searchSite("ABC123", 0, 100, new String());
        assertNotNull(response);
        assertEquals(2, response.getNumFound());
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                any(Class.class));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearch_LastPage() throws Exception {

        ResponseEntity<SiteSearchContainer> responseEntity = new ResponseEntity<SiteSearchContainer>(
                createSiteContainer("ABC123", 100, 162, 100), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(responseEntity);
        SitePagesSearchResponse response = siteSearchService.searchSite("stuff", 2, 100, new String());
        assertNotNull(response);
        assertEquals(162, response.getNumFound());
        assertEquals(2, response.getCurrentPage());
        assertEquals(0, response.getNextPage());
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                any(Class.class));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearch_Exception() throws Exception {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenThrow(new RuntimeException("I didn't expect this to happen"));
        try {
            siteSearchService.searchSite("find-nothing", 0, 100, new String());
            assertTrue(false);   //fail test as we should not get here
        } catch (Exception e) {
            assertTrue(e instanceof Exception);
            assertEquals("No search results found", e.getMessage());
        }
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                any(Class.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearch_Null_PageTitle() throws Exception {

        ResponseEntity<SiteSearchContainer> responseEntity = new ResponseEntity<SiteSearchContainer>(
                createSiteContainerWithNullPageTitle("ABC123", 0, 1, 1), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(responseEntity);
        SitePagesSearchResponse response = siteSearchService.searchSite("stuff", 2, 100, new String());
        assertNotNull(response);
        assertEquals(1, response.getNumFound());
        assertEquals(2, response.getCurrentPage());
        Page[] page=response.getPages();
        assertEquals("Graduate School Curr",page[0].getTitle());
        verify(restTemplate, times(1))
                .exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));

    }

    private SiteSearchContainer createSiteContainerWithNullPageTitle(String id, int start, int numFound, int pageSize) {
        final SiteSearchContainer container = new SiteSearchContainer();
        final SiteSearchGrouped grouped = new SiteSearchGrouped();
        final SiteSearchGroup group = new SiteSearchGroup();
        final SiteSearchDocList docList = new SiteSearchDocList();
        List<SiteSearchDoc> docs = new ArrayList<SiteSearchDoc>();
        for (int i = 0; i < pageSize; i++) {
            SiteSearchDoc doc = new SiteSearchDoc();
            doc.setTitle(null);
            doc.setUrl("http://ec2-52-3-249-243.compute-1.amazonaws.com/");
            doc.setContent("Graduate School Current Students Prospective");
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



    private SiteSearchContainer createSiteContainer(String id, int start, int numFound, int pageSize) {
        final SiteSearchContainer container = new SiteSearchContainer();
        final SiteSearchGrouped grouped = new SiteSearchGrouped();
        final SiteSearchGroup group = new SiteSearchGroup();
        final SiteSearchDocList docList = new SiteSearchDocList();
        List<SiteSearchDoc> docs = new ArrayList<SiteSearchDoc>();
        for (int i = 0; i < pageSize; i++) {
            SiteSearchDoc doc = new SiteSearchDoc();
            doc.setTitle("Graduate School");
            doc.setUrl("http://ec2-52-3-249-243.compute-1.amazonaws.com/");
            doc.setContent("Graduate School Current Students Prospective");
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

    private SiteSearchContainer createSiteContainerNothing() {
        final SiteSearchContainer container = new SiteSearchContainer();
        final SiteSearchGrouped grouped = new SiteSearchGrouped();
        final SiteSearchGroup group = new SiteSearchGroup();
        final SiteSearchDocList docList = new SiteSearchDocList();
        List<SiteSearchDoc> docs = new ArrayList<SiteSearchDoc>();
        docList.setDocs(docs);
        docList.setNumFound(0);
        docList.setStart(0);
        group.setDoclist(docList);
        group.setMatches(0);
        group.setNgroups(0);
        grouped.setGroup(group);
        container.setGrouped(grouped);
        return container;
    }

}
