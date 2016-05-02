package com.gs.api.exception;

/**
 * Exception to be thrown when attempting to create a user which already exists in the database
 */
public class DuplicateUserException extends Exception {

    /**
     * Constructs a new exception with the specified detail message.  The cause is not initialized,
     * and may subsequently be initialized by a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for later retrieval by the
     *                {@link #getMessage()} method.
     */
    public DuplicateUserException(String message) {
        super(message);
    }
}
