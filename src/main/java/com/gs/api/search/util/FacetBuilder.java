package com.gs.api.search.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gs.api.domain.CourseCategory;
import com.gs.api.domain.CourseSubject;

@Service
public class FacetBuilder {

    @Value("#{'${course.search.facet.location.exclude}'.split(';')}")
    private String[] locationFacetExclude;
    
    /**
     * To get the list of categories from the category subject filters list
     * @param categorySubjectFilter filter by this subject list
     * @return CourseCategory
     */
    public CourseCategory[] buildCategorySubjectFacets(List<String> categorySubjectFilter) {
        List<CourseCategory> categories = new ArrayList<>();
        List<CourseSubject> subjects = new ArrayList<>();
        CourseCategory courseCategory = null;
        CourseSubject subject;
        for (int categorySubject = 0; categorySubject < categorySubjectFilter.size(); categorySubject = categorySubject + 2) {
            //split the category and subject by the forward slash
            String[] categorySubjectItem = StringUtils.split(String.valueOf(categorySubjectFilter.get(categorySubject)), "/");
            //only add subject if it has a category/subject description
            //exclude anything with a pipe (|) as this is an invalid facet from SOLR
            if (categorySubjectItem.length > 0 && !categorySubjectItem[1].contains("|")) {
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
    public Map<String, Integer> buildLocationFacets(Map<String, Integer> map) {
        Map<String, Integer> out = new HashMap<>();
        for (String key : map.keySet()) {
            if (!Arrays.asList(locationFacetExclude).contains(key)
                    && map.get(key) > 0) {
                out.put(key, map.get(key));
            }
        }
        return out;
    }
    
}
