package com.gs.api.controller.registration;

import com.gs.api.controller.BaseController;
import com.gs.api.domain.authentication.AuthCredentials;
import com.gs.api.domain.registration.User;
import com.gs.api.service.registration.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

import javax.validation.Valid;

@Configuration
@EnableAsync
@RestController
@RequestMapping("/users")
public class UserController extends BaseController {

    final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public User createUser(@RequestBody @Valid User user) throws Exception  {

        logger.debug("User Create API initiated");

        // create the user
        userService.createUser(user);

        return user;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteUser(@PathVariable("id") String id) throws Exception  {

        logger.debug("Delete User {}", id);

        userService.deleteUser(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public User getUser(@PathVariable("id") String id) throws Exception {

        logger.debug("Get User {}", id);

        return userService.getUser(id);
    }

    /**
     * Status 204 (NO CONTENT) - success
     * Status 404 (NOT FOUND) - no user found with specified username
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/password/forgot", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public void forgotPassword(@RequestBody @Valid AuthCredentials authCredentials) throws Exception  {

        logger.debug("Resetting password for user {}", authCredentials.getUsername());

        userService.forgotPassword(authCredentials);
    }
}
