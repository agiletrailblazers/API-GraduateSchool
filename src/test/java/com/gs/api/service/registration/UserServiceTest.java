package com.gs.api.service.registration;

import com.gs.api.dao.registration.UserDAO;
import com.gs.api.domain.Address;
import com.gs.api.domain.Person;
import com.gs.api.domain.registration.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class UserServiceTest {

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
    private static final String PASSWORD_CLEAR = "test1234";
    private static final String PASSWORD_ENCRYPTED = "937E8D5FBB48BD4949536CD65B8D35C426B80D2F830C5C308E2CDEC422AE2244";
    private static final String DOB = "05/05/1955";
    private static final String LAST_FOUR_SSN = "5555";

    private User user;

    @Mock
    private UserDAO userDao;

    @InjectMocks
    @Autowired
    private UserServiceImpl userService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        user = new User();
        user.setUsername(EMAIL_ADDRESS);
        user.setPassword(PASSWORD_CLEAR);
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

        final String id = "test-55555";
        when(userDao.insertNewUser(user)).thenReturn(id);

        userService.createUser(user);

        verify(userDao).insertNewUser(user);

        assertEquals("ID not set on user", id, user.getId());
        assertEquals("Encrypted password not set on user", PASSWORD_ENCRYPTED, user.getPassword());
    }

    @Test
    public void testDeleteUser() throws Exception {
        final String id = "persn0001234";
        final String timestamp = Long.toString(new Date().getTime());
        user.setTimestamp(timestamp);

        when(userDao.getUser(id)).thenReturn(user);

        userService.deleteUser(id);

        verify(userDao).getUser(id);
        verify(userDao).deleteUser(id, timestamp);
    }
}
