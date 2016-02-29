package com.gs.api.domain.registration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gs.api.domain.payment.PaymentConfirmation;

import java.util.List;


@JsonInclude(Include.ALWAYS)
public class RegistrationResponse {

    @JsonProperty("registrations")
    private List<Registration> registrations;

    @JsonProperty("paymentConfirmations")
    private List<PaymentConfirmation> paymentConfirmations;

    /**
     * Constructor for the RegistrationResponse object.
     *
     * @param registrations
     * @param paymentConfirmations
     */
    public RegistrationResponse(@JsonProperty("registrations")List<Registration> registrations, @JsonProperty("paymentConfirmations")List<PaymentConfirmation> paymentConfirmations){
        this.registrations = registrations;
        this.paymentConfirmations = paymentConfirmations;
    }

    public List<Registration> getRegistrations() {
        return registrations;
    }

    public List<PaymentConfirmation> getPaymentConfirmations() {
        return paymentConfirmations;
    }
}
