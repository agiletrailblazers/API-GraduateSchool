package com.gs.api.service.email;

import com.gs.api.dao.CourseSessionDAO;
import com.gs.api.dao.registration.UserDAO;
import com.gs.api.domain.course.CourseSession;
import com.gs.api.domain.registration.Registration;
import com.gs.api.domain.registration.RegistrationResponse;
import com.gs.api.domain.registration.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;


@Service
public class EmailServiceImpl implements EmailService {

    final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private VelocityEngine velocityEngine;


    @Autowired
    private UserDAO userDao;

    @Autowired
    private CourseSessionDAO sessionDao;

    @Override
    public void sendSimpleMail(String to, String subject, String body)
    {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    @Override
    public void sendPaymentReceiptEmail(String to, RegistrationResponse registrationResponse) throws Exception {
        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage) throws Exception {
                MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true);
                message.setTo(to);
                message.setSubject("Graduate School Payment Receipt");

                Map model = new HashMap();
                for(int i = 0; i <= registrationResponse.getRegistrations().size(); i++) {
                    Registration currentRegistration = registrationResponse.getRegistrations().get(i);

                    CourseSession courseSession =  sessionDao.getSessionById(currentRegistration.getSessionId());
                    User student = userDao.getUser(currentRegistration.getStudentId());
                }

                String htmlText = VelocityEngineUtils.mergeTemplateIntoString(
                        velocityEngine, "templates/paymentReceiptHtmlTemplate.vm","UTF-8", model);
                String plainText = VelocityEngineUtils.mergeTemplateIntoString(
                        velocityEngine, "templates/paymentReceiptTextTemplate.vm","UTF-8", model);
                message.setText(plainText, htmlText);
            }
        };
        logger.debug("Sending payment receipt to {}", to);
        try {
            mailSender.send(preparator);
        }
        catch (Exception e) {
            logger.error("Error sending payment receipt", e);
        }
    }


}
