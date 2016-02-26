package com.gs.api.service.registration;

import com.gs.api.dao.CourseSessionDAO;
import com.gs.api.dao.registration.RegistrationDAO;
import com.gs.api.dao.registration.UserDAO;
import com.gs.api.domain.Person;
import com.gs.api.domain.course.CourseSession;
import com.gs.api.domain.payment.Payment;
import com.gs.api.domain.payment.PaymentConfirmation;
import com.gs.api.domain.registration.Registration;
import com.gs.api.domain.registration.RegistrationRequest;
import com.gs.api.domain.registration.RegistrationResponse;
import com.gs.api.domain.registration.User;
import com.gs.api.service.payment.PaymentService;

import org.junit.Before;
import org.junit.Test;
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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class RegistrationServiceTest {

    private static final String USER_ID = "person654321";
    private static final String SESSION_ID = "session12345";
    private static final String STUDENT_ID_1 = "person44444";
    private static final String STUDENT_ID_2 = "person55555";

    //private List<Registration> registrations;
    private RegistrationRequest registrationRequest;

    @Mock
    private RegistrationDAO registrationDao;

    @Mock
    private CourseSessionDAO sessionDao;

    @Mock
    private PaymentService paymentService;

    @Mock
    private UserDAO userDao;

    @InjectMocks
    @Autowired
    private RegistrationServiceImpl registrationService;

    private String userTimestamp;

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
        when(sessionDao.getSession(SESSION_ID)).thenReturn(session);
        when(paymentService.processPayment(payment)).thenReturn(paymentConfirmation);

        Registration createdRegistration1 = new Registration();
        createdRegistration1.setId("12345");
        Registration createdRegistration2 = new Registration();
        createdRegistration2.setId("54321");

        when(registrationDao.registerForCourse(user, student1, session)).thenReturn(createdRegistration1);
        when(registrationDao.registerForCourse(user, student2, session)).thenReturn(createdRegistration2);

        RegistrationResponse createdRegistrationResponse = registrationService.register(USER_ID, registrationRequest);

        verify(userDao, times(4)).getUser(any(String.class));
        verify(sessionDao, times(2)).getSession(any(String.class));
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
    public void testRegisterUserNotFound() throws Exception {
        when(userDao.getUser(USER_ID)).thenReturn(null);
        try {

            registrationService.register(USER_ID, registrationRequest);
            fail("Shouldn't reach here");
        }
        catch (Exception e) {
            assertTrue(e.getMessage().contains("No user found for logged in user"));
        }
    }

    @Test
    public void testRegisterStudentNotFound() throws Exception {
        User user = new User(USER_ID, "user1", "", "1234", new Person(), "", "", "", "", userTimestamp);

        when(userDao.getUser(USER_ID)).thenReturn(user);
        when(userDao.getUser(STUDENT_ID_1)).thenReturn(null);

        try {
            registrationService.register(USER_ID, registrationRequest);
            fail("Shouldn't reach here");
        }
        catch (Exception e) {
            assertTrue(e.getMessage().contains("No user found for student"));
        }
    }

    @Test
    public void testRegisterSessionNotFound() throws Exception {
        User user = new User(USER_ID, "user1", "", "1234", new Person(), "", "", "", "", userTimestamp);
        User student1 = new User(STUDENT_ID_1, "student1", "", "1234", new Person(), "", "", "", "", userTimestamp);


        when(userDao.getUser(USER_ID)).thenReturn(user);
        when(userDao.getUser(STUDENT_ID_1)).thenReturn(student1);

        when(sessionDao.getSession(SESSION_ID)).thenReturn(null);
        try {
            registrationService.register(USER_ID, registrationRequest);
            fail("Shouldn't reach here");
        }
        catch (Exception e) {
            assertTrue(e.getMessage().contains("No course session found for session id"));
        }
    }
}
