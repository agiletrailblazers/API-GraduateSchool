package com.gs.api.domain;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.ALWAYS)
public class CourseSearchResponse {

    //page navigators
    private int currentPage = 0;
    private int totalPages = 0;
    private int nextPage = 0;
    private int previousPage = 0;
    private int[] pageNavRange;
    //results and page size
    private int pageSize = 0;
    private int numRequested = 0;
    private int numFound = 0;
    //other stuff
    private boolean exactMatch = false;
    private Course[] courses;
    private Map<String, Integer> locationFacets;
    private Map<String, Integer> statusFacets;

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
        return locationFacets;
    }

    public void setLocationFacets(Map<String, Integer> locationFacets) {
        this.locationFacets = locationFacets;
    }

    public Map<String, Integer> getStatusFacets() {
        return statusFacets;
    }

    public void setStatusFacets(Map<String, Integer> statusFacets) {
        this.statusFacets = statusFacets;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getNextPage() {
        return nextPage;
    }

    public void setNextPage(int nextPage) {
        this.nextPage = nextPage;
    }

    public int getPreviousPage() {
        return previousPage;
    }

    public void setPreviousPage(int previousPage) {
        this.previousPage = previousPage;
    }

    public int[] getPageNavRange() {
        return pageNavRange;
    }

    public void setPageNavRange(int[] pageNavRange) {
        this.pageNavRange = pageNavRange;
    }
    
}
