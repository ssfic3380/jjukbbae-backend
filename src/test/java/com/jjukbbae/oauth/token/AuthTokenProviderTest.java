package com.jjukbbae.oauth.token;

import com.jjukbbae.oauth.exception.TokenValidFailedException;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class AuthTokenProviderTest {

    private AuthTokenProvider authTokenProvider;
    private Key key;
    private static final String SECRET_KEY = "mysecretkeymysecretkeymysecretkeymysecretkey";

    @BeforeEach
    public void setUp() {
        key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
        authTokenProvider = new AuthTokenProvider(SECRET_KEY);
    }

    @Test
    public void testCreateAuthToken() {
        // given
        String userId = "testUser";
        Date expiry = new Date(System.currentTimeMillis() + 1000 * 60 * 10); // 10분 후 만료

        // when
        AuthToken authToken = authTokenProvider.createAuthToken(userId, expiry);

        // then
        assertNotNull(authToken);
        assertNotNull(authToken.getToken());
    }

    @Test
    public void testConvertAuthToken() {
        // given
        String userId = "testUser";
        Date expiry = new Date(System.currentTimeMillis() + 1000 * 60 * 10); // 10분 후 만료
        AuthToken originalAuthToken = authTokenProvider.createAuthToken(userId, expiry);
        String tokenString = originalAuthToken.getToken();

        // when
        AuthToken convertedAuthToken = authTokenProvider.convertAuthToken(tokenString);

        // then
        assertNotNull(convertedAuthToken);
        assertEquals(tokenString, convertedAuthToken.getToken());
    }

    @Test
    public void testGetAuthentication_Success() {
        // given
        String userId = "testUser";
        String role = "ROLE_USER";
        Date expiry = new Date(System.currentTimeMillis() + 1000 * 60 * 10); // 10분 후 만료
        AuthToken authToken = authTokenProvider.createAuthToken(userId, role, expiry);

        // when
        Authentication authentication = authTokenProvider.getAuthentication(authToken);

        // then
        assertNotNull(authentication);
        assertEquals(userId, authentication.getName());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(role)));
    }

    @Test
    public void testGetAuthentication_InvalidToken() {
        // given
        String invalidToken = "invalidTokenString";
        AuthToken authToken = authTokenProvider.convertAuthToken(invalidToken);

        // when & then
        assertThrows(TokenValidFailedException.class, () -> {
            authTokenProvider.getAuthentication(authToken);
        });
    }
}