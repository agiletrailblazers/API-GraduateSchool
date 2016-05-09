package com.gs.api.service.registration;


import com.gs.api.dao.CourseSessionDAO;
import com.gs.api.dao.registration.RegistrationDAO;
import com.gs.api.dao.UserDAO;
import com.gs.api.domain.course.CourseSession;
import com.gs.api.domain.payment.Payment;
import com.gs.api.domain.payment.PaymentConfirmation;
import com.gs.api.domain.registration.Registration;
import com.gs.api.domain.registration.RegistrationRequest;
import com.gs.api.domain.registration.RegistrationResponse;
import com.gs.api.domain.registration.RegistrationDetails;
import com.gs.api.domain.User;
import com.gs.api.exception.PaymentAcceptedException;
import com.gs.api.exception.PaymentException;
import com.gs.api.service.email.EmailService;
import com.gs.api.service.payment.PaymentService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    static final String REGISTRATION_FAILURE_AFTER_SUCCESSFUL_PAYMENT_MSG = "Registration failure after successful payment";
    final Logger logger = LoggerFactory.getLogger(RegistrationServiceImpl.class);

    @Autowired
    private RegistrationDAO registrationDao;

    @Autowired
    private UserDAO userDao;

    @Autowired
    private CourseSessionDAO sessionDao;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private EmailService emailService;

    @Override
    public RegistrationResponse register(String userId, RegistrationRequest registrationRequest) throws PaymentException {

        List<PaymentConfirmation> confirmedPayments = new ArrayList<>();
        List<Registration> completedRegistrations = new ArrayList<>();
        // make the payments
        for (Payment payment : registrationRequest.getPayments()) {

            logger.debug("Processing payment for user {}", userId);

            confirmedPayments.add(paymentService.processPayment(payment));

            logger.info("Successful payment: User {}, payment reference number {}", userId, payment.getMerchantReferenceId());
        }

        User user;

        try {
            user = userDao.getUser(userId);
            if (user == null) {
                logger.error("No user found for logged in user: {}", userId);
                throw new Exception("No user found for logged in user " + userId);
            }
            for (Registration registration : registrationRequest.getRegistrations()) {
                logger.debug("User {} is registering student {} for class no {}", new String[]{userId,
                        registration.getStudentId(), registration.getSessionId()});

                //Get student, session, and courseSession from specified IDs
                User studentUser = userDao.getUser(registration.getStudentId());
                if (studentUser == null) {
                    logger.error("No user found for student {}", registration.getStudentId());
                    throw new Exception("No user found for student " + registration.getSessionId());
                }

                CourseSession session = sessionDao.getSessionById(registration.getSessionId());
                if (session == null) {
                    logger.error("No course session found for session id {}", registration.getSessionId());
                    throw new Exception("No course session found for session id " + registration.getSessionId());
                }

                completedRegistrations.add(registrationDao.registerForCourse(user, studentUser, session));
                logger.info("Successful registration: User {} registered student {} for class number {}, order number {}",
                        new String[]{userId, registration.getStudentId(), registration.getSessionId(), registration.getOrderNumber()});
            }
        }
        catch (Exception e) {
            // payment was successful but registration failed
            throw new PaymentAcceptedException(REGISTRATION_FAILURE_AFTER_SUCCESSFUL_PAYMENT_MSG, e);
        }

        RegistrationResponse registrationResponse = new RegistrationResponse(completedRegistrations, confirmedPayments);

        // email user payment receipt upon successful completion
        try {
            String[] recipients = {user.getPerson().getEmailAddress()};
            emailService.sendPaymentReceiptEmail(recipients, registrationResponse);
        }
        catch (Exception e) {
            logger.debug("Sending email failed, but registration and payment completed", e);
        }

        logger.debug("Payment Receipt email for order " + completedRegistrations.get(0).getOrderNumber() + " asynchronously sending, returning to normal flow");
        return registrationResponse;
    }

    @Override
    public List<Registration> getRegistrationForSession(final String userId, final String sessionId) throws Exception{
        logger.debug("Getting Registration for student");
        return registrationDao.getRegistration(userId, sessionId);
    }

    @Override
    public List<RegistrationDetails> getRegistrationDetails(final String userId) throws Exception{
        logger.debug("Getting registration details for student {}", userId);
        return registrationDao.getRegistrationDetails(userId);
    }
}
