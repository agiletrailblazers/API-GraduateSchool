package com.gs.api.dao.registration;

import com.gs.api.domain.course.CourseSession;
import com.gs.api.domain.registration.Registration;
import com.gs.api.domain.registration.User;
import com.gs.api.exception.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.ResultSet;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class RegistrationDAOTest {

    @InjectMocks
    @Autowired
    private RegistrationDAO registrationDAO;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private SimpleJdbcCall insertOfferingActionProfileActor;
    @Mock
    private SimpleJdbcCall insertRegistrationActor;
    @Mock
    private SimpleJdbcCall insertOrderActor;
    @Mock
    private SimpleJdbcCall insertOrderItemActor;
    @Mock
    private SimpleJdbcCall insertChargeActor;
    @Mock
    private SimpleJdbcCall insertPaymentActor;
    @Mock
    private SimpleJdbcCall orderCompleteActor;

    @Captor
    private ArgumentCaptor<SqlParameterSource> insertOfferingActionCaptor;
    @Captor
    private ArgumentCaptor<SqlParameterSource> insertRegistrationCaptor;
    @Captor
    private ArgumentCaptor<SqlParameterSource> insertOrderCaptor;
    @Captor
    private ArgumentCaptor<SqlParameterSource> insertOrderItemCaptor;
    @Captor
    private ArgumentCaptor<SqlParameterSource> insertChargeCaptor;
    @Captor
    private ArgumentCaptor<SqlParameterSource> insertPaymentCaptor;
    @Captor
    private ArgumentCaptor<SqlParameterSource> orderCompleteCaptor;
    @Captor
    private ArgumentCaptor<Object[]> getOrderNoCaptor;
    @Captor
    private ArgumentCaptor<Object[]> getRegistrationQueryParamsCaptor;


    @Value("${sql.registration.getOrderNo.query}")
    private String getOrderNumberQuery;
    @Value("${sql.registration.offeringActionInsert.procedure}")
    private String insertOfferingActionProcedureName;
    @Value("${sql.registration.insertRegistration.procedure}")
    private String insertRegistrationProcedureName;
    @Value("${sql.registration.insertOrder.procedure}")
    private String insertOrderProcedureName;
    @Value("${sql.registration.insertOrderItem.procedure}")
    private String insertOrderItemProcedureName;
    @Value("${sql.registration.insertCharge.procedure}")
    private String insertChargeProcedureName;
    @Value("${sql.registration.insertPayment.procedure}")
    private String insertPaymentProcedureName;
    @Value("${sql.registration.orderComplete.procedure}")
    private String orderCompleteProcedureName;

    @Value("${sql.registration.offeringActionId.sequence}")
    private String getOfferingActionSequenceQuery;
    @Value("${sql.registration.registrationId.sequence}")
    private String getRegistrationSequenceQuery;
    @Value("${sql.registration.orderId.sequence}")
    private String getOrderSequenceQuery;
    @Value("${sql.registration.orderItemId.sequence}")
    private String getOrderItemSequenceQuery;
    @Value("${sql.registration.chargeId.sequence}")
    private String getChargeSequenceQuery;
    @Value("${sql.registration.paymentId.sequence}")
    private String getPaymentSequenceQuery;
    @Value("${sql.registration.getExisting.query}")
    private String existingRegistrationQuery;

    private User loggedInUser;

    private User student;

    private CourseSession session;

    private static final String COURSE_ID = "course12345";
    private static final String OFFERING_SESSION_ID = "class90351";
    private static final String CLASS_NO = "123456";
    private static final double TUITION = 1000.00;
    private static final Date START_DATE = new Date();
    private static final String USER_USERNAME = "testUser";
    private static final String USER_USER_ID = "empl00001";
    private static final String STUDENT_USER_ID = "persn12345";
    private static final String STUDENT_ACCOUNT_ID = "acct01123";
    private static final String REGISTRATION_ID = "12345";
    private static final String ORDER_NUMBER = "23456";
    private RegistrationDAO.RegistrationRowMapper rowMapper;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        loggedInUser = new User();
        loggedInUser.setUsername(USER_USERNAME);
        loggedInUser.setId(USER_USER_ID);

        session = new CourseSession();
        session.setClassNumber(CLASS_NO);
        session.setTuition(TUITION);
        session.setStartDate(START_DATE);
        session.setCourseId(COURSE_ID);
        session.setOfferingSessionId(OFFERING_SESSION_ID);

        student = new User();
        student.setId(STUDENT_USER_ID);
        student.setAccountId(STUDENT_ACCOUNT_ID);

        rowMapper = registrationDAO.new RegistrationRowMapper();

    }

    @Test
    public void testRegister() throws Exception {
        HashMap<String, Object> sqlResult = new HashMap<>();

        String expectedOfferingActionProfileId = "ofapr100";
        String expectedRegistrationId = "regdw1000";
        String expectedOrderId = "intor100";
        String expectedOrderItemId = "ioreg1000";
        String expectedChargeId = "chrgs100";
        String expectedPaymentId = "mopay100";
        String expectedOrderNumber = "order12345";

        when(jdbcTemplate.queryForObject(getOfferingActionSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(insertOfferingActionProfileActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getRegistrationSequenceQuery, String.class)).thenReturn("1000");
        doReturn(sqlResult).when(insertRegistrationActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getOrderSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(insertOrderActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getOrderItemSequenceQuery, String.class)).thenReturn("1000");
        doReturn(sqlResult).when(insertOrderItemActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getChargeSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(insertChargeActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getPaymentSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(insertPaymentActor).execute(any(SqlParameterSource.class));

        doReturn(sqlResult).when(orderCompleteActor).execute(any(SqlParameterSource.class));

        Object[] expectedQueryParams = new Object[] {expectedOrderId};
        when(jdbcTemplate.queryForObject(getOrderNumberQuery, expectedQueryParams, String.class)).thenReturn(expectedOrderNumber);

        Registration createdRegistration = registrationDAO.registerForCourse(loggedInUser, student, session);

        assertEquals(student.getId(), createdRegistration.getStudentId());
        assertEquals(session.getClassNumber(), createdRegistration.getSessionId());
        assertEquals(expectedRegistrationId, createdRegistration.getId());
        assertEquals(expectedOrderNumber, createdRegistration.getOrderNumber());

        verify(insertOfferingActionProfileActor).execute(insertOfferingActionCaptor.capture());
        SqlParameterSource registrationParameters = insertOfferingActionCaptor.getValue();
        assertEquals(expectedOfferingActionProfileId, registrationParameters.getValue("xid"));
        assertEquals(student.getId(), registrationParameters.getValue("xparty_id"));
        assertEquals(session.getCourseId(), registrationParameters.getValue("xoffering_temp_id"));
        assertEquals(loggedInUser.getUsername(), registrationParameters.getValue("xcreated_by"));
        assertEquals(loggedInUser.getId(), registrationParameters.getValue("xcreated_id"));
        assertEquals(loggedInUser.getUsername(), registrationParameters.getValue("xupdated_by"));
        assertEquals(session.getStartDate(), registrationParameters.getValue("xstart_date"));
        assertEquals(session.getStartDate(), registrationParameters.getValue("xoffrng_start_date"));

        verify(insertRegistrationActor).execute(insertRegistrationCaptor.capture());
        registrationParameters = insertRegistrationCaptor.getValue();
        assertEquals(expectedRegistrationId, registrationParameters.getValue("xid"));
        assertEquals(session.getOfferingSessionId(), registrationParameters.getValue("xclass_id"));
        assertEquals(student.getId(), registrationParameters.getValue("xstudent_id"));
        assertEquals(expectedOfferingActionProfileId, registrationParameters.getValue("xoffer_action_id"));

        verify(insertOrderActor).execute(insertOrderCaptor.capture());
        registrationParameters = insertOrderCaptor.getValue();
        assertEquals(expectedOrderId, registrationParameters.getValue("xid"));
        assertEquals(loggedInUser.getId(), registrationParameters.getValue("xcreated_id"));
        assertEquals(loggedInUser.getUsername(), registrationParameters.getValue("xcreated_by"));
        assertEquals(loggedInUser.getUsername(), registrationParameters.getValue("xupdated_by"));
        assertEquals(loggedInUser.getId(), registrationParameters.getValue("xsold_by_id"));
        assertEquals(student.getAccountId(), registrationParameters.getValue("xaccount_id"));
        assertEquals(session.getTuition(), registrationParameters.getValue("xtotal_charges"));
        assertEquals(student.getId(), registrationParameters.getValue("xcompany_id"));
        assertEquals(student.getId(), registrationParameters.getValue("xdept_id"));
        assertEquals(student.getId(), registrationParameters.getValue("xcontact_id"));

        verify(insertOrderItemActor).execute(insertOrderItemCaptor.capture());
        registrationParameters = insertOrderItemCaptor.getValue();
        assertEquals(expectedOrderItemId, registrationParameters.getValue("xid"));
        assertEquals(expectedOrderId, registrationParameters.getValue("xorder_id"));
        assertEquals(session.getTuition(), registrationParameters.getValue("xtotal_cost"));
        assertEquals(expectedRegistrationId, registrationParameters.getValue("xreg_id"));
        assertEquals(session.getOfferingSessionId(), registrationParameters.getValue("xpart_id")); //TODO replace with Session ID
        assertEquals(student.getId(), registrationParameters.getValue("xstudent_id"));
        assertEquals(loggedInUser.getUsername(), registrationParameters.getValue("xcreated_by"));
        assertEquals(session.getTuition(), registrationParameters.getValue("xno_units"));
        assertEquals(session.getCourseId(), registrationParameters.getValue("xoffering_template_id"));
        assertEquals(loggedInUser.getId(), registrationParameters.getValue("xcreated_id"));

        verify(insertChargeActor).execute(insertChargeCaptor.capture());
        registrationParameters = insertChargeCaptor.getValue();
        assertEquals(expectedChargeId, registrationParameters.getValue("xid"));
        assertEquals(session.getTuition(), registrationParameters.getValue("xamount"));
        assertEquals(expectedOrderItemId, registrationParameters.getValue("xowner_id"));
        assertEquals(loggedInUser.getUsername(), registrationParameters.getValue("xcreated_by"));

        verify(insertPaymentActor).execute(insertPaymentCaptor.capture());
        registrationParameters = insertPaymentCaptor.getValue();
        assertEquals(expectedPaymentId, registrationParameters.getValue("xid"));
        assertEquals(loggedInUser.getUsername(), registrationParameters.getValue("xcreated_by"));
        assertEquals(loggedInUser.getUsername(), registrationParameters.getValue("xupdated_by"));
        assertEquals(expectedOrderId, registrationParameters.getValue("xowner_id"));
        assertEquals(student.getId(), registrationParameters.getValue("xcontact_id"));
        assertEquals(student.getId(), registrationParameters.getValue("xcompany_id"));
        assertEquals(loggedInUser.getId(), registrationParameters.getValue("xcreated_id"));
        assertEquals(session.getTuition(), registrationParameters.getValue("xmoney_amt"));

        verify(orderCompleteActor).execute(orderCompleteCaptor.capture());
        registrationParameters = orderCompleteCaptor.getValue();
        assertEquals(expectedOrderId, registrationParameters.getValue("xorder_id"));
    }

    @Test
    public void testFailInsertOfferingActionProfile() throws Exception {
        when(jdbcTemplate.queryForObject(getOfferingActionSequenceQuery, String.class)).thenReturn("100");
        final IllegalArgumentException illegalArgumentException = new IllegalArgumentException("BAD SQL");
        when(insertOfferingActionProfileActor.execute(any(SqlParameterSource.class))).thenThrow(illegalArgumentException);
        try {
            registrationDAO.registerForCourse(loggedInUser, student, session);
            assertTrue(false); //Should never reach this line
        }
        catch (IllegalArgumentException iE) {
            assertSame(illegalArgumentException, iE);
        }
    }

    @Test
    public void testFailInsertRegistration() throws Exception {
        HashMap<String, Object> sqlResult = new HashMap<>();

        when(jdbcTemplate.queryForObject(getOfferingActionSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(insertOfferingActionProfileActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getRegistrationSequenceQuery, String.class)).thenReturn("100");
        final IllegalArgumentException illegalArgumentException = new IllegalArgumentException("BAD SQL");
        when(insertRegistrationActor.execute(any(SqlParameterSource.class))).thenThrow(illegalArgumentException);
        try {
            registrationDAO.registerForCourse(loggedInUser, student, session);
            assertTrue(false); //Should never reach this line
        }
        catch (IllegalArgumentException iE) {
            assertSame(illegalArgumentException, iE);
            verify(insertOfferingActionProfileActor).execute(any(SqlParameterSource.class));
        }
    }

    @Test
    public void testFailInsertOrder() throws Exception {
        HashMap<String, Object> sqlResult = new HashMap<>();

        when(jdbcTemplate.queryForObject(getOfferingActionSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(insertOfferingActionProfileActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getRegistrationSequenceQuery, String.class)).thenReturn("1000");
        doReturn(sqlResult).when(insertRegistrationActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getOrderSequenceQuery, String.class)).thenReturn("100");
        final IllegalArgumentException illegalArgumentException = new IllegalArgumentException("BAD SQL");
        when(insertOrderActor.execute(any(SqlParameterSource.class))).thenThrow(illegalArgumentException);
        try {
            registrationDAO.registerForCourse(loggedInUser, student, session);
            assertTrue(false); //Should never reach this line
        }
        catch (IllegalArgumentException iE) {
            assertSame(illegalArgumentException, iE);
            verify(insertOfferingActionProfileActor).execute(any(SqlParameterSource.class));
            verify(insertRegistrationActor).execute(any(SqlParameterSource.class));
        }
    }

    @Test
    public void testFailInsertOrderItem() throws Exception {
        HashMap<String, Object> sqlResult = new HashMap<>();

        when(jdbcTemplate.queryForObject(getOfferingActionSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(insertOfferingActionProfileActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getRegistrationSequenceQuery, String.class)).thenReturn("1000");
        doReturn(sqlResult).when(insertRegistrationActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getOrderSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(insertOrderActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getOrderItemSequenceQuery, String.class)).thenReturn("100");
        final IllegalArgumentException illegalArgumentException = new IllegalArgumentException("BAD SQL");
        when(insertOrderItemActor.execute(any(SqlParameterSource.class))).thenThrow(illegalArgumentException);

        try {
            registrationDAO.registerForCourse(loggedInUser, student, session);
            assertTrue(false); //Should never reach this line
        }
        catch (IllegalArgumentException iE) {
            assertSame(illegalArgumentException, iE);
            verify(insertOfferingActionProfileActor).execute(any(SqlParameterSource.class));
            verify(insertRegistrationActor).execute(any(SqlParameterSource.class));
            verify(insertOrderActor).execute(any(SqlParameterSource.class));
        }
    }

    @Test
    public void testFailInsertCharge() throws Exception {
        HashMap<String, Object> sqlResult = new HashMap<>();

        when(jdbcTemplate.queryForObject(getOfferingActionSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(insertOfferingActionProfileActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getRegistrationSequenceQuery, String.class)).thenReturn("1000");
        doReturn(sqlResult).when(insertRegistrationActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getOrderSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(insertOrderActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getOrderItemSequenceQuery, String.class)).thenReturn("1000");
        doReturn(sqlResult).when(insertOrderItemActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getChargeSequenceQuery, String.class)).thenReturn("100");
        final IllegalArgumentException illegalArgumentException = new IllegalArgumentException("BAD SQL");
        when(insertChargeActor.execute(any(SqlParameterSource.class))).thenThrow(illegalArgumentException);

        try {
            registrationDAO.registerForCourse(loggedInUser, student, session);
            assertTrue(false); //Should never reach this line
        }
        catch (IllegalArgumentException iE) {
            assertSame(illegalArgumentException, iE);
            verify(insertOfferingActionProfileActor).execute(any(SqlParameterSource.class));
            verify(insertRegistrationActor).execute(any(SqlParameterSource.class));
            verify(insertOrderActor).execute(any(SqlParameterSource.class));
            verify(insertOrderItemActor).execute(any(SqlParameterSource.class));
        }
    }

    @Test
    public void testFailInsertPayment() throws Exception {
        HashMap<String, Object> sqlResult = new HashMap<>();

        when(jdbcTemplate.queryForObject(getOfferingActionSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(insertOfferingActionProfileActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getRegistrationSequenceQuery, String.class)).thenReturn("1000");
        doReturn(sqlResult).when(insertRegistrationActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getOrderSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(insertOrderActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getOrderItemSequenceQuery, String.class)).thenReturn("1000");
        doReturn(sqlResult).when(insertOrderItemActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getChargeSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(insertChargeActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getPaymentSequenceQuery, String.class)).thenReturn("100");
        final IllegalArgumentException illegalArgumentException = new IllegalArgumentException("BAD SQL");
        when(insertPaymentActor.execute(any(SqlParameterSource.class))).thenThrow(illegalArgumentException);

        try {
            registrationDAO.registerForCourse(loggedInUser, student, session);
            assertTrue(false); //Should never reach this line
        }
        catch (IllegalArgumentException iE) {
            assertSame(illegalArgumentException, iE);
            verify(insertOfferingActionProfileActor).execute(any(SqlParameterSource.class));
            verify(insertRegistrationActor).execute(any(SqlParameterSource.class));
            verify(insertOrderActor).execute(any(SqlParameterSource.class));
            verify(insertOrderItemActor).execute(any(SqlParameterSource.class));
            verify(insertChargeActor).execute(any(SqlParameterSource.class));
        }
    }

    @Test
    public void testFailOrderComplete() throws Exception {
        HashMap<String, Object> sqlResult = new HashMap<>();

        when(jdbcTemplate.queryForObject(getOfferingActionSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(insertOfferingActionProfileActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getRegistrationSequenceQuery, String.class)).thenReturn("1000");
        doReturn(sqlResult).when(insertRegistrationActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getOrderSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(insertOrderActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getOrderItemSequenceQuery, String.class)).thenReturn("1000");
        doReturn(sqlResult).when(insertOrderItemActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getChargeSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(insertChargeActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getPaymentSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(insertPaymentActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getPaymentSequenceQuery, String.class)).thenReturn("100");
        final IllegalArgumentException illegalArgumentException = new IllegalArgumentException("BAD SQL");
        when(orderCompleteActor.execute(any(SqlParameterSource.class))).thenThrow(illegalArgumentException);

        try {
            registrationDAO.registerForCourse(loggedInUser, student, session);
            assertTrue(false); //Should never reach this line
        }
        catch (IllegalArgumentException iE) {
            assertSame(illegalArgumentException, iE);
            verify(insertOfferingActionProfileActor).execute(any(SqlParameterSource.class));
            verify(insertRegistrationActor).execute(any(SqlParameterSource.class));
            verify(insertOrderActor).execute(any(SqlParameterSource.class));
            verify(insertOrderItemActor).execute(any(SqlParameterSource.class));
            verify(insertChargeActor).execute(any(SqlParameterSource.class));
            verify(insertPaymentActor).execute(any(SqlParameterSource.class));
        }
    }

    @Test
     public void testGetRegistration() throws Exception{
        Object[] expectedQueryParams = new Object[] { STUDENT_USER_ID, OFFERING_SESSION_ID, OFFERING_SESSION_ID, OFFERING_SESSION_ID};

        Registration registration = new Registration();
        registration.setId(REGISTRATION_ID);
        registration.setOrderNumber(ORDER_NUMBER);
        registration.setSessionId(OFFERING_SESSION_ID);
        registration.setStudentId(STUDENT_USER_ID);

        when(jdbcTemplate.query(anyString(), any(Object[].class), any(RegistrationDAO.RegistrationRowMapper.class))).
                thenReturn(Collections.singletonList(registration));

        List<Registration> returnedRegistration = registrationDAO.getRegistration(STUDENT_USER_ID, OFFERING_SESSION_ID);

        verify(jdbcTemplate).query(eq(existingRegistrationQuery), getRegistrationQueryParamsCaptor.capture(), any(RegistrationDAO.RegistrationRowMapper.class));
        Object[] capturedQueryParams = getRegistrationQueryParamsCaptor.getValue();
        assertNotNull("Expected parameters", capturedQueryParams);
        assertArrayEquals("wrong parameters", expectedQueryParams, capturedQueryParams);

        assertNotNull("Expected a registration to be found", returnedRegistration);

        assertEquals("Wrong Registration ID", REGISTRATION_ID, returnedRegistration.get(0).getId());
        assertEquals("Wrong Order Number", ORDER_NUMBER, returnedRegistration.get(0).getOrderNumber());
        assertEquals("Wrong User ID", STUDENT_USER_ID, returnedRegistration.get(0).getStudentId());
        assertEquals("Wrong Session ID", OFFERING_SESSION_ID, returnedRegistration.get(0).getSessionId());
    }

    @Test
    public void testGetRegistrationMultipleResults() throws Exception{
        Object[] expectedQueryParams = new Object[] { STUDENT_USER_ID, OFFERING_SESSION_ID, OFFERING_SESSION_ID, OFFERING_SESSION_ID};

        Registration registration = new Registration();
        registration.setId(REGISTRATION_ID);
        registration.setOrderNumber(ORDER_NUMBER);
        registration.setSessionId(OFFERING_SESSION_ID);
        registration.setStudentId(STUDENT_USER_ID);

        Registration registration2 = new Registration();
        registration.setId(REGISTRATION_ID+"2");
        registration.setOrderNumber(ORDER_NUMBER + "2");
        registration.setSessionId(OFFERING_SESSION_ID);
        registration.setStudentId(STUDENT_USER_ID);

        List<Registration> registrationList = new ArrayList<Registration>();
        registrationList.add(registration);
        registrationList.add(registration2);

        when(jdbcTemplate.query(anyString(), any(Object[].class), any(RegistrationDAO.RegistrationRowMapper.class))).
                thenReturn(registrationList);

        List<Registration> returnedRegistrations = registrationDAO.getRegistration(STUDENT_USER_ID, OFFERING_SESSION_ID);

        verify(jdbcTemplate).query(eq(existingRegistrationQuery), getRegistrationQueryParamsCaptor.capture(), any(RegistrationDAO.RegistrationRowMapper.class));
        Object[] capturedQueryParams = getRegistrationQueryParamsCaptor.getValue();
        assertNotNull("Expected parameters", capturedQueryParams);
        assertArrayEquals("wrong parameters", expectedQueryParams, capturedQueryParams);

        assertNotNull("Expected a registration to be found", returnedRegistrations);
        assertEquals("wrong registrations", registrationList, returnedRegistrations);
    }

    @Test
    public void testGetRegistrationNoResults() throws Exception{
        Object[] expectedQueryParams = new Object[] { STUDENT_USER_ID, OFFERING_SESSION_ID, OFFERING_SESSION_ID, OFFERING_SESSION_ID};

        final List<Registration> emptyRegistrationList = new ArrayList<Registration>();

        when(jdbcTemplate.query(anyString(), any(Object[].class), any(RegistrationDAO.RegistrationRowMapper.class))).
                thenReturn(emptyRegistrationList);

        try {
            registrationDAO.getRegistration(STUDENT_USER_ID, OFFERING_SESSION_ID);
            assertTrue(false); //Should never reach this line
        }
        catch (NotFoundException e) {

            verify(jdbcTemplate).query(eq(existingRegistrationQuery), getRegistrationQueryParamsCaptor.capture(), any(RegistrationDAO.RegistrationRowMapper.class));
            Object[] capturedQueryParams = getRegistrationQueryParamsCaptor.getValue();
            assertNotNull("Expected parameters", capturedQueryParams);
            assertArrayEquals("wrong parameters", expectedQueryParams, capturedQueryParams);
        }

    }

    @Test
    public void testRegistrationDAO_RowMapper() throws Exception {
        ResultSet rs = mock(ResultSet.class);

        when(rs.getString("reg_id")).thenReturn(REGISTRATION_ID);
        when(rs.getString("order_no")).thenReturn(ORDER_NUMBER);
        when(rs.getString("student_id")).thenReturn(STUDENT_USER_ID);
        when(rs.getString("session_id")).thenReturn(OFFERING_SESSION_ID);

        Registration returnedRegistration = rowMapper.mapRow(rs, 0);
        assertNotNull(returnedRegistration);
        assertEquals(REGISTRATION_ID, returnedRegistration.getId());
        assertEquals(ORDER_NUMBER, returnedRegistration.getOrderNumber());
        assertEquals(STUDENT_USER_ID, returnedRegistration.getStudentId());
        assertEquals(OFFERING_SESSION_ID, returnedRegistration.getSessionId());
    }
}
