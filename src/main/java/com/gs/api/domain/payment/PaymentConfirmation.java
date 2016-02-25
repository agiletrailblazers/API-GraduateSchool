package com.gs.api.domain.payment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class PaymentConfirmation {

    @JsonProperty("payment")
    private Payment payment;

    @JsonProperty("transactionID")
    private String transactionID;

    /**
     * Constructor for Payment object
     *
     * @param payment - the payment information
     * @param transactionID - transaction ID for the payment
     */
    public PaymentConfirmation(@JsonProperty("payment") Payment payment, @JsonProperty("transactionID") String transactionID){
        this.payment = payment;
        this.transactionID = transactionID;
    }

    public Payment getPayment(){
        return payment;
    }

    public String getTransactionID(){
        return transactionID;
    }
}
