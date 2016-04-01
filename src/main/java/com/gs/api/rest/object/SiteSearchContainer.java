package com.gs.api.rest.object;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SiteSearchContainer {

    @JsonProperty("grouped")
    private SiteSearchGrouped grouped;

    public SiteSearchGrouped getGrouped() {
        return grouped;
    }


    public void setGrouped(SiteSearchGrouped grouped) {
        this.grouped = grouped;
    }


}
