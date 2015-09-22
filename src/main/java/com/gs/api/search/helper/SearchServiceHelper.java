package com.gs.api.search.helper;

import org.apache.commons.lang3.StringUtils;
import org.jasypt.contrib.org.apache.commons.codec_1_3.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
public class SearchServiceHelper {
    private static final Logger logger = LoggerFactory.getLogger(SearchServiceHelper.class);

    @Value("${search.solr.credentials}")
    private String solrCredentials;

    @Value("${search.solr.endpoint}")
    private String searchSolrEndpoint;

    /**
     * Break apart each work (separated by spaces) in the search string and
     * format into the proper SOLR search format for multiple words. Example:
     * *Word1* AND *Word2*
    */
    public String buildSearchString(String searchSolrQuery,String search, int currentPage, int numRequested, String filter) {
        String solrQuery = searchSolrEndpoint.concat(searchSolrQuery);

        // build search criteria
        solrQuery = StringUtils.replace(solrQuery, "{search}", stripAndEncode(search));

        // update start and num requested
        solrQuery = StringUtils.replace(solrQuery, "{start}", Integer.toString((currentPage - 1) * numRequested));
        solrQuery = StringUtils.replace(solrQuery, "{numRequested}", Integer.toString(numRequested));
        solrQuery = StringUtils.replace(solrQuery, "{filter}", filter);
        return solrQuery;
    }

    /**
     * Strip characters considered invalid to SOLR. Encode other characters
     * which are supported by SOLR but need to be SOLR encoded (add "\" before
     * character).
     * <p>
     * SOLR Invalid: #, %, ^, & LR Encoded: + - || ! ( ) { } [ ] " ~ * ? : \
     * Useless: Remove AND or OR from search string as these only confuse the
     * situation
     *
     * @param search
     * @return string
     */
    public String stripAndEncode(String search) {
        String[] searchList = { "#", "%", "^", "&", "+", "-", "||", "!", "(", ")", "{", "}", "[", "]", "\"", "~", "*",
                "?", ":", "\\" };
        String[] replaceList = { "", "", "", "", "\\+", "\\-", "\\||", "\\!", "\\(", "\\)", "\\{", "\\}", "\\[", "\\]",
                "\\\"", "\\~", "\\*", "\\?", "\\:", "\\\\" };
        return StringUtils.replaceEach(search, searchList, replaceList);
    }

    /**
     * Generate the page range to display for navigation. Show always display only 5 pages max attempting
     * to keep the current page in the "middle" of the range.
     * For example:
     *   - if current page is 3 of 10 show: 1,2,3,4,5
     *   - if current page is 7 of 10 show: 5,6,7,8,9
     *   - if current page is 10 of 10 show: 6,7,8,9,10
     * @param currentPage
     * @param totalPages
     * @return int[]
     */
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

    /**
     * Create the Request Header for the Solr using the solr credentials
     */
    public  HttpEntity<String> createRequestHeader() {
        // create request header contain basic auth credentials
        byte[] plainCredsBytes = solrCredentials.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);
        HttpEntity<String> request = new HttpEntity<String>(headers);
        return request;
    }

}
