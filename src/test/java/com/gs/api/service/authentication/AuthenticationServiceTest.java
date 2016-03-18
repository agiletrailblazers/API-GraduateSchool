package com.gs.api.service.authentication;

import com.gs.api.domain.authentication.AuthCredentials;
import com.gs.api.domain.authentication.AuthToken;
import com.gs.api.domain.authentication.AuthUser;
import com.gs.api.domain.authentication.Role;
import com.gs.api.domain.registration.User;
import com.gs.api.exception.AuthenticationException;
import com.gs.api.service.registration.UserService;

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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class AuthenticationServiceTest {

    private static final String ENCRYPTED_TOKEN_STRING = "encrypted_token_string";
    private static final String USER_ID = "123";
    private static final String USERNAME = "joe@tester.com";
    private static final String PASSWORD = "test1234";

    @Value("${auth.token.header}")
    private String authTokenHeader;

    @Value("${auth.user.attribute}")
    private String authUserAttribute;

    @Value("${auth.token.expire.minutes}")
    private int authTokenExpireMinutes;

    @Autowired
    private WebApplicationContext applicationContext;

    @Mock
    private PBEStringCleanablePasswordEncryptor encryptor;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

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
        ReflectionTestUtils.setField(authenticationService, "authTokenExpireMinutes", authTokenExpireMinutes);
    }

    @Test
    public void testGenerateGuestToken_Success() throws Exception {

        when(encryptor.encrypt(any(String.class))).thenReturn(ENCRYPTED_TOKEN_STRING);

        AuthToken token = authenticationService.generateToken();

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
        assertEquals("Wrong token generated", ENCRYPTED_TOKEN_STRING, token.getToken());
     }

    @Test
    public void testGenerateAuthenticatedToken_Success() throws Exception {

        when(encryptor.encrypt(any(String.class))).thenReturn(ENCRYPTED_TOKEN_STRING);

        AuthToken token = authenticationService.generateToken(USER_ID, Role.AUTHENTICATED);

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
        assertEquals("Wrong role", Role.AUTHENTICATED.name(), keyPieces[2]);
        // make sure user id is empty
        assertEquals("user id should be in key", USER_ID, keyPieces[3]);
        // make sure returning the encrypting key
        assertEquals("Wrong token generated", ENCRYPTED_TOKEN_STRING, token.getToken());
    }

    @Test
    public void testValidateGuessAccess_Success() throws Exception{

        String encryptedToken = "abcde1234554321edcba";
        String validToken = "de703f00-8c20-4c74-a254-277e2020244b|"
                + new Date().getTime() + "|" + Role.GUEST + "|";

        when(request.getHeader(authTokenHeader)).thenReturn(encryptedToken);
        when(encryptor.decrypt(encryptedToken)).thenReturn(validToken);

        authenticationService.validateGuestAccess(request);

        verify(request).getHeader(authTokenHeader);
        verify(encryptor).decrypt(encryptedToken);
        verifyNoMoreInteractions(request);
    }

    @Test
    public void testValidateAuthenticatedAccess_Success() throws Exception{

        String user = "testuser";
        String encryptedToken = "abcde1234554321edcba";
        String validToken = "de703f00-8c20-4c74-a254-277e2020244b|"
                + new Date().getTime() + "|" + Role.AUTHENTICATED + "|" + user;

        when(request.getHeader(authTokenHeader)).thenReturn(encryptedToken);
        when(encryptor.decrypt(encryptedToken)).thenReturn(validToken);

        authenticationService.validateAuthenticatedAccess(request);

        verify(request).getHeader(authTokenHeader);
        verify(encryptor).decrypt(encryptedToken);
        verify(request).setAttribute(authUserAttribute, user);
    }

    @Test
    public void testValidateGuessAccess_MissingToken() throws Exception{

        // setup expected exception
        thrown.expect(AuthenticationException.class);
        thrown.expectMessage(AuthenticationServiceImpl.MISSING_REQUIRED_AUTHENTICATION_TOKEN_MSG);

        authenticationService.validateGuestAccess(request);
    }

    @Test
    public void testValidateGuessAccess_DecryptTokenFailure() throws Exception{

        // setup expected exception
        IllegalArgumentException cause = new IllegalArgumentException("I caused decryption to fail");
        thrown.expect(AuthenticationException.class);
        thrown.expectMessage(AuthenticationServiceImpl.UNABLE_TO_DECRYPT_TOKEN_MSG);
        thrown.expectCause(Matchers.is(cause));

        String encryptedToken = "abcde1234554321edcba";

        when(request.getHeader(authTokenHeader)).thenReturn(encryptedToken);
        when(encryptor.decrypt(encryptedToken)).thenThrow(cause);

        authenticationService.validateGuestAccess(request);
    }

    @Test
    public void testValidateGuessAccess_BadUUID() throws Exception{

        String encryptedToken = "abcde1234554321edcba";
        String validToken = "Iamabaduuid-a254-277e2020244b|"
                + new Date().getTime() + "|" + Role.GUEST + "|";

        when(request.getHeader(authTokenHeader)).thenReturn(encryptedToken);
        when(encryptor.decrypt(encryptedToken)).thenReturn(validToken);

        // setup expected exception
        thrown.expect(AuthenticationException.class);
        thrown.expectMessage(AuthenticationServiceImpl.TOKEN_UUID_IS_NOT_VALID_MSG);
        thrown.expectCause(Matchers.isA(Exception.class));
        authenticationService.validateGuestAccess(request);
    }

    @Test
    public void testValidateGuessAccess_BadTimestamp() throws Exception{

        String encryptedToken = "abcde1234554321edcba";
        String validToken = "de703f00-8c20-4c74-a254-277e2020244b|"
                + "notatimestamp" + "|" + Role.GUEST + "|";

        when(request.getHeader(authTokenHeader)).thenReturn(encryptedToken);
        when(encryptor.decrypt(encryptedToken)).thenReturn(validToken);

        // setup expected exception
        thrown.expect(AuthenticationException.class);
        thrown.expectMessage(AuthenticationServiceImpl.TOKEN_TIMESTAMP_IS_NOT_VALID_MSG);
        thrown.expectCause(Matchers.isA(Exception.class));
        authenticationService.validateGuestAccess(request);
    }

    @Test
    public void testValidateGuessAccess_BadRole() throws Exception{

        String encryptedToken = "abcde1234554321edcba";
        String validToken = "de703f00-8c20-4c74-a254-277e2020244b|"
                + new Date().getTime() + "|" + "notARole" + "|";

        when(request.getHeader(authTokenHeader)).thenReturn(encryptedToken);
        when(encryptor.decrypt(encryptedToken)).thenReturn(validToken);

        // setup expected exception
        thrown.expect(AuthenticationException.class);
        thrown.expectMessage(AuthenticationServiceImpl.TOKEN_ROLE_IS_NOT_VALID_MSG);
        thrown.expectCause(Matchers.isA(IllegalArgumentException.class));

        authenticationService.validateGuestAccess(request);
    }

    @Test
    public void testValidateGuessAccess_GuestTokenDoesNotTimeout() throws Exception {
        String encryptedToken = "abcde1234554321edcba";
        long currentTime = new Date().getTime();
        // make our token old, 2x the expired time limit
        long expiredTime = currentTime - (2 * (authTokenExpireMinutes * 1000 * 1000));
        String validToken = "de703f00-8c20-4c74-a254-277e2020244b|"
                + new Date(expiredTime).getTime() + "|" + Role.GUEST + "|";

        when(request.getHeader(authTokenHeader)).thenReturn(encryptedToken);
        when(encryptor.decrypt(encryptedToken)).thenReturn(validToken);

        authenticationService.validateGuestAccess(request);

        verify(request).getHeader(authTokenHeader);
        verify(encryptor).decrypt(encryptedToken);
        verifyNoMoreInteractions(request);
    }

    @Test
    public void testValidateAuthenticatedAccess_AuthenticatedTokenMissingUser() throws Exception {
        String encryptedToken = "abcde1234554321edcba";
        String validToken = "de703f00-8c20-4c74-a254-277e2020244b|"
                + new Date().getTime() + "|" + Role.AUTHENTICATED + "|";

        when(request.getHeader(authTokenHeader)).thenReturn(encryptedToken);
        when(encryptor.decrypt(encryptedToken)).thenReturn(validToken);

        // setup expected exception
        thrown.expect(AuthenticationException.class);
        thrown.expectMessage(AuthenticationServiceImpl.TOKEN_USER_IS_NOT_VALID_MSG);

        authenticationService.validateAuthenticatedAccess(request);
    }

    @Test
    public void testValidateAuthenticatedAccess_AuthenticatedTokenExpired() throws Exception {
        String user = "testuser";
        String encryptedToken = "abcde1234554321edcba";
        long currentTime = new Date().getTime();
        // make our token old, 10 seconds past the expired time limit
        long expiredTime = currentTime - ((authTokenExpireMinutes * 60 * 1000) + 10000);
        String validToken = "de703f00-8c20-4c74-a254-277e2020244b|"
                + new Date(expiredTime).getTime() + "|" + Role.AUTHENTICATED + "|" + user;

        when(request.getHeader(authTokenHeader)).thenReturn(encryptedToken);
        when(encryptor.decrypt(encryptedToken)).thenReturn(validToken);

        // setup expected exception
        thrown.expect(AuthenticationException.class);
        thrown.expectMessage(AuthenticationServiceImpl.TOKEN_EXPIRED_MSG);

        authenticationService.validateAuthenticatedAccess(request);
    }

    @Test
    public void testValidateGuestAccess_AuthenticatedToken() throws Exception{

        String user = "testuser";
        String encryptedToken = "abcde1234554321edcba";
        String validToken = "de703f00-8c20-4c74-a254-277e2020244b|"
                + new Date().getTime() + "|" + Role.AUTHENTICATED + "|" + user;

        when(request.getHeader(authTokenHeader)).thenReturn(encryptedToken);
        when(encryptor.decrypt(encryptedToken)).thenReturn(validToken);

        authenticationService.validateGuestAccess(request);

        verify(request).getHeader(authTokenHeader);
        verify(encryptor).decrypt(encryptedToken);
        verifyNoMoreInteractions(request);
    }

    @Test
    public void testValidateAuthenticatedAccess_GuestToken() throws Exception{

        String encryptedToken = "abcde1234554321edcba";
        String validToken = "de703f00-8c20-4c74-a254-277e2020244b|"
                + new Date().getTime() + "|" + Role.GUEST + "|";

        when(request.getHeader(authTokenHeader)).thenReturn(encryptedToken);
        when(encryptor.decrypt(encryptedToken)).thenReturn(validToken);

        // setup expected exception
        thrown.expect(AuthenticationException.class);
        thrown.expectMessage(AuthenticationServiceImpl.TOKEN_IS_NOT_AUTHENTICATED_MSG);

        authenticationService.validateAuthenticatedAccess(request);
    }

    @Test
    public void testAuthenticateUser_Success() throws Exception {

        AuthCredentials authCredentials = new AuthCredentials(USERNAME, PASSWORD);
        User validUser = new User();
        validUser.setId(USER_ID);

        when(userService.getUser(authCredentials)).thenReturn(validUser);
        when(encryptor.encrypt(any(String.class))).thenReturn(ENCRYPTED_TOKEN_STRING);

        AuthUser authUser = authenticationService.authenticateUser(authCredentials);

        verify(userService).getUser(authCredentials);
        verify(encryptor).encrypt(encryptStringCaptor.capture());

        // inspect the unencrypted token string
        String key = encryptStringCaptor.getValue();
        String[] keyPieces = StringUtils.splitPreserveAllTokens(key, '|');
        assertEquals("Key missing data", 4, keyPieces.length);
        // make sure the UUID is not null
        assertTrue("No UUID in key", StringUtils.isNotEmpty(keyPieces[0]));
        // make sure we have a timestamp
        assertTrue("No timestamp in key", StringUtils.isNotEmpty(keyPieces[1]));
        // make sure it is a guest role
        assertEquals("Wrong role", Role.AUTHENTICATED.name(), keyPieces[2]);
        // make sure user id is empty
        assertEquals("user id should be in key", USER_ID, keyPieces[3]);
        // make sure returning the encrypting key
        assertEquals("Wrong token generated", ENCRYPTED_TOKEN_STRING, authUser.getAuthToken().getToken());

        // very correct user returned
        assertSame("wrong user", validUser, authUser.getUser());
    }

    @Test
    public void testAuthenticateUser_InvalidUser() throws Exception {

        // setup expected exception
        thrown.expect(AuthenticationException.class);
        thrown.expectMessage(AuthenticationServiceImpl.INVALID_USER_MSG);

        AuthCredentials authCredentials = new AuthCredentials(USERNAME, PASSWORD);

        when(userService.getUser(authCredentials)).thenReturn(null);

        authenticationService.authenticateUser(authCredentials);
    }

    @Test
    public void testAuthenticateUser_Error() throws Exception {

        // setup expected exception
        IllegalArgumentException cause = new IllegalArgumentException("I caused get user to fail");
        thrown.expect(AuthenticationException.class);
        thrown.expectMessage(AuthenticationServiceImpl.ERROR_AUTHENTICATING_USER_MSG);
        thrown.expectCause(Matchers.is(cause));

        AuthCredentials authCredentials = new AuthCredentials(USERNAME, PASSWORD);

        when(userService.getUser(authCredentials)).thenThrow(cause);

        authenticationService.authenticateUser(authCredentials);
    }

    @Test
    public void testVerifyUser_WrongUser() throws Exception{

        // setup expected exception
        thrown.expect(AuthenticationException.class);
        thrown.expectMessage(AuthenticationServiceImpl.MISMATCHED_USER_MSG);

        when(request.getAttribute(authUserAttribute)).thenReturn(USER_ID);

        authenticationService.verifyUser(request, "bogususerid");

        verify(request).getAttribute(authUserAttribute);
    }

    @Test
    public void testVerifyUser_ValidUser() throws Exception{

        when(request.getAttribute(authUserAttribute)).thenReturn(USER_ID);

        authenticationService.verifyUser(request, USER_ID);

        verify(request).getAttribute(authUserAttribute);
    }

}
