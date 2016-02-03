package com.gs.api.dao.registration;

import com.gs.api.domain.registration.Registration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class RegistrationDAOTest {

    private static final String USER_ID = "person654321";
    private static final String STUDENT_ID = "person123456";
    private static final String SESSION_ID = "session12345";

    private Registration registration;

    @Autowired
    private RegistrationDAO registrationDAO;

    @Before
    public void setUp() throws Exception {

        registration = new Registration();
        registration.setStudentId(STUDENT_ID);
        registration.setSessionId(SESSION_ID);
    }

    @Test
    public void testRegister() throws Exception {

        String id = registrationDAO.register(USER_ID, registration);
        assertNotNull(id);
    }

}
