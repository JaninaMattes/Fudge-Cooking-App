package com.foodtracker.foodtrackerapi.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.filter.GenericFilterBean;

import com.foodtracker.foodtrackerapi.common.config.ApiProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

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
 * @author Janina Mattes
 */
public class AuthFilter extends GenericFilterBean {

    /** Name of the header that holds the token. */
    static final String AUTH_HEADER = "Authorization";

    /** Text that stands before the token in the header. */
    static final String BEARER_PREFIX = "Bearer ";

    /** Name of the request attribute that holds the user ID. */
    static final String USER_ID_ATTRIBUTE = "userId";

    private final ApiProperties apiProperties;

    /**
     * Makes the filter.
     *
     * @param apiProperties configuration that holds the signature key
     */
    public AuthFilter(ApiProperties apiProperties) {
        this.apiProperties = apiProperties;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
            FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        String authHeader = httpRequest.getHeader(AUTH_HEADER);
        if (authHeader == null || authHeader.isEmpty()) {
            reject(httpResponse, "An authorization token is necessary");
            return;
        }
        if (!authHeader.startsWith(BEARER_PREFIX)) {
            reject(httpResponse, "The authorization token must be Bearer [token]");
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(apiProperties.getSecretKey())
                    .parseClaimsJws(token)
                    .getBody();
            httpRequest.setAttribute(USER_ID_ATTRIBUTE,
                    Integer.parseInt(String.valueOf(claims.get(USER_ID_ATTRIBUTE))));
        } catch (Exception e) {
            reject(httpResponse, "The authorization token is not valid or has expired");
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private void reject(HttpServletResponse response, String reason) throws IOException {
        response.sendError(HttpStatus.FORBIDDEN.value(), reason);
    }
}
