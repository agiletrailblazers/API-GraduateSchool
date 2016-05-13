package com.gs.api.domain;

import com.gs.api.helper.ValidationHelper;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PersonTest {
    public static final String FIRST_NAME = "FirstName";
    public static final String MIDDLE_NAME = "MiddleName";
    public static final String LAST_NAME = "LastName";
    public static final String EMAIL_ADDRESS = "person@test.com";
    public static final String PRIMARY_PHONE = "1234567890";
    public static final String SECONDARY_PHONE = "0987654321";
    public static final String DATE_OF_BIRTH = "3/22/1950";
    public static final String FIRST_NAME_TEXT = "firstName";
    public static final String LAST_NAME_TEXT = "lastName";
    public static final String EMAIL_ADDRESS_TEXT = "emailAddress";
    public static final String PRIMARY_PHONE_TEXT = "primaryPhone";
    public static final String PRIMARY_ADDRESS_TEXT = "primaryAddress";
    public static final String DATE_OF_BIRTH_TEXT = "dateOfBirth";
    public static final String MIDDLE_NAME_TEXT = "middleName";
    public static final String SECONDARY_PHONE_TEXT = "secondaryPhone";
    public static final String SECONDARY_ADDRESS_TEXT = "secondaryAddress";
    static final String DATE_OF_BIRTH_MSG = "Date of Birth is not in MM/dd/yyyy format";
    private Validator validator = ValidationHelper.getValidator();

    private static Address validAddress;

    @BeforeClass
    public static void setUpClass(){
        validAddress = new Address();
        validAddress.setAddress1("123 Main Street");
        validAddress.setAddress2("101");
        validAddress.setAddress3("Third Line");
        validAddress.setCity("Philadelphia");
        validAddress.setState("PA");
        validAddress.setPostalCode("12345");
    }

    @Test
    public void testValidPerson(){
        Person validPerson = new Person();
        validPerson.setFirstName(FIRST_NAME);
        validPerson.setMiddleName(MIDDLE_NAME);
        validPerson.setLastName(LAST_NAME);
        validPerson.setEmailAddress(EMAIL_ADDRESS);
        validPerson.setPrimaryPhone(PRIMARY_PHONE);
        validPerson.setSecondaryPhone(SECONDARY_PHONE);
        validPerson.setPrimaryAddress(validAddress);
        validPerson.setSecondaryAddress(validAddress);
        validPerson.setVeteran(false);
        validPerson.setDateOfBirth(DATE_OF_BIRTH);

        Set<ConstraintViolation<Person>> violations = this.validator.validate(validPerson);

        assertTrue(violations.isEmpty());
    }

    @Test
    public void testEmptyPerson(){
        Person invalidPerson = new Person();

        HashMap<String, List<ConstraintViolation<Object>>> violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(invalidPerson));

        //Test for required fields
        assertNotNull(violations.get(FIRST_NAME_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(FIRST_NAME_TEXT)).contains(ValidationHelper.REQUIRED_FIELD));
        assertNotNull(violations.get(LAST_NAME_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(LAST_NAME_TEXT)).contains(ValidationHelper.REQUIRED_FIELD));
        assertNotNull(violations.get(EMAIL_ADDRESS_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(EMAIL_ADDRESS_TEXT)).contains(ValidationHelper.REQUIRED_FIELD));
        assertNotNull(violations.get(PRIMARY_PHONE_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(PRIMARY_PHONE_TEXT)).contains(ValidationHelper.REQUIRED_FIELD));
        assertNotNull(violations.get(PRIMARY_ADDRESS_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(PRIMARY_ADDRESS_TEXT)).contains(ValidationHelper.REQUIRED_FIELD));
        assertNotNull(violations.get(DATE_OF_BIRTH_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(DATE_OF_BIRTH_TEXT)).contains(ValidationHelper.REQUIRED_FIELD));

        //Test for not-required fields
        assertNull(violations.get(MIDDLE_NAME_TEXT));
        assertNull(violations.get(SECONDARY_PHONE_TEXT));
        assertNull(violations.get(SECONDARY_ADDRESS_TEXT));
    }

    @Test
    public void testInvalidInfoEmptyStrings(){
        Person invalidPerson = new Person();
        invalidPerson.setFirstName("");
        invalidPerson.setMiddleName("");
        invalidPerson.setLastName("");
        invalidPerson.setEmailAddress("");
        invalidPerson.setPrimaryPhone("");
        invalidPerson.setSecondaryPhone("");
        invalidPerson.setPrimaryAddress(validAddress);
        invalidPerson.setDateOfBirth(DATE_OF_BIRTH);

        HashMap<String, List<ConstraintViolation<Object>>> violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(invalidPerson));

        //Test for incorrect fields
        assertNotNull(violations.get(FIRST_NAME_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(FIRST_NAME_TEXT)).contains("Length must be between 1 and 100 characters"));
        assertNotNull(violations.get(MIDDLE_NAME_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(MIDDLE_NAME_TEXT)).contains("Length must be between 1 and 100 characters"));
        assertNotNull(violations.get(LAST_NAME_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(LAST_NAME_TEXT)).contains("Length must be between 1 and 100 characters"));
        assertNotNull(violations.get(EMAIL_ADDRESS_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(EMAIL_ADDRESS_TEXT)).contains("Length must be between 1 and 1020 characters"));
        assertNotNull(violations.get(PRIMARY_PHONE_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(PRIMARY_PHONE_TEXT)).contains("Length must be between 10 and 100 characters"));
        assertNotNull(violations.get(SECONDARY_PHONE_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(SECONDARY_PHONE_TEXT)).contains("Length must be between 10 and 100 characters"));

        //Test for correct fields
        assertNull(violations.get(SECONDARY_ADDRESS_TEXT));
        assertNull(violations.get(DATE_OF_BIRTH_TEXT));
    }

    @Test
    public void testInvalidInfoTooLongStrings(){
        Person invalidPerson = new Person();
        invalidPerson.setFirstName(RandomStringUtils.randomAlphabetic(101));
        invalidPerson.setMiddleName(RandomStringUtils.randomAlphabetic(101));
        invalidPerson.setLastName(RandomStringUtils.randomAlphabetic(101));
        invalidPerson.setEmailAddress(RandomStringUtils.randomAlphabetic(1021));
        invalidPerson.setPrimaryPhone(RandomStringUtils.randomAlphanumeric(101));
        invalidPerson.setSecondaryPhone(RandomStringUtils.randomAlphanumeric(101));
        invalidPerson.setPrimaryAddress(validAddress);
        invalidPerson.setDateOfBirth(DATE_OF_BIRTH);

        HashMap<String, List<ConstraintViolation<Object>>> violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(invalidPerson));

        //Test for incorrect fields
        assertNotNull(violations.get(FIRST_NAME_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(FIRST_NAME_TEXT)).contains("Length must be between 1 and 100 characters"));
        assertNotNull(violations.get(MIDDLE_NAME_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(MIDDLE_NAME_TEXT)).contains("Length must be between 1 and 100 characters"));
        assertNotNull(violations.get(LAST_NAME_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(LAST_NAME_TEXT)).contains("Length must be between 1 and 100 characters"));
        assertNotNull(violations.get(EMAIL_ADDRESS_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(EMAIL_ADDRESS_TEXT)).contains("Length must be between 1 and 1020 characters"));
        assertNotNull(violations.get(PRIMARY_PHONE_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(PRIMARY_PHONE_TEXT)).contains("Length must be between 10 and 100 characters"));
        assertNotNull(violations.get(SECONDARY_PHONE_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(SECONDARY_PHONE_TEXT)).contains("Length must be between 10 and 100 characters"));

        //Test for correct fields
        assertNull(violations.get(SECONDARY_ADDRESS_TEXT));
        assertNull(violations.get(DATE_OF_BIRTH_TEXT));
    }

    @Test
    public void testInvalidInfoEmailFormatting(){
        Person invalidPerson = new Person();
        invalidPerson.setFirstName(FIRST_NAME);
        invalidPerson.setLastName(LAST_NAME);
        invalidPerson.setEmailAddress("IMPROPER_EMAIL");
        invalidPerson.setPrimaryPhone(PRIMARY_PHONE);
        invalidPerson.setPrimaryAddress(validAddress);
        invalidPerson.setDateOfBirth(DATE_OF_BIRTH);

        HashMap<String, List<ConstraintViolation<Object>>> violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(invalidPerson));
        assertNotNull(violations.get(EMAIL_ADDRESS_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(EMAIL_ADDRESS_TEXT)).contains("Improperly formatted email address"));

        invalidPerson.setEmailAddress("IMPROPER@EMAIL@ADDRESS");
        violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(invalidPerson));
        assertNotNull(violations.get(EMAIL_ADDRESS_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(EMAIL_ADDRESS_TEXT)).contains("Improperly formatted email address"));
    }

    @Test
    public void testInvalidInfoBadDOBFormatting(){
        Person invalidPerson = new Person();
        invalidPerson.setFirstName(FIRST_NAME);
        invalidPerson.setLastName(LAST_NAME);
        invalidPerson.setEmailAddress(EMAIL_ADDRESS);
        invalidPerson.setPrimaryPhone(PRIMARY_PHONE);
        invalidPerson.setPrimaryAddress(validAddress);

        invalidPerson.setDateOfBirth("19501223");
        HashMap<String, List<ConstraintViolation<Object>>> violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(invalidPerson));
        assertNotNull(violations.get(DATE_OF_BIRTH_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(DATE_OF_BIRTH_TEXT)).contains(DATE_OF_BIRTH_MSG));

        invalidPerson.setDateOfBirth("1//1950");
        violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(invalidPerson));
        assertNotNull(violations.get(DATE_OF_BIRTH_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(DATE_OF_BIRTH_TEXT)).contains(DATE_OF_BIRTH_MSG));

        invalidPerson.setDateOfBirth("1/3/50");
        violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(invalidPerson));
        assertNotNull(violations.get(DATE_OF_BIRTH_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(DATE_OF_BIRTH_TEXT)).contains(DATE_OF_BIRTH_MSG));

        invalidPerson.setDateOfBirth("1-3-1950");
        violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(invalidPerson));
        assertNotNull(violations.get(DATE_OF_BIRTH_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(DATE_OF_BIRTH_TEXT)).contains(DATE_OF_BIRTH_MSG));

        invalidPerson.setDateOfBirth("Jan 1 2016");
        violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(invalidPerson));
        assertNotNull(violations.get(DATE_OF_BIRTH_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(DATE_OF_BIRTH_TEXT)).contains(DATE_OF_BIRTH_MSG));

        invalidPerson.setDateOfBirth("201601011030");
        violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(invalidPerson));
        assertNotNull(violations.get(DATE_OF_BIRTH_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(DATE_OF_BIRTH_TEXT)).contains(DATE_OF_BIRTH_MSG));
    }
}
