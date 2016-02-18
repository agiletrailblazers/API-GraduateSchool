package com.gs.api.service.authentication;

import com.gs.api.domain.authentication.Role;

import javax.servlet.http.HttpServletRequest;

public interface AuthTokenService {

    /**
     * Generate a guest token.
     * @return a token that is valid for a guest user
     * @throws Exception error creating the token
     */
    String generateGuestToken() throws Exception;

    /**
     * Generate a user token.
     * @param userId the user id.
     * @param role the user's role.
     * @return a token that is valid for an identified user
     * @throws Exception error creating the token
     */
    String generateToken(String userId, Role role) throws Exception;

    /**
     * Validate a token
     * @param request the http request
     * @return the user user id from the token
     * @throws Exception
     */
    String validateToken(HttpServletRequest request) throws Exception;

}
