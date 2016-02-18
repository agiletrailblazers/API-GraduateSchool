package com.gs.api.controller.authentication;

import com.gs.api.service.authentication.AuthTokenConfig;

import org.jasypt.encryption.pbe.PBEStringCleanablePasswordEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)

public class AuthTokenConfigTest {

    private AuthTokenConfig authTokenConfig = new AuthTokenConfig();

    @Test
    public void testEncryptor() {

        PBEStringCleanablePasswordEncryptor encryptor = authTokenConfig.encryptor("password");
        assertNotNull(encryptor);
        assertTrue(encryptor instanceof StandardPBEStringEncryptor);
    }
}


