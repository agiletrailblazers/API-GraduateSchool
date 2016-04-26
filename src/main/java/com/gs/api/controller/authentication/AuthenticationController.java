package com.gs.api.controller.authentication;

import com.gs.api.controller.BaseController;
import com.gs.api.domain.authentication.AuthCredentials;
import com.gs.api.domain.authentication.AuthToken;
import com.gs.api.domain.authentication.AuthUser;
import com.gs.api.domain.authentication.ReAuthCredentials;
import com.gs.api.service.authentication.AuthenticationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Configuration
@RestController
@RequestMapping("")
public class AuthenticationController extends BaseController {

    final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired
    private AuthenticationService authenticationService;

    @RequestMapping(value = "/tokens", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthToken generateToken() throws Exception  {

        logger.debug("Generating an API token");

        return authenticationService.generateToken();
    }

    @RequestMapping(value = "/authentication", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthUser authenticateUser(@RequestBody AuthCredentials authCredentials) throws Exception  {

        logger.debug("Authenticating user {}", authCredentials.getUsername());

        return authenticationService.authenticateUser(authCredentials);
    }

    @RequestMapping(value = "/reauthentication", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthToken reAuthenticateUser(@RequestBody ReAuthCredentials reAuthCredentials) throws Exception {
        logger.debug("ReAuthenticating user");

        return authenticationService.reAuthenticateUser(reAuthCredentials);
    }
}
