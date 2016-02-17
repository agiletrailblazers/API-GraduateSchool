package com.gs.api.service.authentication;

import org.jasypt.encryption.pbe.PBEStringCleanablePasswordEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthTokenConfig {

    @Bean
    public PBEStringCleanablePasswordEncryptor encryptor(@Value("auth.token.password") String password) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(password);
        return encryptor;
    }

}
