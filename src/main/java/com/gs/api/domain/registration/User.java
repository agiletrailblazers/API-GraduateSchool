package com.gs.api.domain.registration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.gs.api.domain.Person;

@JsonInclude(Include.ALWAYS)
public class User {

    private String id;
    private String username;
    private String dateOfBirth;
    private String lastFourSSN;
    private String password;
    private Person person;

    /* From Main Insert Query */
    /* (xid, xcurrency_id, xsplit, xxlgen_account_no, xnewts */

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

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getLastFourSSN() {
        return lastFourSSN;
    }

    public void setLastFourSSN(String lastFourSSN) {
        this.lastFourSSN = lastFourSSN;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
