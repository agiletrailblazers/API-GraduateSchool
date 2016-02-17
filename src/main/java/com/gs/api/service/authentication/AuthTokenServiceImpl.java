package com.gs.api.service.authentication;

import com.gs.api.domain.authentication.Role;

import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.pbe.PBEStringCleanablePasswordEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

@Service
public class AuthTokenServiceImpl implements AuthTokenService {

    final Logger logger = LoggerFactory.getLogger(AuthTokenServiceImpl.class);

    @Autowired
    private PBEStringCleanablePasswordEncryptor encryptor;

    @Override
    public String generateGuestToken() throws Exception {

        return generateToken(null, Role.GUEST);
    }

    @Override
    public String generateToken(String userId, Role role) throws Exception {

        // generate the token string: UUID|timestamp|Role|User ID
        final Date datetime = new Date();
        final String key = UUID.randomUUID().toString().toUpperCase() +
                "|" + datetime.getTime() +
                "|" + role.name() +
                "|" + (StringUtils.isNotEmpty(userId) ? userId : "");

        logger.debug("Generated token: {}", key);

        return encryptor.encrypt(key);
    }

    @Override
    public String validateToken(HttpServletRequest request) throws Exception {

        return null;
    }
}
