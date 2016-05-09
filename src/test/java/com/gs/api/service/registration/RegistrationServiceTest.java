package com.gs.api.service.registration;

import com.gs.api.dao.CourseSessionDAO;
import com.gs.api.dao.registration.RegistrationDAO;
import com.gs.api.dao.registration.UserDAO;
import com.gs.api.domain.Address;
import com.gs.api.domain.Person;
import com.gs.api.domain.course.CourseSession;
import com.gs.api.domain.payment.Payment;
import com.gs.api.domain.payment.PaymentConfirmation;
import com.gs.api.domain.registration.*;
import com.gs.api.exception.PaymentAcceptedException;
import com.gs.api.exception.PaymentException;
import com.gs.api.service.email.EmailService;
import com.gs.api.service.payment.PaymentService;

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

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class RegistrationServiceTest {

    private static final String USER_ID = "person654321";
    private static final String SESSION_ID = "session12345";
    private static final String STUDENT_ID_1 = "person44444";
    private static final String STUDENT_ID_2 = "person55555";

    private static final String REGISTRATION_ID = "12345";
    public static final String ORDER_NUMBER = "23456";

    private static final Date START_DATE = new Date();
    private static final Date END_DATE = new Date(START_DATE.getTime() + 1000);
    private static final String TYPE = "CLASSROOM";
    private static final String COURSE_NO = "course1234";
    private static final String COURSE_TITLE = "Introduction to Testing";

    private static final String ADDRESS1 = "123 Main Street";
    private static final String ADDRESS2 = "Suite 100";
    private static final String CITY = "Washington";
    private static final String STATE = "DC";
    private static final String ZIP = "12345";

    private Address address;

    //private List<Registration> registrations;
    private RegistrationRequest registrationRequest;

    @Mock
    private RegistrationDAO registrationDao;

    @Mock
    private CourseSessionDAO sessionDao;

    @Mock
    private PaymentService paymentService;

    @Mock
    private EmailService emailService;

    @Mock
    private UserDAO userDao;

    @InjectMocks
    @Autowired
    private RegistrationServiceImpl registrationService;

    private String userTimestamp;

    /*
        By default, no exceptions are expected to be thrown (i.e. tests will fail if an exception is thrown),
        but using this rule allows for verification of operations that are expected to throw specific exceptions
    */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        List<Registration> registrations = new ArrayList<>();

        Registration registration1 = new Registration();
        registration1.setStudentId(STUDENT_ID_1);
        registration1.setSessionId(SESSION_ID);
        registrations.add(registration1);

        List<Payment> payments = new ArrayList<>();

        registrationRequest = new RegistrationRequest(registrations, payments);

        userTimestamp = Long.toString(new Date().getTime());

        address = new Address();
        address.setAddress1(ADDRESS1);
        address.setAddress2(ADDRESS2);
        address.setCity(CITY);
        address.setState(STATE);
        address.setPostalCode(ZIP);
    }

    @Test
    public void testRegisterTwoRegs() throws Exception {
        //Add second reg
        Registration registration2 = new Registration();
        registration2.setStudentId(STUDENT_ID_2);
        registration2.setSessionId(SESSION_ID);
        registrationRequest.getRegistrations().add(registration2);

        User user = new User(USER_ID, "user1", "", "1234", new Person(), "", "", "", "", userTimestamp);
        User student1 = new User(STUDENT_ID_1, "student1", "", "1234", new Person(), "", "", "", "", userTimestamp);
        User student2 = new User(STUDENT_ID_2, "student2", "", "1234", new Person(), "", "", "", "", userTimestamp);

        CourseSession session = new CourseSession();
        session.setClassNumber(SESSION_ID);

        Payment payment = new Payment(100.00, "authId12345", "merchId12345");
        registrationRequest.getPayments().add(payment);
        PaymentConfirmation paymentConfirmation = new PaymentConfirmation(payment, "saleId12345");

        when(userDao.getUser(USER_ID)).thenReturn(user);
        when(userDao.getUser(STUDENT_ID_1)).thenReturn(student1);
        when(userDao.getUser(STUDENT_ID_2)).thenReturn(student2);
        when(sessionDao.getSessionById(SESSION_ID)).thenReturn(session);
        when(paymentService.processPayment(payment)).thenReturn(paymentConfirmation);

        Registration createdRegistration1 = new Registration();
        createdRegistration1.setId("12345");
        Registration createdRegistration2 = new Registration();
        createdRegistration2.setId("54321");

        when(registrationDao.registerForCourse(user, student1, session)).thenReturn(createdRegistration1);
        when(registrationDao.registerForCourse(user, student2, session)).thenReturn(createdRegistration2);

        RegistrationResponse createdRegistrationResponse = registrationService.register(USER_ID, registrationRequest);

        verify(userDao, times(3)).getUser(any(String.class));
        verify(sessionDao, times(2)).getSessionById(any(String.class));
        verify(paymentService).processPayment(payment);
        verify(registrationDao, times(2)).registerForCourse(any(User.class), any(User.class), any(CourseSession.class));

        // the list returned from the service should not be the same instance as the one passed in,
        // it should be a list of the created registrations returned by the DAO
        assertNotSame(createdRegistrationResponse.getRegistrations(), registrationRequest.getRegistrations());
        assertSame(createdRegistration1, createdRegistrationResponse.getRegistrations().get(0));
        assertSame(createdRegistration2, createdRegistrationResponse.getRegistrations().get(1));
        assertEquals(1, createdRegistrationResponse.getPaymentConfirmations().size());
        assertSame(paymentConfirmation, createdRegistrationResponse.getPaymentConfirmations().get(0));
    }

    @Test
    public void testPaymentFailsNoRegistrationCreated() throws Exception {

        Payment payment = new Payment(100.00, "badAuth123", "badMerchId1234");
        registrationRequest.getPayments().add(payment);
        PaymentException pe = new PaymentException("I made payment fail");

        when(paymentService.processPayment(payment)).thenThrow(pe);

        try {
            registrationService.register(USER_ID, registrationRequest);
            fail("Shouldn't reach here");
        }
        catch (PaymentException payException) {
            verify(paymentService).processPayment(payment);
            verifyZeroInteractions(registrationDao);
            verifyZeroInteractions(userDao);
            verifyZeroInteractions(sessionDao);
        }
    }

    @Test
    public void testRegisterUserNotFound() throws Exception {

        thrown.expect(PaymentAcceptedException.class);
        thrown.expectMessage(RegistrationServiceImpl.REGISTRATION_FAILURE_AFTER_SUCCESSFUL_PAYMENT_MSG);

        when(userDao.getUser(USER_ID)).thenReturn(null);

        registrationService.register(USER_ID, registrationRequest);
    }

    @Test
    public void testRegisterStudentNotFound() throws Exception {

        thrown.expect(PaymentAcceptedException.class);
        thrown.expectMessage(RegistrationServiceImpl.REGISTRATION_FAILURE_AFTER_SUCCESSFUL_PAYMENT_MSG);

        User user = new User(USER_ID, "user1", "", "1234", new Person(), "", "", "", "", userTimestamp);

        when(userDao.getUser(USER_ID)).thenReturn(user);
        when(userDao.getUser(STUDENT_ID_1)).thenReturn(null);

        registrationService.register(USER_ID, registrationRequest);
    }

    @Test
    public void testRegisterSessionNotFound() throws Exception {

        thrown.expect(PaymentAcceptedException.class);
        thrown.expectMessage(RegistrationServiceImpl.REGISTRATION_FAILURE_AFTER_SUCCESSFUL_PAYMENT_MSG);

        User user = new User(USER_ID, "user1", "", "1234", new Person(), "", "", "", "", userTimestamp);
        User student1 = new User(STUDENT_ID_1, "student1", "", "1234", new Person(), "", "", "", "", userTimestamp);

        when(userDao.getUser(USER_ID)).thenReturn(user);
        when(userDao.getUser(STUDENT_ID_1)).thenReturn(student1);

        when(sessionDao.getSessionById(SESSION_ID)).thenReturn(null);

        registrationService.register(USER_ID, registrationRequest);
    }

    @Test
    public void testRegisterSucceedsEmailFails() throws Exception {
        Exception expectedException = new Exception("Mail fail");
        User user = new User(USER_ID, "user1", "", "1234", new Person(), "", "", "", "", userTimestamp);
        User student1 = new User(STUDENT_ID_1, "student1", "", "1234", new Person(), "", "", "", "", userTimestamp);
        CourseSession session = new CourseSession();
        session.setClassNumber(SESSION_ID);

        Payment payment = new Payment(100.00, "authId12345", "merchId12345");
        registrationRequest.getPayments().add(payment);
        PaymentConfirmation paymentConfirmation = new PaymentConfirmation(payment, "saleId12345");

        when(userDao.getUser(USER_ID)).thenReturn(user);
        when(userDao.getUser(STUDENT_ID_1)).thenReturn(student1);
        when(sessionDao.getSessionById(SESSION_ID)).thenReturn(session);
        when(paymentService.processPayment(payment)).thenReturn(paymentConfirmation);

        Registration createdRegistration1 = new Registration();
        createdRegistration1.setId("12345");
        Registration createdRegistration2 = new Registration();
        createdRegistration2.setId("54321");

        when(registrationDao.registerForCourse(user, student1, session)).thenReturn(createdRegistration1);

        doThrow(expectedException).when(emailService).sendPaymentReceiptEmail(any(String[].class), any(RegistrationResponse.class));

        RegistrationResponse createdRegistrationResponse = registrationService.register(USER_ID, registrationRequest);
    }

    @Test
    public void testGetRegistrationForSession() throws Exception {

        Registration mockedRegistration = new Registration();
        mockedRegistration.setId(REGISTRATION_ID);
        mockedRegistration.setSessionId(SESSION_ID);
        mockedRegistration.setOrderNumber(ORDER_NUMBER);
        mockedRegistration.setStudentId(USER_ID);

        when(registrationDao.getRegistration(USER_ID,SESSION_ID)).thenReturn(Arrays.asList(mockedRegistration));

        List<Registration> createdRegistration = registrationService.getRegistrationForSession(USER_ID, SESSION_ID);

        verify(registrationDao).getRegistration(eq(USER_ID), eq(SESSION_ID));

        assertEquals(USER_ID, createdRegistration.get(0).getStudentId());
        assertEquals(SESSION_ID, createdRegistration.get(0).getSessionId());
        assertEquals(ORDER_NUMBER, createdRegistration.get(0).getOrderNumber());
        assertEquals(REGISTRATION_ID, createdRegistration.get(0).getId());
    }

    @Test
    public void testGetRegistrationDetails() throws Exception {
        RegistrationDetails registrationDetails = new RegistrationDetails(
                SESSION_ID,
                COURSE_NO,
                COURSE_TITLE,
                START_DATE.getTime(),
                END_DATE.getTime(),
                address,
                TYPE
        );

        when(registrationDao.getRegistrationDetails(USER_ID)).thenReturn(Collections.singletonList(registrationDetails));

        List<RegistrationDetails> createdRegistrationDetailsList = registrationService.getRegistrationDetails(USER_ID);

        verify(registrationDao).getRegistrationDetails(eq(USER_ID));

        assertEquals(SESSION_ID, createdRegistrationDetailsList.get(0).getSessionNo());
        assertEquals(COURSE_NO, createdRegistrationDetailsList.get(0).getCourseNo());
        assertEquals(COURSE_TITLE, createdRegistrationDetailsList.get(0).getCourseTitle());
        assertTrue(START_DATE.getTime() == createdRegistrationDetailsList.get(0).getStartDate());
        assertTrue(END_DATE.getTime() == createdRegistrationDetailsList.get(0).getEndDate());
        assertEquals(address, createdRegistrationDetailsList.get(0).getAddress());
        assertEquals(TYPE, createdRegistrationDetailsList.get(0).getType());
    }
}
