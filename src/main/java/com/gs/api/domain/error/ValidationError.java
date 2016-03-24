package com.gs.api.domain.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class ValidationError {

    @JsonProperty("fieldName")
    private String fieldName;

    @JsonProperty("errorMessage")
    private String errorMessage;

    /**
     * Construct a validation error.
     * @param fieldName the field that had a validation error.
     * @param errorMessage the error message.
     */
    public ValidationError(@JsonProperty("fieldName") String fieldName, @JsonProperty("errorMessage") String errorMessage) {
        this.fieldName = fieldName;
        this.errorMessage = errorMessage;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
