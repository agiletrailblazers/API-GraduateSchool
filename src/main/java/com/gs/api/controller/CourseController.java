package com.gs.api.controller;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.gs.api.domain.Course;
import com.gs.api.domain.CourseSearchResponse;
import com.gs.api.service.CourseDetailService;
import com.gs.api.service.CourseSearchService;

@Configuration
@RestController
public class CourseController {

    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);

    @Autowired
    private CourseSearchService courseSearchService;
    
    @Autowired
    private CourseDetailService courseDetailService;

    /**
     * A simple "is alive" API.
     * 
     * @return Empty response with HttpStatus of OK
     * @throws Exception
     */
    @RequestMapping(value = "/ping", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HttpStatus> ping() throws Exception {
        logger.debug("Service ping initiated");
        return new ResponseEntity<HttpStatus>(HttpStatus.OK);
    }

    /**
     * Given search criteria for a course return the results.
     * 
     * @return SearchResponse
     * @throws Exception
     */
    @RequestMapping(value = "/course", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CourseSearchResponse searchCourse(@RequestParam String search, 
            @RequestParam(required=false) String start, 
            @RequestParam(required=false) String numRequested) throws Exception {
        
        logger.debug("Course search initiated with search param of: " + search);

        if (StringUtils.isEmpty(search)) {
            logger.error("Search string not provided");
            throw new Exception("Search string not provided");
        }
        
        return courseSearchService.searchCourses(search, 
                NumberUtils.toInt(start, 0), 
                NumberUtils.toInt(numRequested, 100));
    }
    
    @RequestMapping(value = "/course/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Course getCourse(@PathVariable("id") String id) throws Exception {        
        logger.debug("Course details initiated with  course id: " + id);
        final Course course = courseDetailService.getCourse(id);
        if (null == course || StringUtils.isEmpty(course.getId())){
            logger.error("No course found for id {}", id);
            throw new Exception("No course found for course id " + id);
        }
        return course;
    }

    /**
     * Return json formatted error response for any internal server error
     * 
     * @return ResponseBody
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({ Exception.class })
    public @ResponseBody String handleException(Exception ex) {
        logger.error(ex.getMessage());
        final StringBuffer response = new StringBuffer();
        response.append("{\"message\":\"");
        response.append(ex.getMessage());
        response.append("\"}");
        return response.toString();
    }

    /**
     * Return json formatted error response for bad request
     * 
     * @return ResponseBody
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ HttpMessageNotReadableException.class })
    public @ResponseBody String handleValidationException(HttpMessageNotReadableException ex) throws IOException {
        // method called when a input validation failure occurs
        return "{\"message\": \"Invalid Request \"}";
    }

}
