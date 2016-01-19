package com.gs.api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gs.api.dao.LocationDAO;
import com.gs.api.domain.course.Location;

@Service
public class LocationServiceImpl implements LocationService {
    
    @Autowired
    private LocationDAO locationDAO;
    
    /*
     * (non-Javadoc)
     * @see com.gs.api.service.LocationService#getLocations()
     */
    @Override
    public List<Location> getLocations() throws Exception {
        return locationDAO.getLocationByCityState();
    }

}
