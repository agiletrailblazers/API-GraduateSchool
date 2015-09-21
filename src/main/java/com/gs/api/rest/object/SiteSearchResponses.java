package com.gs.api.rest.object;

import java.util.Collection;

public class SiteSearchResponses {

    private int numFound;
    private int start;
    private Collection<SiteSearchDoc> docs;

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
    public Collection<SiteSearchDoc> getDocs() {
        return docs;
    }
    public void setDocs(Collection<SiteSearchDoc> docs) {
        this.docs = docs;
    }

    
}
