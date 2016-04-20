package com.gs.api.domain.authentication;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.ALWAYS)
public class AuthCredentials {

    @JsonProperty("username")
    protected String username;

    @JsonProperty("password")
    protected String password;

    /**
     * Construct the auth credentials.
     * @param username the username.
     * @param password the clear-text password.
     */
    public AuthCredentials(@JsonProperty("username")String username, @JsonProperty("password")String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
