package com.gs.api.rest.object;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SiteSearchGrouped {
    
    @JsonProperty("alternate-groupby")
    private SiteSearchGroup group;

    public SiteSearchGroup getGroup() {
        return group;
    }

    public void setGroup(SiteSearchGroup group) {
        this.group = group;
    }

}
