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

    @Value("${auth.token.filter.uri.no.token.required}")
    private String[] noTokenRequiredList;

    @Value("${auth.token.filter.uri.guest.token.required}")
    private String[] guestTokenRequiredList;

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
            // if the requested URI IS NOT on the "no token required" list then it must be verified for either guest or authenticated access
            String requestedURI = httpRequest.getRequestURI();
            logger.debug("Applying auth token filter for URI {}", requestedURI);

            if (!ArrayUtils.contains(noTokenRequiredList, requestedURI)) {

                if (isGuestTokenRequired(requestedURI)) {
                    logger.debug("URI {} requires a guest token", requestedURI);
                    authenticationService.validateGuestAccess(httpRequest);
                }
                else {
                    // any URI not specifically listed on the guest token required list must have an authenticated token
                    logger.debug("URI {} requires an authenticated token", requestedURI);
                    authenticationService.validateAuthenticatedAccess(httpRequest);
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

    private boolean isGuestTokenRequired(String requestURI) {

        /*
           the request URI may include path variables, in which case the
           white listed URI may contain a regular expression for the match.
        */
        for(String whiteListedURI: guestTokenRequiredList) {
            if (requestURI.matches(whiteListedURI)) {
                return true;
            }
        }
        return false;
    }

}
