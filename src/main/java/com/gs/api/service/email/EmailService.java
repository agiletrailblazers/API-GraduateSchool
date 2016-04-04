package com.gs.api.service.email;

import java.util.List;

import com.gs.api.domain.payment.Payment;
import com.gs.api.domain.registration.Registration;
import com.gs.api.domain.registration.RegistrationRequest;
import com.gs.api.domain.registration.RegistrationResponse;
import com.gs.api.domain.registration.User;
import com.gs.api.exception.PaymentException;
import org.apache.velocity.Template;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.MimeMessagePreparator;

public interface EmailService {

    void sendSimpleMail(String to, String subject, String body) throws MailException;

    /**
     * Create order confirmation email from template
     *
     * @throws Exception
     */
    void sendPaymentReceiptEmail(String to, RegistrationResponse registrationResponse) throws Exception;

}
