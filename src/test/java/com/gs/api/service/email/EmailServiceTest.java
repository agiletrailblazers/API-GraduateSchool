package com.gs.api.service.email;

import com.gs.api.dao.CourseSessionDAO;
import com.gs.api.dao.registration.UserDAO;
import com.gs.api.domain.Person;
import com.gs.api.domain.course.CourseSession;
import com.gs.api.domain.course.Location;
import com.gs.api.domain.payment.Payment;
import com.gs.api.domain.payment.PaymentConfirmation;
import com.gs.api.domain.registration.Registration;
import com.gs.api.domain.registration.RegistrationResponse;
import com.gs.api.domain.registration.User;
import org.apache.velocity.app.VelocityEngine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;

import static org.mockito.Mockito.*;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ContextConfiguration;

import java.util.Map;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.velocity.VelocityEngineUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest(VelocityEngineUtils.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class EmailServiceTest {

    private EmailServiceImpl emailService;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private VelocityEngine velocityEngine;

    @Mock
    private UserDAO userDao;

    @Mock
    private CourseSessionDAO sessionDao;

    @Captor
    private ArgumentCaptor<Map<String, Object>> orderModelCaptor;

    private RegistrationResponse registrationResponse;
    private final static String[] RECIPIENTS = {"atestemaill@test.com", "betestemail@test.com"};
    private static final String SESSION_ID = "12345";
    private static final String STUDENT_ID = "person44444";
    private static final String REGISTRATION_ID = "12345";
    private static final String ORDER_NUMBER = "23456";
    private static final double SESSION_TUITION = 400.10;
    private static final String AUTHORIZATION_ID = "auth1234";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        emailService = new EmailServiceImpl();
        ReflectionTestUtils.setField(emailService, "mailSender", mailSender);
        ReflectionTestUtils.setField(emailService, "velocityEngine", velocityEngine);

        ReflectionTestUtils.setField(emailService, "userDao", userDao);
        ReflectionTestUtils.setField(emailService, "sessionDao", sessionDao);
        PowerMockito.mockStatic(VelocityEngineUtils.class);
        Registration reg = new Registration();
        reg.setId(REGISTRATION_ID);
        reg.setOrderNumber(ORDER_NUMBER);
        reg.setSessionId(SESSION_ID);
        reg.setStudentId(STUDENT_ID);
        List<Registration> regs = new ArrayList<>();
        regs.add(reg);

        PaymentConfirmation pay = new PaymentConfirmation(new Payment(SESSION_TUITION, AUTHORIZATION_ID, "ref123"), "sale123");
        List<PaymentConfirmation> pays = new ArrayList<>();
        pays.add(pay);

        registrationResponse = new RegistrationResponse(regs, pays);
    }

    @Test
    public void testSuccessfulEmail() throws Exception {
        CourseSession courseSession = new CourseSession();
        courseSession.setClassNumber(SESSION_ID);
        courseSession.setCourseTitle("A Course on Tests");
        courseSession.setCourseCode("ABC00123");
        Location location = new Location();
        location.setCity("Media");
        location.setState("PA");
        courseSession.setLocation(location);
        courseSession.setStartDate(new Date());
        courseSession.setEndDate(new Date());
        courseSession.setStartTime("10:00 AM");
        courseSession.setEndTime("12:00 PM");
        courseSession.setDays("M W F");
        courseSession.setTuition(SESSION_TUITION);
        Person person = new Person();
        person.setFirstName("Tom");
        person.setLastName("Tester");
        person.setEmailAddress("test@test.com");
        User student = new User(STUDENT_ID,"test@test.com","","", person, "", "", "", "", "");

        when(userDao.getUser(STUDENT_ID)).thenReturn(student);
        when(sessionDao.getSessionById(SESSION_ID)).thenReturn(courseSession);
        VelocityEngineUtils mockUtils = PowerMockito.mock(VelocityEngineUtils.class);
        PowerMockito.when(mockUtils.mergeTemplateIntoString(
                any(VelocityEngine.class), Mockito.anyString(), Mockito.anyString(), Mockito.any(Map.class)))
                .thenReturn("Html Page").thenReturn("Text Page");

        emailService.sendPaymentReceiptEmail(RECIPIENTS, registrationResponse);

        PowerMockito.verifyStatic();
        verify(mockUtils.mergeTemplateIntoString(any(VelocityEngine.class), Mockito.anyString(), Mockito.anyString(), Mockito.any(Map.class)));

        Map<String, Object> orderModel = orderModelCaptor.getValue();
    }


    @Test
    public void testSuccessfulEmailMultiplePaymentsAndRegs() throws Exception {

    }

    @Test
    public void testInvalidEmailAddress() throws Exception {
    }
}
