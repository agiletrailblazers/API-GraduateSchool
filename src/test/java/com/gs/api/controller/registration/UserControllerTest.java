package com.gs.api.controller.registration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.api.domain.Address;
import com.gs.api.domain.Person;
import com.gs.api.domain.authentication.AuthCredentials;
import com.gs.api.domain.registration.Timezone;
import com.gs.api.domain.registration.User;
import com.gs.api.exception.NotFoundException;
import com.gs.api.service.registration.UserService;

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

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext applicationContext;

    @Mock
    private UserService userService;

    @Autowired
    @InjectMocks
    private UserController userController;

    @Captor
    private ArgumentCaptor<User> capturedUser;

    @Captor
    private ArgumentCaptor<AuthCredentials> capturedAuthCredentials;

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
    public void testGetTimezones() throws Exception {
        Timezone expectedTimezone = new Timezone();
        expectedTimezone.setId("tmz123");
        expectedTimezone.setName("Easternish");
        List<Timezone> expectedList  = new ArrayList<>();
        expectedList.add(expectedTimezone);
        when(userService.getTimezones()).thenReturn(expectedList);

        mockMvc.perform(get("/users/timezones")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(expectedTimezone.getId())))
                .andExpect(jsonPath("$.[0].name", is(expectedTimezone.getName())));

        verify(userService).getTimezones();
    }

    @Test
    public void testGetTimezones_RuntimeException() throws Exception {
        when(userService.getTimezones()).thenThrow(new RuntimeException("The test broke stuff intentionally"));

        mockMvc.perform(get("/users/timezones")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(userService).getTimezones();
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
        person.setDateOfBirth("19550505");
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
