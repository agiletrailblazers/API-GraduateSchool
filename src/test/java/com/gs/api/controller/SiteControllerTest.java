package com.gs.api.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.gs.api.domain.SitePagesSearchResponse;
import com.gs.api.service.SiteSearchService;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class SiteControllerTest {

    @InjectMocks
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext applicationContext;

    @Autowired
    @InjectMocks
    private SiteController siteController;
        
    @Mock
    private SiteSearchService siteSearchService;
        
    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testPing() throws Exception {
        mockMvc.perform(get("/ping").accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
    }
    
    @Test
    public void testSiteSearch() throws Exception {
        when(siteSearchService.searchSite(anyString(), anyInt(), anyInt(), any(String.class))).thenReturn(new SitePagesSearchResponse());
        mockMvc.perform(get("/site?search=xxx").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
        verify(siteSearchService, times(1)).searchSite(anyString(), anyInt(), anyInt(), any(String.class));
    }
    
}
