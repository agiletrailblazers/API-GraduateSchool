package com.gs.api.search.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestOperations;

import com.gs.api.search.util.SessionQueryParamsBuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class SessionQueryParamsBuilderTest {

    @InjectMocks
    @Autowired
    private SessionQueryParamsBuilder sessionQueryParamsBuilder;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void buildSessionQueryParams() {
        String sessionId="123";
        Map<String,Object> params = sessionQueryParamsBuilder.buildSessionQueryParams(sessionId);
        assertNotNull(params);
    }

    @Test
    public void buildCourseSessionsQueryParams() {
        String status="S";
        String sessionDomain ="CD";
        Map<String,Object> params = sessionQueryParamsBuilder.buildCourseSessionsQueryParams(status, sessionDomain);
        assertNotNull(params);
    }

    @Test
    public void buildCourseSessionsQueryParams_OnlyStatus() {
        String status="S";
        Map<String,Object> params = sessionQueryParamsBuilder.buildCourseSessionsQueryParams(status, null);
        assertNotNull(params);
    }

    @Test
    public void buildCourseSessionsQueryParams_OnlyDayTimeDomain() {
        String domain="CD";
        Map<String,Object> params = sessionQueryParamsBuilder.buildCourseSessionsQueryParams(null, domain);
        assertNotNull(params);
    }

    @Test
    public void buildCourseSessionsQueryParams_OnlyEPDomain() {
        String domain="EP";
        Map<String,Object> params = sessionQueryParamsBuilder.buildCourseSessionsQueryParams(null, domain);
        assertNotNull(params);
    }

    @Test
    public void buildCourseSessionsQueryParams_OnlyOtherDomain() {
        String domain="123";
        Map<String,Object> params = sessionQueryParamsBuilder.buildCourseSessionsQueryParams(null, domain);
        assertNotNull(params);
    }


}
