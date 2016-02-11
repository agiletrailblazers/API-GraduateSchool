package com.gs.api.controller.registration;

import com.gs.api.controller.BaseController;
import com.gs.api.domain.registration.Registration;
import com.gs.api.service.registration.RegistrationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Configuration
@RestController
@RequestMapping("/registration")
public class RegistrationController extends BaseController {

    final Logger logger = LoggerFactory.getLogger(RegistrationController.class);

    @Autowired
    private RegistrationService registrationService;

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/user/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Registration> createRegistration(@PathVariable("id") String id, @RequestBody List<Registration> registrations) throws Exception  {

        logger.debug("{} is creating {} registration(s)", id, registrations.size());

        // TODO basic input validation

        // createUser the user
        registrationService.register(id, registrations);

        return registrations;
    }

}
