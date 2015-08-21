package com.gs.api.controller;

import java.io.IOException;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
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
import com.gs.api.domain.CourseSession;
import com.gs.api.domain.Location;
import com.gs.api.exception.NotFoundException;
import com.gs.api.service.CourseService;
import com.gs.api.service.CourseSearchService;
import com.gs.api.service.LocationService;

@Configuration
@RestController
public class CourseController {

    static final Logger logger = LoggerFactory.getLogger(CourseController.class);

    @Autowired
    private CourseSearchService courseSearchService;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private LocationService locationService;

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
    @RequestMapping(value = "/courses", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CourseSearchResponse searchCourse(@RequestParam(required=false) String search,
            @RequestParam(required=false) String start, 
            @RequestParam(required=false) String numRequested) throws Exception {
        
        logger.info("Course API initiated");

        if (!StringUtils.isEmpty(search)) {
            //this is a course search
            return courseSearchService.searchCourses(search, 
                    NumberUtils.toInt(start, 0), 
                    NumberUtils.toInt(numRequested, 100));
        }
        else {
            if (StringUtils.isNotEmpty(start) || StringUtils.isNoneEmpty(numRequested)) {
                logger.error("Parameter 'start' and 'numRequest' not supported with this request");
                throw new Exception("Parameter 'start' and 'numRequest' not supported with this request");
            }
            //this is a lookup of all courses
           List<Course> courses = courseService.getCourses();
           CourseSearchResponse response = new CourseSearchResponse();
           if (null != courses) {
               response.setCourses(courses.toArray(new Course[courses.size()]));
               response.setNumFound(courses.size());
           }
           return response;
        }
        
    }
    
    /**
     * Get a course for the given id
     * @param id
     * @return Course
     * @throws Exception
     */
    @RequestMapping(value = "/courses/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Course getCourse(@PathVariable("id") String id) throws Exception {        
        logger.debug("Course details initiated with  course id: " + id);
        final Course course = courseService.getCourse(id);
        if (null == course || StringUtils.isEmpty(course.getId())){
            logger.error("No course found for id {}", id);
            throw new NotFoundException("No course found for course id " + id);
        }
        return course;
    }
    
    /**
     * Get course sessions given a course id
     * @param id
     * @return List of Course Sessions
     * @throws Exception
     */
    @RequestMapping(value = "/courses/{id}/sessions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<CourseSession> getSessions(@PathVariable("id") String id) throws Exception {        
        logger.debug("Course sessions initiated with  course id: " + id);
        final List<CourseSession> sessions = courseService.getSessions(id);
        if (CollectionUtils.isEmpty(sessions)){
            logger.error("No sessions found for id {}", id);
            throw new NotFoundException("No sessions found for course id " + id);
        }
        return sessions;
    }
    
    /**
     * Get a list of active locations
     * 
     * @return SearchResponse
     * @throws Exception
     */
    @RequestMapping(value = "/locations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<Location> getLocations() throws Exception {
        logger.debug("Location search initiated");        
        return locationService.getLocations();
    }
    
    /**
     * Return json formatted error response for any custom "not found" errors
     * @return ResponseBody
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({ NotFoundException.class })
    @ResponseBody
    public String handleNotFoundException(Exception ex) {
        logger.error(ex.getMessage());
        final StringBuffer response = new StringBuffer();
        response.append("{\"message\":\"");
        response.append(ex.getMessage());
        response.append("\"}");
        return response.toString();
    }

    /**
     * Return json formatted error response for any internal server errors
     * @return ResponseBody
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({ Exception.class })
    @ResponseBody
    public String handleException(Exception ex) {
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
    @ResponseBody
    public String handleValidationException(HttpMessageNotReadableException ex) throws IOException {
        // method called when a input validation failure occurs
        return "{\"message\": \"Invalid Request \"}";
    }

}
