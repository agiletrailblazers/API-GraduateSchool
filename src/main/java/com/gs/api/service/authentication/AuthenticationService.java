package com.gs.api.service.authentication;

import com.gs.api.domain.authentication.AuthCredentials;
import com.gs.api.domain.authentication.AuthToken;
import com.gs.api.domain.authentication.AuthUser;
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
     * Validate guest access.
     * @param request the http request
     * @throws AuthenticationException error validating the token or the token is not valid.
     */
    void validateGuestAccess(HttpServletRequest request) throws AuthenticationException;

    /**
     * Validate authenticated access.  If the token is valid, the user id will be set to the configured request attribute.
     * @param request the http request
     * @throws AuthenticationException error validating the token or the token is not valid.
     */
    void validateAuthenticatedAccess(HttpServletRequest request) throws AuthenticationException;

    /**
     * Authenticate a user.
     * @param authCredentials the user credentials.
     * @return authenticated user information (authenticated token and user info)
     * @throws AuthenticationException if the user credentials are invalid.
     */
    AuthUser authenticateUser(AuthCredentials authCredentials) throws AuthenticationException;

}
