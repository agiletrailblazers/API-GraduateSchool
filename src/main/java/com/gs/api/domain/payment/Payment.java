package com.gs.api.domain.payment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class Payment {

    @JsonProperty("amount")
    private double amount;

    @JsonProperty("transactionID")
    private String transactionID;

    /**
     * Constructor for Payment object
     *
     * @param amount - total amount of transaction
     * @param transactionID - transaction ID
     */
    public Payment(@JsonProperty("amount")double amount, @JsonProperty("transactionID")String transactionID){
        this.amount = amount;
        this.transactionID = transactionID;
    }

    public double getAmount(){
        return this.amount;
    }

    public String getTransactionID(){
        return this.transactionID;
    }
}
