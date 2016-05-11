package com.gs.api.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.gs.api.domain.Person;

import org.hibernate.validator.constraints.Email;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@JsonInclude(Include.ALWAYS)
public class User extends BaseUser {

    @NotNull(message = "Required field")
    @Size(min = 4, max = 4, message = "Length must be 4 characters")
    @Pattern(regexp = "[0-9]*", message = "Contains non-numeric characters")
    private String lastFourSSN;


    @NotNull(message = "Required field")
    @Size(min = 5, max = 1020, message="Length must be between 5 and 1020 characters")
    // TODO is there a pattern for allowed characters?
    private String password;

    public User() { }

    public User(String id, String username, String password, String lastFourSSN, Person person, String timezoneId, String accountId, String accountNumber,
                String split, String currencyId, String timestamp) {
        super(id, username, person, timezoneId, accountId, accountNumber,
                split, currencyId, timestamp);
        this.password = password;
        this.lastFourSSN = lastFourSSN;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLastFourSSN() {
        return lastFourSSN;
    }

    public void setLastFourSSN(String lastFourSSN) {
        this.lastFourSSN = lastFourSSN;
    }
}
