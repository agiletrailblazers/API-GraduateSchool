package com.gs.api.rest.object;

public class SiteSearchGroup {

    private int matches;
    private int ngroups;
    private SiteSearchDocList doclist;

    public SiteSearchDocList getDoclist() {
        return doclist;
    }

    public void setDoclist(SiteSearchDocList doclist) {
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
