package com.gs.api.service;

import java.util.ArrayList;
import java.util.List;

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
import com.gs.api.rest.object.CourseSearchContainer;
import com.gs.api.rest.object.CourseSearchDoc;

@Service
public class CourseSearchServiceImpl implements CourseSearchService {

    private static final Logger logger = LoggerFactory.getLogger(CourseSearchServiceImpl.class);
    
    @Value("${course.search.solr.endpoint}")
    private String courseSearchSolrEndpoint;
    
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
    public CourseSearchResponse searchCourses(String search, int start, int numRequested) {

        boolean exactMatch = false;
        int numFound = 0;
        
        //get search string
        final String searchString = buildSearchString(courseSearchSolrEndpoint, search);
        logger.info(searchString);

        //create request header contain basic auth credentials
        byte[] plainCredsBytes = solrCredentials.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);
        HttpEntity<String> request = new HttpEntity<String>(headers);
        
        //perform search
        ResponseEntity<CourseSearchContainer> responseEntity = restTemplate.exchange(searchString, HttpMethod.GET, 
                request, CourseSearchContainer.class);
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
                Course newCourse = new Course(doc.getCourse_id(), doc.getCourse_name(),
                                doc.getCourse_description());
                courses.add(newCourse);
                //if the course id returned is exactly the same as the search string, this is 
                //  almost certainly an exact match
                if (doc.getCourse_id().equalsIgnoreCase(search)) {
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
        return response;
    }

    /**
     * Break apart each work (separated by spaces) in the search string and format into
     * the proper SOLR search format for multiple words.  Example: *Word1* AND *Word2*
     */
    @Override
    public String buildSearchString(String endpoint, String search) {
        final String[] searchTerm = StringUtils.split(stripAndEncode(search), " ");
        final StringBuffer searchTerms = new StringBuffer(StringUtils.join(searchTerm, "* AND *"));
        searchTerms.insert(0, "*").append("*");
        
        StringBuffer solrEndpoint = new StringBuffer(endpoint.replace("{search}", searchTerms));
        return solrEndpoint.toString();
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
