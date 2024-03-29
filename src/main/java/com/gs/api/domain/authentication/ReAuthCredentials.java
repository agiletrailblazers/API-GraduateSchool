package com.gs.api.domain.authentication;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.ALWAYS)
public class ReAuthCredentials {

    @JsonProperty("authToken")
    private AuthToken authToken;

    @JsonProperty("renewalToken")
    private RenewalToken renewalToken;

    /**
     * Contructor for ReAuthCredentials
     * @param authToken
     * @param renewalToken
     */
    public ReAuthCredentials(@JsonProperty("authToken") AuthToken authToken, @JsonProperty("renewalToken") RenewalToken renewalToken) {
        this.authToken = authToken;
        this.renewalToken = renewalToken;
    }

    public AuthToken getAuthToken() {
        return authToken;
    }

    public RenewalToken getRenewalToken() {
        return renewalToken;
    }
}
