package com.foodtracker.foodtrackerapi.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.foodtracker.foodtrackerapi.common.config.ApiProperties;
import com.foodtracker.foodtrackerapi.common.security.TokenService;
import com.foodtracker.foodtrackerapi.models.User;

/**
 * Tests for {@link AuthFilter}.
 *
 * @author Janina Mattes
 */
@ExtendWith(MockitoExtension.class)
class AuthFilterTest {

    private static final String SECRET_KEY = "a-test-secret-of-more-than-32-characters";

    @Mock
    private FilterChain filterChain;

    private AuthFilter authFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private static String tokenFor(int userId, long validityMillis) {
        return tokenService(validityMillis)
                .issue(new User(userId, "Janina", "Mattes", "a@b.c", "hash"));
    }

    private static TokenService tokenService(long validityMillis) {
        ApiProperties apiProperties = new ApiProperties();
        apiProperties.setSecretKey(SECRET_KEY);
        apiProperties.setTokenValidityMillis(validityMillis);
        return new TokenService(apiProperties);
    }

    @BeforeEach
    void setUp() {
        authFilter = new AuthFilter(tokenService(60_000L));
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("A request with a valid token reaches the next filter")
    void validTokenPassesTheFilter() throws Exception {
        request.addHeader(AuthFilter.AUTH_HEADER,
                AuthFilter.BEARER_PREFIX + tokenFor(42, 60_000L));

        authFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(request.getAttribute(AuthFilter.USER_ID_ATTRIBUTE)).isEqualTo(42);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("A request without a token gets the status 403")
    void requestWithoutTokenIsRejected() throws Exception {
        authFilter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getErrorMessage()).contains("necessary");
    }

    @Test
    @DisplayName("A request with an empty header gets the status 403")
    void requestWithEmptyHeaderIsRejected() throws Exception {
        request.addHeader(AuthFilter.AUTH_HEADER, "");

        authFilter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("A request without the word Bearer gets the status 403")
    void requestWithoutBearerPrefixIsRejected() throws Exception {
        request.addHeader(AuthFilter.AUTH_HEADER, tokenFor(42, 60_000L));

        authFilter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getErrorMessage()).contains("Bearer");
    }

    @Test
    @DisplayName("A request with a token that is not valid gets the status 403")
    void requestWithInvalidTokenIsRejected() throws Exception {
        request.addHeader(AuthFilter.AUTH_HEADER, AuthFilter.BEARER_PREFIX + "not-a-token");

        authFilter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getErrorMessage()).contains("not valid");
    }

    @Test
    @DisplayName("A request with a token that has expired gets the status 403")
    void requestWithExpiredTokenIsRejected() throws Exception {
        request.addHeader(AuthFilter.AUTH_HEADER,
                AuthFilter.BEARER_PREFIX + tokenFor(42, -60_000L));

        authFilter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }
}
