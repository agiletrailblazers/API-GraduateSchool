package com.gs.api.service.registration;

import com.gs.api.dao.registration.UserDAO;
import com.gs.api.domain.Address;
import com.gs.api.domain.PWChangeCredentials;
import com.gs.api.domain.Person;
import com.gs.api.domain.authentication.AuthCredentials;
import com.gs.api.domain.registration.User;
import com.gs.api.exception.NotFoundException;
import com.gs.api.service.email.EmailService;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import java.util.Date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RandomStringUtils.class)
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
    // don't change either of these password values, they match the encryption implementation
    private static final String PASSWORD_CLEAR = "test1234";
    private static final String PASSWORD_ENCRYPTED = "937E8D5FBB48BD4949536CD65B8D35C426B80D2F830C5C308E2CDEC422AE2244";
    private static final String DOB = "05/05/1955";
    private static final String LAST_FOUR_SSN = "5555";

    private User user;

    @Mock
    private UserDAO userDao;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserServiceImpl userService;

    /*
    By default, no exceptions are expected to be thrown (i.e. tests will fail if an exception is thrown),
    but using this rule allows for verification of operations that are expected to throw specific exceptions
    */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {

        mockStatic(RandomStringUtils.class);

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

        when(userDao.createUser(user)).thenReturn(USER_ID);

        userService.createUser(user);

        verify(userDao).createUser(user);

        assertEquals("ID not set on user", USER_ID, user.getId());
        assertEquals("Encrypted password not set on user", PASSWORD_ENCRYPTED, user.getPassword());
    }

    @Test
    public void testCreateUserEmailFails() throws Exception {

        Exception expectedException = new Exception("Mail Fail");
        when(userDao.createUser(user)).thenReturn(USER_ID);
        doThrow(expectedException).when(emailService).sendNewUserEmail(user);
        userService.createUser(user);

        verify(userDao).createUser(user);

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
    public void testForgotPassword() throws Exception {

        AuthCredentials acr = new AuthCredentials(user.getUsername(), null);
        when(userDao.getUserByUsername(user.getUsername())).thenReturn(user);

        when(RandomStringUtils.randomAlphanumeric(10)).thenReturn(PASSWORD_CLEAR);

        userService.forgotPassword(acr);

        PowerMockito.verifyStatic();
        RandomStringUtils.randomAlphanumeric(10);

        verify(userDao).getUserByUsername(user.getUsername());
        verify(userDao).resetForgottenPassword(USER_ID, PASSWORD_ENCRYPTED);
        verify(emailService).sendPasswordResetEmail(user, PASSWORD_CLEAR);
    }

    @Test
    public void testForgotPassword_userNotFound() throws Exception {

        AuthCredentials acr = new AuthCredentials(user.getUsername(), null);
        when(userDao.getUserByUsername(user.getUsername())).thenReturn(null);

        // setup expected exception
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("No user found with name " + acr.getUsername());

        userService.forgotPassword(acr);
    }

    @Test
    public void testForgotPassword_emailNotSentOnFailure() throws Exception {

        AuthCredentials acr = new AuthCredentials(user.getUsername(), null);
        when(userDao.getUserByUsername(user.getUsername())).thenReturn(user);

        final RuntimeException expectedException = new RuntimeException("test password reset failure");
        doThrow(expectedException).when(userDao).resetForgottenPassword(USER_ID, PASSWORD_ENCRYPTED);

        when(RandomStringUtils.randomAlphanumeric(10)).thenReturn(PASSWORD_CLEAR);

        try {
            userService.forgotPassword(acr);
        }
        catch (RuntimeException e) {
            if (e != expectedException) {
                fail("Unexpected exception");
            }
        }

        PowerMockito.verifyStatic();
        RandomStringUtils.randomAlphanumeric(10);

        verify(userDao).getUserByUsername(user.getUsername());
        verify(userDao).resetForgottenPassword(USER_ID, PASSWORD_ENCRYPTED);
        verifyZeroInteractions(emailService);
    }

    @Test
    public void testForgotPassword_emailFailure() throws Exception {

        AuthCredentials acr = new AuthCredentials(user.getUsername(), null);
        when(userDao.getUserByUsername(user.getUsername())).thenReturn(user);

        final RuntimeException expectedException = new RuntimeException("test send email failure");
        doThrow(expectedException).when(emailService).sendPasswordResetEmail(user, PASSWORD_CLEAR);

        when(RandomStringUtils.randomAlphanumeric(10)).thenReturn(PASSWORD_CLEAR);

        userService.forgotPassword(acr);

        PowerMockito.verifyStatic();
        RandomStringUtils.randomAlphanumeric(10);

        verify(userDao).getUserByUsername(user.getUsername());
        verify(userDao).resetForgottenPassword(USER_ID, PASSWORD_ENCRYPTED);
        verify(emailService).sendPasswordResetEmail(user, PASSWORD_CLEAR);
    }

    @Test
    public void testChangePassword() throws Exception {

        PWChangeCredentials pwChangeCredentials = new PWChangeCredentials(user.getUsername(), PASSWORD_ENCRYPTED, "NewPassword");
        when(userDao.getUser(user.getUsername(), PASSWORD_ENCRYPTED)).thenReturn(user);

        userService.changePassword(pwChangeCredentials);

        verify(userDao).getUser(user.getUsername(), PASSWORD_ENCRYPTED);
        verify(userDao).changeUserPassword(USER_ID, PASSWORD_ENCRYPTED, "NewPassword");
    }

    @Test
    public void testChangePassword_userNotFound() throws Exception {

        PWChangeCredentials pwChangeCredentials = new PWChangeCredentials(user.getUsername(), PASSWORD_ENCRYPTED, "NewPassword");
        when(userDao.getUser(user.getUsername(), PASSWORD_ENCRYPTED)).thenReturn(null);

        // setup expected exception
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Username or Password Incorrect for user " + pwChangeCredentials.getUsername());

        userService.changePassword(pwChangeCredentials);
    }
}
