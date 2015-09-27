package com.gs.api.search.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class HttpRequestBuilderTest {

    @Autowired
    @InjectMocks
    private HttpRequestBuilder httpRequestBuilder;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void buildHeader() throws Exception {
        HttpEntity<String> request = httpRequestBuilder.createRequestHeader();
        assertNotNull(request.getHeaders().get("Authorization"));
    }
    
}
