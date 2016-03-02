package com.gs.api.service.payment;

import com.gs.api.domain.payment.Payment;
import com.gs.api.domain.payment.PaymentConfirmation;
import com.gs.api.exception.PaymentException;

public interface PaymentService {

    /**
     * Process a payment.
     * @param payment the payment information.
     * @return payment confirmation.
     * @throws PaymentException error processing the payment
     */
    PaymentConfirmation processPayment(final Payment payment) throws PaymentException;

    void reversePayment(final Payment payment) throws PaymentException;

}
