package com.gs.api.service.registration;

import com.gs.api.dao.CourseSessionDAO;
import com.gs.api.dao.registration.RegistrationDAO;
import com.gs.api.dao.registration.UserDAO;
import com.gs.api.domain.Person;
import com.gs.api.domain.course.CourseSession;
import com.gs.api.domain.registration.Registration;
import com.gs.api.domain.registration.User;

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

import static junit.framework.Assert.assertSame;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
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

    @Mock
    private RegistrationDAO registrationDao;

    @Mock
    private CourseSessionDAO sessionDao;

    @Mock
    private UserDAO userDao;

    @InjectMocks
    @Autowired
    private RegistrationServiceImpl registrationService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        registrations = new ArrayList<>();

        Registration registration1 = new Registration();
        registration1.setStudentId(STUDENT_ID_1);
        registration1.setSessionId(SESSION_ID);
        registrations.add(registration1);

    }

    @Test
    public void testRegisterTwoRegs() throws Exception {
        //Add second reg
        Registration registration2 = new Registration();
        registration2.setStudentId(STUDENT_ID_2);
        registration2.setSessionId(SESSION_ID);
        registrations.add(registration2);

        User user = new User(USER_ID, "user1", "", "1234", new Person(), "", "", "", "");
        User student1 = new User(STUDENT_ID_1, "student1", "", "1234", new Person(), "", "", "", "");
        User student2 = new User(STUDENT_ID_2, "student2", "", "1234", new Person(), "", "", "", "");

        CourseSession session = new CourseSession();
        session.setClassNumber(SESSION_ID);

        when(userDao.getUser(USER_ID)).thenReturn(user);
        when(userDao.getUser(STUDENT_ID_1)).thenReturn(student1);
        when(userDao.getUser(STUDENT_ID_2)).thenReturn(student2);
        when(sessionDao.getSession(SESSION_ID)).thenReturn(session);

        Registration createdRegistration1 = new Registration();
        createdRegistration1.setId("12345");
        Registration createdRegistration2 = new Registration();
        createdRegistration2.setId("54321");

        when(registrationDao.registerForCourse(user, student1, session)).thenReturn(createdRegistration1);
        when(registrationDao.registerForCourse(user, student2, session)).thenReturn(createdRegistration2);

        List<Registration> createdRegistrations = registrationService.register(USER_ID, registrations);

        verify(userDao, times(4)).getUser(any(String.class));
        verify(sessionDao, times(2)).getSession(any(String.class));
        verify(registrationDao, times(2)).registerForCourse(any(User.class), any(User.class), any(CourseSession.class));

        // the list returned from the service should not be the same instance as the one passed in,
        // it should be a list of the created registrations returned by the DAO
        assertNotSame(createdRegistrations, registrations);
        assertSame(createdRegistration1, createdRegistrations.get(0));
        assertSame(createdRegistration2, createdRegistrations.get(1));
    }

    @Test
    public void testRegisterUserNotFound() throws Exception {
        when(userDao.getUser(USER_ID)).thenReturn(null);
        try {
            registrationService.register(USER_ID, registrations);
            fail("Shouldn't reach here");
        }
        catch (Exception e) {
            assertTrue(e.getMessage().contains("No user found for logged in user"));
        }
    }

    @Test
    public void testRegisterStudentNotFound() throws Exception {
        User user = new User(USER_ID, "user1", "", "1234", new Person(), "", "", "", "");

        when(userDao.getUser(USER_ID)).thenReturn(user);
        when(userDao.getUser(STUDENT_ID_1)).thenReturn(null);

        try {
            registrationService.register(USER_ID, registrations);
            fail("Shouldn't reach here");
        }
        catch (Exception e) {
            assertTrue(e.getMessage().contains("No user found for student"));
        }
    }

    @Test
    public void testRegisterSessionNotFound() throws Exception {
        User user = new User(USER_ID, "user1", "", "1234", new Person(), "", "", "", "");
        User student1 = new User(STUDENT_ID_1, "student1", "", "1234", new Person(), "", "", "", "");


        when(userDao.getUser(USER_ID)).thenReturn(user);
        when(userDao.getUser(STUDENT_ID_1)).thenReturn(student1);

        when(sessionDao.getSession(SESSION_ID)).thenReturn(null);
        try {
            registrationService.register(USER_ID, registrations);
            fail("Shouldn't reach here");
        }
        catch (Exception e) {
            assertTrue(e.getMessage().contains("No course session found for session id"));
        }
    }
}
