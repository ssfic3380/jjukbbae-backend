package com.jjukbbae.oauth.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class AuthTokenTest {

    private Key key;
    private static final String SECRET_KEY = "mysecretkeymysecretkeymysecretkeymysecretkey";

    @BeforeEach
    public void setUp() {
        key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    @Test
    public void testCreateAuthToken_WhenGivenIdAndExpiry_ThenCreateToken() {
        // given
        String userId = "testUser";
        Date expiry = new Date(System.currentTimeMillis() + 1000 * 60 * 10);  // 10분 후 만료

        // when
        AuthToken authToken = new AuthToken(userId, expiry, key);

        // then
        assertThat(authToken).isNotNull();
        assertThat(authToken.getToken()).isNotNull();
    }

    @Test
    public void testCreateAuthToken_WhenGivenIdRoleAndExpiry_ThenCreateToken() {
        // given
        String userId = "testUser";
        String role = "ROLE_USER";
        Date expiry = new Date(System.currentTimeMillis() + 1000 * 60 * 10); // 10분 후 만료

        // when
        AuthToken authToken = new AuthToken(userId, role, expiry, key);

        // then
        assertThat(authToken).isNotNull();
        assertThat(authToken.getToken()).isNotNull();
    }

    @Test
    public void testValidate_WhenValid_ThenSucceed() {
        // given
        String userId = "testUser";
        Date expiry = new Date(System.currentTimeMillis() + 1000 * 60 * 10); // 10분 후 만료
        AuthToken authToken = new AuthToken(userId, expiry, key);

        // when
        boolean isValid = authToken.validate();

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    public void testValidate_WhenExpired_ThenFail() throws InterruptedException {
        // given
        String userId = "testUser";
        Date expiry = new Date(System.currentTimeMillis() + 1000); // 1초 후 만료
        AuthToken authToken = new AuthToken(userId, expiry, key);

        // 잠시 기다려 토큰이 만료되도록 함
        Thread.sleep(1500);

        // when
        boolean isValid = authToken.validate();

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    public void testGetTokenClaims_WhenValid_ThenReturnClaims() {
        // given
        String userId = "testUser";
        String role = "ROLE_USER";
        Date expiry = new Date(System.currentTimeMillis() + 1000 * 60 * 10); // 10분 후 만료
        AuthToken authToken = new AuthToken(userId, role, expiry, key);

        // when
        Claims claims = authToken.getTokenClaims();

        // then
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo(userId);
        assertThat(claims.get("role")).isEqualTo(role);
    }

    @Test
    public void testGetExpiredTokenClaims_WhenExpired_ThenReturnClaims() {
        // given
        String userId = "testUser";
        Date expiry = new Date(System.currentTimeMillis() - 1000); // 이미 만료된 토큰
        AuthToken authToken = new AuthToken(userId, expiry, key);

        // when
        Claims claims = authToken.getExpiredTokenClaims();

        // then
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo(userId);
    }
}