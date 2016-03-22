package com.gs.api.domain;

import com.gs.api.helper.ValidationHelper;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

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
        invalidAddress.setCity("");
        invalidAddress.setState("");
        invalidAddress.setPostalCode("");

        HashMap<String, List<ConstraintViolation<Object>>> violations =  ValidationHelper.convertConstraintViolationsToHashMap(validator.validate(invalidAddress));

        assertNotNull(violations.get(ADDRESS_1_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(ADDRESS_1_TEXT)).contains("Length must be between 1 and 1020 characters"));
        assertNotNull(violations.get(CITY_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(CITY_TEXT)).contains("Length must be between 1 and 200 characters"));
        assertNotNull(violations.get(STATE_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(STATE_TEXT)).contains("Length must be between 1 and 200 characters"));
        assertNotNull(violations.get(POSTAL_CODE_TEXT));
        assertTrue(ValidationHelper.getMessagesFromList(violations.get(POSTAL_CODE_TEXT)).contains("Length must be between 1 and 200 characters"));
    }
}
