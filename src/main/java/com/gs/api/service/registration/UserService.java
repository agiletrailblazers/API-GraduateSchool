package com.gs.api.service.registration;

import com.gs.api.domain.registration.User;

public interface UserService {

    /**
     * Create a new user in the system.  The user object passed in will
     * be updated with the unique id of the created user and the password
     * will be updated to its encrypted value.
     * @param user the user information.
     * @throws Exception error creating user.
     */
    void createUser(final User user) throws Exception;

}
