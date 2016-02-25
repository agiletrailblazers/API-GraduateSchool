package com.gs.api.service.registration;


import com.gs.api.dao.CourseSessionDAO;
import com.gs.api.dao.registration.RegistrationDAO;
import com.gs.api.dao.registration.UserDAO;
import com.gs.api.domain.course.CourseSession;
import com.gs.api.domain.registration.Registration;

import com.gs.api.domain.registration.RegistrationRequest;
import com.gs.api.domain.registration.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    public List<Registration> register(String userId, RegistrationRequest registrationRequest) throws Exception {

        List<Registration> completedRegistrations = new ArrayList<>();

        for (Registration registration : registrationRequest.getRegistrations()) {
            logger.info("User {} is registering student {} for class no {}", new String[] {userId,
                    registration.getStudentId(), registration.getSessionId()});

            //Get actual user, student, session, and courseSession from specified IDs
            User user = userDao.getUser(userId);
            if (user == null) {
                logger.error("No user found for logged in user: {}", userId);
                throw new Exception("No user found for logged in user " + userId);
            }

            User studentUser = userDao.getUser(registration.getStudentId());
            if (studentUser == null) {
                logger.error("No user found for student {}", registration.getStudentId());
                throw new Exception("No user found for student " + registration.getSessionId());
            }

            CourseSession session = sessionDao.getSession(registration.getSessionId());
            if (session == null) {
                logger.error("No course session found for session id {}", registration.getSessionId());
                throw new Exception("No course session found for session id " + registration.getSessionId());
            }

            completedRegistrations.add(registrationDao.registerForCourse(user, studentUser, session));
        }

        return completedRegistrations;
    }
}
