package com.gs.api.controller.authentication;

import com.gs.api.controller.BaseController;
import com.gs.api.service.authentication.AuthTokenService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Configuration
@RestController
@RequestMapping("/token")
public class AuthenticationController extends BaseController {

    final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired
    private AuthTokenService authTokenService;

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthToken generateGuestToken() throws Exception  {

        logger.debug("Generating a guest API token");

        AuthToken token = new AuthToken();
        token.setToken(authTokenService.generateGuestToken());
        return token;
    }
}
