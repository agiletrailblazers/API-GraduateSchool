package com.gs.api.service;

import org.springframework.stereotype.Service;

import com.gs.api.domain.SiteSearchResponse;
import com.gs.api.exception.NotFoundException;

@Service
public class SiteSearchServiceImpl implements SiteSearchService {

    /*
     * (non-Javadoc)
     * @see com.gs.api.service.SiteSearchService#searchSite(java.lang.String, int, int)
     */
    @Override
    public SiteSearchResponse searchSite(String search, int page, int numRequested) throws NotFoundException {
        return new SiteSearchResponse();
    }

}
