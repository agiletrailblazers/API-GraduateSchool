package com.gs.api.domain;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.ALWAYS)
public class CourseSearchResponse {

    private int start = 1;
    private int pageSize = 0;
    private int numRequested = 0;
    private int numFound = 0;
    private int startNext = -1;
    private int totalPages = 0;
    private boolean exactMatch = false;
    private Course[] courses;
    private Map<String, Integer> locationfacets;

    public boolean isExactMatch() {
        return exactMatch;
    }

    public void setExactMatch(boolean exactMatch) {
        this.exactMatch = exactMatch;
    }

    public Course[] getCourses() {
        return courses;
    }

    public void setCourses(Course[] courses) {
        this.courses = courses;
    }

    public int getNumFound() {
        return numFound;
    }

    public void setNumFound(int numFound) {
        this.numFound = numFound;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getStartNext() {
        return startNext;
    }

    public void setStartNext(int startNext) {
        this.startNext = startNext;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getNumRequested() {
        return numRequested;
    }

    public void setNumRequested(int numRequested) {
        this.numRequested = numRequested;
    }

    public Map<String, Integer> getLocationFacets() {
        return locationfacets;
    }

    public void setLocationFacets(Map<String, Integer> facets) {
        this.locationfacets = facets;
    }
}
