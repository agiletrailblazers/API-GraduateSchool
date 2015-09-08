package com.gs.api.service;

import java.util.*;

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

@Service
public class CourseSearchServiceImpl implements CourseSearchService {

    private static final Logger logger = LoggerFactory.getLogger(CourseSearchServiceImpl.class);
    
    @Value("${course.search.solr.endpoint}")
    private String courseSearchSolrEndpoint;
    
    @Value("${course.search.solr.query}")
    private String courseSearchSolrQuery;
    
    @Value("${course.search.solr.credentials}")
    private String solrCredentials;

    @Autowired(required=true)
    private RestOperations restTemplate;
    
    /**
     * Perform a search for courses
     * 
     * @param request
     * @return SearchResponse
     */
    public CourseSearchResponse searchCourses(String search, int start, int numRequested) throws NotFoundException {

        boolean exactMatch = false;
        int numFound = 0;
        
        //get search string
        final String searchString = buildSearchString(search, start, numRequested);
        logger.info(searchString);

        //create request header contain basic auth credentials
        byte[] plainCredsBytes = solrCredentials.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);
        HttpEntity<String> request = new HttpEntity<String>(headers);
        
        //perform search
        ResponseEntity<CourseSearchContainer> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange(searchString, HttpMethod.GET, 
                    request, CourseSearchContainer.class);
        } catch (Exception e) {
            logger.error("Failed to get search results from SOLR", e);
            throw new NotFoundException("No search results found");
        }
        CourseSearchContainer container = responseEntity.getBody();
        
        //log results
        if (CollectionUtils.isNotEmpty(container.getResponse().getDocs())) {
            numFound = container.getResponse().getDocs().size();
        }
        logger.info("Found " + numFound + " matches for search " + search);

        // loop through responses
        final CourseSearchResponse response = new CourseSearchResponse();
        List<Course> courses = new ArrayList<Course>();
        if (CollectionUtils.isNotEmpty(container.getResponse().getDocs())) {
            for (CourseSearchDoc doc : container.getResponse().getDocs()) {
                String courseId = doc.getCourse_id();
                Course newCourse = new Course(courseId, doc.getCourse_code(), 
                        doc.getCourse_name(), doc.getCourse_description());
                courses.add(newCourse);

                // if the course id returned is exactly the same as the search string, or the search
                // string is contained in the course id then this is almost certainly an exact match
                if (numFound == 1 && StringUtils.containsIgnoreCase(courseId, search)) {
                    exactMatch = true;
                }
            }
            response.setCourses(courses.toArray(new Course[courses.size()]));
        }
        response.setStart(container.getResponse().getStart());
        response.setNumFound(container.getResponse().getNumFound());
        int nextStart = start + numRequested; 
        if (nextStart <= container.getResponse().getNumFound()) {
            response.setStartNext(nextStart);
        }
        response.setExactMatch(exactMatch);
        // Add a set facets (create method to populate facets, take response and iterate through... build and populate.
        response.setFacets(arrayToMap(container.getRestFacetCount().getRestFacetFields().getCityState()));
//        System.out.println(container.getRestFacetCount().getRestFacetFields().getCityState());
//        Create the method
//        System.out.println(arrayToMap(container.getRestFacetCount().getRestFacetFields().getCityState()).keySet().toArray());
        return response;
    }

    /**
     *
     */
//    @Override
    public Map<String, String> arrayToMap(List list) {
        Map<String, String> locations = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            locations.put(String.valueOf(list.get(i)), String.valueOf(list.get(i + 1)));
        }
        System.out.println(locations.keySet());
        return locations;
    }


    /**
     * Break apart each work (separated by spaces) in the search string and format into
     * the proper SOLR search format for multiple words.  Example: *Word1* AND *Word2*
     */
    @Override
    public String buildSearchString(String search, int start, int numRequested) {
        
        String solrQuery = courseSearchSolrEndpoint.concat(courseSearchSolrQuery);
        
        // build search criteria
        solrQuery = StringUtils.replace(solrQuery, "{search}", stripAndEncode(search));
        
        // update start and num requested
        solrQuery = StringUtils.replace(solrQuery, "{start}", Integer.toString(start));
        solrQuery = StringUtils.replace(solrQuery, "{numRequested}", Integer.toString(numRequested));

        return solrQuery;
    }

    /**
     * Strip characters considered invalid to SOLR.  Encode other characters which are
     * supported by SOLR but need to be SOLR encoded (add "\" before character). 
     * 
     * SOLR Invalid: #, %, ^, &
     * SOLR Encoded: + - || ! ( ) { } [ ] " ~ * ? : \
     * Useless: Remove AND or OR from search string as these only confuse the situation
     * 
     * @param search
     * @return string
     */
    @Override
    public String stripAndEncode(String search) {
        String[] searchList = {"#", "%", "^", "&", "+", "-", "||", "!", "(", ")", "{", "}", "[", "]", "\"", "~", "*", "?", ":", "\\"};        
        String[] replaceList = {"", "", "", "", "\\+", "\\-", "\\||", "\\!", "\\(", "\\)", "\\{", "\\}", "\\[", "\\]", "\\\"", "\\~", "\\*", "\\?", "\\:", "\\\\"};
        return StringUtils.replaceEach(search, searchList, replaceList);
    }

}
