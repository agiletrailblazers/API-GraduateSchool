package com.gs.api.dao.registration;

import com.gs.api.domain.course.CourseSession;

import com.gs.api.domain.registration.User;
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

import java.util.Date;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


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
    }

    @Test
    public void testRegister() throws Exception {
        HashMap<String, Object> sqlResult = new HashMap();

        String expectedOfferingActionProfileId = "ofapr100";
        String expectedRegistrationId = "regdw1000";
        String expectedOrderId = "intor100";
        String expectedOrderItemId = "ioreg1000";
        String expectedChargeId = "chrgs100";
        String expectedPaymentId = "mopay100";

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

        String id = registrationDAO.registerForCourse(loggedInUser, student, session);

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
        when(insertOfferingActionProfileActor.execute(any(SqlParameterSource.class))).thenThrow(new IllegalArgumentException("BAD SQL"));
        try {
            String id = registrationDAO.registerForCourse(loggedInUser, student, session);
            assertTrue(false); //Should never reach this line
        }
        catch (IllegalArgumentException iE) {
            assertNotNull(iE);
            assertTrue(iE instanceof Exception);
        }
    }

    @Test
    public void testFailInsertRegistration() throws Exception {
        HashMap<String, Object> sqlResult = new HashMap();

        when(jdbcTemplate.queryForObject(getOfferingActionSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(insertOfferingActionProfileActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getRegistrationSequenceQuery, String.class)).thenReturn("100");
        when(insertRegistrationActor.execute(any(SqlParameterSource.class))).thenThrow(new IllegalArgumentException("BAD SQL"));
        try {
            String id = registrationDAO.registerForCourse(loggedInUser, student, session);
            assertTrue(false); //Should never reach this line
        }
        catch (IllegalArgumentException iE) {
            assertNotNull(iE);
            assertTrue(iE instanceof Exception);
            verify(insertOfferingActionProfileActor).execute(any(SqlParameterSource.class));
        }
    }

    @Test
    public void testFailInsertOrder() throws Exception {
        HashMap<String, Object> sqlResult = new HashMap();

        when(jdbcTemplate.queryForObject(getOfferingActionSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(insertOfferingActionProfileActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getRegistrationSequenceQuery, String.class)).thenReturn("1000");
        doReturn(sqlResult).when(insertRegistrationActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getOrderSequenceQuery, String.class)).thenReturn("100");
        when(insertOrderActor.execute(any(SqlParameterSource.class))).thenThrow(new IllegalArgumentException("BAD SQL"));
        try {
            String id = registrationDAO.registerForCourse(loggedInUser, student, session);
            assertTrue(false); //Should never reach this line
        }
        catch (IllegalArgumentException iE) {
            assertNotNull(iE);
            assertTrue(iE instanceof Exception);
            verify(insertOfferingActionProfileActor).execute(any(SqlParameterSource.class));
            verify(insertRegistrationActor).execute(any(SqlParameterSource.class));
        }
    }

    @Test
    public void testFailInsertOrderItem() throws Exception {
        HashMap<String, Object> sqlResult = new HashMap();

        when(jdbcTemplate.queryForObject(getOfferingActionSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(insertOfferingActionProfileActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getRegistrationSequenceQuery, String.class)).thenReturn("1000");
        doReturn(sqlResult).when(insertRegistrationActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getOrderSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(insertOrderActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getOrderItemSequenceQuery, String.class)).thenReturn("100");
        when(insertOrderItemActor.execute(any(SqlParameterSource.class))).thenThrow(new IllegalArgumentException("BAD SQL"));

        try {
            String id = registrationDAO.registerForCourse(loggedInUser, student, session);
            assertTrue(false); //Should never reach this line
        }
        catch (IllegalArgumentException iE) {
            assertNotNull(iE);
            assertTrue(iE instanceof Exception);
            verify(insertOfferingActionProfileActor).execute(any(SqlParameterSource.class));
            verify(insertRegistrationActor).execute(any(SqlParameterSource.class));
            verify(insertOrderActor).execute(any(SqlParameterSource.class));
        }
    }

    @Test
    public void testFailInsertCharge() throws Exception {
        HashMap<String, Object> sqlResult = new HashMap();

        when(jdbcTemplate.queryForObject(getOfferingActionSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(insertOfferingActionProfileActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getRegistrationSequenceQuery, String.class)).thenReturn("1000");
        doReturn(sqlResult).when(insertRegistrationActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getOrderSequenceQuery, String.class)).thenReturn("100");
        doReturn(sqlResult).when(insertOrderActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getOrderItemSequenceQuery, String.class)).thenReturn("1000");
        doReturn(sqlResult).when(insertOrderItemActor).execute(any(SqlParameterSource.class));

        when(jdbcTemplate.queryForObject(getChargeSequenceQuery, String.class)).thenReturn("100");
        when(insertChargeActor.execute(any(SqlParameterSource.class))).thenThrow(new IllegalArgumentException("BAD SQL"));

        try {
            String id = registrationDAO.registerForCourse(loggedInUser, student, session);
            assertTrue(false); //Should never reach this line
        }
        catch (IllegalArgumentException iE) {
            assertNotNull(iE);
            assertTrue(iE instanceof Exception);
            verify(insertOfferingActionProfileActor).execute(any(SqlParameterSource.class));
            verify(insertRegistrationActor).execute(any(SqlParameterSource.class));
            verify(insertOrderActor).execute(any(SqlParameterSource.class));
            verify(insertOrderItemActor).execute(any(SqlParameterSource.class));
        }
    }

    @Test
    public void testFailInsertPayment() throws Exception {
        HashMap<String, Object> sqlResult = new HashMap();

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
        when(insertPaymentActor.execute(any(SqlParameterSource.class))).thenThrow(new IllegalArgumentException("BAD SQL"));

        try {
            String id = registrationDAO.registerForCourse(loggedInUser, student, session);
            assertTrue(false); //Should never reach this line
        }
        catch (IllegalArgumentException iE) {
            assertNotNull(iE);
            assertTrue(iE instanceof Exception);
            verify(insertOfferingActionProfileActor).execute(any(SqlParameterSource.class));
            verify(insertRegistrationActor).execute(any(SqlParameterSource.class));
            verify(insertOrderActor).execute(any(SqlParameterSource.class));
            verify(insertOrderItemActor).execute(any(SqlParameterSource.class));
            verify(insertChargeActor).execute(any(SqlParameterSource.class));
        }
    }

    @Test
    public void testFailOrderComplete() throws Exception {
        HashMap<String, Object> sqlResult = new HashMap();

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
        when(orderCompleteActor.execute(any(SqlParameterSource.class))).thenThrow(new IllegalArgumentException("BAD SQL"));

        try {
            String id = registrationDAO.registerForCourse(loggedInUser, student, session);
            assertTrue(false); //Should never reach this line
        }
        catch (IllegalArgumentException iE) {
            assertNotNull(iE);
            assertTrue(iE instanceof Exception);
            verify(insertOfferingActionProfileActor).execute(any(SqlParameterSource.class));
            verify(insertRegistrationActor).execute(any(SqlParameterSource.class));
            verify(insertOrderActor).execute(any(SqlParameterSource.class));
            verify(insertOrderItemActor).execute(any(SqlParameterSource.class));
            verify(insertChargeActor).execute(any(SqlParameterSource.class));
            verify(insertPaymentActor).execute(any(SqlParameterSource.class));
        }
    }

    @Test
    public void testGetOrderNumber() throws Exception {
        String orderId = "test1234";
        Object[] expectedQueryParams = new Object[] {orderId};
        String expectedOrderNo = "999999";
        when(jdbcTemplate.queryForObject(getOrderNumberQuery, expectedQueryParams, String.class)).thenReturn(expectedOrderNo);
        String actualOrderNo = registrationDAO.getOrderNumber(orderId);

        assertNotNull("Expected OrderNumber", actualOrderNo);
        assertEquals("Order number not what expected", expectedOrderNo, actualOrderNo);

        verify(jdbcTemplate).queryForObject(eq(getOrderNumberQuery), getOrderNoCaptor.capture(), eq(String.class));
        Object[] capturedQueryParams = getOrderNoCaptor.getValue();
        assertNotNull("Expected parameters", capturedQueryParams);
        assertArrayEquals("wrong parameters", expectedQueryParams, capturedQueryParams);
    }

    @Test
    public void testFailToGetOrderNumber() throws Exception {
        String orderId = "test1234";
        Object[] expectedQueryParams = new Object[] {orderId};

        when(jdbcTemplate.queryForObject(getOrderNumberQuery, new Object[]{orderId}, String.class)).thenReturn(null);
        String actualOrderNo = registrationDAO.getOrderNumber(orderId);

        assertNull("Expected no order number", actualOrderNo);
        verify(jdbcTemplate).queryForObject(eq(getOrderNumberQuery), getOrderNoCaptor.capture(),eq(String.class));
        Object[] capturedQueryParams = getOrderNoCaptor.getValue();
        assertNotNull("Expected parameters", capturedQueryParams);
        assertArrayEquals("wrong parameters", expectedQueryParams, capturedQueryParams);
    }
}
