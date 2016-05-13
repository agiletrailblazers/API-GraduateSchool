package com.gs.api.service.email;

import com.gs.api.dao.CourseSessionDAO;
import com.gs.api.dao.UserDAO;
import com.gs.api.domain.Person;
import com.gs.api.domain.course.CourseSession;
import com.gs.api.domain.course.Location;
import com.gs.api.domain.payment.Payment;
import com.gs.api.domain.payment.PaymentConfirmation;
import com.gs.api.domain.registration.Registration;
import com.gs.api.domain.registration.RegistrationResponse;
import com.gs.api.domain.User;

import org.apache.velocity.app.VelocityEngine;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.velocity.VelocityEngineUtils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.mail.internet.MimeMessage;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(VelocityEngineUtils.class)
public class EmailServiceTest {

    private EmailServiceImpl emailService;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private VelocityEngine velocityEngine;

    @Mock
    private MimeMessageHelper mimeMessageHelper;

    @Mock
    private UserDAO userDao;

    @Mock
    private CourseSessionDAO sessionDao;

    @Mock
    private MimeMessage mimeMessage;

    /*
        By default, no exceptions are expected to be thrown (i.e. tests will fail if an exception is thrown),
        but using this rule allows for verification of operations that are expected to throw specific exceptions
    */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    public MailException expectedException;

    @Captor
    private ArgumentCaptor<MimeMessagePreparator> mimeMessagePreparatorCaptor;

    @Captor
    private ArgumentCaptor<String> templatePathCaptor;

    @Captor
    private ArgumentCaptor<Map<String, Object>> modelCaptor;

    @Captor
    private ArgumentCaptor<String[]> recipientsCaptor;

    @Captor
    private ArgumentCaptor<String> subjectCaptor;

    @Captor
    private ArgumentCaptor<String> htmlTextCaptor;

    @Captor
    private ArgumentCaptor<String> plainTextCaptor;

    private NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM. dd, YYYY");
    private RegistrationResponse registrationResponse;
    private CourseSession expectedSession;
    private User expectedStudent;
    private final static String[] RECIPIENTS = {"atestemaill@test.com", "betestemail@test.com"};
    private static final String SESSION_ID = "12345";
    private static final String STUDENT_ID = "person44444";
    private static final String REGISTRATION_ID = "12345";
    private static final String ORDER_NUMBER = "23456";
    private static final double SESSION_TUITION = 400.10;
    private static final String AUTHORIZATION_ID = "auth1234";
    private static final String HTML_EMAIL_TEXT ="<html>email text</html>";
    private static final String PLAIN_EMAIL_TEXT ="email text";
    private static final String LOGIN_URL = "someurl";
    private static final String SUBJECT = "some subject";

    @Before
    public void setUp() throws Exception {
        emailService = new EmailServiceImpl();
        mockStatic(VelocityEngineUtils.class);

        ReflectionTestUtils.setField(emailService, "mailSender", mailSender);
        ReflectionTestUtils.setField(emailService, "velocityEngine", velocityEngine);
        ReflectionTestUtils.setField(emailService, "userDao", userDao);
        ReflectionTestUtils.setField(emailService, "sessionDao", sessionDao);
        ReflectionTestUtils.setField(emailService, "mimeMessageHelper", mimeMessageHelper);
        ReflectionTestUtils.setField(emailService, "paymentReceiptEmailSubject", "Graduate School Payment Receipt");
        ReflectionTestUtils.setField(emailService, "newUserEmailSubject", "- Welcome to the Graduate School!");
        ReflectionTestUtils.setField(emailService, "passwordResetSubject", SUBJECT);
        ReflectionTestUtils.setField(emailService, "userLoginPage", LOGIN_URL);

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

        expectedSession = new CourseSession();
        expectedSession.setClassNumber(SESSION_ID);
        expectedSession.setCourseTitle("A Course on Tests");
        expectedSession.setCourseCode("ABC00123");
        Location expectedLocation = new Location();
        expectedLocation.setCity("Media");
        expectedLocation.setState("PA");
        expectedSession.setLocation(expectedLocation);
        expectedSession.setStartDate(new Date());
        expectedSession.setEndDate(new Date());
        expectedSession.setStartTime("10:00 AM");
        expectedSession.setEndTime("12:00 PM");
        expectedSession.setDays("M W F");
        expectedSession.setTuition(SESSION_TUITION);

        Person expectedStudentPerson = new Person();
        expectedStudentPerson.setFirstName("Tom");
        expectedStudentPerson.setLastName("Tester");
        expectedStudentPerson.setEmailAddress("test@test.com");
        expectedStudent = new User(STUDENT_ID,"test@test.com","","", expectedStudentPerson, "", "", "", "", "", "");
    }

    @Test
    public void testSuccessfulPaymentReceiptEmail() throws Exception {
        when(userDao.getUser(STUDENT_ID)).thenReturn(expectedStudent);
        when(sessionDao.getSessionById(SESSION_ID)).thenReturn(expectedSession);
        when(VelocityEngineUtils.mergeTemplateIntoString(
                any(VelocityEngine.class), Mockito.anyString(), Mockito.anyString(), Mockito.any(Map.class)))
                .thenReturn("Html Page").thenReturn("Text Page");

        emailService.sendPaymentReceiptEmail(RECIPIENTS, registrationResponse);

        verify(mailSender).send(mimeMessagePreparatorCaptor.capture());
        MimeMessagePreparator preparator = mimeMessagePreparatorCaptor.getValue();
        assertNotNull("No mime message preparator provided to send", preparator);

        // call the prepare() method so we can verify the logic
        preparator.prepare(mimeMessage);
        verify(mimeMessageHelper).setTo(recipientsCaptor.capture());
        verify(mimeMessageHelper).setSubject(subjectCaptor.capture());
        verify(mimeMessageHelper).setText(plainTextCaptor.capture(), htmlTextCaptor.capture());

        // verify mime message attributes
        assertArrayEquals("Wrong Recipients", RECIPIENTS, recipientsCaptor.getAllValues().get(0));
        assertEquals("Wrong Subject", "Graduate School Payment Receipt", subjectCaptor.getAllValues().get(0));
        assertEquals("HTML Template wrong", "Html Page", htmlTextCaptor.getAllValues().get(0));
        assertEquals("Text Template wrong", "Text Page", plainTextCaptor.getAllValues().get(0));

        // the static will get called twice, once for each template, verify both
        PowerMockito.verifyStatic(Mockito.times(2));
        VelocityEngineUtils.mergeTemplateIntoString(any(VelocityEngine.class), templatePathCaptor.capture(), Mockito.anyString(), modelCaptor.capture());

        assertEquals("Wrong html template path", EmailServiceImpl.PAYMENT_RECEIPT_HTML_TEMPLATE_VM, templatePathCaptor.getAllValues().get(0));
        assertEquals("Wrong text template path", EmailServiceImpl.PAYMENT_RECEIPT_TEXT_TEMPLATE_VM, templatePathCaptor.getAllValues().get(1));

        // verify the same model map is passed to both templates
        assertSame(modelCaptor.getAllValues().get(0), modelCaptor.getAllValues().get(1));

        // verify the orderModel's data
        Map<String, Object> capturedOrderModel = modelCaptor.getAllValues().get(0);
        Registration expectedRegistration = registrationResponse.getRegistrations().get(0);
        assertEquals("Wrong Order Number", expectedRegistration.getOrderNumber(), capturedOrderModel.get("orderId"));
        assertEquals("Wrong Total Charged", currencyFormatter.format(expectedSession.getTuition()), capturedOrderModel.get("totalCharged"));
        ArrayList<Map<String, String>> orderPaymentModel = (ArrayList<Map<String, String>> ) capturedOrderModel.get("payments");
        assertEquals("Wrong Auth Code", registrationResponse.getPaymentConfirmations().get(0).getPayment().getAuthorizationId(), orderPaymentModel.get(0).get("authCode"));
        ArrayList<Map<String, String>> orderRegistrationModel = (ArrayList<Map<String, String>> ) capturedOrderModel.get("registrations");
        assertEquals("Wrong Student Name", expectedStudent.getPerson().getFirstName() + " " + expectedStudent.getPerson().getLastName(), orderRegistrationModel.get(0).get("studentName"));
        assertEquals("Wrong Session Tuition", currencyFormatter.format(expectedSession.getTuition()), orderRegistrationModel.get(0).get("tuition"));
        assertEquals("Wrong Course Title", expectedSession.getCourseTitle(), orderRegistrationModel.get(0).get("title"));
        assertEquals("Wrong Course Code", expectedSession.getCourseCode(), orderRegistrationModel.get(0).get("code"));
        assertEquals("Wrong Class Number", expectedSession.getClassNumber(), orderRegistrationModel.get(0).get("classId"));
        String expectedLocation = expectedSession.getLocation().getCity() +  ", " + expectedSession.getLocation().getState();
        assertEquals("Wrong Location", expectedLocation, orderRegistrationModel.get(0).get("location"));
        String expectedDates = dateFormatter.format(expectedSession.getStartDate());
        expectedDates = expectedDates.substring(0, expectedDates.length() - 6) +" - " + dateFormatter.format(expectedSession.getEndDate());
        assertEquals("Wrong Class Dates", expectedDates, orderRegistrationModel.get(0).get("dates"));
        String expectedTimes = expectedSession.getStartTime() + " - " + expectedSession.getEndTime();
        assertEquals("Wrong Class Times", expectedTimes, orderRegistrationModel.get(0).get("times"));
        assertEquals("Wrong Class Days", expectedSession.getDays(), orderRegistrationModel.get(0).get("days"));
        assertEquals("Wrong Class Email", expectedStudent.getPerson().getEmailAddress(), orderRegistrationModel.get(0).get("email"));
    }

    @Test
    public void testPaymentReceiptEmailNoLocation() throws Exception {
        expectedSession.getLocation().setCity(null);
        when(userDao.getUser(STUDENT_ID)).thenReturn(expectedStudent);
        when(sessionDao.getSessionById(SESSION_ID)).thenReturn(expectedSession);
        when(VelocityEngineUtils.mergeTemplateIntoString(
                any(VelocityEngine.class), Mockito.anyString(), Mockito.anyString(), Mockito.any(Map.class)))
                .thenReturn("Html Page").thenReturn("Text Page");

        emailService.sendPaymentReceiptEmail(RECIPIENTS, registrationResponse);

        verify(mailSender).send(mimeMessagePreparatorCaptor.capture());
        MimeMessagePreparator preparator = mimeMessagePreparatorCaptor.getValue();
        assertNotNull("No mime message preparator provided to send", preparator);

        // call the prepare() method so we can verify the logic
        preparator.prepare(mimeMessage);

        // the static will get called twice, once for each template, verify both
        PowerMockito.verifyStatic(Mockito.times(2));
        VelocityEngineUtils.mergeTemplateIntoString(any(VelocityEngine.class), templatePathCaptor.capture(), Mockito.anyString(), modelCaptor.capture());

        // verify the same model map is passed to both templates
        assertSame(modelCaptor.getAllValues().get(0), modelCaptor.getAllValues().get(1));

        // verify the orderModel's data
        Map<String, Object> capturedOrderModel = modelCaptor.getAllValues().get(0);
        Registration expectedRegistration = registrationResponse.getRegistrations().get(0);
        assertEquals("Wrong Order Number", expectedRegistration.getOrderNumber(), capturedOrderModel.get("orderId"));
        ArrayList<Map<String, String>> orderRegistrationModel = (ArrayList<Map<String, String>> ) capturedOrderModel.get("registrations");
        assertNull("Location shouldn't exist", orderRegistrationModel.get(0).get("location"));
    }

    @Test
    public void testPaymentReceiptEmailNoLocationState() throws Exception {
        expectedSession.getLocation().setState(null);
        when(userDao.getUser(STUDENT_ID)).thenReturn(expectedStudent);
        when(sessionDao.getSessionById(SESSION_ID)).thenReturn(expectedSession);
        when(VelocityEngineUtils.mergeTemplateIntoString(
                any(VelocityEngine.class), Mockito.anyString(), Mockito.anyString(), Mockito.any(Map.class)))
                .thenReturn("Html Page").thenReturn("Text Page");

        emailService.sendPaymentReceiptEmail(RECIPIENTS, registrationResponse);

        verify(mailSender).send(mimeMessagePreparatorCaptor.capture());
        MimeMessagePreparator preparator = mimeMessagePreparatorCaptor.getValue();
        assertNotNull("No mime message preparator provided to send", preparator);

        // call the prepare() method so we can verify the logic
        preparator.prepare(mimeMessage);

        // the static will get called twice, once for each template, verify both
        PowerMockito.verifyStatic(Mockito.times(2));
        VelocityEngineUtils.mergeTemplateIntoString(any(VelocityEngine.class), templatePathCaptor.capture(), Mockito.anyString(), modelCaptor.capture());

        // verify the same model map is passed to both templates
        assertSame(modelCaptor.getAllValues().get(0), modelCaptor.getAllValues().get(1));

        // verify the orderModel's data
        Map<String, Object> capturedOrderModel = modelCaptor.getAllValues().get(0);
        Registration expectedRegistration = registrationResponse.getRegistrations().get(0);
        assertEquals("Wrong Order Number", expectedRegistration.getOrderNumber(), capturedOrderModel.get("orderId"));
        ArrayList<Map<String, String>> orderRegistrationModel = (ArrayList<Map<String, String>> ) capturedOrderModel.get("registrations");
        assertEquals("Location be city", expectedSession.getLocation().getCity(), orderRegistrationModel.get(0).get("location"));
    }

    @Test
    public void testPaymentReceiptEmailNoDates() throws Exception {
        expectedSession.setStartDate(null);
        when(userDao.getUser(STUDENT_ID)).thenReturn(expectedStudent);
        when(sessionDao.getSessionById(SESSION_ID)).thenReturn(expectedSession);
        when(VelocityEngineUtils.mergeTemplateIntoString(
                any(VelocityEngine.class), Mockito.anyString(), Mockito.anyString(), Mockito.any(Map.class)))
                .thenReturn("Html Page").thenReturn("Text Page");

        emailService.sendPaymentReceiptEmail(RECIPIENTS, registrationResponse);

        verify(mailSender).send(mimeMessagePreparatorCaptor.capture());
        MimeMessagePreparator preparator = mimeMessagePreparatorCaptor.getValue();
        assertNotNull("No mime message preparator provided to send", preparator);

        // call the prepare() method so we can verify the logic
        preparator.prepare(mimeMessage);

        // the static will get called twice, once for each template, verify both
        PowerMockito.verifyStatic(Mockito.times(2));
        VelocityEngineUtils.mergeTemplateIntoString(any(VelocityEngine.class), templatePathCaptor.capture(), Mockito.anyString(), modelCaptor.capture());

        // verify the same model map is passed to both templates
        assertSame(modelCaptor.getAllValues().get(0), modelCaptor.getAllValues().get(1));

        // verify the orderModel's data
        Map<String, Object> capturedOrderModel = modelCaptor.getAllValues().get(0);
        Registration expectedRegistration = registrationResponse.getRegistrations().get(0);
        assertEquals("Wrong Order Number", expectedRegistration.getOrderNumber(), capturedOrderModel.get("orderId"));
        ArrayList<Map<String, String>> orderRegistrationModel = (ArrayList<Map<String, String>> ) capturedOrderModel.get("registrations");
        assertNull("No dates should exist", orderRegistrationModel.get(0).get("dates"));
    }

    @Test
    public void testPaymentReceiptEmailStartDateOnly() throws Exception {
        expectedSession.setEndDate(null);
        when(userDao.getUser(STUDENT_ID)).thenReturn(expectedStudent);
        when(sessionDao.getSessionById(SESSION_ID)).thenReturn(expectedSession);
        when(VelocityEngineUtils.mergeTemplateIntoString(
                any(VelocityEngine.class), Mockito.anyString(), Mockito.anyString(), Mockito.any(Map.class)))
                .thenReturn("Html Page").thenReturn("Text Page");

        emailService.sendPaymentReceiptEmail(RECIPIENTS, registrationResponse);

        verify(mailSender).send(mimeMessagePreparatorCaptor.capture());
        MimeMessagePreparator preparator = mimeMessagePreparatorCaptor.getValue();
        assertNotNull("No mime message preparator provided to send", preparator);

        // call the prepare() method so we can verify the logic
        preparator.prepare(mimeMessage);

        // the static will get called twice, once for each template, verify both
        PowerMockito.verifyStatic(Mockito.times(2));
        VelocityEngineUtils.mergeTemplateIntoString(any(VelocityEngine.class), templatePathCaptor.capture(), Mockito.anyString(), modelCaptor.capture());

        // verify the same model map is passed to both templates
        assertSame(modelCaptor.getAllValues().get(0), modelCaptor.getAllValues().get(1));

        // verify the orderModel's data
        Map<String, Object> capturedOrderModel = modelCaptor.getAllValues().get(0);
        Registration expectedRegistration = registrationResponse.getRegistrations().get(0);
        assertEquals("Wrong Order Number", expectedRegistration.getOrderNumber(), capturedOrderModel.get("orderId"));
        ArrayList<Map<String, String>> orderRegistrationModel = (ArrayList<Map<String, String>> ) capturedOrderModel.get("registrations");
        assertEquals("Dates should be start date", dateFormatter.format(expectedSession.getStartDate()), orderRegistrationModel.get(0).get("dates"));
    }

    @Test
    public void testPaymentReceiptEmailNoTimes() throws Exception {
        expectedSession.setStartTime(null);
        when(userDao.getUser(STUDENT_ID)).thenReturn(expectedStudent);
        when(sessionDao.getSessionById(SESSION_ID)).thenReturn(expectedSession);
        when(VelocityEngineUtils.mergeTemplateIntoString(
                any(VelocityEngine.class), Mockito.anyString(), Mockito.anyString(), Mockito.any(Map.class)))
                .thenReturn("Html Page").thenReturn("Text Page");

        emailService.sendPaymentReceiptEmail(RECIPIENTS, registrationResponse);

        verify(mailSender).send(mimeMessagePreparatorCaptor.capture());
        MimeMessagePreparator preparator = mimeMessagePreparatorCaptor.getValue();
        assertNotNull("No mime message preparator provided to send", preparator);

        // call the prepare() method so we can verify the logic
        preparator.prepare(mimeMessage);

        // the static will get called twice, once for each template, verify both
        PowerMockito.verifyStatic(Mockito.times(2));
        VelocityEngineUtils.mergeTemplateIntoString(any(VelocityEngine.class), templatePathCaptor.capture(), Mockito.anyString(), modelCaptor.capture());

        // verify the same model map is passed to both templates
        assertSame(modelCaptor.getAllValues().get(0), modelCaptor.getAllValues().get(1));

        // verify the orderModel's data
        Map<String, Object> capturedOrderModel = modelCaptor.getAllValues().get(0);
        Registration expectedRegistration = registrationResponse.getRegistrations().get(0);
        assertEquals("Wrong Order Number", expectedRegistration.getOrderNumber(), capturedOrderModel.get("orderId"));
        ArrayList<Map<String, String>> orderRegistrationModel = (ArrayList<Map<String, String>> ) capturedOrderModel.get("registrations");
        assertNull("No times should exist", orderRegistrationModel.get(0).get("times"));
    }

    @Test
    public void testPaymentReceiptEmailStartTimeOnly() throws Exception {
        expectedSession.setEndTime(null);
        when(userDao.getUser(STUDENT_ID)).thenReturn(expectedStudent);
        when(sessionDao.getSessionById(SESSION_ID)).thenReturn(expectedSession);
        when(VelocityEngineUtils.mergeTemplateIntoString(
                any(VelocityEngine.class), Mockito.anyString(), Mockito.anyString(), Mockito.any(Map.class)))
                .thenReturn("Html Page").thenReturn("Text Page");

        emailService.sendPaymentReceiptEmail(RECIPIENTS, registrationResponse);

        verify(mailSender).send(mimeMessagePreparatorCaptor.capture());
        MimeMessagePreparator preparator = mimeMessagePreparatorCaptor.getValue();
        assertNotNull("No mime message preparator provided to send", preparator);

        // call the prepare() method so we can verify the logic
        preparator.prepare(mimeMessage);

        // the static will get called twice, once for each template, verify both
        PowerMockito.verifyStatic(Mockito.times(2));
        VelocityEngineUtils.mergeTemplateIntoString(any(VelocityEngine.class), templatePathCaptor.capture(), Mockito.anyString(), modelCaptor.capture());

        // verify the same model map is passed to both templates
        assertSame(modelCaptor.getAllValues().get(0), modelCaptor.getAllValues().get(1));

        // verify the orderModel's data
        Map<String, Object> capturedOrderModel = modelCaptor.getAllValues().get(0);
        Registration expectedRegistration = registrationResponse.getRegistrations().get(0);
        assertEquals("Wrong Order Number", expectedRegistration.getOrderNumber(), capturedOrderModel.get("orderId"));
        ArrayList<Map<String, String>> orderRegistrationModel = (ArrayList<Map<String, String>> ) capturedOrderModel.get("registrations");
        assertEquals("Times should be start date", expectedSession.getStartTime(), orderRegistrationModel.get(0).get("times"));
    }

    @Test
    public void testSuccessfulEmailMultiplePaymentsAndRegs() throws Exception {
        Registration secondRegistration = new Registration();
        secondRegistration.setId("ID2");
        secondRegistration.setOrderNumber(ORDER_NUMBER);
        secondRegistration.setSessionId(SESSION_ID);
        secondRegistration.setStudentId("PERS2");
        registrationResponse.getRegistrations().add(secondRegistration);
        Person studentPerson = new Person();
        studentPerson.setFirstName("Jane");
        studentPerson.setLastName("Tester");
        studentPerson.setEmailAddress("jane@test.com");
        User secondStudent = new User(STUDENT_ID,"jane@test.com","","", studentPerson, "", "", "", "", "", "");

        PaymentConfirmation secondPaymentConf = new PaymentConfirmation(new Payment(SESSION_TUITION, "authId2", "ref2"), "sale2");
        registrationResponse.getPaymentConfirmations().add(secondPaymentConf);

        when(userDao.getUser(STUDENT_ID)).thenReturn(expectedStudent);
        when(userDao.getUser("PERS2")).thenReturn(secondStudent);
        when(sessionDao.getSessionById(SESSION_ID)).thenReturn(expectedSession);
        when(VelocityEngineUtils.mergeTemplateIntoString(
                any(VelocityEngine.class), Mockito.anyString(), Mockito.anyString(), Mockito.any(Map.class)))
                .thenReturn("Html Page").thenReturn("Text Page");

        emailService.sendPaymentReceiptEmail(RECIPIENTS, registrationResponse);

        verify(mailSender).send(mimeMessagePreparatorCaptor.capture());
        MimeMessagePreparator preparator = mimeMessagePreparatorCaptor.getValue();
        assertNotNull("No mime message preparator provided to send", preparator);

        // call the prepare() method so we can verify the logic
        preparator.prepare(mimeMessage);

        // the static will get called twice, once for each template, verify both
        PowerMockito.verifyStatic(Mockito.times(2));
        VelocityEngineUtils.mergeTemplateIntoString(any(VelocityEngine.class), templatePathCaptor.capture(), Mockito.anyString(), modelCaptor.capture());

        // verify the same model map is passed to both templates
        assertSame(modelCaptor.getAllValues().get(0), modelCaptor.getAllValues().get(1));

        Map<String, Object> capturedOrderModel = modelCaptor.getAllValues().get(0);
        ArrayList<Map<String, String>> orderPaymentModel = (ArrayList<Map<String, String>> ) capturedOrderModel.get("payments");
        ArrayList<Map<String, String>> orderRegistrationtModel = (ArrayList<Map<String, String>> ) capturedOrderModel.get("payments");

        assertEquals("Registrations count not equal 2", 2, orderRegistrationtModel.size());
        assertEquals("Payments count not equal 2", 2, orderPaymentModel.size());
    }

    @Test
    public void testPaymentReceiptHandlesInvalidEmailSilently() throws Exception {
        when(userDao.getUser(STUDENT_ID)).thenReturn(expectedStudent);
        when(sessionDao.getSessionById(SESSION_ID)).thenReturn(expectedSession);
        when(VelocityEngineUtils.mergeTemplateIntoString(
                any(VelocityEngine.class), Mockito.anyString(), Mockito.anyString(), Mockito.any(Map.class)))
                .thenReturn("Html Page").thenReturn("Text Page");
        String[] badRecepient =  {"areallybademailaddress"};

        doThrow(expectedException).when(mailSender).send(any(MimeMessagePreparator.class));
        emailService.sendPaymentReceiptEmail(badRecepient, registrationResponse);
    }

    @Test
    public void testNewUserEmail() throws Exception {
        Person person = new Person();
        person.setFirstName("Tom");
        person.setLastName("Tester");
        person.setEmailAddress("test@test.com");
        User student = new User(STUDENT_ID,"test@test.com","","", person, "", "", "", "", "", "");

        when(VelocityEngineUtils.mergeTemplateIntoString(
                any(VelocityEngine.class), Mockito.anyString(), Mockito.anyString(), Mockito.any(Map.class)))
                .thenReturn(HTML_EMAIL_TEXT).thenReturn(PLAIN_EMAIL_TEXT);

        emailService.sendNewUserEmail(student);
        verify(mailSender).send(mimeMessagePreparatorCaptor.capture());
        MimeMessagePreparator preparator = mimeMessagePreparatorCaptor.getValue();
        assertNotNull("No mime message preparator provided to send", preparator);

        // call the prepare() method so we can verify the logic
        preparator.prepare(mimeMessage);

        verify(mimeMessageHelper).setTo(recipientsCaptor.capture());
        verify(mimeMessageHelper).setSubject(subjectCaptor.capture());
        verify(mimeMessageHelper).setText(plainTextCaptor.capture(), htmlTextCaptor.capture());

        String[] recipients = new String[] {student.getPerson().getEmailAddress()};
        String[] recipientsFromCaptor = recipientsCaptor.getValue();
        for (int i=0; i < recipients.length; i++){
            assertTrue("Recipients are not the same", recipients[i].equals(recipientsFromCaptor[i]));
        }
        assertTrue("The email subject is wrong", (student.getPerson().getFirstName() + " " + student.getPerson().getLastName() + " - Welcome to the Graduate School!").equals(subjectCaptor.getValue()));
        assertTrue("The html text email is incorrect", HTML_EMAIL_TEXT.equals(htmlTextCaptor.getValue()));
        assertTrue("The plain text email is incorrect", PLAIN_EMAIL_TEXT.equals(plainTextCaptor.getValue()));

        // the static will get called twice, once for each template, verify both
        PowerMockito.verifyStatic(Mockito.times(2));
        VelocityEngineUtils.mergeTemplateIntoString(any(VelocityEngine.class), templatePathCaptor.capture(), Mockito.anyString(), modelCaptor.capture());

        assertEquals("Wrong html template", EmailServiceImpl.NEW_USER_HTML_TEMPLATE_VM, templatePathCaptor.getAllValues().get(0));
        assertEquals("Wrong text template", EmailServiceImpl.NEW_USER_TEXT_TEMPLATE_VM, templatePathCaptor.getAllValues().get(1));

        // verify the same model map is passed to both templates
        assertSame(modelCaptor.getAllValues().get(0), modelCaptor.getAllValues().get(1));
    }

    @Test
    public void testNewUserEmailHandlesInvalidEmailSilently() throws Exception {
       // thrown.expect(MailException.class);

        Person person = new Person();
        person.setFirstName("Tom");
        person.setLastName("Tester");
        person.setEmailAddress("bademail");
        User student = new User(STUDENT_ID,"bademail","","", person, "", "", "", "", "", "");

        when(VelocityEngineUtils.mergeTemplateIntoString(
                any(VelocityEngine.class), Mockito.anyString(), Mockito.anyString(), Mockito.any(Map.class)))
                .thenReturn(HTML_EMAIL_TEXT).thenReturn(PLAIN_EMAIL_TEXT);

        doThrow(expectedException).when(mailSender).send(any(MimeMessagePreparator.class));
        emailService.sendNewUserEmail(student);

        verify(mailSender).send(mimeMessagePreparatorCaptor.capture());
    }

    @Test
    public void testGetMimeMessageHelper() throws Exception {
        ReflectionTestUtils.setField(emailService, "mimeMessageHelper", null);

        MimeMessageHelper helper1 = emailService.getMimeMessageHelper(mimeMessage);
        MimeMessageHelper helper2 = emailService.getMimeMessageHelper(mimeMessage);
        assertNotSame(helper1, helper2);

    }

    @Test
    public void testPasswordResetEmail() throws Exception {

        String newPassword = "new_password_text";

        Person person = new Person();
        person.setFirstName("Tom");
        person.setLastName("Tester");
        person.setEmailAddress("test@test.com");
        User student = new User(STUDENT_ID,"test@test.com","","", person, "", "", "", "", "", "");

        when(VelocityEngineUtils.mergeTemplateIntoString(
                any(VelocityEngine.class), Mockito.anyString(), Mockito.anyString(), Mockito.any(Map.class)))
                .thenReturn(HTML_EMAIL_TEXT).thenReturn(PLAIN_EMAIL_TEXT);

        emailService.sendPasswordResetEmail(student, newPassword);

        verify(mailSender).send(mimeMessagePreparatorCaptor.capture());
        MimeMessagePreparator preparator = mimeMessagePreparatorCaptor.getValue();
        assertNotNull("No mime message preparator provided to send", preparator);

        // call the prepare() method so we can verify the logic
        preparator.prepare(mimeMessage);

        verify(mimeMessageHelper).setTo(recipientsCaptor.capture());
        verify(mimeMessageHelper).setSubject(subjectCaptor.capture());
        verify(mimeMessageHelper).setText(plainTextCaptor.capture(), htmlTextCaptor.capture());

        String[] recipients = new String[] {student.getPerson().getEmailAddress()};
        String[] recipientsFromCaptor = recipientsCaptor.getValue();
        for (int i=0; i < recipients.length; i++){
            assertTrue("Recipients are not the same", recipients[i].equals(recipientsFromCaptor[i]));
        }
        assertEquals("The email subject is wrong", SUBJECT, subjectCaptor.getValue());
        assertEquals("The html text email is incorrect", HTML_EMAIL_TEXT, htmlTextCaptor.getValue());
        assertEquals("The plain text email is incorrect", PLAIN_EMAIL_TEXT, plainTextCaptor.getValue());

        // the static will get called twice, once for each template, verify both
        PowerMockito.verifyStatic(Mockito.times(2));
        VelocityEngineUtils.mergeTemplateIntoString(any(VelocityEngine.class), templatePathCaptor.capture(), Mockito.anyString(), modelCaptor.capture());

        assertEquals("Wrong html template", EmailServiceImpl.PASSWORD_RESET_HTML_TEMPLATE_VM, templatePathCaptor.getAllValues().get(0));
        assertEquals("Wrong text template", EmailServiceImpl.PASSWORD_RESET_TEXT_TEMPLATE_VM, templatePathCaptor.getAllValues().get(1));

        // verify the same model map is passed to both templates
        assertSame(modelCaptor.getAllValues().get(0), modelCaptor.getAllValues().get(1));

        Map<String, Object> model = modelCaptor.getAllValues().get(0);
        assertEquals(student.getUsername(), model.get(EmailServiceImpl.USER_NAME));
        assertEquals(newPassword, model.get(EmailServiceImpl.NEW_PASSWORD));
        assertEquals(LOGIN_URL, model.get(EmailServiceImpl.USER_LOGIN_PAGE));
    }

    @Test
    public void testPasswordResetEmail_sendFailsSilently() throws Exception {

        Person person = new Person();
        person.setFirstName("Tom");
        person.setLastName("Tester");
        person.setEmailAddress("test@test.com");
        User student = new User(STUDENT_ID,"test@test.com","","", person, "", "", "", "", "", "");

        when(VelocityEngineUtils.mergeTemplateIntoString(
                any(VelocityEngine.class), Mockito.anyString(), Mockito.anyString(), Mockito.any(Map.class)))
                .thenReturn(HTML_EMAIL_TEXT).thenReturn(PLAIN_EMAIL_TEXT);

        doThrow(expectedException).when(mailSender).send(any(MimeMessagePreparator.class));

        emailService.sendPasswordResetEmail(student, "new_password_text");

        verify(mailSender).send(mimeMessagePreparatorCaptor.capture());
    }

}
