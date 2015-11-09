package com.gs.api.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.gs.api.domain.Location;
import com.gs.api.service.LocationService;

@Configuration
@RestController
@RequestMapping("/locations")
public class LocationController extends BaseController {

    static final Logger logger = LoggerFactory.getLogger(LocationController.class);

    @Autowired
    private LocationService locationService;

    /**
     * Get a list of active locations
     *
     * @return SearchResponse
     * @throws Exception
     */
    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<Location> getLocations() throws Exception {
        logger.debug("Location search initiated");
        return locationService.getLocations();
    }

}
