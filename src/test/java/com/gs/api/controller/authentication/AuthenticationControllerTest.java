package com.gs.api.controller.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.api.domain.authentication.AuthCredentials;
import com.gs.api.domain.authentication.AuthToken;
import com.gs.api.domain.authentication.AuthUser;
import com.gs.api.domain.authentication.RenewalToken;
import com.gs.api.domain.registration.User;
import com.gs.api.exception.AuthenticationException;
import com.gs.api.service.authentication.AuthenticationService;

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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class AuthenticationControllerTest {

    private static final String TEST_TOKEN = "thisisthetesttoken";
    private static final String USERNAME = "joe@tester.com";
    private static final String PASSWORD = "test1234";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext applicationContext;

    @Mock
    private AuthenticationService authenticationService;

    @Autowired
    @InjectMocks
    private AuthenticationController authenticationController;

    @Captor
    private ArgumentCaptor<AuthCredentials> capturedAuthCredentials;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGenerateGuestToken_Success() throws Exception {

        AuthToken authToken = new AuthToken(TEST_TOKEN);

        when(authenticationService.generateToken()).thenReturn(authToken);

        mockMvc.perform(get("/token")
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(is(TEST_TOKEN)));

        verify(authenticationService).generateToken();
    }


    @Test
    public void testAuthenticateUser_Success() throws Exception {

        AuthCredentials authCredentials = new AuthCredentials(USERNAME, PASSWORD);
        final User user = new User();
        user.setId("user12345");
        AuthUser authUser = new AuthUser(new AuthToken(TEST_TOKEN), new RenewalToken(TEST_TOKEN), user);
        String jsonModel = new ObjectMapper().writeValueAsString(authCredentials);

        when(authenticationService.authenticateUser(any(AuthCredentials.class))).thenReturn(authUser);

        mockMvc.perform(post("/authentication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonModel))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authToken.token").value(is(TEST_TOKEN)))
                .andExpect(jsonPath("$.user.id").value(is(user.getId())));

        verify(authenticationService).authenticateUser(capturedAuthCredentials.capture());
        assertEquals(USERNAME, capturedAuthCredentials.getValue().getUsername());
        assertEquals(PASSWORD, capturedAuthCredentials.getValue().getPassword());
    }

    @Test
    public void testAuthenticateUser_AuthenticationException() throws Exception {

        AuthCredentials authCredentials = new AuthCredentials(USERNAME, PASSWORD);
        String jsonModel = new ObjectMapper().writeValueAsString(authCredentials);

        when(authenticationService.authenticateUser(any(AuthCredentials.class))).thenThrow(new AuthenticationException("Invalid User"));

        mockMvc.perform(post("/authentication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonModel))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(is("Invalid User")));

        verify(authenticationService).authenticateUser(any(AuthCredentials.class));
    }


}
