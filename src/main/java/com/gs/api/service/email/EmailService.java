package com.gs.api.service.email;

import com.gs.api.domain.registration.RegistrationResponse;
import com.gs.api.domain.registration.User;

public interface EmailService {
     /**
     * Create order confirmation email from template
     *
     * @throws Exception
     */
    void sendPaymentReceiptEmail(String[] recipients, RegistrationResponse registrationResponse) throws Exception;

    /**
     * Send the new user email.
     * @param newUser the new user.
     * @throws Exception error sending email.
     */
    void sendNewUserEmail(User newUser) throws Exception;

    /**
     * Send the password reset email.
     * @param user the user.
     * @param newPassword the new password.
     * @throws Exception error sending email.
     */
    void sendPasswordResetEmail(User user, String newPassword) throws Exception;

}
