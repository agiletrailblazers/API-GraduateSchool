package com.gs.api.service.registration;


import com.gs.api.dao.CourseSessionDAO;
import com.gs.api.dao.registration.RegistrationDAO;
import com.gs.api.dao.registration.UserDAO;
import com.gs.api.domain.course.CourseSession;
import com.gs.api.domain.payment.Payment;
import com.gs.api.domain.payment.PaymentConfirmation;
import com.gs.api.domain.registration.Registration;
import com.gs.api.domain.registration.RegistrationRequest;
import com.gs.api.domain.registration.RegistrationResponse;
import com.gs.api.domain.registration.User;
import com.gs.api.exception.PaymentAcceptedException;
import com.gs.api.exception.PaymentException;
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

        try {
            for (Registration registration : registrationRequest.getRegistrations()) {
                logger.debug("User {} is registering student {} for class no {}", new String[]{userId,
                        registration.getStudentId(), registration.getSessionId()});

                //Get actual user, student, session, and courseSession from specified IDs
                User user = userDao.getUser(userId);
                if (user == null) {
                    logger.error("No user found for logged in user: {}", userId);
                    throw new Exception("No user found for logged in user " + userId);
                }

                User studentUser = userDao.getUser(registration.getStudentId());
                if (studentUser == null) {
                    logger.error("No user found for student {}", registration.getStudentId());
                    throw new Exception("No user found for student " + registration.getSessionId());
                }

                CourseSession session = sessionDao.getSession(registration.getSessionId());
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

        return registrationResponse;
    }

    @Override
    public Registration getRegistrationForSession(final String studentId, final String sessionId) throws Exception{

        Registration registration = new Registration();
        registration.setId("12345");
        registration.setOrderNumber("23456");
        registration.setSessionId(studentId);
        registration.setStudentId(sessionId);

        return registration;
    }
}
