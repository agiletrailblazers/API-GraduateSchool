package com.gs.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.api.controller.UserController;
import com.gs.api.domain.*;
import com.gs.api.domain.authentication.AuthCredentials;
import com.gs.api.exception.AuthenticationException;
import com.gs.api.exception.NotFoundException;
import com.gs.api.exception.ReusedPasswordException;
import com.gs.api.service.authentication.AuthenticationService;
import com.gs.api.service.UserService;

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

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class UserControllerTest {

    private static final String USER_NAME = "joe.tester@test.com";
    private static final String USER_ID = "PRSN0000123123123123";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext applicationContext;

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationService authenticationService;

    @Autowired
    @InjectMocks
    private UserController userController;

    @Captor
    private ArgumentCaptor<User> capturedUser;

    @Captor
    private ArgumentCaptor<BaseUser> capturedBaseUser;

    @Captor
    private ArgumentCaptor<AuthCredentials> capturedAuthCredentials;

    @Captor
    private ArgumentCaptor<PasswordChangeAuthCredentials> capturedPWChangeCredentials;

    @Captor
    private ArgumentCaptor<String> capturedUserId;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateUser() throws Exception {

        User user = createValidTestUser();

        String jsonModel = new ObjectMapper().writeValueAsString(user);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonModel))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(is(USER_NAME)));

        verify(userService).createUser(capturedUser.capture());
        assertEquals(USER_NAME, capturedUser.getValue().getUsername());
     }

    @Test
    public void testCreateUser_validationError() throws Exception {

        User user = createValidTestUser();

        // null out a required field
        user.setUsername(null);

        String jsonModel = new ObjectMapper().writeValueAsString(user);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonModel))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors[0].fieldName").value(is("username")))
                .andExpect(jsonPath("$.validationErrors[0].errorMessage").value(is("Required field")));

        verifyZeroInteractions(userService);
     }

    @Test
    public void testUpdateUser() throws Exception {
        User user = createValidTestUser();
        String id = "persn0001234";
        user.setId(id);

        when(userService.getUser(id)).thenReturn(user);

        BaseUser baseUser = new BaseUser(user.getId(),user.getUsername(),user.getLastFourSSN(),user.getPerson(),user.getTimezoneId(),user.getAccountId(),
                user.getAccountNumber(), user.getSplit(), user.getCurrencyId(), user.getTimestamp());

        String jsonModel = new ObjectMapper().writeValueAsString(baseUser);

        mockMvc.perform(post("/users/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonModel))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(is(USER_NAME)));

        verify(userService).updateUser(capturedBaseUser.capture());
        verify(userService).getUser(id);
        assertEquals(USER_NAME, capturedBaseUser.getValue().getUsername());
        assertEquals("Ensure getUser returns same user", USER_NAME, user.getUsername());
    }

    @Test
    public void testUpdateUser_validationError() throws Exception {
        User user = createValidTestUser();
        String id = "persn0001234";
        user.setId(id);
        // null out a required field
        user.setUsername(null);
        BaseUser baseUser = new BaseUser(user.getId(),user.getUsername(),user.getLastFourSSN(),user.getPerson(),user.getTimezoneId(),user.getAccountId(),
                user.getAccountNumber(), user.getSplit(), user.getCurrencyId(), user.getTimestamp());

        String jsonModel = new ObjectMapper().writeValueAsString(baseUser);

        mockMvc.perform(post("/users/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonModel))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors[0].fieldName").value(is("username")))
                .andExpect(jsonPath("$.validationErrors[0].errorMessage").value(is("Required field")));

        verifyZeroInteractions(userService);
    }

    @Test
    public void testDeleteUser() throws Exception {

        String id = "persn0001234";

        mockMvc.perform(delete("/users/" + id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(id);
    }

    @Test
    public void testGetUser() throws Exception {

        String id = "persn000123123";

        User user = new User();
        user.setUsername(USER_NAME);

        when(userService.getUser(id)).thenReturn(user);

        mockMvc.perform(get("/users/" + id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(is(USER_NAME)));

        verify(userService).getUser(id);
    }

    @Test
    public void testGetUser_NotFound() throws Exception {

        String id = "persn000123123";

        User user = new User();
        user.setUsername(USER_NAME);

        when(userService.getUser(id)).thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/users/" + id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userService).getUser(id);
    }

    @Test
    public void testForgotPassword() throws Exception {

        AuthCredentials acr = new AuthCredentials("dummyUsername", "dummyOriginalPwd");

        String jsonModel = new ObjectMapper().writeValueAsString(acr);

        mockMvc.perform(post("/users/password/forgot")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonModel))
                .andExpect(status().isNoContent());

        verify(userService).forgotPassword(capturedAuthCredentials.capture());

        assertNotNull("Expected new credentials", capturedAuthCredentials.getValue());
        assertEquals("Wrong username", acr.getUsername(), capturedAuthCredentials.getValue().getUsername());
    }

    @Test
    public void testForgotPassword_noSuchUser() throws Exception {

        AuthCredentials acr = new AuthCredentials("dummyUsername", "dummyOriginalPwd");
        String jsonModel = new ObjectMapper().writeValueAsString(acr);

        doThrow(new NotFoundException("no such test user")).when(userService).forgotPassword(isA(AuthCredentials.class));

        mockMvc.perform(post("/users/password/forgot")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonModel))
                .andExpect(status().isNotFound());

        verify(userService).forgotPassword(isA(AuthCredentials.class));
    }

    @Test
    public void testChangePassword() throws Exception {

        PasswordChangeAuthCredentials passwordChangeAuthCredentials = new PasswordChangeAuthCredentials("dummyUsername", "dummyOriginalPwd", "dummyNewPassword");

        String jsonModel = new ObjectMapper().writeValueAsString(passwordChangeAuthCredentials);

        mockMvc.perform(post("/users/" + USER_ID + "/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonModel))
                .andExpect(status().isNoContent());

        verify(userService).changePassword(capturedPWChangeCredentials.capture(), capturedUserId.capture());
        verify(authenticationService).verifyUser(isA(HttpServletRequest.class), eq(USER_ID));

        assertNotNull("Expected new credentials", capturedPWChangeCredentials.getValue());
        assertEquals("Wrong username", passwordChangeAuthCredentials.getUsername(), capturedPWChangeCredentials.getValue().getUsername());
        assertEquals("Wrong new password", passwordChangeAuthCredentials.getNewPassword(), capturedPWChangeCredentials.getValue().getNewPassword());
    }

    @Test
    public void testChangePassword_reUsedPassword() throws Exception {

        PasswordChangeAuthCredentials passwordChangeAuthCredentials = new PasswordChangeAuthCredentials("dummyUsername", "dummyOriginalPwd", "dummyNewPassword");
        String jsonModel = new ObjectMapper().writeValueAsString(passwordChangeAuthCredentials);

        doThrow(new ReusedPasswordException("Password already used", new Exception("I caused this to happen"))).when(userService).changePassword(isA(PasswordChangeAuthCredentials.class), isA(String.class));

        mockMvc.perform(post("/users/" + USER_ID + "/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonModel))
                .andExpect(status().isConflict());

        verify(userService).changePassword(isA(PasswordChangeAuthCredentials.class), isA(String.class));
        verify(authenticationService).verifyUser(isA(HttpServletRequest.class), eq(USER_ID));

    }

    @Test
    public void testChangePassword_badUserId() throws Exception {

        PasswordChangeAuthCredentials passwordChangeAuthCredentials = new PasswordChangeAuthCredentials("dummyUsername", "dummyOriginalPwd", "dummyNewPassword");
        String jsonModel = new ObjectMapper().writeValueAsString(passwordChangeAuthCredentials);

        doThrow(new AuthenticationException("I caused this error")).when(authenticationService).verifyUser(isA(HttpServletRequest.class), eq(USER_ID));

        mockMvc.perform(post("/users/" + USER_ID + "/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonModel))
                .andExpect(status().isUnauthorized());

        verify(authenticationService).verifyUser(isA(HttpServletRequest.class), eq(USER_ID));
    }

    private User createValidTestUser() {
        User user = new User();
        user.setUsername(USER_NAME);
        user.setPassword("password");
        user.setLastFourSSN("5555");
        user.setTimezoneId("timezone1234");

        Person person = new Person();
        person.setFirstName("Joe");
        person.setLastName("Tester");
        person.setEmailAddress("joe.tester@test.com");
        person.setDateOfBirth("05/05/1955");
        person.setPrimaryPhone("5555555555");


        Address address = new Address();
        address.setAddress1("55 Test Street");
        address.setCity("Testingville");
        address.setState("TT");
        address.setPostalCode("55555");
        person.setPrimaryAddress(address);

        user.setPerson(person);

        return user;
    }
}
