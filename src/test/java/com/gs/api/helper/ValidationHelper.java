package com.gs.api.helper;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ValidationHelper {

    public static final String REQUIRED_FIELD = "Required field";

    /**
     * Convert set returned from Validator to Hashmap with the field name as the key
     * and a list of ConstraintViolation<Object> as the value
     *
     * @param inViolations
     * @return
     */
    public static HashMap<String, List<ConstraintViolation<Object>>> convertConstraintViolationsToHashMap(Set<ConstraintViolation<Object>> inViolations){
        HashMap<String, List<ConstraintViolation<Object>>> violations = new HashMap<>();

        for (ConstraintViolation<Object> cv : inViolations){
            String fieldName = cv.getPropertyPath().toString();

            if (!violations.containsKey(fieldName)){
                violations.put(fieldName, new ArrayList<>());
            }
            violations.get(fieldName).add(cv);
        }

        return violations;
    }

    /**
     * Get a list of the validation errors as Strings from a List<ConstraintViolation<Object>>
     *
     * @param violations
     * @return
     */
    public static List<String> getMessagesFromList(List<ConstraintViolation<Object>> violations){
        ArrayList<String> messages = new ArrayList<>();

        for (ConstraintViolation<Object> violation : violations){
            messages.add(violation.getMessage());
        }

        return messages;
    }

    /**
     * Utility to get the validator for unit testing
     *
     * @return
     */
    public static Validator getValidator(){
        ValidatorFactory validationFactory = Validation.buildDefaultValidatorFactory();
        return validationFactory.getValidator();
    }
}
