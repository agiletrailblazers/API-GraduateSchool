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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertSame;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
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

    @Captor
    private ArgumentCaptor<MimeMessagePreparator> mimeMessagePreparatorCaptor;

    @Captor
    private ArgumentCaptor<String> templatePathCaptor;

    @Captor
    private ArgumentCaptor<Map<String, Object>> orderModelCaptor;

    @Captor
    private ArgumentCaptor<String[]> recipientsCaptor;

    @Captor
    private ArgumentCaptor<String> subjectCaptor;

    @Captor
    private ArgumentCaptor<String> htmlTextCaptor;

    @Captor
    private ArgumentCaptor<String> plainTextCaptor;

    private RegistrationResponse registrationResponse;
    private final static String[] RECIPIENTS = {"atestemaill@test.com", "betestemail@test.com"};
    private static final String SESSION_ID = "12345";
    private static final String STUDENT_ID = "person44444";
    private static final String REGISTRATION_ID = "12345";
    private static final String ORDER_NUMBER = "23456";
    private static final double SESSION_TUITION = 400.10;
    private static final String AUTHORIZATION_ID = "auth1234";
    private static final String NEW_USER_HTML_EMAIL_TEXT="<html>\n<head>\n    <title>Graduate School USA</title>\n</head>\n<body>\n    <h1>Graduate School USA</h1>\n<hr />\n    <p>Thank you for setting up an account with the Graduate School. We hope that it makes it easy to manage your learning with us.</p>\n\n    <p>You can login to access your account at anytime at <a href=\"${email.user.accountPage}\">${email.user.accountPage}</a></p>\n\n    <p>You can update your account information by using the 'My Account' feature. The security of your account information is important to us - please review our privacy policy <a href=\"${email.user.privacyPolicyPage}\">here</a>.</p>\n\n    <p>We look forward to serving your learning needs. Please be sure to save this email for future reference.</p>\n\n    <p>\n        Thank you! <br />\n        Graduate School USA\n    </p>\n</body>";
    private static final String NEW_USER_PLAIN_EMAIL_TEXT="Graduate School USA\n------------------------------------------------------\nThank you for setting up an account with the Graduate School. We hope that it makes it easy to manage your learning with us.\n \nYou can login to access your account at anytime at ${email.user.accountPage}\n\nYou can update your account information by using the 'My Account' feature. The security of your account\ninformation is important to us - please review our privacy policy at ${email.user.privacyPolicyPage}.\n\nWe look forward to serving your learning needs. Please be sure to save this email for future reference.\n\nThank you!\nGraduate School USA\n";

    @Before
    public void setUp() throws Exception {

        emailService = new EmailServiceImpl();
        mockStatic(VelocityEngineUtils.class);

        ReflectionTestUtils.setField(emailService, "mailSender", mailSender);
        ReflectionTestUtils.setField(emailService, "velocityEngine", velocityEngine);
        ReflectionTestUtils.setField(emailService, "userDao", userDao);
        ReflectionTestUtils.setField(emailService, "sessionDao", sessionDao);
        ReflectionTestUtils.setField(emailService, "mimeMessageHelper", mimeMessageHelper);

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
        when(VelocityEngineUtils.mergeTemplateIntoString(
                any(VelocityEngine.class), Mockito.anyString(), Mockito.anyString(), Mockito.any(Map.class)))
                .thenReturn("Html Page").thenReturn("Text Page");

        emailService.sendPaymentReceiptEmail(RECIPIENTS, registrationResponse);

        verify(mailSender).send(mimeMessagePreparatorCaptor.capture());
        MimeMessagePreparator preparator = mimeMessagePreparatorCaptor.getValue();
        assertNotNull("No mime message preparator provided to send", preparator);

        // TODO what else can be verified on the mime message, i.e. subject? recipients?

        // call the prepare() method so we can verify the logic
        preparator.prepare(mimeMessage);

        // the static will get called twice, once for each template, verify both
        PowerMockito.verifyStatic(Mockito.times(2));
        VelocityEngineUtils.mergeTemplateIntoString(any(VelocityEngine.class), templatePathCaptor.capture(), Mockito.anyString(), orderModelCaptor.capture());

        assertEquals("Wrong html template", EmailServiceImpl.PAYMENT_RECEIPT_HTML_TEMPLATE_VM, templatePathCaptor.getAllValues().get(0));
        assertEquals("Wrong text template", EmailServiceImpl.PAYMENT_RECEIPT_TEXT_TEMPLATE_VM, templatePathCaptor.getAllValues().get(1));

        // verify the same model map is passed to both templates
        assertSame(orderModelCaptor.getAllValues().get(0), orderModelCaptor.getAllValues().get(1));

        // TODO verify the data in the model map (only need to verify in one of them, since we already verified they are the same)
    }


    @Test
    public void testSuccessfulEmailMultiplePaymentsAndRegs() throws Exception {

    }

    @Test
    public void testInvalidEmailAddress() throws Exception {
    }

    @Test
    public void testNewUserEmail() throws Exception {
        Person person = new Person();
        person.setFirstName("Tom");
        person.setLastName("Tester");
        person.setEmailAddress("test@test.com");
        User student = new User(STUDENT_ID,"test@test.com","","", person, "", "", "", "", "");

        when(VelocityEngineUtils.mergeTemplateIntoString(
                any(VelocityEngine.class), Mockito.anyString(), Mockito.anyString(), Mockito.any(Map.class)))
                .thenReturn(NEW_USER_HTML_EMAIL_TEXT).thenReturn(NEW_USER_PLAIN_EMAIL_TEXT);

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
        assertTrue("The html text email is incorrect", NEW_USER_HTML_EMAIL_TEXT.equals(htmlTextCaptor.getValue()));
        assertTrue("The plain text email is incorrect", NEW_USER_PLAIN_EMAIL_TEXT.equals(plainTextCaptor.getValue()));

        // the static will get called twice, once for each template, verify both
        PowerMockito.verifyStatic(Mockito.times(2));
        VelocityEngineUtils.mergeTemplateIntoString(any(VelocityEngine.class), templatePathCaptor.capture(), Mockito.anyString(), orderModelCaptor.capture());

        assertEquals("Wrong html template", EmailServiceImpl.NEW_USER_HTML_TEMPLATE_VM, templatePathCaptor.getAllValues().get(0));
        assertEquals("Wrong text template", EmailServiceImpl.NEW_USER_TEXT_TEMPLATE_VM, templatePathCaptor.getAllValues().get(1));

        // verify the same model map is passed to both templates
        assertSame(orderModelCaptor.getAllValues().get(0), orderModelCaptor.getAllValues().get(1));
    }
}
