package com.gs.api.rest.object;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CourseSearchFacetFields {

    @JsonProperty("city_state")
    private List<String> cityState;
    @JsonProperty("status")
    private List<String> status;
    @JsonProperty("category_subject")
    private List<String> categorysubject;
    @JsonProperty("delivery_method")
    private List<String> deliveryMethod;

    public List<String> getCityState() {
        return cityState;
    }

    public void setCityState(List<String> cityState) {
        this.cityState = cityState;
    }

    public List<String> getStatus() {
        return status;
    }

    public void setStatus(List<String> status) {
        this.status = status;
    }

    public List<String> getCategorysubject() {
        return categorysubject;
    }

    public void setCategorysubject(List<String> categorysubject) {
        this.categorysubject = categorysubject;
    }

    public List<String> getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(List<String> deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }


}