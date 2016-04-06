package com.gs.api.service.email;

import com.gs.api.domain.registration.RegistrationResponse;
import com.gs.api.domain.registration.User;
import org.springframework.mail.MailException;

public interface EmailService {

    void sendSimpleMail(String to, String subject, String body) throws MailException;

    /**
     * Create order confirmation email from template
     *
     * @throws Exception
     */
    void sendPaymentReceiptEmail(String[] recipients, RegistrationResponse registrationResponse) throws Exception;

    void sendNewUserEmail(User newUser) throws Exception;

}
