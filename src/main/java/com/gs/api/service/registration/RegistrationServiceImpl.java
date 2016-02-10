package com.gs.api.service.registration;


import com.gs.api.dao.CourseSessionDAO;
import com.gs.api.dao.registration.RegistrationDAO;
import com.gs.api.dao.registration.UserDAO;
import com.gs.api.domain.Person;
import com.gs.api.domain.course.Course;
import com.gs.api.domain.course.CourseSession;
import com.gs.api.domain.registration.Registration;

import com.gs.api.domain.registration.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    final Logger logger = LoggerFactory.getLogger(RegistrationServiceImpl.class);

    @Autowired
    private RegistrationDAO registrationDao;

    @Autowired
    private UserDAO userDao;

    @Autowired
    private CourseSessionDAO sessionDao;

    @Override
    public void register(String userId, List<Registration> registrations) throws Exception {

        for (Registration registration : registrations) {
            logger.info("User {} is registering student {} for class no {}", new String[] {userId,
                    registration.getStudentId(), registration.getSessionId()});
            //Get user, student, session, and course.
            if (userId == null) {
                userId = "emplo000000000002360"; //Sabauser - Remove when we are getting logged in user
            }
            if (registration.getStudentId() == null) {
                registration.setStudentId("persn000000000535454"); //Remove when we have student
            }

            User user = userDao.getUser(userId);
            User studentUser = userDao.getUser(registration.getStudentId());

            CourseSession session = sessionDao.getSession(registration.getSessionId());

            //TODO fail if session or either user is null
            // create the registration
            String id = registrationDao.register(user, studentUser, session);

            // set the id for the newly created registration
            registration.setId(id);
        }
    }
}
