package com.gs.api.controller;

import com.gs.api.domain.registration.Timezone;
import com.gs.api.service.CommonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Configuration
@EnableAsync
@RestController
@RequestMapping("/common")
public class CommonController extends BaseController {

    final Logger logger = LoggerFactory.getLogger(CommonController.class);

    @Autowired
    private CommonService commonService;

    @RequestMapping(value = "/timezones", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Timezone> getTimezones() throws Exception {

        logger.debug("Get timezones");

        return commonService.getTimezones();
    }
}
