package com.gs.api.service.registration;


import com.gs.api.dao.registration.RegistrationDAO;
import com.gs.api.domain.registration.Registration;

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

    @Override
    public void register(String userId, List<Registration> registrations) throws Exception {

        for (Registration registration : registrations) {

            logger.info("User {} is registering student {} for session {}", new String[] {userId, registration.getStudentId(), registration.getSessionId()});

            // create the registration
            String id = registrationDao.register(userId, registration);

            // set the id for the newly created registration
            registration.setId(id);
        }
    }
}
