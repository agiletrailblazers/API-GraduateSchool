package com.gs.api.domain.registration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.gs.api.domain.Person;

import org.hibernate.validator.constraints.Email;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@JsonInclude(Include.ALWAYS)
public class User {

    private String id;

    @NotNull(message = "Required field")
    @Size(min = 1, max = 1020, message="Length must be between 1 and 1020 characters")
    @Email(message = "Improperly formatted email address")
    private String username;

    @NotNull(message = "Required field")
    @Size(min = 1, max = 1020, message="Length must be between 1 and 1020 characters")
    // TODO is there a pattern for allowed characters?
    private String password;

    @NotNull(message = "Required field")
    @Size(min = 4, max = 4, message = "Length must be 4 characters")
    @Pattern(regexp = "[0-9]*", message = "Contains non-numeric characters")
    private String lastFourSSN;

    @NotNull(message = "Required field")
    @Size(min = 1, max = 20, message = "Length must be between 1 and 20 characters")
    @Pattern(regexp = "[a-zA-Z0-9]*", message = "Contains non-alphanumeric characters")
    private String timezoneId;

    private String accountId;
    private String currencyId;
    private String split;
    // this is the timestamp associated with the user creation, some of the stored procedures require that this timestamp be passed in,
    // keeping it as a string because the DB currently has timestamps with different formats
    private String timestamp;

    @NotNull(message = "Required field")
    @Valid
    private Person person;

    public User() { }

    public User(String id, String username, String password, String lastFourSSN, Person person, String timezoneId, String accountId,
                String split, String currencyId, String timestamp) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.lastFourSSN = lastFourSSN;
        this.person = person;
        this.timezoneId = timezoneId;
        this.accountId = accountId;
        this.split = split;
        this.currencyId = currencyId;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getTimezoneId() {
        return timezoneId;
    }

    public void setTimezoneId(String timezoneId) {
        this.timezoneId = timezoneId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }

    public String getSplit() {
        return split;
    }

    public void setSplit(String split) {
        this.split = split;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
