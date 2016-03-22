package com.gs.api.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import org.hibernate.validator.constraints.Email;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@JsonInclude(Include.ALWAYS)
public class Person {

    @NotNull(message = "Required field")
    @Size(min = 1, max = 100, message="Length must be between 1 and 100 characters")
    // TODO is there a pattern for allowed characters?
    private String firstName;

    @Size(min = 1, max = 100, message="Length must be between 1 and 100 characters")
    // TODO is there a pattern for allowed characters?
    private String middleName;

    @NotNull(message = "Required field")
    @Size(min = 1, max = 100, message="Length must be between 1 and 100 characters")
    // TODO is there a pattern for allowed characters?
    private String lastName;

    @NotNull(message = "Required field")
    @Size(min = 1, max = 1020, message="Length must be between 1 and 1020 characters")
    @Email(message = "Improperly formatted email address")
    private String emailAddress;

    @NotNull(message = "Required field")
    @Size(min = 1, max = 100, message="Length must be between 1 and 100 characters")
    // TODO is there a pattern for allowed characters?
    private String primaryPhone;

    @Size(min = 1, max = 100, message="Length must be between 1 and 100 characters")
    // TODO is there a pattern for allowed characters?
    private String secondaryPhone;

    @NotNull(message = "Required field")
    @Valid
    private Address primaryAddress;

    @Valid
    private Address secondaryAddress;

    private Boolean veteran;

    @NotNull(message = "Required field")
    // TODO do we need a custom validator or just a pattern match?
    private String dateOfBirth;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPrimaryPhone() {
        return primaryPhone;
    }

    public void setPrimaryPhone(String primaryPhone) {
        this.primaryPhone = primaryPhone;
    }

    public String getSecondaryPhone() {
        return secondaryPhone;
    }

    public void setSecondaryPhone(String secondaryPhone) {
        this.secondaryPhone = secondaryPhone;
    }

    public Address getPrimaryAddress() {
        return primaryAddress;
    }

    public void setPrimaryAddress(Address primaryAddress) {
        this.primaryAddress = primaryAddress;
    }

    public Address getSecondaryAddress() {
        return secondaryAddress;
    }

    public void setSecondaryAddress(Address secondaryAddress) {
        this.secondaryAddress = secondaryAddress;
    }

    public Boolean getVeteran() {
        return veteran;
    }

    public void setVeteran(Boolean veteran) {
        this.veteran = veteran;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
