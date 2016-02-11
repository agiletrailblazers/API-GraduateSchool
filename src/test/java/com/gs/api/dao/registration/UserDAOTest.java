package com.gs.api.dao.registration;

import com.gs.api.domain.Address;
import com.gs.api.domain.Person;
import com.gs.api.domain.registration.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class UserDAOTest {
    @Value("${sql.user.single.query}")
    private String sqlForSingleUser;

    @Value("${sql.user.personInsert.procedure}")
    private String insertUserStoredProcedureName;
    @Value("${sql.user.profileInsert.procedure}")
    private String insertProfileStoredProcedureName;
    @Value("${sql.user.listEntryInsert.procedure}")
    private String insertfgtListEntryStoredProcedureName;
    @Value("${sql.user.deleteUser.procedure}")
    private String deleteUserStoredProcedureName;

    @Value("${sql.user.personId.sequence}")
    private String getPersIdSequenceQuery;
    @Value("${sql.user.profileId.sequence}")
    private String getProfileIdSequenceQuery;
    @Value("${sql.user.listEntry.sequence}")
    private String getListEntryIdSequenceQuery;

    private static final String USER_ID = "persn0001";
    private static final String FIRST_NAME = "Joe";
    private static final String MIDDLE_NAME = "Bob";
    private static final String LAST_NAME = "Smith";
    private static final String USERNAME = "FOO@BAR.COM";
    private static final String EMAIL_ADDRESS = "foo@bar.com";
    private static final String ADDRESS_1 = "42 Some Street";
    private static final String ADDRESS_2 = "Dept of Something";
    private static final String ADDRESS_3 = "#2";
    private static final String ADDRESS_CITY = "Boston";
    private static final String ADDRESS_STATE = "MA";
    private static final String ADDRESS_ZIP = "55555";
    private static final String PHONE = "555-555-5555";
    private static final String PASSWORD_CLEAR = "test1234";
    private static final String DOB = "05/05/1955";
    private static final String LAST_FOUR_SSN = "5555";
    private static final String TIMEZONE_ID = "testId";
    private static final String TIMEZONE_ID_DEFAULT = "tzone000000000000007";
    private static final Boolean VETERAN_STATUS = true;
    private static final String SPLIT = "domin000000000000001";
    private static final String CURRENCY_ID = "crncy000000000000167";

    private User user;

    @InjectMocks
    @Autowired
    private UserDAO userDAO;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private SimpleJdbcCall userInsertActor;

    @Mock
    private SimpleJdbcCall profileInsertActor;

    @Mock
    private SimpleJdbcCall listEntryActor;

    @Mock
    private SimpleJdbcCall deleteUserActor;

    @Captor
    private ArgumentCaptor<SqlParameterSource> insertUserCaptor;

    @Captor
    private ArgumentCaptor<SqlParameterSource> insertProfileCaptor;

    @Captor
    private ArgumentCaptor<SqlParameterSource> insertListEntryCaptor;

    @Captor
    private ArgumentCaptor<SqlParameterSource> deleteUserCaptor;

    @Captor
    private ArgumentCaptor<Object[]> singleUserQueryParamsCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        user = new User();
        user.setId(USER_ID);
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD_CLEAR);
        user.setTimezoneId(TIMEZONE_ID);
        user.setLastFourSSN(LAST_FOUR_SSN);
        user.setSplit(SPLIT);
        user.setCurrencyId(CURRENCY_ID);

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
        address.setAddress3(ADDRESS_3);
        address.setCity(ADDRESS_CITY);
        address.setState(ADDRESS_STATE);
        address.setPostalCode(ADDRESS_ZIP);
        person.setPrimaryAddress(address);

        user.setPerson(person);
    }

    @Test
    public void testInsertUser() throws Exception {
        HashMap<String, Object> sqlResult = new HashMap();
        String expectedPersonId = "persn100";
        String expectedProfileId = "ppcor1000";
        String expectedListEntryId = "liste10000";

        when(jdbcTemplate.queryForObject(getPersIdSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(userInsertActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getProfileIdSequenceQuery, String.class)).thenReturn("1000");
        doReturn(sqlResult).when(profileInsertActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getListEntryIdSequenceQuery, String.class)).thenReturn("10000");
        doReturn(sqlResult).when(listEntryActor).execute(any(SqlParameterSource.class));

        String actualUserId = userDAO.insertNewUser(user);

        verify(userInsertActor).execute(insertUserCaptor.capture());
        SqlParameterSource userParameters = insertUserCaptor.getValue();
        assertEquals(expectedPersonId, actualUserId);
        assertEquals(actualUserId, userParameters.getValue("xid"));
        assertEquals(user.getUsername(), userParameters.getValue("xusername"));
        assertEquals(user.getPassword(), userParameters.getValue("xpassword"));
        assertEquals(user.getTimezoneId(), userParameters.getValue("xtimezone_id"));
        assertEquals(TIMEZONE_ID, userParameters.getValue("xtimezone_id"));

        Person userPerson = user.getPerson();
        assertEquals(userPerson.getFirstName(), userParameters.getValue("xfname"));
        assertEquals(userPerson.getMiddleName(), userParameters.getValue("xmname"));
        assertEquals(userPerson.getLastName(), userParameters.getValue("xlname"));
        assertEquals(userPerson.getVeteran(), userParameters.getValue("xcustom9"));
        assertEquals(VETERAN_STATUS, userParameters.getValue("xcustom9"));
        assertEquals(userPerson.getPrimaryPhone(), userParameters.getValue("xhomephone"));
        assertEquals(userPerson.getEmailAddress(), userParameters.getValue("xemail"));

        Address personPrimaryAddress = userPerson.getPrimaryAddress();
        assertEquals(personPrimaryAddress.getAddress1(), userParameters.getValue("xaddr3"));
        assertEquals(personPrimaryAddress.getAddress2(), userParameters.getValue("xaddr1"));
        assertEquals(personPrimaryAddress.getAddress3(), userParameters.getValue("xaddr2"));
        assertEquals(personPrimaryAddress.getCity(), userParameters.getValue("xcity"));
        assertEquals(personPrimaryAddress.getState(), userParameters.getValue("xstate"));
        assertEquals(personPrimaryAddress.getPostalCode(), userParameters.getValue("xzip"));

        verify(profileInsertActor).execute(insertProfileCaptor.capture());
        SqlParameterSource profileParameters = insertProfileCaptor.getValue();

        assertEquals(expectedProfileId, profileParameters.getValue("xid"));
        assertEquals(expectedPersonId, profileParameters.getValue("xprofiled_id"));

        verify(listEntryActor, times(2)).execute(insertListEntryCaptor.capture());
        List<SqlParameterSource> listEntryParameters = insertListEntryCaptor.getAllValues();

        assertTrue(listEntryParameters.size() == 2);

        assertEquals(expectedListEntryId, listEntryParameters.get(0).getValue("xid"));
        assertEquals(expectedPersonId, listEntryParameters.get(0).getValue("xperson_id"));
        assertEquals("listl000000000000101", listEntryParameters.get(0).getValue("xlist_id"));

        assertEquals(expectedListEntryId, listEntryParameters.get(1).getValue("xid"));
        assertEquals(expectedPersonId, listEntryParameters.get(1).getValue("xperson_id"));
        assertEquals("listl000000000001004", listEntryParameters.get(1).getValue("xlist_id"));
    }

    @Test
    public void testInsertUserNoTimezoneNoVeteranStatus() throws Exception {
        HashMap<String, Object> sqlResult = new HashMap();
        String expectedPersonId = "persn100";
        String expectedProfileId = "ppcor1000";

        user.setTimezoneId(null);

        when(jdbcTemplate.queryForObject(getPersIdSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(userInsertActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getProfileIdSequenceQuery, String.class)).thenReturn("1000");
        doReturn(sqlResult).when(profileInsertActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getListEntryIdSequenceQuery, String.class)).thenReturn("1000");
        doReturn(sqlResult).when(listEntryActor).execute(any(SqlParameterSource.class));

        String actualUserId = userDAO.insertNewUser(user);

        verify(userInsertActor).execute(insertUserCaptor.capture());
        SqlParameterSource userParameters = insertUserCaptor.getValue();
        assertEquals(user.getTimezoneId(), userParameters.getValue("xtimezone_id"));
        assertEquals(TIMEZONE_ID_DEFAULT, userParameters.getValue("xtimezone_id"));
    }


    @Test
    public void testFailToInsertUser() throws Exception {
        when(jdbcTemplate.queryForObject(getPersIdSequenceQuery, String.class)).thenReturn("100");
        when(userInsertActor.execute(any(SqlParameterSource.class))).thenThrow(new IllegalArgumentException("BAD SQL"));
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
    public void testFailToInsertProfile() throws Exception {
        HashMap<String, Object> sqlResult = new HashMap();

        //Mock successful User insert
        when(jdbcTemplate.queryForObject(getPersIdSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(userInsertActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getProfileIdSequenceQuery, String.class)).thenReturn("1000");
        when(profileInsertActor.execute(any(SqlParameterSource.class)))
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
    public void testFailInsertListEntry() throws Exception {
        HashMap<String, Object> sqlResult = new HashMap();
        String expectedPersonId = "persn100";
        String expectedProfileId = "ppcor1000";

        when(jdbcTemplate.queryForObject(getPersIdSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(userInsertActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getProfileIdSequenceQuery, String.class)).thenReturn("1000");
        doReturn(sqlResult).when(profileInsertActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getListEntryIdSequenceQuery, String.class)).thenReturn("1000");
        when(listEntryActor.execute(any(SqlParameterSource.class)))
                .thenThrow(new IllegalArgumentException("BAD SQL"));

        try {
            userDAO.insertNewUser(user);
            assertTrue(false); //Should never reach this line
        } catch (IllegalArgumentException iE) {
            assertNotNull(iE);
            assertTrue(iE instanceof Exception);
        }
    }

    @Test
    public void testDeleteUser() throws Exception {
        HashMap<String, Object> sqlResult = new HashMap();
        String userId = "persn1234";
        doReturn(sqlResult).when(deleteUserActor).execute(any(SqlParameterSource.class));

        userDAO.deleteUser(userId);

        verify(deleteUserActor).execute(deleteUserCaptor.capture());
        SqlParameterSource userParameters = deleteUserCaptor.getValue();

        assertEquals(userId, userParameters.getValue("xid"));
    }

    @Test
    public void testGetUser() throws Exception {
        Object[] expectedQueryParams = new Object[] { USER_ID };

        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), any(UserDAO.UserRowMapper.class))).
                thenReturn(user);

        User returnedUser = userDAO.getUser(USER_ID);

        assertNotNull("Expected a user to be found", returnedUser);
        assertTrue("Wrong user", USER_ID.equals(returnedUser.getId()));

        verify(jdbcTemplate).queryForObject(eq(sqlForSingleUser), singleUserQueryParamsCaptor.capture(), any(UserDAO.UserRowMapper.class));
        Object[] capturedQueryParams = singleUserQueryParamsCaptor.getValue();
        assertNotNull("Expected parameters", capturedQueryParams);
        assertArrayEquals("wrong parameters", expectedQueryParams, capturedQueryParams);

        assertEquals(user.getUsername(), returnedUser.getUsername());
        assertEquals(user.getPassword(), returnedUser.getPassword());
        assertEquals(user.getAccountId(), returnedUser.getAccountId());
        assertEquals(user.getCurrencyId(), returnedUser.getCurrencyId());
        assertEquals(user.getId(), returnedUser.getId());
        assertEquals(user.getSplit(), returnedUser.getSplit());
    }
}
