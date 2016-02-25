package com.gs.api.domain.payment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class Payment {

    @JsonProperty("amount")
    private double amount;

    @JsonProperty("authorizationId")
    private String authorizationId;

    @JsonProperty("merchantReferenceId")
    private String merchantReferenceId;

    /**
     * Constructor for Payment object
     *
     * @param amount - total amount of transaction
     * @param authorizationId - transaction ID
     */
    public Payment(@JsonProperty("amount") double amount, @JsonProperty("authorizationId") String authorizationId, @JsonProperty("merchantReferenceId") String merchantReferenceId){
        this.amount = amount;
        this.authorizationId = authorizationId;
        this.merchantReferenceId = merchantReferenceId;
    }

    public double getAmount(){
        return amount;
    }

    public String getAuthorizationId(){
        return authorizationId;
    }

    public String getMerchantReferenceId() {
        return merchantReferenceId;
    }
}
