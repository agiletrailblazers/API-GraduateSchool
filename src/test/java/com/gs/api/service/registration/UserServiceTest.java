package com.gs.api.service.registration;

import com.gs.api.dao.registration.UserDAO;
import com.gs.api.domain.Address;
import com.gs.api.domain.Person;
import com.gs.api.domain.authentication.AuthCredentials;
import com.gs.api.domain.registration.Timezone;
import com.gs.api.domain.registration.User;
import com.gs.api.exception.NotFoundException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class UserServiceTest {

    private static final String USER_ID = "pern0000123123123123";
    private static final String FIRST_NAME = "Joe";
    private static final String MIDDLE_NAME = "Bob";
    private static final String LAST_NAME = "Smith";
    private static final String EMAIL_ADDRESS = "foo@bar.com";
    private static final String ADDRESS_1 = "42 Some Street";
    private static final String ADDRESS_2 = "#2";
    private static final String ADDRESS_CITY = "Boston";
    private static final String ADDRESS_STATE = "MA";
    private static final String ADDRESS_ZIP = "55555";
    private static final String PHONE = "555-555-5555";
    private static final String PASSWORD_ENCRYPTED = "937E8D5FBB48BD4949536CD65B8D35C426B80D2F830C5C308E2CDEC422AE2244";
    private static final String DOB = "05/05/1955";
    private static final String LAST_FOUR_SSN = "5555";

    private User user;

    @Mock
    private UserDAO userDao;

    @InjectMocks
    @Autowired
    private UserServiceImpl userService;

    /*
    By default, no exceptions are expected to be thrown (i.e. tests will fail if an exception is thrown),
    but using this rule allows for verification of operations that are expected to throw specific exceptions
    */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        user = new User();
        user.setId(USER_ID);
        user.setUsername(EMAIL_ADDRESS);
        user.setPassword(PASSWORD_ENCRYPTED);
        user.setLastFourSSN(LAST_FOUR_SSN);

        Person person = new Person();
        person.setFirstName(FIRST_NAME);
        person.setMiddleName(MIDDLE_NAME);
        person.setLastName(LAST_NAME);
        person.setEmailAddress(EMAIL_ADDRESS);
        person.setPrimaryPhone(PHONE);
        person.setDateOfBirth(DOB);

        Address address = new Address();
        address.setAddress1(ADDRESS_1);
        address.setAddress2(ADDRESS_2);
        address.setCity(ADDRESS_CITY);
        address.setState(ADDRESS_STATE);
        address.setPostalCode(ADDRESS_ZIP);
        person.setPrimaryAddress(address);

        user.setPerson(person);
    }

    @Test
    public void testCreateUser() throws Exception {

        when(userDao.insertNewUser(user)).thenReturn(USER_ID);

        userService.createUser(user);

        verify(userDao).insertNewUser(user);

        assertEquals("ID not set on user", USER_ID, user.getId());
        assertEquals("Encrypted password not set on user", PASSWORD_ENCRYPTED, user.getPassword());
    }

    @Test
    public void testDeleteUser() throws Exception {

        final String timestamp = Long.toString(new Date().getTime());
        user.setTimestamp(timestamp);

        when(userDao.getUser(USER_ID)).thenReturn(user);

        userService.deleteUser(USER_ID);

        verify(userDao).getUser(USER_ID);
        verify(userDao).deleteUser(USER_ID, timestamp);
    }

    @Test
    public void testGetUserByCredentials() throws Exception {

        when(userDao.getUser(EMAIL_ADDRESS, PASSWORD_ENCRYPTED)).thenReturn(user);

        User retrievedUser = userService.getUser(new AuthCredentials(EMAIL_ADDRESS, PASSWORD_ENCRYPTED));

        verify(userDao).getUser(EMAIL_ADDRESS, PASSWORD_ENCRYPTED);

        assertSame("wrong user", user, retrievedUser);
    }

    @Test
    public void testGetUserById() throws Exception {

        when(userDao.getUser(USER_ID)).thenReturn(user);

        User retrievedUser = userService.getUser(USER_ID);
        verify(userDao).getUser(USER_ID);

        assertSame("wrong user", user, retrievedUser);
    }

    @Test
    public void testGetUserById_NotFound() throws Exception {

        when(userDao.getUser(USER_ID)).thenReturn(null);

        // setup expected exception
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("User not found by id " + USER_ID);

        userService.getUser(USER_ID);
    }

    @Test
    public void testGetTimezones() throws Exception {
        Timezone expectedTimezone = new Timezone();
        expectedTimezone.setId("tmz123");
        expectedTimezone.setName("Easternish");
        List<Timezone> expectedList  = new ArrayList<Timezone>();
        expectedList.add(expectedTimezone);
        when(userDao.getTimezones()).thenReturn(expectedList);
        List<Timezone> returnedTimezones = userService.getTimezones();
        assertEquals(1, returnedTimezones.size());
        assertEquals(returnedTimezones.get(0).getId(), expectedTimezone.getId());
        assertEquals(returnedTimezones.get(0).getName(), expectedTimezone.getName());

    }

    @Test
    public void testGetTimezones_RuntimeException() throws Exception {

        when(userDao.getTimezones()).thenThrow(new RuntimeException("random exception"));
        try {
            userService.getTimezones();
            assertTrue(false); //Should never reach this line
        }
        catch (Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof Exception);
        }
    }
}
