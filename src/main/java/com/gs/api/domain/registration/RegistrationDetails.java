package com.gs.api.domain.registration;

import java.util.Date;

/**
 * Registration details object
 */
public class RegistrationDetails {
    private String sessionId;
    private Date startDate;
    private Date endDate;
    private String location;
    private String type;

    public RegistrationDetails(String sessionId, Date startDate, Date endDate, String location, String type){
        this.sessionId = sessionId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.location = location;
        this.type = type;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getLocation() {
        return location;
    }

    public String getType() {
        return type;
    }
}
