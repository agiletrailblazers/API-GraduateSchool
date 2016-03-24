package com.gs.api.controller;

import com.gs.api.domain.course.Course;
import com.gs.api.domain.course.CourseCategory;
import com.gs.api.domain.course.CourseSearchResponse;
import com.gs.api.domain.course.CourseSession;
import com.gs.api.exception.NotFoundException;
import com.gs.api.service.CategoryService;
import com.gs.api.service.CourseSearchService;
import com.gs.api.service.CourseService;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Configuration
@RestController
@RequestMapping("/courses")
public class CourseController extends BaseController {

    static final Logger logger = LoggerFactory.getLogger(CourseController.class);

    @Autowired
    private CourseSearchService courseSearchService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CategoryService categoryService;

    @Value("${course.search.page.size}")
    private int searchPageSize;
    
    /**
     * Given search criteria for a course return the results.
     *
     * @return SearchResponse
     * @throws Exception
     */
    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CourseSearchResponse searchCourse(
            @RequestParam Map<String,String> allRequestParams,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String numRequested,
            @RequestParam(required = false) String[] filter) throws Exception {
        logger.info("Course Search API initiated");

        if (!StringUtils.isEmpty(search) || allRequestParams.containsKey("search")) {
            //this is a course search
            return courseSearchService.searchCourses(search,
                    NumberUtils.toInt(page, 1),
                    NumberUtils.toInt(numRequested, searchPageSize),filter);
        }
        else {
            if (StringUtils.isNotEmpty(page) || StringUtils.isNoneEmpty(numRequested)) {
                logger.error("Parameter 'page' and 'numRequest' not supported with this request");
                throw new Exception("Parameter 'page' and 'numRequest' not supported with this request");
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
     * Get a course for the given courseId
     * @param id the course ID
     * @return Course
     * @throws Exception
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Course getCourse(@PathVariable("id") String id) throws Exception {
        logger.debug("Course details initiated with  course courseId: {}",id);
        final Course course = courseService.getCourse(id);
        if (null == course || StringUtils.isEmpty(course.getId())){
            logger.error("No course found for courseId {}", id);
            throw new NotFoundException("No course found for course courseId " + id);
        }
        return course;
    }

    /**
     * Get course sessions given a course courseId
     * @param id the course ID
     * @return List of Course Sessions
     * @throws Exception
     */
    @RequestMapping(value = "/{id}/sessions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<CourseSession> getSessionsByCourseId(@PathVariable("id") String id) throws Exception {
        logger.debug("Course sessions initiated with  course courseId: {}", id);
        final List<CourseSession> sessions = courseService.getSessionsByCourseId(id);
        if (CollectionUtils.isEmpty(sessions)){
            logger.error("No sessions found for courseId {}", id);
            throw new NotFoundException("No sessions found for course courseId " + id);
        }
        return sessions;
    }

    /**
     * Get course session given a course id and session id
     * @param sessionId the session ID
     * @return the course session
     * @throws Exception
     */
    @RequestMapping(value = "/session/{sessionId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CourseSession getSessionById(@PathVariable("sessionId") String sessionId) throws Exception {
        logger.debug("Get course session by session id {}", sessionId);
        final CourseSession session = courseService.getSessionById(sessionId);
        if (session == null) {
            String msg = String.format("No session found for session id %s", sessionId);
            logger.error(msg);
            throw new NotFoundException(msg);
        }
        return session;
    }

    /**
     * Get a list of categories containing a list of subjects
     *
     * @return Category List containing Subjects
     * @throws Exception
     */
    @RequestMapping(value = "/categories", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CourseCategory[] getCategories() throws Exception {
        logger.debug("Category/subject search initiated");
        return categoryService.getCategories();
    }


    @RequestMapping(value = "/sessions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<CourseSession> getSessions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sessiondomain) throws Exception {
        logger.debug("Course sessions initiated");
        final List<CourseSession> sessions = courseService.getSessions(status,sessiondomain);
        if (CollectionUtils.isEmpty(sessions)){
            logger.error("No sessions found");
            throw new NotFoundException("No sessions found for course");
        }
        return sessions;
    }

}
