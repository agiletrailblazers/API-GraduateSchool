package com.gs.api.service.authentication;

import com.gs.api.domain.authentication.*;
import com.gs.api.domain.registration.User;
import com.gs.api.exception.AuthenticationException;
import com.gs.api.service.registration.UserService;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.pbe.PBEStringCleanablePasswordEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.UUID;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    static final String TOKEN_FIELD_DELIMITER = "|";
    static final int TOKEN_FIELD_UUID_INDEX = 0;
    static final int TOKEN_FIELD_TIMESTAMP_INDEX = 1;
    static final int TOKEN_FIELD_ROLE_INDEX = 2;
    static final int TOKEN_FIELD_USER_INDEX = 3;
    static final String MISSING_REQUIRED_AUTHENTICATION_TOKEN_MSG = "Missing required authentication token";
    static final String UNABLE_TO_DECRYPT_TOKEN_MSG = "Unable to decrypt token";
    static final String TOKEN_TIMESTAMP_IS_NOT_VALID_MSG = "Token timestamp is not valid";
    static final String TOKEN_UUID_IS_NOT_VALID_MSG = "Token uuid is not valid";
    static final String TOKEN_ROLE_IS_NOT_VALID_MSG = "Token role is not valid";
    static final String TOKEN_USER_IS_NOT_VALID_MSG = "Token user is not valid";
    static final String MISMATCHED_USER_MSG = "User is not the authenticated user";
    static final String TOKEN_EXPIRED_MSG = "Token expired";
    static final String INVALID_USER_MSG = "Invalid user";
    static final String ERROR_AUTHENTICATING_USER_MSG = "Error authenticating user";
    static final String TOKEN_IS_NOT_AUTHENTICATED_MSG = "Token is not authenticated";

    final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    @Autowired
    private PBEStringCleanablePasswordEncryptor encryptor;

    @Autowired
    private UserService userService;

    @Value("${auth.token.header}")
    private String authTokenHeader;

    @Value("${auth.user.attribute}")
    private String authUserAttribute;

    @Value("${auth.token.expire.minutes}")
    private int authTokenExpireMinutes;

    @Override
    public AuthToken generateToken() throws AuthenticationException {

        return generateToken(null, Role.GUEST);
    }

    /**
     * Generate a user token.
     * @param userId the user id.
     * @param role the user's role.
     * @return a token that is valid for an identified user
     * @throws AuthenticationException error creating the token
     */
    public AuthToken generateToken(String userId, Role role) throws AuthenticationException {

        // generate the token string: UUID|timestamp|Role|User ID
        final Date datetime = new Date();
        final String token = UUID.randomUUID().toString().toUpperCase() +
                TOKEN_FIELD_DELIMITER + datetime.getTime() +
                TOKEN_FIELD_DELIMITER + role.name() +
                TOKEN_FIELD_DELIMITER + (StringUtils.isNotBlank(userId) ? userId.trim() : "");

        logger.debug("Generated token {}", token);

        return new AuthToken(encryptor.encrypt(token));
    }

    /**
     * Create a new renewal token
     *
     * @return
     */
    public RenewalToken generateRenewalToken(){
        final Date datetime = new Date();
        final String token = UUID.randomUUID().toString().toUpperCase() +
                TOKEN_FIELD_DELIMITER + datetime.getTime();

        return new RenewalToken(encryptor.encrypt(token));
    }

    @Override
    public void validateGuestAccess(HttpServletRequest request) throws AuthenticationException {

        performBasicValidation(request);
    }

    @Override
    public void validateAuthenticatedAccess(HttpServletRequest request) throws AuthenticationException {

        String[] tokenFields = performBasicValidation(request);

        Role role = Role.valueOf(tokenFields[TOKEN_FIELD_ROLE_INDEX]);

        // verify that authenticated token has not expired
        if (role == Role.AUTHENTICATED) {

            // check if token is expired
            long currentTime = new Date().getTime();
            long tokenTime = Long.parseLong(tokenFields[TOKEN_FIELD_TIMESTAMP_INDEX]);
            // convert expire time from seconds to milliseconds
            long authTokenExpire = authTokenExpireMinutes * 60 * 1000;

            if ((tokenTime + authTokenExpire) < currentTime) {
                // token is expired
                throw new AuthenticationException(TOKEN_EXPIRED_MSG);
            }

            // set the user id in attribute for use in API code
            if (StringUtils.isNotBlank(tokenFields[TOKEN_FIELD_USER_INDEX])) {
                request.setAttribute(authUserAttribute, tokenFields[TOKEN_FIELD_USER_INDEX]);
            }
            else {
                // authenticated tokens must have a user id
                throw new AuthenticationException(TOKEN_USER_IS_NOT_VALID_MSG);
            }
        }
        else {
            throw new AuthenticationException(TOKEN_IS_NOT_AUTHENTICATED_MSG);
        }
    }

    @Override
    public AuthUser authenticateUser(AuthCredentials authCredentials) throws AuthenticationException {

        User user;
        try {
            user = userService.getUser(authCredentials);
        }
        catch (Exception e) {
            throw new AuthenticationException(ERROR_AUTHENTICATING_USER_MSG, e);
        }

        if (user == null) {
            throw new AuthenticationException(INVALID_USER_MSG);
        }

        // user is valid, create an authenticated token
        AuthToken authToken = generateToken(user.getId(), Role.AUTHENTICATED);

        //Create a new renewal token for re-authorization
        RenewalToken renewalToken = generateRenewalToken();

        return new AuthUser(authToken, renewalToken, user);
    }

    @Override
    public void verifyUser(HttpServletRequest request, String userId) throws AuthenticationException {
        if (!StringUtils.equals(userId, (String) request.getAttribute(authUserAttribute))) {
            throw new AuthenticationException(MISMATCHED_USER_MSG);
        }
    }

    private String[] performBasicValidation(HttpServletRequest request) throws AuthenticationException {

        // get token and check if empty
        final String authToken = request.getHeader(authTokenHeader);
        if (StringUtils.isEmpty(authToken)) {
            throw new AuthenticationException(MISSING_REQUIRED_AUTHENTICATION_TOKEN_MSG);
        }

        // decrypt token
        final String token;
        try {
            logger.debug("Decrypting token {}", authToken);
            token = encryptor.decrypt(authToken);
            logger.debug("Token {}", token);
        }
        catch (Exception e) {
            throw new AuthenticationException(UNABLE_TO_DECRYPT_TOKEN_MSG, e);
        }

        String[] tokenFields = StringUtils.splitPreserveAllTokens(token, TOKEN_FIELD_DELIMITER);

        // verify that the timestamp is still a valid timestamp
        try {
            new Date().setTime(Long.parseLong(tokenFields[TOKEN_FIELD_TIMESTAMP_INDEX]));
        }
        catch (Exception e) {
            throw new AuthenticationException(TOKEN_TIMESTAMP_IS_NOT_VALID_MSG, e);
        }

        // verify that UUID is still a valid UUID
        try {
            UUID.fromString(tokenFields[TOKEN_FIELD_UUID_INDEX]);
        }
        catch (Exception e) {
            throw new AuthenticationException(TOKEN_UUID_IS_NOT_VALID_MSG, e);
        }

        // verify that the role is still a valid role
        try {
            // if it is a valid Role then it passes basic validation
            Role.valueOf(tokenFields[TOKEN_FIELD_ROLE_INDEX]);
        }
        catch (IllegalArgumentException e) {
            throw new AuthenticationException(TOKEN_ROLE_IS_NOT_VALID_MSG, e);
        }

        return tokenFields;
    }

    @Override
    public AuthUser reAuthenticateUser(ReAuthCredentials reAuthCredentials) throws AuthenticationException {

        //TODO Validate the authtoken is real
        if (true){

        } else {
            //TODO Return a 401
            throw new AuthenticationException("error");
        }

        return authenticateUser(reAuthCredentials);
    };
}
