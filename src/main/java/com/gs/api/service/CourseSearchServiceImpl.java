package com.gs.api.service;

import java.util.ArrayList;
import java.util.Arrays;
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

import com.gs.api.domain.Course;
import com.gs.api.domain.CourseCategory;
import com.gs.api.domain.CourseSearchResponse;
import com.gs.api.domain.CourseSubject;
import com.gs.api.exception.NotFoundException;
import com.gs.api.rest.object.CourseSearchContainer;
import com.gs.api.rest.object.CourseSearchDoc;
import com.gs.api.rest.object.CourseSearchGroup;
import com.gs.api.search.util.HttpRequestBuilder;
import com.gs.api.search.util.NavRangeBuilder;
import com.gs.api.search.util.SearchSortBuilder;
import com.gs.api.search.util.SearchUrlBuilder;

@Service
public class CourseSearchServiceImpl implements CourseSearchService {

    private static final Logger logger = LoggerFactory.getLogger(CourseSearchServiceImpl.class);

    @Value("${course.search.solr.query}")
    private String courseSearchSolrQuery;

    @Value("#{'${course.search.facet.location.exclude}'.split(';')}")
    private String[] locationFacetExclude;

    @Autowired
    private SearchUrlBuilder searchUrlBuilder;

    @Autowired
    private HttpRequestBuilder httpRequestBuilder;

    @Autowired
    private NavRangeBuilder navRangeBuilder;

    @Autowired
    private SearchSortBuilder searchSortBuilder;

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
            throws NotFoundException {

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
        List<Course> courses = new ArrayList<>();
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
            response.setLocationFacets(cleanLocationFacetMap(
                    arrayToMap(container.getRestFacetCount().getRestFacetFields().getCityState())));
            response.setStatusFacets(
                    arrayToMap(container.getRestFacetCount().getRestFacetFields().getStatus()));
            if (null != container.getRestFacetCount().getRestFacetFields().getCategorysubject()) {
                response.setCategorySubjectFacets(getCategorySubjectFacets(
                        container.getRestFacetCount().getRestFacetFields().getCategorysubject()));
            }
        }
        return response;
    }

    /**
     * To get the list of categories from the category subject filters list
     * @param categorySubjectFilter filter by this subject list
     * @return CourseCategory
     */
    private CourseCategory[] getCategorySubjectFacets(List<String> categorySubjectFilter) {
        List<CourseCategory> categories = new ArrayList<>();
        List<CourseSubject> subjects = new ArrayList<>();
        CourseCategory courseCategory = null;
        CourseSubject subject;
        for (int categorySubject = 0; categorySubject < categorySubjectFilter.size(); categorySubject = categorySubject + 2) {
            //split the category and subject by the forward slash
            String[] categorySubjectItem = StringUtils.split(String.valueOf(categorySubjectFilter.get(categorySubject)), "/");
            //only add subject if it has a category/subject description
            if (categorySubjectItem.length > 0) {
                int subjectCount = Integer.valueOf(categorySubjectFilter.get(categorySubject + 1));
                if (subjectCount > 0) {
                    //create a new subject if count is more than zero
                    subject = new CourseSubject(categorySubjectItem[1],
                        String.valueOf(categorySubjectFilter.get(categorySubject)),
                        subjectCount);
                }
                else {
                    subject = null;  //reset subject
                }
                if (null == courseCategory) {
                    //this will only happen the first time through
                    courseCategory = new CourseCategory();
                } else if (!categorySubjectItem[0].equals(courseCategory.getCategory())) {
                    //when every the category changes we need to "end" the current category and start a new one
                    if (subjects.size() > 0) {
                        courseCategory.setCourseSubject(subjects.toArray(new CourseSubject[subjects.size()]));
                        categories.add(courseCategory);
                    }
                    courseCategory = new CourseCategory();
                    subjects = new ArrayList<>();
                }
                courseCategory.setCategory(categorySubjectItem[0]);
                if (null != subject) {
                    subjects.add(subject);
                }
            }
        }
        if (null != courseCategory) {
            //this handles the last category on the list
            if (subjects.size() > 0) {
                courseCategory.setCourseSubject(subjects.toArray(new CourseSubject[subjects.size()]));
                categories.add(courseCategory);
            }
        }
        return categories.toArray(new CourseCategory[categories.size()]);
    }

    /**
     * Clean up the location facet map
     * @param map clean this in bound data
     * @return Map
     */
    private Map<String, Integer> cleanLocationFacetMap(Map<String, Integer> map) {
        Map<String, Integer> out = new HashMap<>();
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
     * @param list convert this list to a map
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



}
