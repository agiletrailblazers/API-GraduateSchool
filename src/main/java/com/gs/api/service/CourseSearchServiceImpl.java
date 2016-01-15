package com.gs.api.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;

import com.gs.api.domain.course.Course;
import com.gs.api.domain.course.CourseDescription;
import com.gs.api.domain.course.CourseSearchResponse;
import com.gs.api.rest.object.CourseSearchContainer;
import com.gs.api.rest.object.CourseSearchDoc;
import com.gs.api.rest.object.CourseSearchGroup;
import com.gs.api.search.util.FacetBuilder;
import com.gs.api.search.util.HttpRequestBuilder;
import com.gs.api.search.util.NavRangeBuilder;
import com.gs.api.search.util.SearchSortBuilder;
import com.gs.api.search.util.SearchUrlBuilder;

@Service
public class CourseSearchServiceImpl implements CourseSearchService {

    private static final Logger logger = LoggerFactory.getLogger(CourseSearchServiceImpl.class);

    @Value("${course.search.solr.query}")
    private String courseSearchSolrQuery;

    @Autowired
    private SearchUrlBuilder searchUrlBuilder;

    @Autowired
    private HttpRequestBuilder httpRequestBuilder;

    @Autowired
    private NavRangeBuilder navRangeBuilder;

    @Autowired
    private SearchSortBuilder searchSortBuilder;
    
    @Autowired
    private FacetBuilder facetBuilder;

    @Autowired(required = true)
    private RestOperations restTemplate;

    /**
     * Perform a search for courses
     *
     * @param search Solr Search string
     * @param currentPage what page of the data I am on
     * @param numRequested how many results did I request
     * @param filter filter by these things
     *
     * @return SearchResponse
     */
    public CourseSearchResponse searchCourses(String search, int currentPage, int numRequested, String[] filter)
            throws Exception {

        boolean exactMatch = false;
        int numFound = 0;
        int pageSize = 0;

        // get search string
        String searchString = searchUrlBuilder.build(courseSearchSolrQuery, search, currentPage,
                numRequested, filter);
        searchString = searchSortBuilder.build(searchString, StringUtils.isNotBlank(search), true);
        // create request header contain basic auth credentials
        HttpEntity<String> request = httpRequestBuilder.createRequestHeader();
        // due to a quirk in rest template these facet filters need to be injected as params
        Map<String, String> uriParams = new HashMap<>();
        uriParams.put("facet-exclude", "{!ex=dt}");
        uriParams.put("facet-countall", "{!tag=dt}");
        logger.info(searchString);
        // perform search
        ResponseEntity<CourseSearchContainer> responseEntity;
        try {
           responseEntity = restTemplate.exchange(searchString, HttpMethod.POST, request, CourseSearchContainer.class, uriParams);
        } catch (Exception e) {
            logger.error("Failed to get search results from SOLR", e);
            throw new Exception("No search results found");
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
        List<Course> courses = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(docs)) {
            for (CourseSearchDoc doc : docs) {
                String courseId = doc.getCourse_id();
                Course newCourse = new Course();
                newCourse.setId(courseId);
                newCourse.setCode(doc.getCourse_code());
                newCourse.setTitle(doc.getCourse_name());
                CourseDescription description = new CourseDescription(doc.getCourse_description(), null);
                newCourse.setDescription(description);
                newCourse.setCategory(facetBuilder.removeInvalidEntryAndDups(doc.getCategory()));
                newCourse.setCategorySubject(facetBuilder.removeInvalidEntryAndDups(doc.getCategory_subject()));
                courses.add(newCourse);

                // if the course id returned is exactly the same as the search
                // string, or the search
                // string is contained in the course id then this is almost
                // and search string has something in it
                // certainly an exact match
                if (numFound == 1
                        && StringUtils.containsIgnoreCase(courseId, search)
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
            response.setPageNavRange(navRangeBuilder.createNavRange(currentPage, totalPages));
        }
        response.setExactMatch(exactMatch);
        // Add a set facets (create method to populate facets, take response and
        // iterate through... build and populate.
        if (null != container.getRestFacetCount()) {
            Map<String,Integer> locationMap = arrayToMap(container.getRestFacetCount().getRestFacetFields().getCityState());
            locationMap = facetBuilder.buildLocationFacets(locationMap);
            response.setLocationFacets(locationMap);
            response.setStatusFacets(
                    arrayToMap(container.getRestFacetCount().getRestFacetFields().getStatus()));
            if (null != container.getRestFacetCount().getRestFacetFields().getCategorysubject()) {
                response.setCategorySubjectFacets(facetBuilder.buildCategorySubjectFacets(
                        container.getRestFacetCount().getRestFacetFields().getCategorysubject()));
            }
            if (null != container.getRestFacetCount().getRestFacetFields().getDeliveryMethod()) {
                response.setDilveryMethodFacets(
                        arrayToMap(container.getRestFacetCount().getRestFacetFields().getDeliveryMethod()));
            }
        }
        return response;
    }    

    /**
     * Convert a array to a map so every even element is the key and odd element
     * is the value.
     *
     * @param list convert this list to a map
     * @return Map
     */
    public Map<String, Integer> arrayToMap(List<String> list) {
        Map<String, Integer> locations = new HashMap<>();
        if (list != null) {
            for (int i = 0; i < list.size(); i = i + 2) {
                if (StringUtils.isNotEmpty(list.get(i + 1))) {
                    locations.put(String.valueOf(list.get(i)), Integer.valueOf(list.get(i + 1)));
                }
            }
        }
        return locations;
    }

}
