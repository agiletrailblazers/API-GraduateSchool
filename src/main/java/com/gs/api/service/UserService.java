package com.gs.api.service;

import com.gs.api.domain.PasswordChangeAuthCredentials;
import com.gs.api.domain.authentication.AuthCredentials;
import com.gs.api.domain.User;
import com.gs.api.domain.BaseUser;

public interface UserService {

    /**
     * Create a new user in the system.  The user object passed in will
     * be updated with the unique id of the created user and the password
     * will be updated to its encrypted value.
     * @param user the user information.
     * @throws Exception error creating user.
     */
    void createUser(final User user) throws Exception;

    /**
     * Delete the specified user.
     * @param userId the user id.
     * @throws Exception error deleting the user.
     */
    void deleteUser(final String userId) throws Exception;

    /**
     * Updates the user in the database
     * @param user the updated user info
     * @throws Exception error updating the user
     */
    void updateUser(final BaseUser user) throws Exception;

    /**
     * Get a user using the supplied credentials.
     * @param authCredentials user credentials.
     * @return the user or null if no user found matching the supplied credentials
     * @throws Exception Error getting the user.
     */
    User getUser(final AuthCredentials authCredentials) throws Exception;

    /**
     * Get a user by the user id
     * @param userId the user id.
     * @throws Exception error getting the user.
     */
    User getUser(final String userId) throws Exception;

    /**
     * Create and reset the password for the specified user.
     * @param authCredentials user credentials containing username.
     * @throws Exception Error resetting the password.
     */
    void forgotPassword(final AuthCredentials authCredentials) throws Exception;

    /**
     * Change the password for specified user/
     * @param passwordChangeAuthCredentials Contains username, old password, and new password.
     * @throws Exception
     */
    void changePassword(final PasswordChangeAuthCredentials passwordChangeAuthCredentials, final String authUserID) throws Exception;
}
