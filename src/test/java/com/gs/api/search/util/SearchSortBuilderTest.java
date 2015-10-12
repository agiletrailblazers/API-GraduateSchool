package com.gs.api.search.util;

import static org.junit.Assert.assertEquals;

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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class SearchSortBuilderTest {

    @Mock
    private RestOperations restTemplate;

    @InjectMocks
    @Autowired
    private SearchSortBuilder searchSortBuilder;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testScoreAndCourseId() {
        String url = "http://test.me?sort={sort}";
        String endpoint = searchSortBuilder.build(url, true, true);
        assertEquals("http://test.me?sort=score desc,course_id asc", endpoint);
    }
    
    @Test
    public void testCourseIdOnly() {
        String url = "http://test.me?sort={sort}";
        String endpoint = searchSortBuilder.build(url, false, true);
        assertEquals("http://test.me?sort=course_id asc", endpoint);
    }
    
    @Test
    public void testScoreOnly() {
        String url = "http://test.me?sort={sort}";
        String endpoint = searchSortBuilder.build(url, true, false);
        assertEquals("http://test.me?sort=score desc", endpoint);
    }

}

