package com.gs.api.service.email;

import com.gs.api.dao.CourseSessionDAO;
import com.gs.api.dao.registration.UserDAO;
import com.gs.api.domain.course.CourseSession;
import com.gs.api.domain.payment.Payment;
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
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.ArrayList;
import java.util.Locale;
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
    public void sendPaymentReceiptEmail(String[] recipients, RegistrationResponse registrationResponse) throws Exception {
        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage) throws Exception {
                // Setup email
                MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true);
                message.setTo(recipients);
                message.setSubject("Graduate School Payment Receipt");

                // create the data model for passing the info into the template
                Map<String, Object> orderModel= getOrderData(registrationResponse);

                String htmlText = mergeTemplate(velocityEngine, "templates/paymentReceiptHtmlTemplate.vm","UTF-8", orderModel);
                String plainText = mergeTemplate(velocityEngine, "templates/paymentReceiptTextTemplate.vm","UTF-8", orderModel);
                message.setText(plainText, htmlText);
            }
        };
        try {
            mailSender.send(preparator);
            logger.debug("Payment receipt successfully sent");
        }
        catch (Exception e) {
            logger.error("Error sending payment receipt", e);
        }
    }

    public Map<String, Object> getOrderData(RegistrationResponse registrationResponse){
        Map<String, Object> orderModel= new HashMap<>();
        SimpleDateFormat formatter = new SimpleDateFormat("MMM, dd, YYYY");
        orderModel.put("transactionDate", formatter.format(new Date()));
        orderModel.put("orderId", "00123");

        // Format each payment for the email
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
        double totalCharged = 0.0;
        ArrayList<Map<String, String>> paymentList = new ArrayList<>();
        for (int p = 0; p < registrationResponse.getPaymentConfirmations().size(); p++) {
            Payment currentPayment = registrationResponse.getPaymentConfirmations().get(p).getPayment();
            totalCharged += currentPayment.getAmount();
            Map<String, String> paymentModel = new HashMap<>();
            paymentModel.put("cardType", "Viso");
            paymentModel.put("cardHolderName", "Ashley");
            paymentModel.put("cardEmail", "test@atb.com");
            paymentModel.put("cardNumber", "0000");
            paymentModel.put("authCode", currentPayment.getAuthorizationId());
            paymentList.add(paymentModel);
        }
        orderModel.put("totalCharged", currencyFormatter.format(totalCharged));
        orderModel.put("payments", paymentList);

        // Format Registration, Class, and Student info for the form
        ArrayList<Map<String, String>> registrationList = new ArrayList<>();
        for (int i = 0; i < registrationResponse.getRegistrations().size(); i++) {
            Map<String, String> registrationModel = new HashMap<>();
            Registration currentRegistration = registrationResponse.getRegistrations().get(i);


            CourseSession courseSession =  sessionDao.getSessionById(currentRegistration.getSessionId());
            User student = userDao.getUser(currentRegistration.getStudentId());

            registrationModel.put("studentName", student.getPerson().getFirstName() + " " + student.getPerson().getLastName());
            logger.debug(student.getPerson().getFirstName() + " " + student.getPerson().getLastName());
            registrationModel.put("tuition", currencyFormatter.format(courseSession.getTuition()));
            registrationModel.put("status", "Confirmed"); // If registration got to this point, its status is confirmed in the DB
            registrationModel.put("title", courseSession.getCourseTitle());
            registrationModel.put("code", courseSession.getCourseCode());
            registrationModel.put("classId", courseSession.getClassNumber());

            if (courseSession.getLocation() != null && courseSession.getLocation().getCity() != null) {
                String location = courseSession.getLocation().getCity();
                if (courseSession.getLocation().getState() != null) {
                    location += ", " + courseSession.getLocation().getState();
                }
                registrationModel.put("location", location);
            }


            if (courseSession.getStartDate() != null ) {
                String dates = formatter.format(courseSession.getStartDate());
                if (courseSession.getEndDate() != null) {
                    dates = dates.substring(0, dates.length() - 6); //Remove year from start date
                    dates += " - " + formatter.format(courseSession.getEndDate());
                }
                registrationModel.put("dates", dates);
            }
            if (courseSession.getStartDate() != null || courseSession.getEndTime() != null) {
                String times = courseSession.getStartTime() + " - " + courseSession.getEndTime();
                registrationModel.put("times", times);
            }

            registrationModel.put("days", courseSession.getDays());
            registrationModel.put("email", student.getPerson().getEmailAddress());
            registrationList.add(registrationModel);
        }

        orderModel.put("registrations", registrationList);

        return orderModel;
    }

    public String mergeTemplate(VelocityEngine velocityEngine, String templatePath, String encoding, Map<String, Object> model) {
        return VelocityEngineUtils.mergeTemplateIntoString(
                velocityEngine, templatePath, encoding, model);
    }
}
