package com.gs.api.service.authentication;

import com.gs.api.domain.authentication.Role;

import org.apache.commons.lang.StringUtils;
import org.jasypt.encryption.pbe.PBEStringCleanablePasswordEncryptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class AuthTokenServiceTest {

    public static final String ENCRYPTED_TOKEN_STRING = "encrypted_token_string";
    public static final String USER_ID = "123";

    @Autowired
    private WebApplicationContext applicationContext;

    @Mock
    private PBEStringCleanablePasswordEncryptor encryptor;

    @InjectMocks
    private AuthTokenServiceImpl authenticationService;

    @Captor
    private ArgumentCaptor<String> encryptStringCaptor;

    @Before
    public void setUp() throws Exception {
        MockMvcBuilders.webAppContextSetup(applicationContext).build();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGenerateGuestToken_Success() throws Exception {

        when(encryptor.encrypt(any(String.class))).thenReturn(ENCRYPTED_TOKEN_STRING);

        String token = authenticationService.generateGuestToken();

        verify(encryptor).encrypt(encryptStringCaptor.capture());

        // inspect the key
        String key = encryptStringCaptor.getValue();
        String[] keyPieces = StringUtils.splitPreserveAllTokens(key, '|');
        assertEquals("Key missing data", 4, keyPieces.length);
        // make sure the UUID is not null
        assertTrue("No UUID in key", StringUtils.isNotEmpty(keyPieces[0]));
        // make sure we have a timestamp
        assertTrue("No timestamp in key", StringUtils.isNotEmpty(keyPieces[1]));
        // make sure it is a guest role
        assertEquals("Wrong role", Role.GUEST.name(), keyPieces[2]);
        // make sure user id is empty
        assertTrue("user id should not be in key", StringUtils.isEmpty(keyPieces[3]));
        // make sure returning the encrypting key
        assertEquals("Wrong token generated", ENCRYPTED_TOKEN_STRING, token);
     }

    @Test
    public void testGenerateToken_Success() throws Exception {

        when(encryptor.encrypt(any(String.class))).thenReturn(ENCRYPTED_TOKEN_STRING);

        String token = authenticationService.generateToken(USER_ID, Role.STUDENT);

        verify(encryptor).encrypt(encryptStringCaptor.capture());

        // inspect the key
        String key = encryptStringCaptor.getValue();
        String[] keyPieces = StringUtils.splitPreserveAllTokens(key, '|');
        assertEquals("Key missing data", 4, keyPieces.length);
        // make sure the UUID is not null
        assertTrue("No UUID in key", StringUtils.isNotEmpty(keyPieces[0]));
        // make sure we have a timestamp
        assertTrue("No timestamp in key", StringUtils.isNotEmpty(keyPieces[1]));
        // make sure it is a guest role
        assertEquals("Wrong role", Role.STUDENT.name(), keyPieces[2]);
        // make sure user id is empty
        assertEquals("user id should be in key", USER_ID, keyPieces[3]);
        // make sure returning the encrypting key
        assertEquals("Wrong token generated", ENCRYPTED_TOKEN_STRING, token);
    }

    @Test
    public void testValidateToken_Success() throws Exception{
        String userID = authenticationService.validateToken(null);

        assertNull("should be null", userID);
    }

}
