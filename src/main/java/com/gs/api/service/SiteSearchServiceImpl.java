package com.gs.api.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;

import com.gs.api.domain.Page;
import com.gs.api.domain.SitePagesSearchResponse;
import com.gs.api.rest.object.SiteSearchContainer;
import com.gs.api.rest.object.SiteSearchGroup;
import com.gs.api.rest.object.SiteSearchDoc;
import com.gs.api.search.util.HttpRequestBuilder;
import com.gs.api.search.util.NavRangeBuilder;
import com.gs.api.search.util.SearchUrlBuilder;
import com.gs.api.search.util.TitleBuilder;

@Service
public class SiteSearchServiceImpl implements SiteSearchService {

    private static final Logger logger = LoggerFactory.getLogger(SiteSearchServiceImpl.class);

    @Value("${site.search.solr.query}")
    private String siteSearchSolrQuery;

    @Autowired(required = true)
    private RestOperations restTemplate;

    @Autowired
    private SearchUrlBuilder searchServiceHelper;

    @Autowired
    private HttpRequestBuilder httpRequestBuilder;

    @Autowired
    private NavRangeBuilder navRangeBuilder;

    @Autowired
    private TitleBuilder titleBuilder;
    
    /**
     * Perform a search for Site
     *
     * @param search the query for searching
     * @param currentPage what page of results am I on
     * @param numRequested how many results do I want
     * @param filter filter by these things
     *
     * @return SearchResponse
    */
    public SitePagesSearchResponse searchSite(String search, int currentPage, int numRequested, String filter)
            throws Exception {
        int numFound = 0;
        int pageSize = 0;
        String searchString = searchServiceHelper.build(siteSearchSolrQuery,search, currentPage, numRequested, new String[] {filter});
        logger.info(searchString);
        HttpEntity<String> request = httpRequestBuilder.createRequestHeader();
        ResponseEntity<SiteSearchContainer> responseEntity;

        try {
            responseEntity = restTemplate.exchange(searchString, HttpMethod.POST, request, SiteSearchContainer.class);

        } catch (Exception e) {
            logger.error("Failed to get search results from SOLR", e);
            throw new Exception("No search results found");
        }

        SiteSearchContainer container = responseEntity.getBody();
        // get docs from withing the grouped response
        SiteSearchGroup group = container.getGrouped().getGroup();
        Collection<SiteSearchDoc> docs = container.getGrouped().getGroup().getDoclist().getDocs();
        // log results
        if (CollectionUtils.isNotEmpty(docs)) {
            numFound = group.getNgroups();
            pageSize = docs.size();
        }
        logger.info("Found " + numFound + " matches for search " + search + " page size " + pageSize);
        // loop through responses
        // loop through responses
        SitePagesSearchResponse response = new SitePagesSearchResponse();
        List<Page> pages = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(docs)) {
            for (SiteSearchDoc doc : docs) {
                Page newPage = new Page(titleBuilder.parseTitle(doc.getTitle(),doc.getContent(), doc.getUrl()), doc.getUrl(),doc.getContent());
                pages.add(newPage);
            }
            response.setPages(pages.toArray(new Page[pages.size()]));
        }

        if (pageSize > 0) {
            response.setPreviousPage(currentPage-1);
            response.setCurrentPage(currentPage);
            response.setPageSize(pageSize);
            response.setNumFound(numFound);
            response.setNumRequested(numRequested);
            int totalPages = ((int) Math.ceil((double) numFound / numRequested));
            response.setTotalPages(totalPages);
            if (currentPage+1 <= totalPages) {
                response.setNextPage(currentPage+1);
            }
            response.setPageNavRange(navRangeBuilder.createNavRange(currentPage, totalPages));
        }
        return response;
    }

}
