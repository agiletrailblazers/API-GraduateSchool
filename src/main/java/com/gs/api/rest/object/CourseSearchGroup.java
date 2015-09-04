package com.gs.api.rest.object;

public class CourseSearchGroup {

    private int matches;
    private int ngroups;
    private CourseSearchDocList doclist;

    public CourseSearchDocList getDoclist() {
        return doclist;
    }

    public void setDoclist(CourseSearchDocList doclist) {
        this.doclist = doclist;
    }

    public int getMatches() {
        return matches;
    }

    public void setMatches(int matches) {
        this.matches = matches;
    }

    public int getNgroups() {
        return ngroups;
    }

    public void setNgroups(int ngroups) {
        this.ngroups = ngroups;
    }
    
}
