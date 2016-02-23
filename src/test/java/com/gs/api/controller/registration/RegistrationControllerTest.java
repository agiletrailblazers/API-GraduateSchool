package com.gs.api.controller.registration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.api.domain.registration.Registration;
import com.gs.api.service.authentication.AuthenticationService;
import com.gs.api.service.registration.RegistrationService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class RegistrationControllerTest {

    private static final String USER_ID = "person654321";
    private static final String SESSION_ID = "session654321";
    private static final String REGISTRATION_ID = "12345";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext applicationContext;

    @Mock
    private RegistrationService registrationService;

    @Mock
    private AuthenticationService authenticationService;

    @Autowired
    @InjectMocks
    private RegistrationController registrationController;

    @Captor
    private ArgumentCaptor<List<Registration>> capturedRegistrations;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateRegistration() throws Exception {

        Registration registration = new Registration();
        registration.setStudentId(USER_ID);
        registration.setSessionId(SESSION_ID);
        List<Registration> registrations = Collections.singletonList(registration);

        Registration createdRegistration = new Registration();
        createdRegistration.setId(REGISTRATION_ID);

        String jsonModel = new ObjectMapper().writeValueAsString(registrations);

        when(registrationService.register(eq(USER_ID), isA(List.class))).thenReturn(Collections.singletonList(createdRegistration));

        mockMvc.perform(post("/registration/user/" + USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonModel))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(REGISTRATION_ID)));

        verify(authenticationService).verifyUser(isA(HttpServletRequest.class), eq(USER_ID));
        verify(registrationService).register(eq(USER_ID), capturedRegistrations.capture());
        assertEquals("Wrong user id", USER_ID, capturedRegistrations.getValue().get(0).getStudentId());
        assertEquals("Wrong session id", SESSION_ID, capturedRegistrations.getValue().get(0).getSessionId());
     }

}
