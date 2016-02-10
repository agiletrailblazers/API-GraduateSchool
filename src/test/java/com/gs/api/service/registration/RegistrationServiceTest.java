package com.gs.api.service.registration;

import com.gs.api.dao.CourseSessionDAO;
import com.gs.api.dao.registration.RegistrationDAO;
import com.gs.api.dao.registration.UserDAO;
import com.gs.api.domain.Person;
import com.gs.api.domain.course.Course;
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;

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
    private CourseSession session;


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

        final String id = "reg1";
        final String id2 = "reg2";

        when(sessionDao.getSession(any(String.class))).thenReturn(new CourseSession());
        //when(userDao.getUser(STUDENT_ID_1)).thenReturn(new User(STUDENT_ID_1, "user1", "", "1234", new Person(), "", ""));
        //when(userDao.getUser(STUDENT_ID_1)).thenReturn(new User(STUDENT_ID_1, "user1", "", "1234", new Person(), "", ""));
        when(registrationDao.register(any(User.class), any(User.class), any(CourseSession.class))).thenReturn(id);

        registrationService.register(USER_ID, registrations);

        verify(registrationDao, times(2)).register(any(User.class), any(User.class), any(CourseSession.class));

        assertEquals(id, registration1.getId());
        //TODO assert the second registration is different after user lookup is done
    }

}
