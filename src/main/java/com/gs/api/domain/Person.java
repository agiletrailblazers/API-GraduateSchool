package com.gs.api.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.List;

@JsonInclude(Include.ALWAYS)
public class Person {

    private String id;
    private String timeStamp;
    private String firstName;
    private String middleName;
    private String lastName;
    private List<Address> personAddresses;
    private List<String> phones;

    /*  All properties from stored procedure */
    /*(id, time_stamp, title, person_no, fname, lname, mname, homephone,
                  workphone, fax, created_by, created_on, updated_by,
                  updated_on, custom0, custom1, custom2,
                  company_id, split, email,
                  custom3, custom4,ci_lname,ci_fname, desired_job_type_id, jobtype_id,
                  locale_id, password, username, manager_id, flags, timezone_id,
          custom5, custom6, custom7, custom8, custom9,
         suffix, job_title, ss_no, status, person_type, ci_person_type,
         location_id, home_domain, allow_nonbuddies, gender, started_on, terminated_on,
        date_of_birth,religion,ethnicity, secret_question, secret_answer, type)       */

    public Person() { }

    public Person(String id, String timeStamp, String firstName, String middleName, String lastName,
                  List<Address> personAddresses, List<String> phoneNumbers) {
        this.id = id;
        this.timeStamp = timeStamp;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.personAddresses = personAddresses;
        this.phones = phoneNumbers;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

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

    public List<Address> getPersonAddresses() {
        return personAddresses;
    }

    public void setPersonAddresses(List<Address> personAddresses) {
        this.personAddresses = personAddresses;
    }

    public List<String> getPhones() {
        return phones;
    }

    public void setPrimaryPhone(List<String> phones) {
        this.phones = phones;
    }

}
