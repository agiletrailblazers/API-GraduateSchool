package com.gs.api.domain.payment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class PaymentConfirmation {

    @JsonProperty("payment")
    private Payment payment;

    @JsonProperty("saleId")
    private String saleId;

    /**
     * Constructor for Payment object
     *
     * @param payment - the payment information
     * @param saleId - transaction ID for the payment
     */
    public PaymentConfirmation(@JsonProperty("payment") Payment payment, @JsonProperty("saleId") String saleId){
        this.payment = payment;
        this.saleId = saleId;
    }

    public Payment getPayment(){
        return payment;
    }

    public String getSaleId(){
        return saleId;
    }
}
