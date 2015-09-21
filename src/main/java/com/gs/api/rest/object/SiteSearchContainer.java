package com.gs.api.rest.object;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SiteSearchContainer {

    @JsonProperty("response")
    private SiteSearchResponses siteSearchresponse;

    public SiteSearchResponses getResponse() {
        return siteSearchresponse;
    }

    public void setResponse(SiteSearchResponses siteSearchresponse) {
        this.siteSearchresponse = siteSearchresponse;
    }



}
