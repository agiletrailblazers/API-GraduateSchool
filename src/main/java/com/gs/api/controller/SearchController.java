package com.gs.api.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.gs.api.domain.CourseSearchResponse;
import com.gs.api.service.CourseSearchService;

@Configuration
@RestController
public class SearchController {

	private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

	@Autowired
	private CourseSearchService courseSearchService;

	/**
	 * A simple "is alive" API.
	 * @return Empty response with HttpStatus of OK
	 * @throws Exception
	 */
	@RequestMapping(value = "/v1/ping", method = RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HttpStatus> ping() throws Exception {
		logger.debug("Service /ping initiated");
		return new ResponseEntity<HttpStatus>(HttpStatus.OK);
	}
	
	/**
	 * Given search criteria for a course return the results.
	 * @return SearchResponse
	 * @throws Exception
	 */
	@RequestMapping(value = "/v1/course", method = RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody CourseSearchResponse searchCourse(@RequestParam String search) throws Exception {

		logger.debug("initiated with search param of: " + search);
		
		if (StringUtils.isEmpty(search)) {
			logger.error("Search string not provided");
			throw new Exception("Search string not provided"); 
		}
		
		return courseSearchService.searchCourses(search);
	}

	/**
	 * Internal server error response
	 * @return ResponseBody
	 */
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler({ Exception.class })
	public @ResponseBody String handleException(Exception ex) {
		// method called when a security exception occurs
		logger.error(ex.getMessage());
		return "{\"code\":\"500\", \"userMessage\":\"It blew up\"}";
	}

	/**
	 * Bad Request response
	 * @return ResponseBody
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler({ HttpMessageNotReadableException.class })
	public @ResponseBody String handleValidationException(HttpMessageNotReadableException ex) throws JsonGenerationException, JsonMappingException, IOException {
		// method called when a input validation failure occurs
		return "{\"code\":\"400\",  \"systemMessage\": \"Invalid Request \"}";
	}

}
