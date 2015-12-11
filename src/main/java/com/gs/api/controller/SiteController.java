package com.gs.api.controller;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.gs.api.domain.SitePagesSearchResponse;
import com.gs.api.service.SiteSearchService;

@Configuration
@RestController
@RequestMapping("/site")
public class SiteController extends BaseController {

    static final Logger logger = LoggerFactory.getLogger(SiteController.class);

    @Autowired
    private SiteSearchService siteSearchService;

    @Value("${course.search.page.size}")
    private int searchPageSize;

    /**
     * Search site by keyword
     * @param search what is my keyword search
     * @param page what page of results am I on
     * @param numRequested number of results requested
     * @return Site Search Response
     * @throws Exception
     */
    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody SitePagesSearchResponse searchSite(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String numRequested,
            @RequestParam(required = false) String filter) throws Exception {
        logger.info("Site Search API initiated");
        return siteSearchService.searchSite(search,
                NumberUtils.toInt(page, 1),
                NumberUtils.toInt(numRequested, searchPageSize),
                filter);
    }

}
