package com.gs.api.service;

import org.springframework.stereotype.Service;

import com.gs.api.domain.Course;
import com.gs.api.domain.CourseSearchResponse;

@Service
public class CourseSearchServiceImpl implements CourseSearchService {

    /**
     * Perform a search for courses
     * 
     * @param request
     * @return SearchResponse
     */
    public CourseSearchResponse searchCourses(String search) {

        // TODO: implement calls to SOLR
        // stubbed response for now
        CourseSearchResponse response = new CourseSearchResponse();
        if (search.equals("WRIT7043D")) {
            response.setExactMatch(true);
            response.setCourses(new Course[] { new Course("WRIT7043D", "Plain Writing: It is the Law (Classroom-Day)",
                    "The Plain Writing Act of 2015 (October 13, 2010) requires the Federal government to...") });
        } else {
            response.setExactMatch(false);
            response.setCourses(new Course[] {
                    new Course("WRIT7043D", "Plain Writing: It is the Law (Classroom-Day)",
                            "The Plain Writing Act of 2010 (October 13, 2010) requires the Federal government to..."),
                    new Course("AUDT8036G", "Contract and Procurement Fraud (Classroom-Day)",
                            "The possibility of fraud in government procurement presents a constant risk. Learn to recognize the..."),
                    new Course("WRIT7901A", "Keys to Productive Correspondence and Email (Virtual Class)",
                            "Obtain the knowledge and skills necessary to consistently produce high-quality documents - including email ...") });
        }
        // end stubbing

        return response;
    }

}
