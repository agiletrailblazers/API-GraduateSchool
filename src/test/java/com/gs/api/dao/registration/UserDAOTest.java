package com.gs.api.dao.registration;

import com.gs.api.domain.Address;
import com.gs.api.domain.Person;
import com.gs.api.domain.registration.User;
import com.gs.api.exception.DuplicateUserException;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;

import java.lang.annotation.Annotation;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class UserDAOTest {
    @Value("${sql.user.single.query}")
    private String sqlForSingleUser;

    @Value("${sql.user.login.query}")
    private String sqlForUserLogin;

    @Value("${sql.user.password.query}")
    private String sqlForUserPassword;

    @Value("${sql.user.timezones.query}")
    private String sqlForTimezones;

    @Value("${sql.user.username.query}")
    private String sqlForUserByUsername;

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
    private static final String PASSWORD = "test1234";
    private static final String NEW_PASSWORD = "newtest1234";
    private static final String DOB = "19550505";
    private static final String LAST_FOUR_SSN = "5555";
    private static final String TIMEZONE_ID = "testId";
    private static final Boolean VETERAN_STATUS = true;
    private static final String SPLIT = "domin000000000000001";
    private static final String CURRENCY_ID = "crncy000000000000167";
    private static final String USER_TIMESTAMP = "12345678987654";

    private User user;

    private UserDAO.UserRowMapper userRowMapper;

    /* By default, no exceptions are expected to be thrown (i.e. tests will fail if an exception is thrown),
    but using this rule allows for verification of operations that are expected to throw specific exception */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

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

    @Mock
    private SimpleJdbcCall resetPasswordActor;

    @Captor
    private ArgumentCaptor<SqlParameterSource> insertUserCaptor;

    @Captor
    private ArgumentCaptor<SqlParameterSource> insertProfileCaptor;

    @Captor
    private ArgumentCaptor<SqlParameterSource> insertListEntryCaptor;

    @Captor
    private ArgumentCaptor<SqlParameterSource> deleteUserCaptor;

    @Captor
    private ArgumentCaptor<SqlParameterSource> resetPasswordCaptor;

    @Captor
    private ArgumentCaptor<Object[]> singleUserQueryParamsCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        user = new User();
        user.setId(USER_ID);
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
        user.setTimezoneId(TIMEZONE_ID);
        user.setLastFourSSN(LAST_FOUR_SSN);
        user.setSplit(SPLIT);
        user.setCurrencyId(CURRENCY_ID);
        user.setTimestamp(USER_TIMESTAMP);

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

        userRowMapper = userDAO.new UserRowMapper();
    }

    @Test
    public void testCreateUserSuccess() throws Exception {

        //Check if method is annotated for Spring Transaction
        Method method = UserDAO.class.getMethod("createUser", new Class[] {User.class});
        Annotation[] annotations = method.getAnnotations();
        boolean classAnnotatedWithTransactional = false;
        for (int i=0; i<annotations.length; i++){
            if(annotations[i].annotationType().equals(Transactional.class)){
                classAnnotatedWithTransactional = true;
            }
        }
        assertTrue(classAnnotatedWithTransactional);

        HashMap<String, Object> sqlResult = new HashMap<>();
        String expectedPersonId = "persn100";
        String expectedProfileId = "ppcor1000";
        String expectedListEntryId = "liste10000";

        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), any(UserDAO.UserRowMapper.class))).
                thenReturn(null);

        when(jdbcTemplate.queryForObject(getPersIdSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(userInsertActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getProfileIdSequenceQuery, String.class)).thenReturn("1000");
        doReturn(sqlResult).when(profileInsertActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getListEntryIdSequenceQuery, String.class)).thenReturn("10000");
        doReturn(sqlResult).when(listEntryActor).execute(any(SqlParameterSource.class));

        String actualUserId = userDAO.createUser(user);

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
    public void testCreateUserThrowsDuplicateException() throws Exception {
        // setup expected exception
        thrown.expect(DuplicateUserException.class);
        thrown.expectMessage(("User "+ USERNAME + " already exists"));

        Object[] expectedQueryParams = new Object[] { USERNAME};

        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), any(UserDAO.UserRowMapper.class))).
                thenReturn(user);

        userDAO.createUser(user);
    }

    @Test
    public void testFailToInsertUser() throws Exception {
        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), any(UserDAO.UserRowMapper.class))).
                thenReturn(null);
        when(jdbcTemplate.queryForObject(getPersIdSequenceQuery, String.class)).thenReturn("100");
        final IllegalArgumentException illegalArgumentException = new IllegalArgumentException("BAD SQL");
        when(userInsertActor.execute(any(SqlParameterSource.class))).thenThrow(illegalArgumentException);
        try {
            userDAO.createUser(user);
            assertTrue(false); //Should never reach this line
        }
        catch (IllegalArgumentException iE) {
            assertSame(illegalArgumentException, iE);
        }
    }

    @Test
    public void testFailToInsertProfile() throws Exception {
        HashMap<String, Object> sqlResult = new HashMap<>();

        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), any(UserDAO.UserRowMapper.class))).
                thenReturn(null);

        //Mock successful User insert
        when(jdbcTemplate.queryForObject(getPersIdSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(userInsertActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getProfileIdSequenceQuery, String.class)).thenReturn("1000");
        final IllegalArgumentException illegalArgumentException = new IllegalArgumentException("BAD SQL");
        when(profileInsertActor.execute(any(SqlParameterSource.class)))
                .thenThrow(illegalArgumentException);

        try {
            userDAO.createUser(user);
            assertTrue(false); //Should never reach this line
        }
        catch (IllegalArgumentException iE) {
            assertSame(illegalArgumentException, iE);
        }
    }

    @Test
    public void testFailInsertListEntry() throws Exception {
        HashMap<String, Object> sqlResult = new HashMap<>();

        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), any(UserDAO.UserRowMapper.class))).
                thenReturn(null);

        when(jdbcTemplate.queryForObject(getPersIdSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(userInsertActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getProfileIdSequenceQuery, String.class)).thenReturn("1000");
        doReturn(sqlResult).when(profileInsertActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getListEntryIdSequenceQuery, String.class)).thenReturn("1000");
        final IllegalArgumentException illegalArgumentException = new IllegalArgumentException("BAD SQL");
        when(listEntryActor.execute(any(SqlParameterSource.class)))
                .thenThrow(illegalArgumentException);

        try {
            userDAO.createUser(user);
            assertTrue(false); //Should never reach this line
        } catch (IllegalArgumentException iE) {
            assertSame(illegalArgumentException, iE);
        }
    }

    @Test
    public void testDeleteUser() throws Exception {
        HashMap<String, Object> sqlResult = new HashMap<>();
        String userId = "persn1234";
        doReturn(sqlResult).when(deleteUserActor).execute(any(SqlParameterSource.class));

        userDAO.deleteUser(userId, USER_TIMESTAMP);

        verify(deleteUserActor).execute(deleteUserCaptor.capture());
        SqlParameterSource userParameters = deleteUserCaptor.getValue();

        assertEquals(userId, userParameters.getValue("xid"));
        assertEquals(USER_TIMESTAMP, userParameters.getValue("xts"));
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

    @Test
    public void testUserDAO_RowMapper() throws Exception {
        ResultSet rs = mock(ResultSet.class);

        when(rs.getString("USER_ID")).thenReturn(USER_ID);
        when(rs.getString("USERNAME")).thenReturn(USERNAME);
        when(rs.getString("SS_NO")).thenReturn(null);
        when(rs.getString("TIMEZONE_ID")).thenReturn(TIMEZONE_ID);
        when(rs.getString("ACCOUNT_ID")).thenReturn("1234");
        when(rs.getString("SPLIT")).thenReturn(SPLIT);
        when(rs.getString("CURRENCY_ID")).thenReturn("abc1234");
        when(rs.getString("FNAME")).thenReturn(FIRST_NAME);
        when(rs.getString("LNAME")).thenReturn(LAST_NAME);
        when(rs.getString("EMAIL")).thenReturn(EMAIL_ADDRESS);
        when(rs.getString("HOMEPHONE")).thenReturn(PHONE);
        when(rs.getString("WORKPHONE")).thenReturn("123456");

        when(rs.getDate("DATE_OF_BIRTH")).thenReturn(null);
        when(rs.getString("VETERAN")).thenReturn("N");
        when(rs.getString("TIME_STAMP")).thenReturn(USER_TIMESTAMP);

        when(rs.getString("ADDRESS1")).thenReturn(ADDRESS_1);
        when(rs.getString("ADDRESS2")).thenReturn(ADDRESS_2);
        when(rs.getString("CITY")).thenReturn(ADDRESS_CITY);
        when(rs.getString("STATE")).thenReturn(ADDRESS_STATE);
        when(rs.getString("ZIP")).thenReturn(ADDRESS_ZIP);

        User returnedUser = userRowMapper.mapRow(rs, 0);
        assertNotNull(returnedUser);
        assertEquals(USER_ID, returnedUser.getId());
        assertEquals(USERNAME, returnedUser.getUsername());
        assertNull(returnedUser.getLastFourSSN());
        assertEquals(TIMEZONE_ID, returnedUser.getTimezoneId());
        assertEquals("1234", returnedUser.getAccountId());
        assertEquals(SPLIT, returnedUser.getSplit());
        assertEquals("abc1234", returnedUser.getCurrencyId());
        assertEquals(FIRST_NAME, returnedUser.getPerson().getFirstName());
        assertEquals(LAST_NAME, returnedUser.getPerson().getLastName());
        assertEquals(EMAIL_ADDRESS, returnedUser.getPerson().getEmailAddress());
        assertEquals(PHONE, returnedUser.getPerson().getPrimaryPhone());
        assertEquals("123456", returnedUser.getPerson().getSecondaryPhone());
        assertNull(returnedUser.getPerson().getDateOfBirth());
        assertEquals(false, returnedUser.getPerson().getVeteran());

        Address returnedAddress = returnedUser.getPerson().getPrimaryAddress();
        assertEquals(ADDRESS_1,returnedAddress.getAddress1());
        assertEquals(ADDRESS_2,returnedAddress.getAddress2());
        assertEquals(ADDRESS_CITY, returnedAddress.getCity());
        assertEquals(ADDRESS_STATE, returnedAddress.getState());
        assertEquals(ADDRESS_ZIP, returnedAddress.getPostalCode());
    }

    @Test
    public void testUserDAOwithoutVetStatus_RowMapper() throws Exception {
        ResultSet rs = mock(ResultSet.class);

        when(rs.getString("USER_ID")).thenReturn(USER_ID);
        when(rs.getString("VETERAN")).thenReturn(null);
        when(rs.getString("TIME_STAMP")).thenReturn(USER_TIMESTAMP);
        User user = userRowMapper.mapRow(rs, 0);
        assertNotNull(user);
        assertEquals(USER_ID, user.getId());
        assertNull(user.getPerson().getVeteran());
    }

    @Test
    public void testUserDAOwithVetStatusYes_RowMapper() throws Exception {
        ResultSet rs = mock(ResultSet.class);

        when(rs.getString("USER_ID")).thenReturn(USER_ID);
        when(rs.getString("VETERAN")).thenReturn("y");
        when(rs.getString("TIME_STAMP")).thenReturn(USER_TIMESTAMP);
        User user = userRowMapper.mapRow(rs, 0);
        assertNotNull(user);
        assertEquals(USER_ID, user.getId());
        assertTrue(user.getPerson().getVeteran());
    }

    @Test
    public void testUserDAOwithDoB_RowMapper() throws Exception {
        ResultSet rs = mock(ResultSet.class);

        when(rs.getString("USER_ID")).thenReturn(USER_ID);
        when(rs.getString("TIME_STAMP")).thenReturn(USER_TIMESTAMP);
        //Convert dates to sql dates
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date parsed = format.parse("20110210");
        java.sql.Date sqlDateOfBirth = new java.sql.Date(parsed.getTime());
        when(rs.getDate("DATE_OF_BIRTH")).thenReturn(sqlDateOfBirth);
        User user = userRowMapper.mapRow(rs, 0);
        assertNotNull(user);
        assertEquals(USER_ID, user.getId());
        assertEquals(sqlDateOfBirth.toString(), user.getPerson().getDateOfBirth());
    }

    @Test
    public void testUserDAOwithSsnFull_RowMapper() throws Exception {
        ResultSet rs = mock(ResultSet.class);

        when(rs.getString("USER_ID")).thenReturn(USER_ID);
        when(rs.getString("SS_NO")).thenReturn("00000" + LAST_FOUR_SSN);
        when(rs.getString("TIME_STAMP")).thenReturn(USER_TIMESTAMP);
        User user = userRowMapper.mapRow(rs, 0);
        assertNotNull(user);
        assertEquals(USER_ID, user.getId());
        assertEquals(LAST_FOUR_SSN, user.getLastFourSSN());
    }

    @Test
    public void testUserDAOwithTooSmallSsn_RowMapper() throws Exception {
        ResultSet rs = mock(ResultSet.class);

        when(rs.getString("USER_ID")).thenReturn(USER_ID);
        when(rs.getString("SS_NO")).thenReturn("123");
        when(rs.getString("TIME_STAMP")).thenReturn(USER_TIMESTAMP);
        User user = userRowMapper.mapRow(rs, 0);
        assertNotNull(user);
        assertEquals(USER_ID, user.getId());
        assertNull(user.getLastFourSSN());
    }


    @Test
    public void testUserDAOwithTooFourSsn_RowMapper() throws Exception {
        ResultSet rs = mock(ResultSet.class);

        when(rs.getString("USER_ID")).thenReturn(USER_ID);
        when(rs.getString("SS_NO")).thenReturn(LAST_FOUR_SSN);
        when(rs.getString("TIME_STAMP")).thenReturn(USER_TIMESTAMP);
        User user = userRowMapper.mapRow(rs, 0);
        assertNotNull(user);
        assertEquals(USER_ID, user.getId());
        assertEquals(LAST_FOUR_SSN, user.getLastFourSSN());
    }

    @Test
    public void testGetUser_InvalidUser() throws Exception {
        Object[] expectedQueryParams = new Object[] { USER_ID };

        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), any(UserDAO.UserRowMapper.class))).
                thenReturn(null);

        User returnedUser = userDAO.getUser(USER_ID);

        assertNull("Expected no user to be found", returnedUser);

        verify(jdbcTemplate).queryForObject(eq(sqlForSingleUser), singleUserQueryParamsCaptor.capture(), any(UserDAO.UserRowMapper.class));
        Object[] capturedQueryParams = singleUserQueryParamsCaptor.getValue();
        assertNotNull("Expected parameters", capturedQueryParams);
        assertArrayEquals("wrong parameters", expectedQueryParams, capturedQueryParams);

    }

    @Test
    public void testGetUserByUsernamePassword() throws Exception {
        Object[] expectedQueryParams = new Object[] { USER_ID, PASSWORD};

        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), any(UserDAO.UserRowMapper.class))).
                thenReturn(user);

        User returnedUser = userDAO.getUser(USER_ID, PASSWORD);

        verify(jdbcTemplate).queryForObject(eq(sqlForUserLogin), singleUserQueryParamsCaptor.capture(), any(UserDAO.UserRowMapper.class));

        Object[] capturedQueryParams = singleUserQueryParamsCaptor.getValue();
        assertNotNull("Expected parameters", capturedQueryParams);
        assertArrayEquals("wrong parameters", expectedQueryParams, capturedQueryParams);

        assertNotNull("Expected a user to be found", returnedUser);
        assertSame("Wrong user", user, returnedUser);
    }

    @Test
    public void testGetUserByUsernamePassword_InvalidUser() throws Exception {
        Object[] expectedQueryParams = new Object[] { USER_ID, PASSWORD};

        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), any(UserDAO.UserRowMapper.class))).
                thenThrow(new IncorrectResultSizeDataAccessException("No results found", 1));

        User returnedUser = userDAO.getUser(USER_ID, PASSWORD);

        verify(jdbcTemplate).queryForObject(eq(sqlForUserLogin), singleUserQueryParamsCaptor.capture(), any(UserDAO.UserRowMapper.class));

        Object[] capturedQueryParams = singleUserQueryParamsCaptor.getValue();
        assertNotNull("Expected parameters", capturedQueryParams);
        assertArrayEquals("wrong parameters", expectedQueryParams, capturedQueryParams);

        assertNull("No user should be found", returnedUser);
    }

    @Test
    public void testGetUserByUsername() throws Exception {
        Object[] expectedQueryParams = new Object[] { USERNAME};

        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), any(UserDAO.UserRowMapper.class))).
                thenReturn(user);

        User returnedUser = userDAO.getUserByUsername(USERNAME);

        verify(jdbcTemplate).queryForObject(eq(sqlForUserByUsername), singleUserQueryParamsCaptor.capture(), any(UserDAO.UserRowMapper.class));

        Object[] capturedQueryParams = singleUserQueryParamsCaptor.getValue();
        assertNotNull("Expected parameters", capturedQueryParams);
        assertArrayEquals("wrong parameters", expectedQueryParams, capturedQueryParams);

        assertNotNull("Expected a user to be found", returnedUser);
        assertSame("Wrong user", user, returnedUser);
    }

    @Test
    public void testGetUserByUsername_InvalidUser() throws Exception {
        Object[] expectedQueryParams = new Object[] { USERNAME};

        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), any(UserDAO.UserRowMapper.class))).
                thenThrow(new IncorrectResultSizeDataAccessException("No results found", 1));

        User returnedUser = userDAO.getUserByUsername(USERNAME);

        verify(jdbcTemplate).queryForObject(eq(sqlForUserByUsername), singleUserQueryParamsCaptor.capture(), any(UserDAO.UserRowMapper.class));

        Object[] capturedQueryParams = singleUserQueryParamsCaptor.getValue();
        assertNotNull("Expected parameters", capturedQueryParams);
        assertArrayEquals("wrong parameters", expectedQueryParams, capturedQueryParams);

        assertNull("No user should be found", returnedUser);
    }

    @Test
    public void testForgotPassword() throws Exception {

        // setup mock for current password
        Object[] expectedQueryParams = new Object[] {USER_ID};
        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), eq(String.class))).thenReturn(PASSWORD);

        HashMap<String, Object> sqlResult = new HashMap<>();
        doReturn(sqlResult).when(resetPasswordActor).execute(any(SqlParameterSource.class));

        userDAO.resetForgottenPassword(USER_ID, NEW_PASSWORD);

        // verify query for current password
        verify(jdbcTemplate).queryForObject(eq(sqlForUserPassword), singleUserQueryParamsCaptor.capture(), eq(String.class));
        Object[] capturedQueryParams = singleUserQueryParamsCaptor.getValue();
        assertNotNull("Expected parameters", capturedQueryParams);
        assertArrayEquals("wrong parameters", expectedQueryParams, capturedQueryParams);

        // verify stored procedure call to reset password
        verify(resetPasswordActor).execute(resetPasswordCaptor.capture());
        SqlParameterSource parameters = resetPasswordCaptor.getValue();

        assertNotNull("no parameters passed to reset password", parameters);
        assertEquals(USER_ID, parameters.getValue("xid"));
        assertEquals(PASSWORD, parameters.getValue("xold_password"));
        assertEquals(NEW_PASSWORD, parameters.getValue("xnew_password"));
        assertEquals(UserDAO.SABA_ADMIN_ID, parameters.getValue("xcurr_user_id"));
    }
}
