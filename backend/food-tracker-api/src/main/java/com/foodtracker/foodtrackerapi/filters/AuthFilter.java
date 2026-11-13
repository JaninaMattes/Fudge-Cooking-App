package com.foodtracker.foodtrackerapi.filters;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.GenericFilterBean;

import com.foodtracker.foodtrackerapi.common.security.TokenService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * <p>
 * Reads the JWT token of a request and puts the user ID into the request. A
 * request without a valid token gets the status code 403 (Forbidden).
 * </p>
 *
 * <p>
 * The filter is not active. To make it active, give the bean back in
 * {@code FoodTrackerApiApplication} and remove the login endpoints from the URL
 * pattern, because a user has no token before the login.
 * </p>
 *
 * <p>
 * Each rejection writes one line to the log. The line holds the method, the
 * path and the reason, so a reader sees why a request of the client failed. The
 * line never holds the token.
 * </p>
 *
 * @author Janina Mattes
 */
public class AuthFilter extends GenericFilterBean {

    /** Name of the header that holds the token. */
    static final String AUTH_HEADER = "Authorization";

    /** Text that stands before the token in the header. */
    static final String BEARER_PREFIX = "Bearer ";

    /** Name of the request attribute that holds the user ID. */
    static final String USER_ID_ATTRIBUTE = TokenService.USER_ID_CLAIM;

    private static final Logger LOG = LoggerFactory.getLogger(AuthFilter.class);

    private final TokenService tokenService;

    /**
     * Makes the filter.
     *
     * @param tokenService service that reads a token
     */
    public AuthFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
            FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        String authHeader = httpRequest.getHeader(AUTH_HEADER);
        if (authHeader == null || authHeader.isEmpty()) {
            reject(httpRequest, httpResponse, "An authorization token is necessary");
            return;
        }
        if (!authHeader.startsWith(BEARER_PREFIX)) {
            reject(httpRequest, httpResponse, "The authorization token must be Bearer [token]");
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        try {
            httpRequest.setAttribute(USER_ID_ATTRIBUTE, tokenService.readUserId(token));
        } catch (Exception e) {
            LOG.warn("The token of {} {} is not valid: {}", httpRequest.getMethod(),
                    httpRequest.getRequestURI(), e.getMessage());
            reject(httpRequest, httpResponse,
                    "The authorization token is not valid or has expired");
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private void reject(HttpServletRequest request, HttpServletResponse response, String reason)
            throws IOException {
        LOG.warn("Refused {} {}: {}", request.getMethod(), request.getRequestURI(), reason);
        response.sendError(HttpStatus.FORBIDDEN.value(), reason);
    }
}
