package com.gs.api.service;

import java.util.ArrayList;
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

    @Autowired(required = true)
    private RestOperations restTemplate;

    /**
     * Perform a search for courses
     *
     * @param request
     * @return SearchResponse
     */
    public CourseSearchResponse searchCourses(String search, int start, int numRequested, String[] filter)
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
        String searchString = buildSearchString(search, start, numRequested, groupFacetParamString.toString());
        logger.info(searchString);

        // create request header contain basic auth credentials
        byte[] plainCredsBytes = solrCredentials.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);
        HttpEntity<String> request = new HttpEntity<String>(headers);

        // perform search
        ResponseEntity<CourseSearchContainer> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange(searchString, HttpMethod.GET, request, CourseSearchContainer.class);
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
                // certainly an exact match
                if (numFound == 1 && StringUtils.containsIgnoreCase(courseId, search)) {
                    exactMatch = true;
                }
            }
            response.setCourses(courses.toArray(new Course[courses.size()]));
        }
        response.setStart(group.getDoclist().getStart());
        response.setPageSize(pageSize);
        response.setNumFound(numFound);
        response.setNumRequested(numRequested);
        int nextStart = start + numRequested;
        if (nextStart <= numFound) {
            response.setStartNext(nextStart);
        }
        if (pageSize > 0) {
            response.setTotalPages((int) Math.ceil((double) numFound / pageSize));
        }
        response.setExactMatch(exactMatch);
        // Add a set facets (create method to populate facets, take response and
        // iterate through... build and populate.
        if (null != container.getRestFacetCount()) {
            response.setLocationFacets(arrayToMap(container.getRestFacetCount().getRestFacetFields().getCityState()));
            response.setStatusFacets(arrayToMap(container.getRestFacetCount().getRestFacetFields().getStatus()));
        }
        return response;
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
    public String buildSearchString(String search, int start, int numRequested, String filter) {

        String solrQuery = courseSearchSolrEndpoint.concat(courseSearchSolrQuery);

        // build search criteria
        solrQuery = StringUtils.replace(solrQuery, "{search}", stripAndEncode(search));

        // update start and num requested
        solrQuery = StringUtils.replace(solrQuery, "{start}", Integer.toString(start));
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

}
