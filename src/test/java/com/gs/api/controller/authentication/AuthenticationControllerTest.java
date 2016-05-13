package com.gs.api.controller.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.api.domain.authentication.*;
import com.gs.api.domain.User;
import com.gs.api.exception.AuthenticationException;
import com.gs.api.service.authentication.AuthenticationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
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
import static org.junit.Assert.assertTrue;
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
    public static final String ENCRYPTED_TOKEN_STRING = "encrypted_token";

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

    @Captor
    private ArgumentCaptor<ReAuthCredentials> capturedReAuthCredentials;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGenerateGuestToken_Success() throws Exception {

        AuthToken authToken = new AuthToken(TEST_TOKEN);

        when(authenticationService.generateToken()).thenReturn(authToken);

        mockMvc.perform(get("/tokens")
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
        AuthUser authUser = new AuthUser(new AuthToken(TEST_TOKEN), new RenewalToken(TEST_TOKEN), user, false);
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

    @Test
    public void testReAuthenticateUser_Success() throws Exception {
        AuthToken currentAuthToken = new AuthToken(ENCRYPTED_TOKEN_STRING);
        RenewalToken currentRenewalToken = new RenewalToken(ENCRYPTED_TOKEN_STRING);

        ReAuthCredentials reAuthCredentials = new ReAuthCredentials(currentAuthToken, currentRenewalToken);

        AuthToken authToken = new AuthToken(ENCRYPTED_TOKEN_STRING);

        String jsonModel = new ObjectMapper().writeValueAsString(reAuthCredentials);

        when(authenticationService.reAuthenticateUser(any(ReAuthCredentials.class))).thenReturn(authToken);

        mockMvc.perform(post("/reauthentication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonModel))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(is(ENCRYPTED_TOKEN_STRING)));

        verify(authenticationService).reAuthenticateUser(capturedReAuthCredentials.capture());
        assertTrue("Wrong Authtoken", currentAuthToken.getToken().equals(capturedReAuthCredentials.getValue().getAuthToken().getToken()));
        assertTrue("Wrong Renewal Token", currentRenewalToken.getToken().equals(capturedReAuthCredentials.getValue().getRenewalToken().getToken()));
    }

    @Test
    public void testReAuthenticateUser_AuthenticationException() throws Exception {

        AuthToken currentAuthToken = new AuthToken(ENCRYPTED_TOKEN_STRING);
        RenewalToken currentRenewalToken = new RenewalToken(ENCRYPTED_TOKEN_STRING);

        ReAuthCredentials reAuthCredentials = new ReAuthCredentials(currentAuthToken, currentRenewalToken);

        String jsonModel = new ObjectMapper().writeValueAsString(reAuthCredentials);

        when(authenticationService.reAuthenticateUser(any(ReAuthCredentials.class))).thenThrow(new AuthenticationException("Intentional Error ReAuthenticating user"));

        mockMvc.perform(post("/reauthentication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonModel))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(is("Intentional Error ReAuthenticating user")));

        verify(authenticationService).reAuthenticateUser(any(ReAuthCredentials.class));
    }


}
