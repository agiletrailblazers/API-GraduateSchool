package com.gs.api.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@JsonInclude(Include.NON_NULL)
public class Address {

    private String id;

    @NotNull(message = "Required field")
    @Size(min = 1, max = 1020, message="Length must be between 1 and 1020 characters")
    private String address1;

    @Size(min = 1, max = 1020, message="Length must be between 1 and 1020 characters")
    private String address2;

    @Size(min = 1, max = 1020, message="Length must be between 1 and 1020 characters")
    private String address3;

    @NotNull(message = "Required field")
    @Size(min = 1, max = 200, message="Length must be between 1 and 200 characters")
    private String city;

    @NotNull(message = "Required field")
    @Size(min = 1, max = 2, message="Length must be between 1 and 2 characters")
    private String state;

    @NotNull(message = "Required field")
    @Size(min = 5, max = 10, message="Length must be between 5 and 10 characters")
    @Pattern(regexp = "[0-9]{5}(-?[0-9]{4})?", message = "Postal Code is not in 5 or 9 digit format")
    private String postalCode;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getAddress3() {
        return address3;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }
}
