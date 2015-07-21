package com.gs.api.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.gs.api.domain.Course;
import com.gs.api.domain.CourseSearchResponse;
import com.gs.api.rest.object.CourseSearchContainer;
import com.gs.api.rest.object.CourseSearchDoc;

@Service
public class CourseSearchServiceImpl implements CourseSearchService {

    private static final Logger logger = LoggerFactory.getLogger(CourseSearchServiceImpl.class);
    
    @Value("${course.search.solr.endpoint}")
    private String courseSearchSolrEndpoint;
    
    /**
     * Perform a search for courses
     * 
     * @param request
     * @return SearchResponse
     */
    public CourseSearchResponse searchCourses(String search) {

        boolean exactMatch = false;
        
        //perform search
        final RestTemplate restTemplate = new RestTemplate();
        final CourseSearchContainer container = restTemplate.getForObject(
                courseSearchSolrEndpoint.replace("~", search), 
                CourseSearchContainer.class);
        logger.info("Found " + container.getResponse().getDocs().size() + " matches for search " + search);
        
        // loop through responses
        final CourseSearchResponse response = new CourseSearchResponse();
        List<Course> courses = new ArrayList<Course>();
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
        response.setNumFound(container.getResponse().getDocs().size());
        response.setCourses(courses.toArray(new Course[courses.size()]));
        response.setExactMatch(exactMatch);
        return response;
    }

}
