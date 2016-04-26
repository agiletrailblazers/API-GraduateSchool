package com.gs.api.dao;

import com.gs.api.domain.registration.Timezone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class CommonDAOTest {

    @Value("${sql.user.timezones.query}")
    private String sqlForTimezones;

    private CommonDAO.TimezoneRowMapper timezoneRowMapper;

    @InjectMocks
    @Autowired
    private CommonDAO commonDAO;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Captor
    private ArgumentCaptor<Object[]> singleUserQueryParamsCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        timezoneRowMapper = commonDAO.new TimezoneRowMapper();
    }


    @Test
    public void testGetTimezonesSuccess() throws Exception {
        when(jdbcTemplate.query(anyString(), any(CommonDAO.TimezoneRowMapper.class))).thenAnswer(new Answer<List<Timezone>>() {
            @Override
            public List<Timezone> answer(InvocationOnMock invocation) throws Throwable {
                return new ArrayList<Timezone>();
            }
        });
        List<Timezone> list = commonDAO.getTimezones();
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    @Test
    public void testGetTimezones_RuntimeException() throws Exception {
        when(jdbcTemplate.query(anyString(), any(CommonDAO.TimezoneRowMapper.class))).thenThrow(new RuntimeException("random exception"));

        try {
            commonDAO.getTimezones();
            assertTrue(false); //Should never reach this line
        }
        catch (Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof Exception);
        }
    }

    @Test
    public void testTimezoneDAO_RowMapper() throws Exception {
        String expectedId = "tmz0011";
        String expectedName = "Eastern Time";
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("ID")).thenReturn(expectedId);
        when(rs.getString("NAME")).thenReturn(expectedName);
        Timezone timezone = timezoneRowMapper.mapRow(rs, 0);
        assertNotNull(timezone);
        assertEquals(expectedId, timezone.getId());
        assertEquals(expectedName, timezone.getName());
    }
}
