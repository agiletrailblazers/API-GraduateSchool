package com.gs.api.domain.authentication;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.ALWAYS)
public class AuthCredentials {

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;

    /**
     * Construct the auth credentials.
     * @param username the username.
     * @param password the encrypted password.
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
