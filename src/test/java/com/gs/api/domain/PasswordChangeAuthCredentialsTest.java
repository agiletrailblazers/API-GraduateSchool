package com.gs.api.domain;

import com.gs.api.helper.ValidationHelper;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PasswordChangeAuthCredentialsTest {
    public static final String NEW_PASSWORD_TEXT = "newPassword";
    private Validator validator = ValidationHelper.getValidator();

    private static final String USERNAME = "testUser";
    private static final String OLD_PASSWORD = "oldPassword";

    @Test
    public void testValidCredentials(){
        PasswordChangeAuthCredentials validPWCredentials = new PasswordChangeAuthCredentials(USERNAME, OLD_PASSWORD, "newPassord");

        Set<ConstraintViolation<PasswordChangeAuthCredentials>> violations = this.validator.validate(validPWCredentials);

        assertTrue(violations.isEmpty());
    }

    @Test
    public void testNullNewPassword(){
        PasswordChangeAuthCredentials invalidPasswordChangeAuthCredentials = new PasswordChangeAuthCredentials(USERNAME, OLD_PASSWORD, null);

        HashMap<String, List<ConstraintViolation<Object>>> violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(invalidPasswordChangeAuthCredentials));

        //Test for password error
        assertNotNull(violations.get(NEW_PASSWORD_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(NEW_PASSWORD_TEXT)).contains(ValidationHelper.REQUIRED_FIELD));
    }

    @Test
    public void testPasswordEmptyString(){
        PasswordChangeAuthCredentials invalidPasswordChangeAuthCredentials = new PasswordChangeAuthCredentials(USERNAME, OLD_PASSWORD, "");

        HashMap<String, List<ConstraintViolation<Object>>> violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(invalidPasswordChangeAuthCredentials));

        //Test for password error
        assertNotNull(violations.get(NEW_PASSWORD_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(NEW_PASSWORD_TEXT)).contains("Length must be between 5 and 1020 characters"));
    }

    @Test
    public void testPasswordTooLongStrings(){
        PasswordChangeAuthCredentials invalidPasswordChangeAuthCredentials = new PasswordChangeAuthCredentials(USERNAME, OLD_PASSWORD, RandomStringUtils.randomAlphanumeric(1021));

        HashMap<String, List<ConstraintViolation<Object>>> violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(invalidPasswordChangeAuthCredentials));

        //Test for password error
        assertNotNull(violations.get(NEW_PASSWORD_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(NEW_PASSWORD_TEXT)).contains("Length must be between 5 and 1020 characters"));
    }
}
