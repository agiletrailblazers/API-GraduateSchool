package com.gs.api.service.registration;

import com.gs.api.dao.registration.RegistrationDAO;
import com.gs.api.domain.registration.Registration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class RegistrationServiceTest {

    private static final String USER_ID = "person654321";
    private static final String SESSION_ID = "session12345";
    private static final String STUDENT_ID_1 = "person44444";
    private static final String STUDENT_ID_2 = "person55555";

    private List<Registration> registrations;
    private Registration registration1;
    private Registration registration2;

    @Mock
    private RegistrationDAO registrationDao;

    @InjectMocks
    @Autowired
    private RegistrationServiceImpl registrationService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        registrations = new ArrayList<>();

        registration1 = new Registration();
        registration1.setStudentId(STUDENT_ID_1);
        registration1.setSessionId(SESSION_ID);
        registrations.add(registration1);

        registration2 = new Registration();
        registration2.setStudentId(STUDENT_ID_2);
        registration2.setSessionId(SESSION_ID);
        registrations.add(registration2);
    }

    @Test
    public void testRegister() throws Exception {

        final String id1 = "reg1";
        final String id2 = "reg2";
        when(registrationDao.register(USER_ID, registration1)).thenReturn(id1);
        when(registrationDao.register(USER_ID, registration2)).thenReturn(id2);

        registrationService.register(USER_ID, registrations);

        verify(registrationDao).register(USER_ID, registration1);
        verify(registrationDao).register(USER_ID, registration2);

        assertEquals("ID not set on registration 1", id1, registration1.getId());
        assertEquals("ID not set on registration 2", id2, registration2.getId());
    }

}
