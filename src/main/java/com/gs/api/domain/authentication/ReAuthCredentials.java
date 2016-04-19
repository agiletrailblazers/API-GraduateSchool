package com.gs.api.domain.authentication;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.ALWAYS)
public class ReAuthCredentials extends AuthCredentials{

    @JsonProperty("authToken")
    private AuthToken authToken;

    @JsonProperty("renewalToken")
    private RenewalToken renewalToken;

    /**
     * Construct the auth credentials.
     * @param username the username.
     * @param password the clear-text password.
     */
    public ReAuthCredentials(@JsonProperty("username") String username, @JsonProperty("password") String password,
                             @JsonProperty("authToken") AuthToken authToken, @JsonProperty("renewalToken") RenewalToken renewalToken) {
        this.username = username;
        this.password = password;
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
