package com.gs.api.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.gs.api.domain.Course;
import com.gs.api.domain.CourseSearchResponse;
import com.gs.api.domain.Location;
import com.gs.api.domain.SitePagesSearchResponse;
import com.gs.api.helper.CourseTestHelper;
import com.gs.api.service.CourseService;
import com.gs.api.service.CourseSearchService;
import com.gs.api.service.LocationService;
import com.gs.api.service.SiteSearchService;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class CourseControllerTest {

    @InjectMocks
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext applicationContext;

    @Autowired
    @InjectMocks
    private CourseController courseController;
    
    @Mock
    private CourseSearchService courseSearchService;
    
    @Mock
    private CourseService courseService;
    
    @Mock
    private LocationService locationService;
    
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
    public void testCourseSearch() throws Exception {
        when(courseSearchService.searchCourses(anyString(), anyInt(), anyInt(),any(String[].class)))
            .thenReturn(createSearchResponse());
        mockMvc.perform(get("/courses?search=training").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exactMatch").value(is(false)))
                .andExpect(jsonPath("$.numFound").value(is(1)));
        verify(courseSearchService, times(1)).searchCourses(anyString(), anyInt(), anyInt(), any(String[].class));
    }

    @Test
    public void testCourseSearch_SearchForAll() throws Exception {
        when(courseSearchService.searchCourses(anyString(), anyInt(), anyInt(),any(String[].class)))
            .thenReturn(createSearchResponse());
        mockMvc.perform(get("/courses?search=").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exactMatch").value(is(false)))
                .andExpect(jsonPath("$.numFound").value(is(1)));
        verify(courseSearchService, times(1)).searchCourses(anyString(), anyInt(), anyInt(), any(String[].class));
    }

    
    @Test
    public void testCourseSearch_InvalidArgs() throws Exception {
        mockMvc.perform(get("/courses?page=1").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value(is("Parameter 'page' and 'numRequest' not supported with this request")));
        verify(courseSearchService, times(0)).searchCourses(anyString(), anyInt(), anyInt(),any(String[].class));
    }

    @Test
    public void testCourseSearch_InvalidArgs2() throws Exception {
        mockMvc.perform(get("/courses?numRequested=1").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value(is("Parameter 'page' and 'numRequest' not supported with this request")));
        verify(courseSearchService, times(0)).searchCourses(anyString(), anyInt(), anyInt(),any(String[].class));
    }

    @Test
    public void testCourseSearch_BadRequest() throws Exception {
        mockMvc.perform(get("/bad=").accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isNotFound());
        verify(courseSearchService, times(0)).searchCourses(anyString(), anyInt(), anyInt(),any(String[].class));
    }
    
    @Test
    public void testGetCourse() throws Exception {
        when(courseService.getCourse(anyString())).thenReturn(CourseTestHelper.createCourse("12345"));
        mockMvc.perform(get("/courses/12345").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(is("12345001")))
                .andExpect(jsonPath("$.code").value(is("12345")))
                .andExpect(jsonPath("$.title").value(is("This is the title of a Course")));
        verify(courseService, times(1)).getCourse(anyString());
    }
    
    @Test
    public void testGetCourse_NullCourse() throws Exception {
        when(courseService.getCourse(anyString())).thenReturn(null);
        mockMvc.perform(get("/courses/1").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value(is("No course found for course id 1")));
        verify(courseService, times(1)).getCourse(anyString());
    }
    
    @Test
    public void testGetCourse_MissingCourseCode() throws Exception {
        when(courseService.getCourse(anyString())).thenReturn(new Course());
        mockMvc.perform(get("/courses/1").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value(is("No course found for course id 1")));
        verify(courseService, times(1)).getCourse(anyString());
    }
    
    @Test
    public void testGetSessions() throws Exception {
        when(courseService.getSessions(anyString())).thenReturn(CourseTestHelper.createSessions());
        mockMvc.perform(get("/courses/1/sessions").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$[0].classNumber").value(is("1")))
                .andExpect(jsonPath("$[1].classNumber").value(is("2")));
        verify(courseService, times(1)).getSessions(anyString());
    }
    
    @Test
    public void testGetSessions_NotFound() throws Exception {
        when(courseService.getSessions(anyString())).thenReturn(null);
        mockMvc.perform(get("/courses/1/sessions").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json"));
        verify(courseService, times(1)).getSessions(anyString());
    }
    
    @Test
    public void testHandleError() throws Exception {
        String response = courseController.handleException(new Exception("test"));
        assertEquals("{\"message\":\"test\"}", response);
    }
    
    @Test
    public void testGetLocations() throws Exception {
        when(locationService.getLocations())
            .thenReturn(createLocationResponse());
        mockMvc.perform(get("/locations").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].city").value(is("Washington")))
                .andExpect(jsonPath("$[0].state").value(is("DC")));
        verify(locationService, times(1)).getLocations();
    }

    @Test
    public void testCourseGetActive() throws Exception {
        when(courseService.getCourses()).thenReturn(CourseTestHelper.createCourseList());
        mockMvc.perform(get("/courses").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exactMatch").value(is(false)))
                .andExpect(jsonPath("$.numFound").value(is(2)))
                .andExpect(jsonPath("$.courses[0].code").value(is("12345")));
        verify(courseService, times(1)).getCourses();
    }
    
    @Test
    public void testValidationException() throws Exception {
        String result = courseController.handleValidationException(new HttpMessageNotReadableException(""));
        assertNotNull(result);
        assertEquals("{\"message\": \"Invalid Request \"}", result);
    }

    @Test
    public void testCourseSearchWithcityStateFilter() throws Exception {
        when(courseSearchService.searchCourses(anyString(), anyInt(), anyInt(),any(String[].class)))
                .thenReturn(createSearchResponse());
        mockMvc.perform(get("/courses?search=training&&filter=city_state:Washington,DC").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exactMatch").value(is(false)))
                .andExpect(jsonPath("$.numFound").value(is(1)));
        verify(courseSearchService, times(1)).searchCourses(anyString(), anyInt(), anyInt(), any(String[].class));
    }

    @Test
    public void testCourseSearchWithCityStateAndStatusFilter() throws Exception {
        when(courseSearchService.searchCourses(anyString(), anyInt(), anyInt(),any(String[].class)))
                .thenReturn(createSearchResponse());
        mockMvc.perform(get("/courses?search=training&&filter=city_state:Washington,DC&&filter=status:S").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exactMatch").value(is(false)))
                .andExpect(jsonPath("$.numFound").value(is(1)));
        verify(courseSearchService, times(1)).searchCourses(anyString(), anyInt(), anyInt(), any(String[].class));
    }
    
    @Test
    public void testSiteSearch() throws Exception {
        when(siteSearchService.searchSite(anyString(), anyInt(), anyInt())).thenReturn(new SitePagesSearchResponse());
        mockMvc.perform(get("/site?search=xxx").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
        verify(siteSearchService, times(1)).searchSite(anyString(), anyInt(), anyInt());
    }
    
    //create object for mocks
    private List<Location> createLocationResponse() {
        List<Location> expect  = new ArrayList<Location>();
        expect.add(new Location("Washington", "DC"));
        expect.add(new Location("Philadelphia", "PA"));
        return expect;
    }

    //create object for mocks
    private CourseSearchResponse createSearchResponse() {
        final CourseSearchResponse response = new CourseSearchResponse();
        response.setExactMatch(false);
        response.setNumFound(1);
        response.setCourses(new Course[] {new Course("1", "code", "title", "description")});
        return response;
    }
    
}
