package com.gs.api.rest.object;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SiteSearchContainer {

    @JsonProperty("response")
    private SiteSearchResponse response;

    public SiteSearchResponse getResponse() {
        return response;
    }

    public void setResponse(SiteSearchResponse response) {
        this.response = response;
    }


}
