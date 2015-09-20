package com.gs.api.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;

import com.gs.api.domain.SiteSearchResponse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class SiteSearchServiceTest {

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
    public void testSearchSite_NoResponse() throws Exception {        
        SiteSearchResponse response = siteSearchService.searchSite("xxx", 1, 10);
        assertNull(response.getPages());
    }
    
}
