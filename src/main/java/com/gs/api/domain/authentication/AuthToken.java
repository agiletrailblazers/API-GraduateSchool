package com.gs.api.domain.authentication;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.ALWAYS)
public class AuthToken {

    @JsonProperty("token")
    private String token;

    public AuthToken(@JsonProperty("token")String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
