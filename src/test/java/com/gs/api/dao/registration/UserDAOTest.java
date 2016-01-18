package com.gs.api.dao.registration;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class UserDAOTest {

    private static final String FIRST_NAME = "Joe";
    private static final String MIDDLE_NAME = "Bob";
    private static final String LAST_NAME = "Smith";
    private static final String USERNAME = "FOO@BAR.COM";
    private static final String EMAIL_ADDRESS = "foo@bar.com";
    private static final String ADDRESS_1 = "42 Some Street";
    private static final String ADDRESS_2 = "#2";
    private static final String ADDRESS_CITY = "Boston";
    private static final String ADDRESS_STATE = "MA";
    private static final String ADDRESS_ZIP = "55555";
    private static final String PHONE = "555-555-5555";
    private static final String PASSWORD_CLEAR = "test1234";
    private static final String DOB = "05/05/1955";
    private static final String LAST_FOUR_SSN = "5555";
    private static final String TIMEZONE_ID = "tzone000000000000007";
    private static final Boolean VETERAN_STATUS = false;

    private User user;

    @InjectMocks
    @Autowired
    private UserDAO userDAO;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private SimpleJdbcCall personInsertActor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        user = new User();
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD_CLEAR);
        user.setTimezoneId(TIMEZONE_ID);
        user.setLastFourSSN(LAST_FOUR_SSN);

        Person person = new Person();
        person.setFirstName(FIRST_NAME);
        person.setMiddleName(MIDDLE_NAME);
        person.setLastName(LAST_NAME);
        person.setEmailAddress(EMAIL_ADDRESS);
        person.setPrimaryPhone(PHONE);
        person.setVeteran(VETERAN_STATUS);
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
    public void testInsertUser() throws Exception {
        String expectedResults = "IDofCreatedUser";
        HashMap<String, Object> sqlResult = new HashMap();

        // TODO you will want to update this test it doesn't actual verify anything relevant
        //      It needs to capture the values passed to the jdbc template and assert that the
        //      expected data is being passed to the stored procedure.

        doReturn(sqlResult).when(personInsertActor).execute(any(SqlParameterSource.class));
        String actualResults = userDAO.insertNewUser(user);

        assertNotNull(actualResults);
        assertEquals(expectedResults, actualResults);
    }

    @Test
    public void testFailToInsertUser() throws Exception {
        when(personInsertActor.execute(any(SqlParameterSource.class)))
                .thenThrow(new IllegalArgumentException("BAD SQL"));

        try {
            userDAO.insertNewUser(user);
            assertTrue(false); //Should never reach this line
        }
        catch (IllegalArgumentException iE) {
            assertNotNull(iE);
            assertTrue(iE instanceof Exception);
        }
    }

    @Test
    public void testGetAccount() throws Exception {

    }

    @Test
    public void testFailToGetAccount() throws Exception {

    }
}
