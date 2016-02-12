package com.gs.api.controller.registration;

import com.gs.api.domain.registration.Registration;
import com.gs.api.service.registration.RegistrationService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class RegistrationControllerTest {

    private static final String USER_ID = "person654321";

    @Autowired
    private WebApplicationContext applicationContext;

    @Mock
    private RegistrationService registrationService;

    @InjectMocks
    private RegistrationController registrationController;

    @Before
    public void setUp() throws Exception {
        MockMvcBuilders.webAppContextSetup(applicationContext).build();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateRegistration() throws Exception {

        Registration registration = new Registration();
        List<Registration> registrations = Collections.singletonList(registration);
        Registration createdRegistration = new Registration();
        createdRegistration.setId("12345");

        when(registrationService.register(USER_ID, registrations)).thenReturn(Collections.singletonList(createdRegistration));

        List<Registration> createdRegistrations = registrationController.createRegistration(USER_ID, registrations);
        assertEquals(1, createdRegistrations.size());
        assertSame(createdRegistration, createdRegistrations.get(0));

        verify(registrationService).register(USER_ID, registrations);
     }

}
