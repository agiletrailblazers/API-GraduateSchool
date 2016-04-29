package com.gs.api.domain.authentication;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gs.api.domain.registration.User;

@JsonInclude(Include.ALWAYS)
public class AuthUser {

    @JsonProperty("authToken")
    private AuthToken authToken;

    @JsonProperty("renewalToken")
    private RenewalToken renewalToken;

    @JsonProperty("user")
    private User user;

    @JsonProperty("resetRequired")
    private boolean resetRequired;

    public AuthUser(@JsonProperty("authToken") AuthToken authToken, @JsonProperty("renewalToken") RenewalToken renewalToken, @JsonProperty("user") User user, @JsonProperty("resetRequired") boolean resetRequired) {
        this.authToken = authToken;
        this.renewalToken = renewalToken;
        this.user = user;
        this.resetRequired = resetRequired;
    }

    public AuthToken getAuthToken() {
        return authToken;
    }

    public RenewalToken getRenewalToken() {
        return renewalToken;
    }

    public User getUser() {
        return user;
    }

    public boolean getResetRequired() { return resetRequired; }
}
