package com.gs.api.rest.object;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseSearchRestResponse {

    @JsonProperty("numFound")
    private int numFound;
    @JsonProperty("start")
    private int start;
    @JsonProperty("docs")
    private Collection<CourseSearchDoc> docs;

    
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

    public Collection<CourseSearchDoc> getDocs() {
        return docs;
    }

    public void setDocs(Collection<CourseSearchDoc> docs) {
        this.docs = docs;
    }
    
    
    
}
