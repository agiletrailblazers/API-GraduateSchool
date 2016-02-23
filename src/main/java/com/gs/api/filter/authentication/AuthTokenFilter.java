package com.gs.api.filter.authentication;

import com.gs.api.exception.AuthenticationException;
import com.gs.api.service.authentication.AuthenticationService;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component("authTokenFilter")
public class AuthTokenFilter implements Filter {

    @Value("${auth.token.filter.active}")
    private boolean authTokenFilterActive;

    @Value("${auth.token.filter.allowed.uri}")
    private String[] authTokenFilterAllowedUri;

    @Value("${auth.token.filter.authentication.whitelist}")
    private String[] authTokenFilterAuthenticationWhiteList;

    @Autowired
    private AuthenticationService authenticationService;

    final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        final HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        final HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        try {

            if (authTokenFilterActive) {

                logger.debug("Applying auth token filter");

                //unless URI is on the "allowed" list, validate token
                String requestedURI = httpRequest.getRequestURI();
                if (!ArrayUtils.contains(authTokenFilterAllowedUri, requestedURI)) {

                    if (isURIWhiteListed(requestedURI)) {
                        // the URI is white listed, validate that the token has guest level access (will throw exception if not valid)
                        authenticationService.validateGuestAccess(httpRequest);
                    }
                    else {
                        // the URI is not white listed, validate the token has authenticated level access (will throw exception if not valid)
                        authenticationService.validateAuthenticatedAccess(httpRequest);
                    }
                }
            }

            // all is good, continue processing the request
            filterChain.doFilter(servletRequest, servletResponse);

        }
        catch (AuthenticationException e) {
            logger.warn("Authentication failure", e);
            httpResponse.setHeader("Content-Type", "application/json");
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    @Override
    public void destroy() {
    }

    private boolean isURIWhiteListed(String requestURI) {

        /*
           the request URI may include path variables, in which case the
           white listed URI may contain a regular expression for the match.
        */
        for(String whiteListedURI: authTokenFilterAuthenticationWhiteList) {
            if (requestURI.matches(whiteListedURI)) {
                return true;
            }
        }
        return false;
    }

}
