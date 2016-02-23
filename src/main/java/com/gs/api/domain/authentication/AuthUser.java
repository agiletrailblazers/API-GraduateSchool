package com.gs.api.domain.authentication;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gs.api.domain.registration.User;

@JsonInclude(Include.ALWAYS)
public class AuthUser {

    @JsonProperty("authToken")
    private AuthToken authToken;

    @JsonProperty("user")
    private User user;

    public AuthUser(@JsonProperty("authToken") AuthToken authToken, @JsonProperty("user") User user) {
        this.authToken = authToken;
        this.user = user;
    }

    public AuthToken getAuthToken() {
        return authToken;
    }

    public User getUser() {
        return user;
    }
}
