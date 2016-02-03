package com.gs.api.service.registration;

import com.gs.api.domain.registration.Registration;

import java.util.List;

public interface RegistrationService {

    /**
     * Create a new registration in the system.  The Registration object passed in will
     * be updated with the unique id of the created registration upon successful registration.
     * @param userId the ID of the user that is performing the registration.  This may or may not be the ID of the student being registered.
     * @param registrations list of registrations to be created.
     * @throws Exception error creating registrations.
     */
    void register(final String userId, final List<Registration> registrations) throws Exception;

}
