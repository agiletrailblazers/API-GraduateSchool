package com.gs.api.service;

import static org.junit.Assert.assertEquals;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.gs.api.dao.LocationDAO;
import com.gs.api.domain.Location;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class LocationServiceTest {

    @InjectMocks
    @Autowired
    private LocationService locationService;
    
    @Mock
    private LocationDAO locationDAO;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetLocation_ResultsFound() throws Exception {
        List<Location> expect  = new ArrayList<Location>();
        expect.add(new Location("Washington", "DC"));
        when(locationDAO.getLocationByCityState()).thenReturn(expect);
        List<Location> locations = locationService.getLocations();
        assertEquals(1, locations.size());
        assertEquals(locations.get(0).getCity(), "Washington");
        assertEquals(locations.get(0).getState(), "DC");

    }

}
