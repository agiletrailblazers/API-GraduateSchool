package com.gs.api.service;

import com.gs.api.domain.Site;
import com.gs.api.rest.object.SiteSearchContainer;
import com.gs.api.rest.object.SiteSearchDoc;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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

import com.gs.api.domain.SiteSearchResponse;
import com.gs.api.exception.NotFoundException;
import org.springframework.web.client.RestOperations;

import java.util.*;

@Service
public class SiteSearchServiceImpl implements SiteSearchService {

    /*
     * (non-Javadoc)
     * @see com.gs.api.service.SiteSearchService#searchSite(java.lang.String, int, int)
     */
    private static final Logger logger = LoggerFactory.getLogger(SiteSearchServiceImpl.class);

    @Value("${course.search.solr.endpoint}")
    private String courseSearchSolrEndpoint;

    @Value("${course.nutchsearch.solr.query}")
    private String courseSearchSolrQuery;

    @Value("${course.search.solr.credentials}")
    private String solrCredentials;


    @Autowired(required = true)
    private RestOperations restTemplate;
    public SiteSearchResponse searchSite(String search, int currentPage, int numRequested)
            throws NotFoundException {
        boolean exactMatch = false;
            int numFound = 0;
            int pageSize = 0;
                        // get search string
            String searchString = buildSearchString(search, currentPage, numRequested);
            // create request header contain basic auth credentials
            byte[] plainCredsBytes = solrCredentials.getBytes();
            byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
            String base64Creds = new String(base64CredsBytes);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + base64Creds);
            HttpEntity<String> request = new HttpEntity<String>(headers);

            // due to a quirk in rest template these facet filters need to be injected as params
            logger.info(searchString);

            // perform search
            ResponseEntity<SiteSearchContainer> responseEntity = null;
            try {
                responseEntity = restTemplate.exchange(searchString, HttpMethod.POST, request, SiteSearchContainer.class);

            } catch (Exception e) {
                logger.error("Failed to get search results from SOLR", e);
                throw new NotFoundException("No search results found");
            }
        SiteSearchContainer container = responseEntity.getBody();
        logger.info("Num Found" + container.getResponse().getNumFound());

        // get docs from withing the grouped response
        Collection<SiteSearchDoc> docs  = container.getResponse().getDocs();
        logger.info("documentSize" + docs.size());
        // log results
        if (CollectionUtils.isNotEmpty(docs)) {
            numFound = 25;
            pageSize = docs.size();
        }
            // loop through responses
        SiteSearchResponse response = new SiteSearchResponse();
            List<Site> sites = new ArrayList<Site>();
            if (CollectionUtils.isNotEmpty(docs)) {
                for (SiteSearchDoc doc : docs) {
                    Site newSite = new Site(doc.getId(), doc.getTitle(), doc.getUrl(),
                            doc.getContent());
                    sites.add(newSite);
                }
                response.setSites(sites.toArray(new Site[sites.size()]));
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
                response.setPageNavRange(createNavRange(currentPage, totalPages));
            }
         //   response.setExactMatch(exactMatch);
            // Add a set facets (create method to populate facets, take response and
            // iterate through... build and populate.
            return response;
        }

    public int[] createNavRange(int currentPage, int totalPages) {
        int[] pageNavRange = new int[(totalPages > 5) ? 5 : totalPages];
        if (totalPages > 5) {
            if (currentPage - 2 <= 0) {
                //begin range
                for (int i=0; i<5; i++) {
                    pageNavRange[i] = i+1;
                }
            }
            else if (currentPage + 2 >= totalPages) {
                //end range
                int j = totalPages - 4;
                for (int i=0; i<5; i++) {
                    pageNavRange[i] = j;
                    j++;
                }
            }
            else {
                //mid range
                int j = currentPage - 2;
                for (int i=0; i<5; i++) {
                    pageNavRange[i] = j;
                    j++;
                }
            }
        }
        else {
            //range is less than 5
            for (int i=0; i<totalPages; i++) {
                pageNavRange[i] = i+1;
            }
        }
        return pageNavRange;
    }


    @Override
    public String buildSearchString(String search, int currentPage, int numRequested) {
        String solrQuery = courseSearchSolrEndpoint.concat(courseSearchSolrQuery);

        // build search criteria
        solrQuery = StringUtils.replace(solrQuery, "{search}", stripAndEncode(search));

        // update start and num requested
        solrQuery = StringUtils.replace(solrQuery, "{start}", Integer.toString((currentPage - 1) * numRequested));
        solrQuery = StringUtils.replace(solrQuery, "{numRequested}", Integer.toString(numRequested));
        return solrQuery;
        }

/**
 * Strip characters considered invalid to SOLR. Encode other characters
 * which are supported by SOLR but need to be SOLR encoded (add "\" before
 * character).
 * <p>
 * SOLR Invalid: #, %, ^, & SOLR Encoded: + - || ! ( ) { } [ ] " ~ * ? : \
 * Useless: Remove AND or OR from search string as these only confuse the
 * situation
 *
 * @param search
 * @return string
 */
@Override
public String stripAndEncode(String search) {
        String[] searchList = { "#", "%", "^", "&", "+", "-", "||", "!", "(", ")", "{", "}", "[", "]", "\"", "~", "*",
        "?", ":", "\\" };
        String[] replaceList = { "", "", "", "", "\\+", "\\-", "\\||", "\\!", "\\(", "\\)", "\\{", "\\}", "\\[", "\\]",
        "\\\"", "\\~", "\\*", "\\?", "\\:", "\\\\" };
        return StringUtils.replaceEach(search, searchList, replaceList);
        }


        }
