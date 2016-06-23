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
    private Address locationAddress;
    private Address facilityAddress;
    private String type;

    public RegistrationDetails(String sessionNo, String courseNo, String courseTitle, Long startDate, Long endDate, Address locationAddress, Address facilityAddress, String type) {
        this.sessionNo = sessionNo;
        this.courseNo = courseNo;
        this.courseTitle = courseTitle;
        this.startDate = startDate;
        this.endDate = endDate;
        this.locationAddress = locationAddress;
        this.facilityAddress = facilityAddress;
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

    public Address getLocationAddress() {
        return locationAddress;
    }

    public Address getFacilityAddress() {
        return facilityAddress;
    }

    public String getType() {
        return type;
    }
}
