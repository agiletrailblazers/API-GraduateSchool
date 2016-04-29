package com.gs.api.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gs.api.domain.authentication.AuthCredentials;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@JsonInclude(Include.ALWAYS)
public class PWChangeCredentials extends AuthCredentials{

    @NotNull(message = "Required field")
    @Size(min = 5, max = 1020, message="Length must be between 5 and 1020 characters")
    @JsonProperty("newPassword")
    private String newPassword;

    /**
     * Construct the password change credentials.
     * @param username the username.
     * @param password the encrypted password.
     * @param newPassword the new encrypted password
     */
    public PWChangeCredentials(@JsonProperty("username") String username, @JsonProperty("password") String password, @JsonProperty("newPassword") String newPassword) {
        super(username, password);
        this.newPassword = newPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }
}
