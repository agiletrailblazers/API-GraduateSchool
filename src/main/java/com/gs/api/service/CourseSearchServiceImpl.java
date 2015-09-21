package com.gs.api.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.client.RestOperations;

import com.gs.api.domain.Course;
import com.gs.api.domain.CourseSearchResponse;
import com.gs.api.exception.NotFoundException;
import com.gs.api.rest.object.CourseSearchContainer;
import com.gs.api.rest.object.CourseSearchDoc;
import com.gs.api.rest.object.CourseSearchGroup;

@Service
public class CourseSearchServiceImpl implements CourseSearchService {

    private static final Logger logger = LoggerFactory.getLogger(CourseSearchServiceImpl.class);

    @Value("${course.search.solr.endpoint}")
    private String courseSearchSolrEndpoint;

    @Value("${course.search.solr.query}")
    private String courseSearchSolrQuery;

    @Value("${course.search.solr.credentials}")
    private String solrCredentials;

    @Value("#{'${course.search.facet.location.exclude}'.split(';')}")
    private String[] locationFacetExclude;
    
    @Autowired(required = true)
    private RestOperations restTemplate;

    /**
     * Perform a search for courses
     *
     * @param request
     * @return SearchResponse
     */
    public CourseSearchResponse searchCourses(String search, int currentPage, int numRequested, String[] filter)
            throws NotFoundException {

        boolean exactMatch = false;
        int numFound = 0;
        int pageSize = 0;
        StringBuffer groupFacetParamString = new StringBuffer();
        if (null != filter) {
            for (String groupFacetParam : filter) {
                groupFacetParam = StringUtils.replace(groupFacetParam,":",":\"");
                groupFacetParamString.append("&fq=").append(groupFacetParam).append("\"");
            }
        }
        // get search string
        String searchString = buildSearchString(search, currentPage, numRequested, groupFacetParamString.toString());

        // create request header contain basic auth credentials
        byte[] plainCredsBytes = solrCredentials.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);
        HttpEntity<String> request = new HttpEntity<String>(headers);

        // due to a quirk in rest template these facet filters need to be injected as params
        Map<String, String> uriParams = new HashMap<String, String>();
        uriParams.put("facet-exclude", "{!ex=dt}");
        uriParams.put("facet-countall", "{!tag=dt}");
        logger.info(searchString);
        
        // perform search
        ResponseEntity<CourseSearchContainer> responseEntity = null;
        try {
           responseEntity = restTemplate.exchange(searchString, HttpMethod.POST, request, CourseSearchContainer.class, uriParams);
        } catch (Exception e) {
            logger.error("Failed to get search results from SOLR", e);
            throw new NotFoundException("No search results found");
        }
        CourseSearchContainer container = responseEntity.getBody();

        // get docs from withing the grouped response
        CourseSearchGroup group = container.getGrouped().getGroup();
        Collection<CourseSearchDoc> docs = container.getGrouped().getGroup().getDoclist().getDocs();
        // log results
        if (CollectionUtils.isNotEmpty(docs)) {
            numFound = group.getNgroups();
            pageSize = docs.size();
        }
        logger.info("Found " + numFound + " matches for search " + search + " page size " + pageSize);

        // loop through responses
        CourseSearchResponse response = new CourseSearchResponse();
        List<Course> courses = new ArrayList<Course>();
        if (CollectionUtils.isNotEmpty(docs)) {
            for (CourseSearchDoc doc : docs) {
                String courseId = doc.getCourse_id();
                Course newCourse = new Course(courseId, doc.getCourse_code(), doc.getCourse_name(),
                        doc.getCourse_description());
                courses.add(newCourse);

                // if the course id returned is exactly the same as the search
                // string, or the search
                // string is contained in the course id then this is almost
                // and search string has something in it
                // certainly an exact match
                if (numFound == 1 && StringUtils.containsIgnoreCase(courseId, search) 
                        && StringUtils.length(search) > 0) {
                    exactMatch = true;
                }
            }
            response.setCourses(courses.toArray(new Course[courses.size()]));
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
        response.setExactMatch(exactMatch);
        // Add a set facets (create method to populate facets, take response and
        // iterate through... build and populate.
        if (null != container.getRestFacetCount()) {
            response.setLocationFacets(cleanLocationFacetMap(
                    arrayToMap(container.getRestFacetCount().getRestFacetFields().getCityState())));
            response.setStatusFacets(
                    arrayToMap(container.getRestFacetCount().getRestFacetFields().getStatus()));
        }
        return response;
    }

    /**
     * Clean up the location facet map
     * @param map
     * @param facet
     * @return Map
     */
    private Map<String, Integer> cleanLocationFacetMap(Map<String, Integer> map) {
        Map<String, Integer> out = new HashMap<String, Integer>();
        for (String key : map.keySet()) {
            if (!Arrays.asList(locationFacetExclude).contains(key)
                    && map.get(key) > 0) {
                out.put(key, map.get(key));
            }
        }
        return out;
    }

    /**
     * Convert a array to a map so every even element is the key and odd element
     * is the value.
     * 
     * @param list
     * @return Map
     */
    public Map<String, Integer> arrayToMap(List<String> list) {
        Map<String, Integer> locations = new HashMap<>();
        if (list != null) {
            for (int i = 0; i < list.size(); i = i + 2) {
                locations.put(String.valueOf(list.get(i)), Integer.valueOf(list.get(i + 1)));
            }
        }
        return locations;
    }

    /**
     * Break apart each work (separated by spaces) in the search string and
     * format into the proper SOLR search format for multiple words. Example:
     * *Word1* AND *Word2*
     */
    @Override
    public String buildSearchString(String search, int currentPage, int numRequested, String filter) {

        String solrQuery = courseSearchSolrEndpoint.concat(courseSearchSolrQuery);

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

}
