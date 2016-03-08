package com.gs.api.controller;

import com.gs.api.exception.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;

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
     * Return json formatted error response when a duplicate registration is found
     * @return ResponseBody
     */
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({ DuplicateRegistrationException.class })
    @ResponseBody
    public String handleDuplicateRegistrationException(DuplicateRegistrationException ex) {
        return "{\"message\": \"" + ex.getMessage() + "\"}";
    }

}
