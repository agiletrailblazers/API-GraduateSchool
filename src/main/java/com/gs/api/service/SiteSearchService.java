package com.gs.api.service;

import com.gs.api.domain.SitePagesSearchResponse;
import com.gs.api.exception.NotFoundException;

public interface SiteSearchService {

    public SitePagesSearchResponse searchSite(String search, int currentPage, int numRequested) throws NotFoundException;

}
