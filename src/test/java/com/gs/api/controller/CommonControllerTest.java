package com.gs.api.controller;

import com.gs.api.domain.registration.Timezone;
import com.gs.api.service.CommonService;
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

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class CommonControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext applicationContext;

    @Mock
    private CommonService commonService;

    @Autowired
    @InjectMocks
    private CommonController commonController;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetTimezones() throws Exception {
        Timezone expectedTimezone = new Timezone();
        expectedTimezone.setId("tmz123");
        expectedTimezone.setName("Easternish");
        List<Timezone> expectedList  = new ArrayList<>();
        expectedList.add(expectedTimezone);
        when(commonService.getTimezones()).thenReturn(expectedList);

        mockMvc.perform(get("/common/timezones")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(expectedTimezone.getId())))
                .andExpect(jsonPath("$.[0].name", is(expectedTimezone.getName())));

        verify(commonService).getTimezones();
    }

    @Test
    public void testGetTimezones_RuntimeException() throws Exception {
        when(commonService.getTimezones()).thenThrow(new RuntimeException("The test broke stuff intentionally"));

        mockMvc.perform(get("/common/timezones")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(commonService).getTimezones();
    }

}
