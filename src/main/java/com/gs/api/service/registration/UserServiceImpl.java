package com.gs.api.service.registration;

import com.gs.api.dao.registration.UserDAO;
import com.gs.api.domain.authentication.AuthCredentials;
import com.gs.api.domain.registration.Timezone;
import com.gs.api.domain.registration.User;
import com.gs.api.exception.NotFoundException;

import com.gs.api.service.email.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserDAO userDao;

    @Autowired
    private EmailService emailService;

    @Override
    public void createUser(User user) throws Exception {

        logger.info("Creating new user: {} {} {}", new String[] {user.getPerson().getFirstName(), user.getPerson().getMiddleName(), user.getPerson().getLastName()});

        String userId = userDao.insertNewUser(user);

        // update the user with the generated ID
        user.setId(userId);

        //sending welcome email to new User
        try {
            emailService.sendNewUserEmail(user);
        }
        catch (Exception e) {
            logger.debug("User created but failed to send new user email");
        }
        logger.debug("User creation email for user " + user.getId() + " asynchronously sending, returning to normal flow");
    }

    @Override
    public void deleteUser(String id) throws Exception {

        logger.info("Deleting user: {}", id);

        // need to lookup the user by id first because the delete dao requires the timestamp associated with the id
        User user = userDao.getUser(id);
        userDao.deleteUser(id, user.getTimestamp());
    }

    @Override
    public User getUser(AuthCredentials authCredentials) throws Exception {

        logger.debug("Get user: {}", authCredentials.getUsername());

        return userDao.getUser(authCredentials.getUsername(), authCredentials.getPassword());
    }

    @Override
    public User getUser(String id) throws Exception {

        logger.debug("Get user: {}", id);

        User user = userDao.getUser(id);

        if (user == null) {
            // user not found
            throw new NotFoundException("User not found by id " + id);
        }

        return user;
    }

    @Override
    public List<Timezone> getTimezones() throws Exception {

        return userDao.getTimezones();
    }

}
