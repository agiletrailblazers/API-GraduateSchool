package com.gs.api.filter.authentication;

import com.gs.api.exception.AuthenticationException;
import com.gs.api.service.authentication.AuthenticationService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthTokenFilterTest {

    private static final String URI_FOR_PATH_WITH_REGEX_REQUIRES_GUEST_ACCESS = "/path/person12345/wildcard/requires/guest/access";
    private static final String URI_FOR_PATH_WITH_REGEX_AT_END_REQUIRES_GUEST_ACCESS = "/path/wildcard/at/end/requires/guest/access/person12345";
    private static final String PATH_REQUIRES_GUEST_ACCESS = "/path/requires/guest/access";
    private static final String PATH_WITH_REGEX_REQUIRES_GUEST_ACCESS = "/path/\\w+/wildcard/requires/guest/access";
    private static final String PATH_WITH_REGEX_AT_END_REQUIRES_GUEST_ACCESS = "/path/wildcard/at/end/requires/guest/access/\\w+$";
    private static final String PATH_REQUIRES_AUTHENTICATED_ACCESS = "/path/requires/authenticated/access";
    private static final String PATH_DOES_NOT_REQUIRE_TOKEN = "/path/does/not/require/token";
    private static final String[] ALLOWED_URI = new String[] {PATH_DOES_NOT_REQUIRE_TOKEN};
    private static final String[] AUTHENTICATION_WHITELIST = new String[] {PATH_REQUIRES_GUEST_ACCESS, PATH_WITH_REGEX_REQUIRES_GUEST_ACCESS, PATH_WITH_REGEX_AT_END_REQUIRES_GUEST_ACCESS};

    @Mock
    private FilterChain filterChain;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthTokenFilter filter;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(filter, "guestTokenRequiredList", AUTHENTICATION_WHITELIST);
        ReflectionTestUtils.setField(filter, "noTokenRequiredList", ALLOWED_URI);
    }

    @Test
    public void testDoFilter_uriTokenNotRequired() throws Exception {

        when(request.getRequestURI()).thenReturn(PATH_DOES_NOT_REQUIRE_TOKEN);

        filter.doFilter(request, response, filterChain);

        verify(request).getRequestURI();
        verifyNoMoreInteractions(request);
        verifyZeroInteractions(authenticationService);
        verify(filterChain).doFilter(request, response);

        filter.destroy();
    }

    @Test
    public void testDoFilter_uriRequiresGuestAccess() throws Exception {

        when(request.getRequestURI()).thenReturn(PATH_REQUIRES_GUEST_ACCESS);

        filter.doFilter(request, response, filterChain);

        verify(request).getRequestURI();
        verify(authenticationService).validateGuestAccess(request);
        verify(filterChain).doFilter(request, response);

        filter.destroy();
    }

    @Test
    public void testDoFilter_uriWithWildcardRequiresGuestAccess() throws Exception {

        when(request.getRequestURI()).thenReturn(URI_FOR_PATH_WITH_REGEX_REQUIRES_GUEST_ACCESS);

        filter.doFilter(request, response, filterChain);

        verify(request).getRequestURI();
        verify(authenticationService).validateGuestAccess(request);
        verify(filterChain).doFilter(request, response);

        filter.destroy();
    }

    @Test
    public void testDoFilter_uriWithWildcardAtEndRequiresGuestAccess() throws Exception {

        when(request.getRequestURI()).thenReturn(URI_FOR_PATH_WITH_REGEX_AT_END_REQUIRES_GUEST_ACCESS);

        filter.doFilter(request, response, filterChain);

        verify(request).getRequestURI();
        verify(authenticationService).validateGuestAccess(request);
        verify(filterChain).doFilter(request, response);

        filter.destroy();
    }

    @Test
    public void testDoFilter_uriRequiresAuthenticatedAccess() throws Exception {

        when(request.getRequestURI()).thenReturn(PATH_REQUIRES_AUTHENTICATED_ACCESS);

        filter.doFilter(request, response, filterChain);

        verify(request).getRequestURI();
        verify(authenticationService).validateAuthenticatedAccessFromHTTPServletRequest(request);
        verify(filterChain).doFilter(request, response);

        filter.destroy();
    }

    @Test
    public void testDoFilter_notAuthorizedGuestAccess() throws Exception {

        when(request.getRequestURI()).thenReturn(PATH_REQUIRES_GUEST_ACCESS);

        AuthenticationException cause = new AuthenticationException("I caused invalid token");
        doThrow(cause).when(authenticationService).validateGuestAccess(request);

        filter.doFilter(request, response, filterChain);

        verify(request).getRequestURI();
        verify(authenticationService).validateGuestAccess(request);
        verify(response).setHeader("Content-Type", "application/json");
        verify(response).setStatus(401);
        verifyZeroInteractions(filterChain);

        filter.destroy();
    }

    @Test
    public void testDoFilter_notAuthorizedAuthenticatedAccess() throws Exception {

        when(request.getRequestURI()).thenReturn(PATH_REQUIRES_AUTHENTICATED_ACCESS);

        AuthenticationException cause = new AuthenticationException("I caused invalid token");
        doThrow(cause).when(authenticationService).validateAuthenticatedAccessFromHTTPServletRequest(request);

        filter.doFilter(request, response, filterChain);

        verify(request).getRequestURI();
        verify(authenticationService).validateAuthenticatedAccessFromHTTPServletRequest(request);
        verify(response).setHeader("Content-Type", "application/json");
        verify(response).setStatus(401);
        verifyZeroInteractions(filterChain);

        filter.destroy();
    }

}
