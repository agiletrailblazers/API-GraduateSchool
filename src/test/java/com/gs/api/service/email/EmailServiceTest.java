package com.gs.api.service.email;

import com.gs.api.dao.CourseSessionDAO;
import com.gs.api.dao.registration.UserDAO;
import com.gs.api.domain.payment.Payment;
import com.gs.api.domain.payment.PaymentConfirmation;
import com.gs.api.domain.registration.Registration;
import com.gs.api.domain.registration.RegistrationResponse;
import org.apache.velocity.app.VelocityEngine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class EmailServiceTest {
    @InjectMocks
    @Autowired
    private JavaMailSender mailSender;

    @InjectMocks
    @Autowired
    private VelocityEngine velocityEngine;

    @Mock
    private UserDAO userDao;

    @Mock
    private CourseSessionDAO sessionDao;

    RegistrationResponse registrationResponse;
    private final static String[] RECIPIENTS = {"atestemaill@test.com", "betestemail@test.com"};
    private static final String USER_ID = "person654321";
    private static final String SESSION_ID = "session12345";
    private static final String STUDENT_ID_1 = "person44444";
    private static final String REGISTRATION_ID = "12345";
    private static final String ORDER_NUMBER = "23456";
     

    @Before
    public void setUp() throws Exception {
        Registration reg = new Registration();
        reg.setId(REGISTRATION_ID);
        reg.setOrderNumber(ORDER_NUMBER);
        reg.setSessionId(SESSION_ID);
        reg.setStudentId(STUDENT_ID_1);

        PaymentConfirmation pay = new PaymentConfirmation(new Payment(100.12, "auth1234", "ref123"), "sale123");

        List<PaymentConfirmation> pays = new ArrayList<>();
        pays.add(pay);

        registrationResponse = new RegistrationResponse(regs, pays);
    }

    @Test
    public void testSuccessfulEmail() throws Exception {

    }


    @Test
    public void testSuccessfulEmailMultiplePaymentsAndRegs() throws Exception {

    }

    @Test
    public void testInvalidEmailAddress() throws Exception {
    }
}
