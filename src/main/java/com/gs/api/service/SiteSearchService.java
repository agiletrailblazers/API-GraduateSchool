package com.gs.api.service;

import com.gs.api.domain.SiteSearchResponse;
import com.gs.api.exception.NotFoundException;

public interface SiteSearchService {

    public SiteSearchResponse searchSite(String search, int page, int numRequested) throws NotFoundException;
    
}
