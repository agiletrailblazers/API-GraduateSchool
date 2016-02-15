package com.gs.api.domain.registration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.gs.api.domain.Person;

@JsonInclude(Include.ALWAYS)
public class User {

    private String id;
    private String username;
    private String password;
    private String lastFourSSN;
    private String timezoneId;
    private String accountId;
    private String currencyId;
    private String split;
    // this is the timestamp associated with the user creation, some of the stored procedures require that this timestamp be passed in
    private Long timestamp;

    private Person person;

    public User() { }

    public User(String id, String username, String password, String lastFourSSN, Person person, String timezoneId, String accountId,
                String split, String currencyId, Long timestamp) {
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

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
