package com.gs.api.controller.registration;

import com.gs.api.controller.BaseController;
import com.gs.api.domain.registration.Registration;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Configuration
@RestController
@RequestMapping("/registration")
public class RegistrationController extends BaseController {

    final Logger logger = LoggerFactory.getLogger(RegistrationController.class);

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private AuthenticationService authenticationService;

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/user/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
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
    @RequestMapping(value = "/user/{uid}/sessionId/{sessionId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Registration getRegistration(@PathVariable("uid") String userId, @PathVariable("sessionId") String sessionId) throws Exception {

        logger.debug("Checking for duplicate registration for " + userId +
                " in session " + sessionId + ".");

        return registrationService.getRegistrationForSession(userId, sessionId);
    }

}
