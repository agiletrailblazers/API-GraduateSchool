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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by mfried on 3/22/16.
 */
public class AddressTest {

    public static final String ADDRESS_1 = "123 Main Street";
    public static final String ADDRESS_2 = "101";
    public static final String ADDRESS_3 = "Third Line";
    public static final String CITY = "Philadelphia";
    public static final String STATE = "PA";
    public static final String POSTAL_CODE = "12345";
    public static final String ADDRESS_1_TEXT = "address1";
    public static final String ADDRESS_2_TEXT = "address2";
    private static final String ADDRESS_3_TEXT = "address3";
    public static final String CITY_TEXT = "city";
    public static final String STATE_TEXT = "state";
    public static final String POSTAL_CODE_TEXT = "postalCode";


    private Validator validator = ValidationHelper.getValidator();

    @Test
    public void testValidAddress(){
        Address validAddress = new Address();
        validAddress.setAddress1(ADDRESS_1);
        validAddress.setAddress2(ADDRESS_2);
        validAddress.setAddress3(ADDRESS_3);
        validAddress.setCity(CITY);
        validAddress.setState(STATE);
        validAddress.setPostalCode(POSTAL_CODE);

        Set<ConstraintViolation<Address>> violations = this.validator.validate(validAddress);

        assertTrue(violations.isEmpty());
    }

    @Test
    public void testEmptyAddress(){
        Address invalidAddress = new Address();

        HashMap<String, List<ConstraintViolation<Object>>> violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(invalidAddress));

        //Test for required fields
        assertNotNull(violations.get(ADDRESS_1_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(ADDRESS_1_TEXT)).contains(ValidationHelper.REQUIRED_FIELD));
        assertNotNull(violations.get(CITY_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(CITY_TEXT)).contains(ValidationHelper.REQUIRED_FIELD));
        assertNotNull(violations.get(STATE_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(STATE_TEXT)).contains(ValidationHelper.REQUIRED_FIELD));
        assertNotNull(violations.get(POSTAL_CODE_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(POSTAL_CODE_TEXT)).contains(ValidationHelper.REQUIRED_FIELD));

        assertNull(violations.get(ADDRESS_2_TEXT));
        assertNull(violations.get(ADDRESS_3_TEXT));
    }

    @Test
    public void testInvalidInfoEmptyStrings(){
        Address invalidAddress = new Address();
        invalidAddress.setAddress1("");
        invalidAddress.setAddress2("");
        invalidAddress.setAddress3("");
        invalidAddress.setCity("");
        invalidAddress.setState("");
        invalidAddress.setPostalCode("");

        HashMap<String, List<ConstraintViolation<Object>>> violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(invalidAddress));

        assertNotNull(violations.get(ADDRESS_1_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(ADDRESS_1_TEXT)).contains("Length must be between 1 and 1020 characters"));
        assertNotNull(violations.get(ADDRESS_2_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(ADDRESS_2_TEXT)).contains("Length must be between 1 and 1020 characters"));
        assertNotNull(violations.get(ADDRESS_3_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(ADDRESS_3_TEXT)).contains("Length must be between 1 and 1020 characters"));
        assertNotNull(violations.get(CITY_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(CITY_TEXT)).contains("Length must be between 1 and 200 characters"));
        assertNotNull(violations.get(STATE_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(STATE_TEXT)).contains("Length must be between 1 and 2 characters"));
        assertNotNull(violations.get(POSTAL_CODE_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(POSTAL_CODE_TEXT)).contains("Length must be between 5 and 10 characters"));
    }

    @Test
    public void testInvalidInfoTooLongStrings(){
        Address invalidAddress = new Address();
        invalidAddress.setAddress1(RandomStringUtils.randomAlphabetic(1021));
        invalidAddress.setAddress2(RandomStringUtils.randomAlphabetic(1021));
        invalidAddress.setAddress3(RandomStringUtils.randomAlphabetic(1021));
        invalidAddress.setCity(RandomStringUtils.randomAlphabetic(201));
        invalidAddress.setState(RandomStringUtils.randomAlphabetic(3));
        invalidAddress.setPostalCode(RandomStringUtils.randomNumeric(11));

        HashMap<String, List<ConstraintViolation<Object>>> violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(invalidAddress));

        assertNotNull(violations.get(ADDRESS_1_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(ADDRESS_1_TEXT)).contains("Length must be between 1 and 1020 characters"));
        assertNotNull(violations.get(ADDRESS_2_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(ADDRESS_2_TEXT)).contains("Length must be between 1 and 1020 characters"));
        assertNotNull(violations.get(ADDRESS_3_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(ADDRESS_3_TEXT)).contains("Length must be between 1 and 1020 characters"));
        assertNotNull(violations.get(CITY_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(CITY_TEXT)).contains("Length must be between 1 and 200 characters"));
        assertNotNull(violations.get(STATE_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(STATE_TEXT)).contains("Length must be between 1 and 2 characters"));
        assertNotNull(violations.get(POSTAL_CODE_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(POSTAL_CODE_TEXT)).contains("Length must be between 5 and 10 characters"));
    }

    @Test
    public void testPostalCodeFormat(){
        Address address = new Address();
        address.setAddress1(ADDRESS_1);
        address.setAddress2(ADDRESS_2);
        address.setAddress3(ADDRESS_3);
        address.setCity(CITY);
        address.setState(STATE);

        //Tests for Good Postal Code
        address.setPostalCode("12345");
        HashMap<String, List<ConstraintViolation<Object>>> violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(address));
        assertTrue(violations.isEmpty());

        address.setPostalCode("12345-1234");
        violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(address));
        assertTrue(violations.isEmpty());

        address.setPostalCode("123451234");
        violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(address));
        assertTrue(violations.isEmpty());

        //Tests for Bad Postal Code
        address.setPostalCode("12345-");
        violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(address));
        assertNotNull(violations.get(POSTAL_CODE_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(POSTAL_CODE_TEXT)).contains("Postal Code is not in 5 or 9 digit format"));

        address.setPostalCode("123451");
        violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(address));
        assertNotNull(violations.get(POSTAL_CODE_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(POSTAL_CODE_TEXT)).contains("Postal Code is not in 5 or 9 digit format"));

        address.setPostalCode("12345 1234");
        violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(address));
        assertNotNull(violations.get(POSTAL_CODE_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(POSTAL_CODE_TEXT)).contains("Postal Code is not in 5 or 9 digit format"));
    }
}
