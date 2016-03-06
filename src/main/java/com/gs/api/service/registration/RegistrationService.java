package com.gs.api.service.registration;

import com.gs.api.domain.registration.RegistrationRequest;
import com.gs.api.domain.registration.RegistrationResponse;
import com.gs.api.exception.PaymentException;

public interface RegistrationService {

    /**
     * Create a new registration in the system.  The Registration object passed in will
     * be updated with the unique id of the created registration upon successful registration.
     * @param userId the ID of the user that is performing the registration.  This may or may not be the ID of the student being registered.
     * @param registrationRequest list of registrations to be created.
     * @return list of registrations updated with the registration id's and order numbers.
     * @throws PaymentException error creating registrations and/or processing payments.
     */
    RegistrationResponse register(final String userId, final RegistrationRequest registrationRequest) throws PaymentException;

}
