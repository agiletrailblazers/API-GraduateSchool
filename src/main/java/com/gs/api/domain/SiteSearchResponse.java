package com.gs.api.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.ALWAYS)
public class SiteSearchResponse {

    //page navigators
    private int currentPage = 0;
    private int totalPages = 0;
    private int nextPage = 0;
    private int previousPage = 0;
    private int[] pageNavRange;
    //results and page size
    private int pageSize = 0;
    private int numRequested = 0;
    private int numFound = 0;
    //results
    private Page[] pages;

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getNextPage() {
        return nextPage;
    }

    public void setNextPage(int nextPage) {
        this.nextPage = nextPage;
    }

    public int getPreviousPage() {
        return previousPage;
    }

    public void setPreviousPage(int previousPage) {
        this.previousPage = previousPage;
    }

    public int[] getPageNavRange() {
        return pageNavRange;
    }

    public void setPageNavRange(int[] pageNavRange) {
        this.pageNavRange = pageNavRange;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getNumRequested() {
        return numRequested;
    }

    public void setNumRequested(int numRequested) {
        this.numRequested = numRequested;
    }

    public int getNumFound() {
        return numFound;
    }

    public void setNumFound(int numFound) {
        this.numFound = numFound;
    }

    public Page[] getPages() {
        return pages;
    }

    public void setPages(Page[] pages) {
        this.pages = pages;
    }
    
}
