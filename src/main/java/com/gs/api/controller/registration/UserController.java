package com.gs.api.controller.registration;

import com.gs.api.controller.BaseController;
import com.gs.api.domain.registration.User;
import com.gs.api.service.registration.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Configuration
@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public User create(@RequestBody User user) throws Exception  {

        logger.debug("User Create API initiated");

        // TODO basic input validation

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
}
