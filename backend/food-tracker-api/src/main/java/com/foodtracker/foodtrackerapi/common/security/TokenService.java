package com.foodtracker.foodtrackerapi.common.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.foodtracker.foodtrackerapi.common.config.ApiProperties;
import com.foodtracker.foodtrackerapi.models.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * <p>
 * One place that writes a JWT token and reads a JWT token. The login endpoint
 * writes a token, and {@code AuthFilter} reads it. Both used their own copy of
 * the jjwt calls before, so a change of the algorithm needed two changes.
 * </p>
 *
 * <p>
 * The signature uses HMAC with SHA-256. That algorithm needs a key of 32
 * bytes, and the configured text can be shorter. The class therefore takes the
 * SHA-256 hash of the text as the key. A short text stays a weak secret, so the
 * class writes a warning when the configuration holds no secret of its own.
 * </p>
 *
 * @author Janina Mattes
 */
@Component
public class TokenService {

    /** Name of the claim that holds the key of the user. */
    public static final String USER_ID_CLAIM = "userId";

    /** Lowest number of characters that a secret needs. */
    static final int MINIMUM_SECRET_LENGTH = 32;

    private static final Logger LOG = LoggerFactory.getLogger(TokenService.class);

    private final SecretKey signatureKey;
    private final long validityMillis;

    /**
     * Makes the service.
     *
     * @param apiProperties configuration that holds the secret and the lifetime
     *                      of a token
     */
    public TokenService(ApiProperties apiProperties) {
        String secret = apiProperties.getSecretKey();
        if (secret == null || secret.length() < MINIMUM_SECRET_LENGTH) {
            LOG.warn("The signature key of the tokens is short or empty. "
                    + "Set FT_API_SECRET_KEY to a text of {} characters or more.",
                    MINIMUM_SECRET_LENGTH);
        }
        this.signatureKey = Keys.hmacShaKeyFor(sha256(secret == null ? "" : secret));
        this.validityMillis = apiProperties.getTokenValidityMillis();
    }

    /**
     * Writes a token for a user that logged in.
     *
     * @param user user that logged in
     * @return the token as text
     */
    public String issue(User user) {
        long timestamp = System.currentTimeMillis();
        String token = Jwts.builder()
                .signWith(signatureKey, Jwts.SIG.HS256)
                .issuedAt(new Date(timestamp))
                .expiration(new Date(timestamp + validityMillis))
                .claim(USER_ID_CLAIM, user.getUserId())
                .claim("email", user.getEmail())
                .claim("firstName", user.getFirstName())
                .claim("lastName", user.getLastName())
                .compact();
        LOG.debug("Wrote a token for the user {} that is valid for {} ms",
                user.getUserId(), validityMillis);
        return token;
    }

    /**
     * Reads the content of a token.
     *
     * @param token token as text, without the word "Bearer"
     * @return the claims of the token
     * @throws JwtException when the signature is wrong, when the text is no
     *                      token or when the token has expired
     */
    public Claims read(String token) {
        return Jwts.parser()
                .verifyWith(signatureKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Reads the key of the user out of a token.
     *
     * @param token token as text, without the word "Bearer"
     * @return the key of the user
     * @throws JwtException when the token is not valid
     */
    public int readUserId(String token) {
        return Integer.parseInt(String.valueOf(read(token).get(USER_ID_CLAIM)));
    }

    private static byte[] sha256(String text) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(text.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            // Every Java platform holds SHA-256, so this line never runs.
            throw new IllegalStateException("The platform has no SHA-256", e);
        }
    }
}
