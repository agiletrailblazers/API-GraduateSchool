package com.gs.api.domain.registration;

import com.gs.api.domain.Address;

import java.util.Date;

/**
 * Registration details object
 */
public class RegistrationDetails {
    private String sessionNo;
    private String courseNo;
    private String courseTitle;
    private Long startDate;
    private Long endDate;
    private Address address;
    private String type;

    public RegistrationDetails(String sessionNo, String courseNo, String courseTitle, Long startDate, Long endDate, Address address, String type) {
        this.sessionNo = sessionNo;
        this.courseNo = courseNo;
        this.courseTitle = courseTitle;
        this.startDate = startDate;
        this.endDate = endDate;
        this.address = address;
        this.type = type;
    }


    public String getSessionNo() {
        return sessionNo;
    }

    public String getCourseNo() {
        return courseNo;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public Long getStartDate() {
        return startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public Address getAddress() {
        return address;
    }

    public String getType() {
        return type;
    }
}
