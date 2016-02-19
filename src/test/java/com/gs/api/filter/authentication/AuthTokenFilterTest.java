package com.gs.api.filter.authentication;

import com.gs.api.exception.AuthenticationException;
import com.gs.api.service.authentication.AuthTokenService;

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

    private static final String PATH_REQUIRES_TOKEN = "/path/requires/token";
    private static final String PATH_DOES_NOT_REQUIRE_TOKEN = "/path/does/not/require/token";
    private static final String[] ALLOWED_URI = new String[] {PATH_DOES_NOT_REQUIRE_TOKEN};

    @Mock
    private FilterChain filterChain;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthTokenService authTokenService;

    @InjectMocks
    private AuthTokenFilter filter;

    @Test
    public void testDoFilter_filterNotActive() throws Exception {

        ReflectionTestUtils.setField(filter, "authTokenFilterActive", false);

        filter.doFilter(request, response, filterChain);

        verifyZeroInteractions(authTokenService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void testDoFilter_filterActive_pathRequired() throws Exception {

        ReflectionTestUtils.setField(filter, "authTokenFilterActive", true);
        ReflectionTestUtils.setField(filter, "authTokenFilterAllowedUri", ALLOWED_URI);

        when(request.getRequestURI()).thenReturn(PATH_REQUIRES_TOKEN);

        // simply calling this to get coverage on a method that is a NOOP
        filter.init(null);

        filter.doFilter(request, response, filterChain);

        // simply calling this to get coverage on a method that is a NOOP
        filter.destroy();

        verify(request).getRequestURI();
        verify(authTokenService).validateToken(request);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void testDoFilter_filterActive_notAuthorized() throws Exception {

        ReflectionTestUtils.setField(filter, "authTokenFilterActive", true);
        ReflectionTestUtils.setField(filter, "authTokenFilterAllowedUri", ALLOWED_URI);

        when(request.getRequestURI()).thenReturn(PATH_REQUIRES_TOKEN);

        AuthenticationException cause = new AuthenticationException("I caused invalid token");
        doThrow(cause).when(authTokenService).validateToken(request);
        filter.doFilter(request, response, filterChain);

        verify(request).getRequestURI();
        verify(authTokenService).validateToken(request);
        verify(response).setHeader("Content-Type", "application/json");
        verify(response).setStatus(401);
        verifyZeroInteractions(filterChain);
    }

    @Test
    public void testDoFilter_filterActive_pathNotRequired() throws Exception {

        ReflectionTestUtils.setField(filter, "authTokenFilterActive", true);
        ReflectionTestUtils.setField(filter, "authTokenFilterAllowedUri", ALLOWED_URI);

        when(request.getRequestURI()).thenReturn(PATH_DOES_NOT_REQUIRE_TOKEN);

        // simply calling this to get coverage on a method that is a NOOP
        filter.init(null);

        filter.doFilter(request, response, filterChain);

        // simply calling this to get coverage on a method that is a NOOP
        filter.destroy();

        verify(request).getRequestURI();
        verifyNoMoreInteractions(request);
        verifyZeroInteractions(authTokenService);
        verify(filterChain).doFilter(request, response);
    }

}
