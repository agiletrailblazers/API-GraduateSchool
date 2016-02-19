package com.gs.api.filter.authentication;

import com.gs.api.exception.AuthenticationException;
import com.gs.api.service.authentication.AuthTokenService;

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

    @Autowired
    private AuthTokenService authTokenService;

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
                if (!ArrayUtils.contains(authTokenFilterAllowedUri, httpRequest.getRequestURI())) {

                    // validate the token (will throw exception if not valid)
                    authTokenService.validateToken(httpRequest);
                }
            }

            // all is good, continue processing the request
            filterChain.doFilter(servletRequest, servletResponse);

        }
        catch (AuthenticationException e) {
            logger.warn("Authentication failure", e);
            httpResponse.setHeader("Content-Type", "application/json");
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
    }

    @Override
    public void destroy() {
    }
}
