package com.gs.api.domain.registration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gs.api.domain.payment.Payment;
import java.util.List;


@JsonInclude(Include.ALWAYS)
public class RegistrationRequest {

    @JsonProperty("registrations")
    private List<Registration> registrations;

    @JsonProperty("payments")
    private List<Payment> payments;

    /**
     * Constructor for the RegistrationRequest object.
     *
     * @param registrations
     * @param payments
     */
    public RegistrationRequest(@JsonProperty("registrations")List<Registration> registrations, @JsonProperty("payments")List<Payment> payments){
        this.registrations = registrations;
        this.payments = payments;
    }

    public List<Registration> getRegistrations() {
        return registrations;
    }

    public List<Payment> getPayments() {
        return payments;
    }
}
