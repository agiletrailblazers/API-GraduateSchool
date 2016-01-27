package com.gs.api.dao.registration;

import com.gs.api.domain.registration.Registration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class RegistrationDAO {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationDAO.class);

    /**
     * Create a registration for the requested session.
     * @param userId the ID of the user that is performing the registration.  This may or may not be the ID of the student being registered.
     * @param registration the registration to be created.
     * @return the id of the created registration.
     * @throws Exception error creating registration.
     */
    public String register(String userId, Registration registration) throws Exception {

        // TODO implement real logic

        logger.debug("Inserting registration into the database");

        return UUID.randomUUID().toString();
    }
}
