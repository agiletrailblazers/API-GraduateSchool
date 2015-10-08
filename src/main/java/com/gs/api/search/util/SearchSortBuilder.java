package com.gs.api.search.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SearchSortBuilder {

    @Value("${course.search.sort.score}")
    private String sortScore;
    
    @Value("${course.search.sort.course_id}")
    private String sortCourseId;
    
    /**
     * Build proper sort criteria based on type of search
     * @param searchSolrQuery
     * @param includeScoreSort
     * @param includeCourseIdSort
     * @return String
     */
    public String build(String searchSolrQuery, boolean includeScoreSort, boolean includeCourseIdSort) {
        StringBuffer sort = new StringBuffer();
        if (includeScoreSort) {
            sort.append(sortScore);
            if (includeCourseIdSort) {
                sort.append(",").append(sortCourseId);
            }
        }
        else {
            sort.append(sortCourseId);
        }
        return StringUtils.replace(searchSolrQuery, "{sort}", sort.toString());
    }
    
}
