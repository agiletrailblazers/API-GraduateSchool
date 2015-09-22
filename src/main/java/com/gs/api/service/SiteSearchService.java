package com.gs.api.service;

import com.gs.api.domain.SiteSearchResponse;
import com.gs.api.exception.NotFoundException;

public interface SiteSearchService {

    public SiteSearchResponse searchSite(String search, int currentPage, int numRequested) throws NotFoundException;
    public String stripAndEncode(String search);
    public String buildSearchString(String search, int start, int numRequested);
    public int[] createNavRange(int currentPage, int totalPages);
}
