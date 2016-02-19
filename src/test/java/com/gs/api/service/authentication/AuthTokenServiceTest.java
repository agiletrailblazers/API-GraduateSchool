package com.gs.api.service.authentication;

import com.gs.api.domain.authentication.Role;
import com.gs.api.exception.AuthenticationException;

import org.apache.commons.lang.StringUtils;
import org.hamcrest.Matchers;
import org.jasypt.encryption.pbe.PBEStringCleanablePasswordEncryptor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class AuthTokenServiceTest {

    public static final String ENCRYPTED_TOKEN_STRING = "encrypted_token_string";
    public static final String USER_ID = "123";

    @Value("${auth.token.header}")
    private String authTokenHeader;

    @Value("${auth.user.attribute}")
    private String authUserAttribute;

    @Value("${auth.role.attribute}")
    private String authRoleAttribute;

    @Autowired
    private WebApplicationContext applicationContext;

    @Mock
    private PBEStringCleanablePasswordEncryptor encryptor;

    @InjectMocks
    private AuthTokenServiceImpl authenticationService;

    @Captor
    private ArgumentCaptor<String> encryptStringCaptor;

    @Mock
    private HttpServletRequest request;

    /*
        By default, no exceptions are expected to be thrown (i.e. tests will fail if an exception is thrown),
        but using this rule allows for verification of operations that are expected to throw specific exceptions
    */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockMvcBuilders.webAppContextSetup(applicationContext).build();
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(authenticationService, "authTokenHeader", authTokenHeader);
        ReflectionTestUtils.setField(authenticationService, "authUserAttribute", authUserAttribute);
        ReflectionTestUtils.setField(authenticationService, "authRoleAttribute", authRoleAttribute);
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
    public void testValidateToken_GuestSuccess() throws Exception{

        String encryptedToken = "abcde1234554321edcba";
        String validToken = "de703f00-8c20-4c74-a254-277e2020244b|"
                + new Date().getTime() + "|" + Role.GUEST + "|";

        when(request.getHeader(authTokenHeader)).thenReturn(encryptedToken);
        when(encryptor.decrypt(encryptedToken)).thenReturn(validToken);

        authenticationService.validateToken(request);

        verify(request).getHeader(authTokenHeader);
        verify(encryptor).decrypt(encryptedToken);
        verify(request).setAttribute(authRoleAttribute, Role.GUEST.name());
        verifyNoMoreInteractions(request);
    }

    @Test
    public void testValidateToken_StudentSuccessWithUser() throws Exception{

        String user = "testuser";
        String encryptedToken = "abcde1234554321edcba";
        String validToken = "de703f00-8c20-4c74-a254-277e2020244b|"
                + new Date().getTime() + "|" + Role.GUEST + "|" + user;

        when(request.getHeader(authTokenHeader)).thenReturn(encryptedToken);
        when(encryptor.decrypt(encryptedToken)).thenReturn(validToken);

        authenticationService.validateToken(request);

        verify(request).getHeader(authTokenHeader);
        verify(encryptor).decrypt(encryptedToken);
        verify(request).setAttribute(authRoleAttribute, Role.GUEST.name());
        verify(request).setAttribute(authUserAttribute, user);
    }

    @Test
    public void testValidateToken_MissingToken() throws Exception{

        // setup expected exception
        thrown.expect(AuthenticationException.class);
        thrown.expectMessage(AuthTokenServiceImpl.MISSING_REQUIRED_AUTHENTICATION_TOKEN_MSG);

        authenticationService.validateToken(request);
    }

    @Test
    public void testValidateToken_DecryptTokenFailure() throws Exception{

        // setup expected exception
        IllegalArgumentException cause = new IllegalArgumentException("I caused decryption to fail");
        thrown.expect(AuthenticationException.class);
        thrown.expectMessage(AuthTokenServiceImpl.UNABLE_TO_DECRYPT_TOKEN_MSG);
        thrown.expectCause(Matchers.is(cause));

        String encryptedToken = "abcde1234554321edcba";

        when(request.getHeader(authTokenHeader)).thenReturn(encryptedToken);
        when(encryptor.decrypt(encryptedToken)).thenThrow(cause);

        authenticationService.validateToken(request);
    }

    @Test
    public void testValidateToken_BadUUID() throws Exception{

        String encryptedToken = "abcde1234554321edcba";
        String validToken = "Iamabaduuid-a254-277e2020244b|"
                + new Date().getTime() + "|" + Role.GUEST + "|";

        when(request.getHeader(authTokenHeader)).thenReturn(encryptedToken);
        when(encryptor.decrypt(encryptedToken)).thenReturn(validToken);

        // setup expected exception
        thrown.expect(AuthenticationException.class);
        thrown.expectMessage(AuthTokenServiceImpl.TOKEN_UUID_IS_NOT_VALID_MSG);
        thrown.expectCause(Matchers.isA(Exception.class));
        authenticationService.validateToken(request);
    }

    @Test
    public void testValidateToken_BadTimestamp() throws Exception{

        String encryptedToken = "abcde1234554321edcba";
        String validToken = "de703f00-8c20-4c74-a254-277e2020244b|"
                + "notatimestamp" + "|" + Role.GUEST + "|";

        when(request.getHeader(authTokenHeader)).thenReturn(encryptedToken);
        when(encryptor.decrypt(encryptedToken)).thenReturn(validToken);

        // setup expected exception
        thrown.expect(AuthenticationException.class);
        thrown.expectMessage(AuthTokenServiceImpl.TOKEN_TIMESTAMP_IS_NOT_VALID_MSG);
        thrown.expectCause(Matchers.isA(Exception.class));
        authenticationService.validateToken(request);
    }

    @Test
    public void testValidateToken_BadRole() throws Exception{

        String encryptedToken = "abcde1234554321edcba";
        String validToken = "de703f00-8c20-4c74-a254-277e2020244b|"
                + new Date().getTime() + "|" + "notARole" + "|";

        when(request.getHeader(authTokenHeader)).thenReturn(encryptedToken);
        when(encryptor.decrypt(encryptedToken)).thenReturn(validToken);

        // setup expected exception
        thrown.expect(AuthenticationException.class);
        thrown.expectMessage(AuthTokenServiceImpl.TOKEN_ROLE_IS_NOT_VALID_MSG);
        thrown.expectCause(Matchers.isA(IllegalArgumentException.class));

        authenticationService.validateToken(request);
    }

}
