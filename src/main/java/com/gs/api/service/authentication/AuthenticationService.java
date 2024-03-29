package com.gs.api.service.authentication;

import com.gs.api.domain.authentication.*;
import com.gs.api.exception.AuthenticationException;

import javax.servlet.http.HttpServletRequest;

public interface AuthenticationService {

    /**
     * Generate an unauthenticated guest token.
     * @return an unauthenticated guest token.
     * @throws AuthenticationException error creating the token
     */
    AuthToken generateToken() throws AuthenticationException;

    /**
     * Generate the renewal token used to reauthorize a user
     * @return
     */
    RenewalToken generateRenewalToken();

    /**
     * Validate guest access.
     * @param request the http request
     * @throws AuthenticationException error validating the token or the token is not valid.
     */
    void validateGuestAccess(HttpServletRequest request) throws AuthenticationException;

    /**
     * Validate authenticated access.  If the token is valid, the user id will be set to the configured request attribute.
     * @param request the http request
     * @param timeCheck validate time expiration (reauthorization should not validate the timestamp)
     * @throws AuthenticationException error validating the token or the token is not valid.
     */
    void validateAuthenticatedAccess(HttpServletRequest request, boolean timeCheck) throws AuthenticationException;

    /**
     * Authenticate a user.
     * @param authCredentials the user credentials.
     * @return authenticated user information (authenticated token and user info)
     * @throws AuthenticationException if the user credentials are invalid.
     */
    AuthUser authenticateUser(AuthCredentials authCredentials) throws AuthenticationException;

    /**
     * Verify that the specified user id matches the user id that was authenticated.
     * @param request the http request
     * @param userId the id of the user.
     * @throws AuthenticationException if the supplied user id does not match the user id that was authenticated.
     */
    void verifyUser(HttpServletRequest request, String userId) throws AuthenticationException;

    /**
     * ReAuthenticate a user with renewal token
     * @param reAuthCredentials
     * @return
     * @throws AuthenticationException
     */
    AuthToken reAuthenticateUser(ReAuthCredentials reAuthCredentials) throws AuthenticationException;

}
