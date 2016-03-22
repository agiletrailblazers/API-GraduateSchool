package com.gs.api.domain;

import com.gs.api.helper.ValidationHelper;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Created by mfried on 3/22/16.
 */
public class PersonTest {
    public static final String FIRST_NAME = "FirstName";
    public static final String MIDDLE_NAME = "MiddleName";
    public static final String LAST_NAME = "LastName";
    public static final String PERSON_TEST_COM = "person@test.com";
    public static final String PRIMARY_PHONE = "1234567890";
    public static final String SECONDARY_PHONE = "0987654321";
    public static final String DATE_OF_BIRTH = "20160322";
    public static final String FIRST_NAME1 = "firstName";
    private Validator validator = ValidationHelper.getValidator();

    private Address validAddress;

    @Before
    public void init(){
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
        validPerson.setEmailAddress(PERSON_TEST_COM);
        validPerson.setPrimaryPhone(PRIMARY_PHONE);
        validPerson.setSecondaryPhone(SECONDARY_PHONE);
        validPerson.setPrimaryAddress(validAddress);
        validPerson.setSecondaryAddress(validAddress);
        validPerson.setVeteran(false);
        validPerson.setDateOfBirth(DATE_OF_BIRTH);

        Set<ConstraintViolation<Person>> violations = this.validator.validate(validPerson);

        assertTrue(violations.isEmpty());
    }

//    @Test
//    public void testEmptyPerson(){
//        Person invalidPerson = new Person();
//
//        HashMap<String, List<ConstraintViolation<Object>>> violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(invalidPerson));
//
//        //Test for required fields
//        assertNotNull(violations.get(FIRST_NAME1));
//        assertTrue(ValidationHelper.getMessagesFromList(violations.get(ADDRESS_1_TEXT)).contains(ValidationHelper.REQUIRED_FIELD));
//        assertNotNull(violations.get(CITY_TEXT));
//        assertTrue(ValidationHelper.getMessagesFromList(violations.get(CITY_TEXT)).contains(ValidationHelper.REQUIRED_FIELD));
//        assertNotNull(violations.get(STATE_TEXT));
//        assertTrue(ValidationHelper.getMessagesFromList(violations.get(STATE_TEXT)).contains(ValidationHelper.REQUIRED_FIELD));
//        assertNotNull(violations.get(POSTAL_CODE_TEXT));
//        assertTrue(ValidationHelper.getMessagesFromList(violations.get(POSTAL_CODE_TEXT)).contains(ValidationHelper.REQUIRED_FIELD));
//
//        assertNull(violations.get(ADDRESS_2_TEXT));
//        assertNull(violations.get(ADDRESS_3_TEXT));
//    }

//    @Test
//    public void testInvalidInfoEmptyStrings(){
//        Address invalidAddress = new Address();
//        invalidAddress.setAddress1("");
//        invalidAddress.setCity("");
//        invalidAddress.setState("");
//        invalidAddress.setPostalCode("");
//
//        HashMap<String, List<ConstraintViolation<Object>>> violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(invalidAddress));
//
//        assertNotNull(violations.get(ADDRESS_1_TEXT));
//        assertTrue(ValidationHelper.getMessagesFromList(violations.get(ADDRESS_1_TEXT)).contains("Length must be between 1 and 1020 characters"));
//        assertNotNull(violations.get(CITY_TEXT));
//        assertTrue(ValidationHelper.getMessagesFromList(violations.get(CITY_TEXT)).contains("Length must be between 1 and 200 characters"));
//        assertNotNull(violations.get(STATE_TEXT));
//        assertTrue(ValidationHelper.getMessagesFromList(violations.get(STATE_TEXT)).contains("Length must be between 1 and 200 characters"));
//        assertNotNull(violations.get(POSTAL_CODE_TEXT));
//        assertTrue(ValidationHelper.getMessagesFromList(violations.get(POSTAL_CODE_TEXT)).contains("Length must be between 1 and 200 characters"));
//    }
}
