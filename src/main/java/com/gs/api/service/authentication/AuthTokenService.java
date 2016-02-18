package com.gs.api.service.authentication;

import com.gs.api.exception.AuthenticationException;

import javax.servlet.http.HttpServletRequest;

public interface AuthTokenService {

    /**
     * Generate a guest token.
     * @return a token that is valid for a guest user
     * @throws AuthenticationException error creating the token
     */
    String generateGuestToken() throws AuthenticationException;

    /**
     * Validate a token.  If the token is valid, the user id will be set to the configured request attribute.
     * @param request the http request
     * @throws AuthenticationException error validating the token or the token is not valid.
     */
    void validateToken(HttpServletRequest request) throws AuthenticationException;

}
