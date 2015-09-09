package com.gs.api.rest.object;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.*;


/**
 * Created by kaiprout on 9/4/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseSearchRestFacetCount {
    @JsonProperty("facet_fields")
    private CourseSearchFacetFields restFacetFields;

    public CourseSearchFacetFields getRestFacetFields() {
        return restFacetFields;
    }

    public void setCourseRestFacetFields(CourseSearchFacetFields restFacetFields) {
        this.restFacetFields = restFacetFields;
    }
}
