package com.gs.api.exception;

public class NotFoundException extends Exception {

    private static final long serialVersionUID = 1L;
    private String message;
    
    public NotFoundException(String message) {
         this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
    
}
