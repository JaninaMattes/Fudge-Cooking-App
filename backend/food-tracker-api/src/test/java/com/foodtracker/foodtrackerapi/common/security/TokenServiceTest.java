package com.foodtracker.foodtrackerapi.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.foodtracker.foodtrackerapi.common.config.ApiProperties;
import com.foodtracker.foodtrackerapi.models.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

/**
 * Tests for {@link TokenService}.
 *
 * @author Janina Mattes
 */
class TokenServiceTest {

    private static final String LONG_SECRET = "a-test-secret-of-more-than-32-characters";
    private static final String SHORT_SECRET = "short";

    private static TokenService serviceWith(String secret, long validityMillis) {
        ApiProperties properties = new ApiProperties();
        properties.setSecretKey(secret);
        properties.setTokenValidityMillis(validityMillis);
        return new TokenService(properties);
    }

    private static User user() {
        return new User(42, "Janina", "Mattes", "a@b.c", "hash");
    }

    @Test
    @DisplayName("A token holds the key and the name of the user")
    void tokenHoldsTheDataOfTheUser() {
        TokenService service = serviceWith(LONG_SECRET, 60_000L);

        Claims claims = service.read(service.issue(user()));

        assertThat(claims.get(TokenService.USER_ID_CLAIM)).isEqualTo(42);
        assertThat(claims.get("email")).isEqualTo("a@b.c");
        assertThat(claims.get("firstName")).isEqualTo("Janina");
        assertThat(claims.get("lastName")).isEqualTo("Mattes");
    }

    @Test
    @DisplayName("The service reads the key of the user out of a token")
    void serviceReadsTheKeyOfTheUser() {
        TokenService service = serviceWith(LONG_SECRET, 60_000L);

        assertThat(service.readUserId(service.issue(user()))).isEqualTo(42);
    }

    @Test
    @DisplayName("A token has a date of issue and a date of expiry")
    void tokenHoldsBothDates() {
        TokenService service = serviceWith(LONG_SECRET, 60_000L);

        Claims claims = service.read(service.issue(user()));

        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    @DisplayName("A secret that is too short still gives a token that works")
    void shortSecretStillWorks() {
        TokenService service = serviceWith(SHORT_SECRET, 60_000L);

        assertThat(service.readUserId(service.issue(user()))).isEqualTo(42);
    }

    @Test
    @DisplayName("A configuration without a secret gives a token that works")
    void emptySecretStillWorks() {
        TokenService service = serviceWith(null, 60_000L);

        assertThat(service.readUserId(service.issue(user()))).isEqualTo(42);
    }

    @Test
    @DisplayName("A token of another secret is not valid")
    void tokenOfAnotherSecretIsRefused() {
        String token = serviceWith(LONG_SECRET, 60_000L).issue(user());
        TokenService other = serviceWith("another-secret-of-more-than-32-characters", 60_000L);

        assertThatThrownBy(() -> other.read(token)).isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("A token that has expired is not valid")
    void expiredTokenIsRefused() {
        TokenService service = serviceWith(LONG_SECRET, -60_000L);
        String token = service.issue(user());

        assertThatThrownBy(() -> service.read(token)).isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("A text that is no token is not valid")
    void textThatIsNoTokenIsRefused() {
        TokenService service = serviceWith(LONG_SECRET, 60_000L);

        assertThatThrownBy(() -> service.read("not-a-token"))
                .isInstanceOf(JwtException.class);
    }
}
