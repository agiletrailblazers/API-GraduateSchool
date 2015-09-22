package com.gs.api.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.gs.api.domain.Page;
import com.gs.api.search.helper.SearchServiceHelper;
import org.apache.commons.collections.CollectionUtils;
import org.jasypt.contrib.org.apache.commons.codec_1_3.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import com.gs.api.domain.SitePagesSearchResponse;
import com.gs.api.exception.NotFoundException;
import com.gs.api.rest.object.SiteSearchContainer;
import com.gs.api.rest.object.SiteSearchDoc;



@Service
public class SiteSearchServiceImpl implements SiteSearchService {

    private static final Logger logger = LoggerFactory.getLogger(SiteSearchServiceImpl.class);

    @Value("${site.search.solr.query}")
    private String siteSearchSolrQuery;

    @Value("${course.search.solr.credentials}")
    private String solrCredentials;

    @Autowired(required = true)
    private RestOperations restTemplate;

    @Autowired
    private SearchServiceHelper searchServiceHelper;

    /**
     * Perform a search for Site
     *
     * @param request
     * @return SearchResponse
     */

    public SitePagesSearchResponse searchSite(String search, int currentPage, int numRequested)
            throws NotFoundException {
        int numFound = 0;
        int pageSize = 0;
        String searchString = searchServiceHelper.buildSearchString(siteSearchSolrQuery,search, currentPage, numRequested,"");
            // create request header contain basic auth credentials
        byte[] plainCredsBytes = solrCredentials.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);
        HttpEntity<String> request = new HttpEntity<String>(headers);
        logger.info(searchString);
        ResponseEntity<SiteSearchContainer> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange(searchString, HttpMethod.POST, request, SiteSearchContainer.class);

        } catch (Exception e) {
            logger.error("Failed to get search results from SOLR", e);
            throw new NotFoundException("No search results found");
        }
        SiteSearchContainer container = responseEntity.getBody();
        Collection<SiteSearchDoc> docs  = container.getResponse().getDocs();
        if (CollectionUtils.isNotEmpty(docs)) {
            numFound =  container.getResponse().getNumFound();
            pageSize = docs.size();
        }
        // loop through responses
        SitePagesSearchResponse response = new SitePagesSearchResponse();
        List<Page> pages = new ArrayList<Page>();
        if (CollectionUtils.isNotEmpty(docs)) {
            for (SiteSearchDoc doc : docs) {
                Page newPage = new Page(doc.getTitle(), doc.getUrl(),doc.getContent());
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
            response.setPageNavRange(searchServiceHelper.createNavRange(currentPage, totalPages));
        }
        return response;
    }

}
