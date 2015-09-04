package com.gs.api.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.gs.api.rest.object.CourseSearchFacetFields;

import java.util.HashMap;

// Kai create HashMap object that has city state.
@JsonInclude(Include.ALWAYS)
public class CourseSearchResponse {

    private int start = 1;
    private int numFound = 0;
    private int startNext = -1;
    private boolean exactMatch = false;
    private Course[] courses;
    private Facet[] facets;
//    Create facet object that will contain map for city state.

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

    public Facet[] getFacets() {
        return facets;
    }

    public void setFacets(Facet[] facets) {
        this.facets = facets;
    }
}
