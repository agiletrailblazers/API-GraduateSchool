package com.gs.api.domain;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.ALWAYS)
public class BaseUser {

    private String id;

    @NotNull(message = "Required field")
    @Size(min = 1, max = 1020, message="Length must be between 1 and 1020 characters")
    @Email(message = "Improperly formatted email address")
    private String username;

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

    private String accountNumber;

    public BaseUser() { }

    public BaseUser(String id, String username, String lastFourSSN, Person person, String timezoneId, String accountId, String accountNo,
                    String split, String currencyId, String timestamp) {
        this.id = id;
        this.username = username;
        this.lastFourSSN = lastFourSSN;
        this.person = person;
        this.timezoneId = timezoneId;
        this.accountId = accountId;
        this.accountNumber = accountNo;
        this.split = split;
        this.currencyId = currencyId;
        this.timestamp = timestamp;
    }

    public BaseUser(String id, String username, Person person, String timezoneId, String accountId, String accountNo,
                    String split, String currencyId, String timestamp) {
        this.id = id;
        this.username = username;
        this.person = person;
        this.timezoneId = timezoneId;
        this.accountId = accountId;
        this.accountNumber = accountNo;
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

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}
