package com.gs.api.search.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SearchUrlBuilder {

    @Value("${search.solr.endpoint}")
    private String searchSolrEndpoint;

    @Value("${course.search.solr.blacklist.connectors}")
    private String[] courseSearchSolrBlacklistConnectors;

    @Value("${course.search.solr.blacklist.terms}")
    private String[] courseSearchSolrBlacklistTerms;

    /**
     * Break apart each work (separated by spaces) in the search string and
     * format into the proper SOLR search format for multiple words. Example:
     * *Word1* AND *Word2*
    */
    public String build(String searchSolrQuery,String search, int currentPage, int numRequested, String[] filter) {
        StringBuffer filterString = new StringBuffer();
        if (null != filter) {
            for (String filterParam : filter) {
                if (filterParam != null) {
                    filterParam = StringUtils.replace(filterParam,":",":\"");
                    filterString.append("&fq=").append(filterParam).append("\"");
                }
            }
        }

        String solrQuery = searchSolrEndpoint.concat(searchSolrQuery);

        //set the curentpage value to 1 when current page is zero
        if (currentPage < 1) {
            currentPage = 1;
        }
        // build search criteria
        solrQuery = StringUtils.replace(solrQuery, "{search}", stripAndEncode(search));
        solrQuery = StringUtils.replace(solrQuery, "{partial_search}", stripAndEncode(blacklistRemove(search)));

        // update start and num requested
        solrQuery = StringUtils.replace(solrQuery, "{start}", Integer.toString((currentPage - 1) * numRequested));
        solrQuery = StringUtils.replace(solrQuery, "{numRequested}", Integer.toString(numRequested));
        solrQuery = StringUtils.replace(solrQuery, "{filter}", filterString.toString());
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
     * @param search the solr search item
     * @return string
     */
    protected String stripAndEncode(String search) {
        String[] searchList = { "#", "%", "^", "&", "+", "-", "||", "!", "(", ")", "{", "}", "[", "]", "\"", "~", "*",
                "?", ":", "\\" };
        String[] replaceList = { "", "", "", "", "\\+", "\\-", "\\||", "\\!", "\\(", "\\)", "\\{", "\\}", "\\[", "\\]",
                "\\\"", "\\~", "\\*", "\\?", "\\:", "\\\\" };
        return StringUtils.replaceEach(search, searchList, replaceList);
    }

    /**
     * Remove words from the search string specified in the properties.
     *
     * This currently contains conntector words such as "of, and, in, the, to, a, for, by, with"
     * And generic terms such as "introduction, basic, intermediate, advanced"
     *
     * @param search - the string to perform the course search
     * @return - the cleaned search string
     */
    protected String blacklistRemove(String search) {

        //Replace the connector words
        for (int i = 0; i<courseSearchSolrBlacklistConnectors.length; i++) {
            search = StringUtils.replacePattern(search, "(?i)([\\s]+|\\b)" + courseSearchSolrBlacklistConnectors[i] + "([\\s]+|\\b)", StringUtils.SPACE);
        }

        //Replace the specified terms
        for (int i = 0; i<courseSearchSolrBlacklistTerms.length; i++) {
            search = StringUtils.replacePattern(search, "(?i)([\\s]+|\\b)" + courseSearchSolrBlacklistTerms[i] + "([\\s]+|\\b)", StringUtils.SPACE);
        }

        return search.trim();
    }

}
