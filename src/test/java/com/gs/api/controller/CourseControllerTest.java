package com.gs.api.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations={
	    "classpath:spring/test-root-context.xml"})
public class CourseControllerTest {
	
	private MockMvc mockMvc;
	
	@Autowired
	private WebApplicationContext applicationContext;
	
	@Before
	public void setUp() throws Exception {
		mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
	}
	
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testPing() throws Exception {
		
		mockMvc.perform(get("/ping")
				.accept(MediaType.APPLICATION_JSON_VALUE))
			.andExpect(status().isOk());
		
	}
	
	@Test
	public void testCourseSearch() throws Exception {
		
		mockMvc.perform(get("/course?search=training")
				.accept(MediaType.APPLICATION_JSON_VALUE))
			.andExpect(status().isOk())
			.andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.exactMatch").value(is(false)));
		
	}
	
	@Test
	public void testCourseSearch_MissingParam() throws Exception {
		
		mockMvc.perform(get("/course?search=")
				.accept(MediaType.APPLICATION_JSON_VALUE))
			.andExpect(status().isInternalServerError())
			.andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.code").value(is("500")))
            .andExpect(jsonPath("$.userMessage").value(is("Search string not provided")));
		
	}
	
	@Test
	public void testCourseSearch_BadRequest() throws Exception {
		
		mockMvc.perform(get("/bad=")
				.accept(MediaType.APPLICATION_JSON_VALUE))
			.andExpect(status().isNotFound());
		
	}
}
