package com.gs.api.exception;


/**
 * Exception to be thrown by the payment service when the payment was declined.
 */
public class PaymentDeclinedException extends PaymentException {

    /**
     * Constructs a new exception with the specified detail message.  The cause is not initialized, and
     * may subsequently be initialized by a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for later retrieval by the {@link
     *                #getMessage()} method.
     */
    public PaymentDeclinedException(String message) {
        super(message);
    }
}
