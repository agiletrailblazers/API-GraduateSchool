package com.gs.api.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
        
        //perform search
        final String searchString = buildSearchString(courseSearchSolrEndpoint, search);
        logger.info(searchString);
        final CourseSearchContainer container = restTemplate.getForObject(searchString, 
                CourseSearchContainer.class);

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
                if (doc.getCourse_id().equals(search)) {
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

    public String buildSearchString(String endpoint, String search) {
        final String[] searchTerm = StringUtils.split(search, " ");
        StringBuffer searchTerms = new StringBuffer(StringUtils.join(searchTerm, "* AND *"));
        searchTerms.insert(0, "*").append("*");
        
        StringBuffer solrEndpoint = new StringBuffer(endpoint.replace("{search}", searchTerms));
        return solrEndpoint.toString();
    }

}
