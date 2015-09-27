package com.gs.api.search.util;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class NavRangeBuilderTest {

    @InjectMocks
    @Autowired
    private NavRangeBuilder navRangeBuilder;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void createNavRange_LessThanFive() {
        int[] out = navRangeBuilder.createNavRange(2, 3);
        assertEquals(1, out[0]);
        assertEquals(2, out[1]);
        assertEquals(3, out[2]);
    }

    @Test
    public void createNavRange_MoreThanFive_BeginRange() {
        int[] out = navRangeBuilder.createNavRange(2, 10);
        assertEquals(1, out[0]);
        assertEquals(2, out[1]);
        assertEquals(3, out[2]);
        assertEquals(4, out[3]);
        assertEquals(5, out[4]);
    }

    @Test
    public void createNavRange_MoreThanFive_MidRange() {
        int[] out = navRangeBuilder.createNavRange(5, 10);
        assertEquals(3, out[0]);
        assertEquals(4, out[1]);
        assertEquals(5, out[2]);
        assertEquals(6, out[3]);
        assertEquals(7, out[4]);
    }

    @Test
    public void createNavRange_MoreThanFive_EndRange() {
        int[] out = navRangeBuilder.createNavRange(9, 10);
        assertEquals(6, out[0]);
        assertEquals(7, out[1]);
        assertEquals(8, out[2]);
        assertEquals(9, out[3]);
        assertEquals(10, out[4]);
    }

    
}
