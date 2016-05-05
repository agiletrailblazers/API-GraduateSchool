package com.gs.api.domain.registration;

import java.util.Date;

/**
 * Registration details object
 */
public class RegistrationDetails {
    private String sessionNo;
    private String courseNo;
    private String courseTitle;
    private Date startDate;
    private Date endDate;
    private String city;
    private String state;
    private String type;

    public RegistrationDetails(String sessionNo, String courseNo, String courseTitle, Date startDate, Date endDate, String city, String state, String type) {
        this.sessionNo = sessionNo;
        this.courseNo = courseNo;
        this.courseTitle = courseTitle;
        this.startDate = startDate;
        this.endDate = endDate;
        this.city = city;
        this.state = state;
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

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getType() {
        return type;
    }

    public String getLocation(){
        return city + ", " + state;
    }
}
