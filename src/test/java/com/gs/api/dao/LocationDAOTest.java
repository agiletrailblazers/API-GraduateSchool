package com.gs.api.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.gs.api.dao.LocationDAO.LocationRowMapper;
import com.gs.api.domain.Location;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class LocationDAOTest {

    @InjectMocks
    @Autowired
    private LocationDAO locationDAO;

    @Mock
    private JdbcTemplate jdbcTemplate;
    
    private LocationDAO.LocationRowMapper rowMapper;    
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        rowMapper = locationDAO.new LocationRowMapper();
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testLocationByCityStateDAO_GetResult() throws Exception {
         
        when(jdbcTemplate.query(anyString(), any(LocationRowMapper.class)))
            .thenAnswer(new Answer<List<Location>>() {
                @Override
                public List<Location> answer(InvocationOnMock invocation) throws Throwable {
                    return new ArrayList<Location>();
                }
            });
        List<Location> list = locationDAO.getLocationByCityState();
        assertNotNull(list);
        assertEquals(0, list.size());
        
    }
    
    @Test
    public void testLocationByCityStateDAO_EmptyResultException() throws Exception {

        when(jdbcTemplate.query(anyString(), any(LocationRowMapper.class)))
            .thenThrow(new EmptyResultDataAccessException(1));
        List<Location> list = locationDAO.getLocationByCityState();
        assertNull(list);
        
    }
    
    @Test
    public void testLocationByCityStateDAO_RuntimeException() throws Exception {

        when(jdbcTemplate.query(anyString(), any(LocationRowMapper.class)))
            .thenThrow(new RuntimeException("random exception"));
        try {
            locationDAO.getLocationByCityState();
            assertTrue(false);   //should not get here
        } catch( Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof Exception);
        }
        
    }
    
    @Test
    public void testLocationDAO_RowMapper() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("CITY")).thenReturn("Washington");
        when(rs.getString("STATE")).thenReturn("DC");
        Location location = rowMapper.mapRow(rs, 0);
        assertNotNull(location);
        assertEquals("Washington", location.getCity());
        assertEquals("DC", location.getState());
    }
    
}
