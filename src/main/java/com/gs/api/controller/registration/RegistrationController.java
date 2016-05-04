package com.gs.api.controller.registration;

import com.gs.api.controller.BaseController;
import com.gs.api.domain.registration.Registration;
import com.gs.api.domain.registration.RegistrationDetails;
import com.gs.api.domain.registration.RegistrationRequest;
import com.gs.api.domain.registration.RegistrationResponse;
import com.gs.api.service.authentication.AuthenticationService;
import com.gs.api.service.registration.RegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableAsync
@RestController
@RequestMapping("/registrations")
public class RegistrationController extends BaseController {

    final Logger logger = LoggerFactory.getLogger(RegistrationController.class);

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private AuthenticationService authenticationService;

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/users/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public RegistrationResponse createRegistration(@PathVariable("id") String id, @RequestBody RegistrationRequest registrationRequest, HttpServletRequest request) throws Exception  {

        logger.debug("{} is creating {} registration(s)", id, registrationRequest.getRegistrations().size());

        // verify that the user making the request is the authenticated user
        authenticationService.verifyUser(request, id);

        return registrationService.register(id, registrationRequest);
    }

    /**
     * Get the registration object using a particular student ID and session ID
     *
     * @param userId
     * @param sessionId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/users/{uid}/sessions/{sessionId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Registration> getRegistration(@PathVariable("uid") String userId, @PathVariable("sessionId") String sessionId) throws Exception {

        logger.debug("Checking for duplicate registration for " + userId +
                " in session " + sessionId + ".");

        return registrationService.getRegistrationForSession(userId, sessionId);
    }

    @RequestMapping(value = "/users/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RegistrationDetails> getRegistrationDetails(@PathVariable("id") String userId, HttpServletRequest request) throws Exception  {

        logger.debug("Getting registrations for user with id {}", userId);

        // verify that the user making the request is the authenticated user
        authenticationService.verifyUser(request, userId);

        return registrationService.getRegistrationDetails(userId);
    }

}
