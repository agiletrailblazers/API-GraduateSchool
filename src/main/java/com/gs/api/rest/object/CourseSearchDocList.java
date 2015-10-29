package com.gs.api.rest.object;

import java.util.Collection;

public class CourseSearchDocList {

    private int numFound;
    private int start;
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
