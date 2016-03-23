package com.gs.api.domain.course;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.ALWAYS)
public class CourseSession {

    private String classNumber;
    private String segment;
    private Date startDate;
    private Date endDate;
    private String startTime;
    private String endTime;
    private String days;
    private int scheduleMaximum;
    private int scheduleAvailable;
    private int scheduleMinimum;
    private String status;
    private String notes;
    private double tuition;
    private Location location;
    private CourseInstructor instructor;
    private String offeringSessionId;
    private String courseId;
    private String curricumTitle;
    private String courseDomain;

    public String getCourseDomain() {
        return courseDomain;
    }

    public void setCourseDomain(String courseDomain) {
        this.courseDomain = courseDomain;
    }



    public String getCurricumTitle() {
        return curricumTitle;
    }

    public void setCurricumTitle(String curricumTitle) {
        this.curricumTitle = curricumTitle;
    }





    public String getClassNumber() {
        return classNumber;
    }

    public void setClassNumber(String classNumber) {
        this.classNumber = classNumber;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public int getScheduleMaximum() {
        return scheduleMaximum;
    }

    public void setScheduleMaximum(int scheduleMaximum) {
        this.scheduleMaximum = scheduleMaximum;
    }

    public int getScheduleAvailable() {
        return scheduleAvailable;
    }

    public void setScheduleAvailable(int scheduleAvailable) {
        this.scheduleAvailable = scheduleAvailable;
    }

    public int getScheduleMinimum() {
        return scheduleMinimum;
    }

    public void setScheduleMinimum(int scheduleMinimum) {
        this.scheduleMinimum = scheduleMinimum;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public double getTuition() {
        return tuition;
    }

    public void setTuition(double tuition) {
        this.tuition = tuition;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public CourseInstructor getInstructor() {
        return instructor;
    }

    public void setInstructor(CourseInstructor instructor) {
        this.instructor = instructor;
    }

    public String getOfferingSessionId() {
        return offeringSessionId;
    }

    public void setOfferingSessionId(String offeringSessionId) {
        this.offeringSessionId = offeringSessionId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }
}
