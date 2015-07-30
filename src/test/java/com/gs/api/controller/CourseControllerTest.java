package com.gs.api.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

import com.gs.api.domain.Course;
import com.gs.api.domain.CourseSearchResponse;
import com.gs.api.helper.CourseTestHelper;
import com.gs.api.service.CourseDetailService;
import com.gs.api.service.CourseSearchService;

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
    private CourseDetailService courseDetailService;
    
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
        when(courseSearchService.searchCourses(anyString(), anyInt(), anyInt()))
            .thenReturn(createSearchResponse());
        mockMvc.perform(get("/course?search=training").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exactMatch").value(is(false)))
                .andExpect(jsonPath("$.numFound").value(is(1)));
    }

    @Test
    public void testCourseSearch_MissingParam() throws Exception {
        mockMvc.perform(get("/course?search=").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value(is("Search string not provided")));
    }

    @Test
    public void testCourseSearch_BadRequest() throws Exception {
        mockMvc.perform(get("/bad=").accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isNotFound());
    }
    
    @Test
    public void testGetCourse() throws Exception {
        when(courseDetailService.getCourse(anyString())).thenReturn(CourseTestHelper.createCourse());
        mockMvc.perform(get("/course/12345").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(is("12345")))
                .andExpect(jsonPath("$.code").value(is("12345")))
                .andExpect(jsonPath("$.title").value(is("This is the title of a Course")));
    }
    
    @Test
    public void testGetCourse_NullCourse() throws Exception {
        when(courseDetailService.getCourse(anyString())).thenReturn(null);
        mockMvc.perform(get("/course/1").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value(is("No course found for course id 1")));
    }
    
    @Test
    public void testGetCourse_MissingCourseCode() throws Exception {
        when(courseDetailService.getCourse(anyString())).thenReturn(new Course());
        mockMvc.perform(get("/course/1").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value(is("No course found for course id 1")));
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
