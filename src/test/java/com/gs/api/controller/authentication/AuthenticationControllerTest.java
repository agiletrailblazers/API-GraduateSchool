package com.gs.api.controller.authentication;

import com.gs.api.service.authentication.AuthTokenService;

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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class AuthenticationControllerTest {

    public static final String TEST_TOKEN = "thisisthetesttoken";
    @Autowired
    private WebApplicationContext applicationContext;

    @Mock
    private AuthTokenService authTokenService;

    @InjectMocks
    private AuthenticationController authenticationController;

    @Before
    public void setUp() throws Exception {
        MockMvcBuilders.webAppContextSetup(applicationContext).build();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGenerateGuestToken_Success() throws Exception {

        when(authTokenService.generateGuestToken()).thenReturn(TEST_TOKEN);

        String token = authenticationController.generateGuestToken();

        verify(authTokenService).generateGuestToken();

        assertEquals("wrong token", TEST_TOKEN, token);
     }
}
