package com.gs.api.service.authentication;

import com.gs.api.domain.authentication.Role;
import com.gs.api.exception.AuthenticationException;

import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.pbe.PBEStringCleanablePasswordEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

@Service
public class AuthTokenServiceImpl implements AuthTokenService {

    static final String MISSING_REQUIRED_AUTHENTICATION_TOKEN_MSG = "Missing required authentication token";
    static final String TOKEN_FIELD_DELIMITER = "|";
    static final int TOKEN_FIELD_UUID_INDEX = 0;
    static final int TOKEN_FIELD_TIMESTAMP_INDEX = 1;
    static final int TOKEN_FIELD_ROLE_INDEX = 2;
    static final int TOKEN_FIELD_USER_INDEX = 3;
    static final String UNABLE_TO_DECRYPT_TOKEN_MSG = "Unable to decrypt token";
    static final String TOKEN_TIMESTAMP_IS_NOT_VALID_MSG = "Token timestamp is not valid";
    static final String TOKEN_UUID_IS_NOT_VALID_MSG = "Token uuid is not valid";
    static final String TOKEN_ROLE_IS_NOT_VALID_MSG = "Token role is not valid";

    final Logger logger = LoggerFactory.getLogger(AuthTokenServiceImpl.class);

    @Autowired
    private PBEStringCleanablePasswordEncryptor encryptor;

    @Value("${auth.token.header}")
    private String authTokenHeader;

    @Value("${auth.user.attribute}")
    private String authUserAttribute;

    @Value("${auth.role.attribute}")
    private String authRoleAttribute;

    @Override
    public String generateGuestToken() throws AuthenticationException {

        return generateToken(null, Role.GUEST);
    }

    /**
     * Generate a user token.
     * @param userId the user id.
     * @param role the user's role.
     * @return a token that is valid for an identified user
     * @throws AuthenticationException error creating the token
     */
    public String generateToken(String userId, Role role) throws AuthenticationException {

        // generate the token string: UUID|timestamp|Role|User ID
        final Date datetime = new Date();
        final String token = UUID.randomUUID().toString().toUpperCase() +
                TOKEN_FIELD_DELIMITER + datetime.getTime() +
                TOKEN_FIELD_DELIMITER + role.name() +
                TOKEN_FIELD_DELIMITER + (StringUtils.isNotBlank(userId) ? userId.trim() : "");

        logger.debug("Generated token {}", token);

        return encryptor.encrypt(token);
    }

    @Override
    public void validateToken(HttpServletRequest request) throws AuthenticationException {

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

        // parse the token
        final String[] tokenFields = StringUtils.splitPreserveAllTokens(token, TOKEN_FIELD_DELIMITER);

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
            Role role = Role.valueOf(tokenFields[TOKEN_FIELD_ROLE_INDEX]);

            // set role in attribute for API use
            request.setAttribute(authRoleAttribute, role.name());
        }
        catch (IllegalArgumentException e) {
            throw new AuthenticationException(TOKEN_ROLE_IS_NOT_VALID_MSG, e);
        }

        // set user id, if exists, in attribute for API use
        if (StringUtils.isNotBlank(tokenFields[TOKEN_FIELD_USER_INDEX])) {
            request.setAttribute(authUserAttribute, tokenFields[TOKEN_FIELD_USER_INDEX]);
        }
    }
}
