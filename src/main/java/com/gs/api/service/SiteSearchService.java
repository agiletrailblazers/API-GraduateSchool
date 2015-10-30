package com.gs.api.service;

import com.gs.api.domain.SitePagesSearchResponse;
import com.gs.api.exception.NotFoundException;

public interface SiteSearchService {

    SitePagesSearchResponse searchSite(String search, int currentPage, int numRequested, String[] filter) throws NotFoundException;

}
