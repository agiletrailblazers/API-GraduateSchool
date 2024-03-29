package com.gs.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.api.domain.error.ValidationError;
import com.gs.api.exception.AuthenticationException;
import com.gs.api.exception.NotFoundException;
import com.gs.api.exception.PaymentAcceptedException;
import com.gs.api.exception.PaymentDeclinedException;
import com.gs.api.exception.PaymentException;
import com.gs.api.exception.DuplicateUserException;
import com.gs.api.exception.ReusedPasswordException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Common code for Controllers 
 */
public abstract class BaseController {
    
    static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(
                String[].class, new StringArrayPropertyEditor(null));
    }

    /**
     * Return json formatted error response for authentication errors
     * @return ResponseBody
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({ AuthenticationException.class })
    @ResponseBody
    public String handleAuthenticationException(Exception ex) {
        logger.error("Not authorized", ex);
        final StringBuffer response = new StringBuffer();
        response.append("{\"message\":\"");
        response.append(ex.getMessage());
        response.append("\"}");
        return response.toString();
    }

    /**
     * Return json formatted error response for any custom "not found" errors
     * @return ResponseBody
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({ NotFoundException.class })
    @ResponseBody
    public String handleNotFoundException(Exception ex) {
        logger.error("API not found", ex);
        final StringBuffer response = new StringBuffer();
        response.append("{\"message\":\"");
        response.append(ex.getMessage());
        response.append("\"}");
        return response.toString();
    }

    /**
     * Return json formatted error response for any internal server errors
     * @return ResponseBody
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({ Exception.class })
    @ResponseBody
    public String handleException(Exception ex) {
        logger.error("Exception occured", ex);
        final StringBuffer response = new StringBuffer();
        response.append("{\"message\":\"");
        response.append(ex.getMessage());
        response.append("\"}");
        return response.toString();
    }

    /**
     * Return json formatted error response for bad request
     *
     * @return ResponseBody
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ HttpMessageNotReadableException.class })
    @ResponseBody
    public String handleValidationException(HttpMessageNotReadableException ex) throws IOException {
        // method called when a input validation failure occurs
        return "{\"message\": \"Invalid Request \"}";
    }

    /**
     * Return json formatted error response for a payment declined failure
     * @return ResponseBody
     */
    @ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
    @ExceptionHandler({ PaymentDeclinedException.class })
    @ResponseBody
    public String handlePaymentDeclined(PaymentDeclinedException ex) {
        return "{\"message\": \"" + ex.getMessage() + "\"}";
    }

    /**
     * Return json formatted error response for a payment accepted exception
     * @return ResponseBody
     */
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ExceptionHandler({ PaymentAcceptedException.class })
    @ResponseBody
    public String handlePaymentAccepted(PaymentAcceptedException ex) {
        return "{\"message\": \"" + ex.getMessage() + "\"}";
    }

    /**
     * Return json formatted error response for a generic payment failure
     * @return ResponseBody
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({ PaymentException.class })
    @ResponseBody
    public String handlePaymentException(PaymentException ex) {
        return "{\"message\": \"" + ex.getMessage() + "\"}";
    }

    /**
     * Return json formatted error response for requests containing invalid data in the request.
     * @return ResponseBody
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ MethodArgumentNotValidException.class })
    @ResponseBody
    public String handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) throws JsonProcessingException {

        // get the errors
        BindingResult result = ex.getBindingResult();

        ObjectMapper mapper = new ObjectMapper();

        List<ValidationError> validationErrors = new ArrayList<>();
        for (FieldError error : result.getFieldErrors()) {
            ValidationError validationError = new ValidationError(error.getField(), error.getDefaultMessage());
            validationErrors.add(validationError);
        }
        return "{\"validationErrors\":" + mapper.writeValueAsString(validationErrors) + "}";
    }

    /**
     * Return json formatted error response for a duplicate user error
     * @return ResponseBody
     */
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({ DuplicateUserException.class })
    @ResponseBody
    public String handleDuplicateUserException(DuplicateUserException ex) {
        return "{\"message\": \"" + ex.getMessage() + "\"}";
    }

    /**
     * Return json formatted error response for a reused password
     * @return ResponseBody
     */
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({ ReusedPasswordException.class })
    @ResponseBody
    public String handleReusedPasswordException(ReusedPasswordException ex) {
        return "{\"message\": \"" + ex.getMessage() + "\"}";
    }

}
