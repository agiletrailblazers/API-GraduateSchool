package com.gs.api.service;

import java.util.List;

import com.gs.api.domain.Location;

public interface LocationService {

    /**
     * Get a list of locations - city and state
     * @return List of CourseLocation
     */
    List<Location> getLocations() throws Exception;

}