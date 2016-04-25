package com.gs.api.service.email;

import com.gs.api.dao.CourseSessionDAO;
import com.gs.api.dao.registration.UserDAO;
import com.gs.api.domain.course.CourseSession;
import com.gs.api.domain.payment.Payment;
import com.gs.api.domain.registration.Registration;
import com.gs.api.domain.registration.RegistrationResponse;
import com.gs.api.domain.registration.User;

import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    public static final String UTF_8_ENCODING = "UTF-8";

    static final String PAYMENT_RECEIPT_HTML_TEMPLATE_VM = "templates/email/paymentReceiptTemplate_html.vm";
    static final String PAYMENT_RECEIPT_TEXT_TEMPLATE_VM = "templates/email/paymentReceiptTemplate_text.vm";
    static final String NEW_USER_HTML_TEMPLATE_VM = "templates/email/newUserTemplate_html.vm";
    static final String NEW_USER_TEXT_TEMPLATE_VM = "templates/email/newUserTemplate_text.vm";
    static final String PASSWORD_RESET_HTML_TEMPLATE_VM = "templates/email/passwordResetTemplate_html.vm";
    static final String PASSWORD_RESET_TEXT_TEMPLATE_VM = "templates/email/passwordResetTemplate_text.vm";
    static final String USER_LOGIN_PAGE = "userLoginPage";
    static final String USER_NAME = "username";
    static final String NEW_PASSWORD = "newPassword";

    final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private VelocityEngine velocityEngine;

    @Autowired
    private UserDAO userDao;

    @Autowired
    private CourseSessionDAO sessionDao;

    @Value("${email.user.accountPage}")
    private String userAccountPage;

    @Value("${email.user.privacyPolicyPage}")
    private String userPrivacyPolicyPage;

    @Value("${email.subject.paymentReceipt}")
    private String paymentReceiptEmailSubject;

    @Value("${email.subject.newUser}")
    private String newUserEmailSubject;

    @Value("${email.subject.passwordReset}")
    private String passwordResetSubject;

    @Value("${email.user.loginPage}")
    private String userLoginPage;


    // ONLY SET FROM UNIT TESTS!
    private MimeMessageHelper mimeMessageHelper;

    /**
     * Send email after registration complete
     * @param recipients people to send the email to
     * @param registrationResponse completed registrations and payments
     * @throws Exception
     */
    @Override
    @Async
    public void sendPaymentReceiptEmail(String[] recipients, RegistrationResponse registrationResponse) throws Exception {
        MimeMessagePreparator preparator = new MimeMessagePreparator() {

            public void prepare(MimeMessage mimeMessage) throws Exception {
                // Setup email
                MimeMessageHelper message = getMimeMessageHelper(mimeMessage);
                message.setTo(recipients);
                message.setSubject(paymentReceiptEmailSubject);

                // create the data model for passing the info into the template
                Map<String, Object> model= getOrderData(registrationResponse);

                String htmlText = mergeTemplate(velocityEngine, PAYMENT_RECEIPT_HTML_TEMPLATE_VM, "UTF-8", model);
                String plainText = mergeTemplate(velocityEngine, PAYMENT_RECEIPT_TEXT_TEMPLATE_VM, "UTF-8", model);
                message.setText(plainText, htmlText);
            }
        };
        try {
            mailSender.send(preparator);
            logger.debug("Payment receipt successfully sent for order {}", registrationResponse.getRegistrations().get(0).getOrderNumber());
        }
        catch (MailException e) {
            logger.error("Error sending payment receipt", e);
        }
    }

    /**
     * Send new user email to new user
     *
     * @param newUser is the newly created user
     * @throws Exception
     */
    @Override
    @Async
    public void sendNewUserEmail(User newUser) throws Exception {
        MimeMessagePreparator preparator = new MimeMessagePreparator() {

            public void prepare(MimeMessage mimeMessage) throws Exception {
                // Setup email
                MimeMessageHelper message = getMimeMessageHelper(mimeMessage);
                message.setTo(new String[] {newUser.getPerson().getEmailAddress()});
                message.setSubject(newUser.getPerson().getFirstName() + " " + newUser.getPerson().getLastName() + " " + newUserEmailSubject);

                // create the data model for passing the info into the template
                Map<String, Object> model= new HashMap<>();

                model.put("accountPage", userAccountPage);
                model.put("privacyPolicy", userPrivacyPolicyPage);

                String htmlText = mergeTemplate(velocityEngine, NEW_USER_HTML_TEMPLATE_VM, UTF_8_ENCODING, model);
                String plainText = mergeTemplate(velocityEngine, NEW_USER_TEXT_TEMPLATE_VM, UTF_8_ENCODING, model);
                message.setText(plainText, htmlText);
            }
        };
        try {
            mailSender.send(preparator);
            logger.debug("New User email successfully sent for user {}", newUser.getId());
        }
        catch (MailException e) {
            logger.error("Error sending new user email", e);
        }
    }

    @Override
    @Async
    public void sendPasswordResetEmail(final User user, final String newPassword) throws Exception {

        MimeMessagePreparator preparator = new MimeMessagePreparator() {

            public void prepare(MimeMessage mimeMessage) throws Exception {
                // Setup email
                MimeMessageHelper message = getMimeMessageHelper(mimeMessage);
                message.setTo(new String[] {user.getPerson().getEmailAddress()});
                message.setSubject(passwordResetSubject);

                // create the data model for passing the info into the template
                Map<String, Object> model= new HashMap<>();

                model.put(USER_LOGIN_PAGE, userLoginPage);
                model.put(NEW_PASSWORD, newPassword);
                model.put(USER_NAME, user.getUsername());

                String htmlText = mergeTemplate(velocityEngine, PASSWORD_RESET_HTML_TEMPLATE_VM, UTF_8_ENCODING, model);
                String plainText = mergeTemplate(velocityEngine, PASSWORD_RESET_TEXT_TEMPLATE_VM, UTF_8_ENCODING, model);
                message.setText(plainText, htmlText);
            }
        };
        try {
            mailSender.send(preparator);
            logger.debug("Password reset email successfully sent for user {}", user.getId());
        }
        catch (MailException e) {
            logger.error("Error sending password reset email", e);
        }
    }

    /**
     * Only used for unit testing to capture input to methods
     * @param mimeMessage
     * @return
     * @throws MessagingException
     */
    MimeMessageHelper getMimeMessageHelper(MimeMessage mimeMessage) throws MessagingException {
        if (mimeMessageHelper == null) {
            // DO NOT SET THE PRIVATE VARIABLE, it is only used for unit tests
            return new MimeMessageHelper(mimeMessage, true);
        }
        else {
            return mimeMessageHelper;
        }
    }

    /**
     * Safely create the string which contains the template
     * @return the string containing the rendered template
     */
    private String mergeTemplate(VelocityEngine velocityEngine, String templatePath, String encoding, Map<String, Object> model) {
        return VelocityEngineUtils.mergeTemplateIntoString(
                velocityEngine, templatePath, encoding, model);
    }

    private Map<String, Object> getOrderData(RegistrationResponse registrationResponse) throws Exception{
        Map<String, Object> orderModel= new HashMap<>();
        SimpleDateFormat formatter = new SimpleDateFormat("MMM. dd, YYYY");
        orderModel.put("transactionDate", formatter.format(new Date()));
        orderModel.put("orderId",registrationResponse.getRegistrations().get(0).getOrderNumber());

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
        List<Registration> registrations = registrationResponse.getRegistrations();
        ArrayList<Map<String, String>> registrationList = new ArrayList<>();
        for (int i = 0; i < registrationResponse.getRegistrations().size(); i++) {
            Map<String, String> registrationModel = new HashMap<>();
            Registration currentRegistration = registrations.get(i);


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

            if (courseSession.getStartTime() != null) {
                String times = courseSession.getStartTime();
                if (courseSession.getEndTime() != null) {
                    times += " - " + courseSession.getEndTime();
                }
                registrationModel.put("times", times);
            }

            registrationModel.put("days", courseSession.getDays());
            registrationModel.put("email", student.getPerson().getEmailAddress());
            registrationList.add(registrationModel);
        }

        orderModel.put("registrations", registrationList);

        return orderModel;
    }
}
