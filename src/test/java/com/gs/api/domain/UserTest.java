package com.gs.api.domain;

import com.gs.api.domain.registration.User;
import com.gs.api.helper.ValidationHelper;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class UserTest {
    public static final String PASSWORD = "password";
    public static final String LAST_FOUR_SSN = "1234";
    public static final String TIMEZONE_ID = "timezoneId";
    public static final String USERNAME_TEXT = "username";
    public static final String PASSWORD_TEXT = "password";
    public static final String LAST_FOUR_SSN_TEXT = "lastFourSSN";
    public static final String TIMEZONE_ID_TEXT = "timezoneId";
    public static final String PERSON_TEXT = "person";
    public static final String ID_TEXT = "id";
    public static final String ACCOUNT_ID_TEXT = "accountId";
    public static final String CURRENCY_ID_TEXT = "currencyId";
    public static final String SPLIT_TEXT = "split";
    public static final String TIMESTAMP_TEXT = "timestamp";
    private Validator validator = ValidationHelper.getValidator();

    private static Address validAddress;
    private static Person validPerson;

    @BeforeClass
    public static void setUpClass(){
        validAddress = new Address();
        validAddress.setAddress1("123 Main Street");
        validAddress.setAddress2("101");
        validAddress.setAddress3("Third Line");
        validAddress.setCity("Philadelphia");
        validAddress.setState("PA");
        validAddress.setPostalCode("12345");

        validPerson = new Person();
        validPerson.setFirstName("FirstName");
        validPerson.setMiddleName("MiddleName");
        validPerson.setLastName("LastName");
        validPerson.setEmailAddress("person@test.com");
        validPerson.setPrimaryPhone("1234567890");
        validPerson.setSecondaryPhone("0987654321");
        validPerson.setPrimaryAddress(validAddress);
        validPerson.setSecondaryAddress(validAddress);
        validPerson.setVeteran(false);
        validPerson.setDateOfBirth("20160322");
    }

    @Test
    public void testValidUser(){
        User validUser = new User(null, validPerson.getEmailAddress(), PASSWORD, LAST_FOUR_SSN, validPerson, TIMEZONE_ID, null, null, null, null);

        Set<ConstraintViolation<User>> violations = this.validator.validate(validUser);

        assertTrue(violations.isEmpty());
    }

    @Test
    public void testEmptyUser(){
        User invalidUser = new User();

        HashMap<String, List<ConstraintViolation<Object>>> violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(invalidUser));

        //Test for required fields
        assertNotNull(violations.get(USERNAME_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(USERNAME_TEXT)).contains(ValidationHelper.REQUIRED_FIELD));
        assertNotNull(violations.get(PASSWORD_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(PASSWORD_TEXT)).contains(ValidationHelper.REQUIRED_FIELD));
        assertNotNull(violations.get(LAST_FOUR_SSN_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(LAST_FOUR_SSN_TEXT)).contains(ValidationHelper.REQUIRED_FIELD));
        assertNotNull(violations.get(TIMEZONE_ID_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(TIMEZONE_ID_TEXT)).contains(ValidationHelper.REQUIRED_FIELD));
        assertNotNull(violations.get(PERSON_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(PERSON_TEXT)).contains(ValidationHelper.REQUIRED_FIELD));

        //Test for not-required fields
        assertNull(violations.get(ID_TEXT));
        assertNull(violations.get(ACCOUNT_ID_TEXT));
        assertNull(violations.get(CURRENCY_ID_TEXT));
        assertNull(violations.get(SPLIT_TEXT));
        assertNull(violations.get(TIMESTAMP_TEXT));
    }

    @Test
    public void testInvalidInfoEmptyStrings(){
        User invalidUser = new User("", "", "", "", validPerson, "", "", "", "", "");

        HashMap<String, List<ConstraintViolation<Object>>> violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(invalidUser));

        //Test for incorrect fields
        assertNotNull(violations.get(USERNAME_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(USERNAME_TEXT)).contains("Length must be between 1 and 1020 characters"));
        assertNotNull(violations.get(PASSWORD_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(PASSWORD_TEXT)).contains("Length must be between 5 and 1020 characters"));
        assertNotNull(violations.get(LAST_FOUR_SSN_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(LAST_FOUR_SSN_TEXT)).contains("Length must be 4 characters"));
        assertNotNull(violations.get(TIMEZONE_ID_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(TIMEZONE_ID_TEXT)).contains("Length must be between 1 and 20 characters"));


        //Test for correct fields
        assertNull(violations.get(ID_TEXT));
        assertNull(violations.get(ACCOUNT_ID_TEXT));
        assertNull(violations.get(CURRENCY_ID_TEXT));
        assertNull(violations.get(SPLIT_TEXT));
        assertNull(violations.get(TIMESTAMP_TEXT));
        assertNull(violations.get(PERSON_TEXT));
    }

    @Test
    public void testInvalidInfoTooLongStrings(){
        User invalidUser = new User(null,
                                    RandomStringUtils.randomAlphabetic(1021),
                                    RandomStringUtils.randomAlphabetic(1021),
                                    RandomStringUtils.randomAlphabetic(5),
                                    validPerson,
                                    RandomStringUtils.randomAlphanumeric(21),
                                    null,
                                    null,
                                    null,
                                    null);

        HashMap<String, List<ConstraintViolation<Object>>> violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(invalidUser));

        //Test for incorrect fields
        assertNotNull(violations.get(USERNAME_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(USERNAME_TEXT)).contains("Length must be between 1 and 1020 characters"));
        assertNotNull(violations.get(PASSWORD_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(PASSWORD_TEXT)).contains("Length must be between 5 and 1020 characters"));
        assertNotNull(violations.get(LAST_FOUR_SSN_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(LAST_FOUR_SSN_TEXT)).contains("Length must be 4 characters"));
        assertNotNull(violations.get(TIMEZONE_ID_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(TIMEZONE_ID_TEXT)).contains("Length must be between 1 and 20 characters"));

        //Test for correct fields
        assertNull(violations.get(ID_TEXT));
        assertNull(violations.get(ACCOUNT_ID_TEXT));
        assertNull(violations.get(CURRENCY_ID_TEXT));
        assertNull(violations.get(SPLIT_TEXT));
        assertNull(violations.get(TIMESTAMP_TEXT));
        assertNull(violations.get(PERSON_TEXT));
    }

    @Test
    public void testInvalidInfoBadFormatting(){
        User invalidUser = new User(null,
                                    "IMPROPERLY_FORMATTED_EMAIL",
                                    PASSWORD,
                                    "ABCD",
                                    validPerson,
                                    "123--has-symbols",
                                    null,
                                    null,
                                    null,
                                    null);

        HashMap<String, List<ConstraintViolation<Object>>> violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(invalidUser));

        //Test for invalid fields
        assertNotNull(violations.get(USERNAME_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(USERNAME_TEXT)).contains("Improperly formatted email address"));
        assertNotNull(violations.get(LAST_FOUR_SSN_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(LAST_FOUR_SSN_TEXT)).contains("Contains non-numeric characters"));
        assertNotNull(violations.get(TIMEZONE_ID_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(TIMEZONE_ID_TEXT)).contains("Contains non-alphanumeric characters"));

        //Test for correct fields
        assertNull(violations.get(ID_TEXT));
        assertNull(violations.get(PASSWORD_TEXT));
        assertNull(violations.get(ACCOUNT_ID_TEXT));
        assertNull(violations.get(CURRENCY_ID_TEXT));
        assertNull(violations.get(SPLIT_TEXT));
        assertNull(violations.get(TIMESTAMP_TEXT));
        assertNull(violations.get(PERSON_TEXT));
    }

}
