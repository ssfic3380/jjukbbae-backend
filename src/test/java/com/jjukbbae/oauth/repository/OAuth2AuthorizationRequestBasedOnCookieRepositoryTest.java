package com.jjukbbae.oauth.repository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OAuth2AuthorizationRequestBasedOnCookieRepositoryTest {

    private OAuth2AuthorizationRequestBasedOnCookieRepository repository;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    public void setUp() {
        repository = new OAuth2AuthorizationRequestBasedOnCookieRepository();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
    }

    @Test
    public void testLoadAuthorizationRequest() {
        // given
        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
                .clientId("test-client-id")
                .authorizationUri("https://example.com/auth")
                .build();

        String serializedRequest = Base64.getUrlEncoder().encodeToString(org.springframework.util.SerializationUtils.serialize(authorizationRequest));
        Cookie cookie = new Cookie(OAuth2AuthorizationRequestBasedOnCookieRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, serializedRequest);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        // when
        OAuth2AuthorizationRequest loadedRequest = repository.loadAuthorizationRequest(request);

        // then
        assertThat(loadedRequest).isNotNull();
        assertThat(loadedRequest.getClientId()).isEqualTo("test-client-id");
    }

    @Test
    public void testSaveAuthorizationRequest_WhenRequestNotNull_ThenSaveCookie() {
        // given
        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
                .clientId("test-client-id")
                .authorizationUri("https://example.com/auth")
                .build();

        // when
        repository.saveAuthorizationRequest(authorizationRequest, request, response);

        // then
        verify(response, times(1)).addCookie(argThat(cookie ->
                OAuth2AuthorizationRequestBasedOnCookieRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME.equals(cookie.getName()) &&
                        cookie.getValue() != null &&
                        cookie.getMaxAge() == 180
        ));
    }

    @Test
    public void testSaveAuthorizationRequest_WhenRequestNull_ThenExpireCookies() {
        // given
        OAuth2AuthorizationRequest authorizationRequest = null;
        Cookie oauthCookie = new Cookie(OAuth2AuthorizationRequestBasedOnCookieRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, "test-value");
        Cookie redirectCookie = new Cookie(OAuth2AuthorizationRequestBasedOnCookieRepository.REDIRECT_URI_PARAM_COOKIE_NAME, "test-redirect-uri");
        Cookie refreshTokenCookie = new Cookie(OAuth2AuthorizationRequestBasedOnCookieRepository.REFRESH_TOKEN, "test-refresh-token");
        when(request.getCookies()).thenReturn(new Cookie[]{oauthCookie, redirectCookie, refreshTokenCookie});

        // when
        repository.saveAuthorizationRequest(authorizationRequest, request, response);

        // then
        verify(response, times(1)).addCookie(argThat(cookie ->
                OAuth2AuthorizationRequestBasedOnCookieRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME.equals(cookie.getName()) &&
                        cookie.getValue().isEmpty() &&
                        cookie.getMaxAge() == 0
        ));
        verify(response, times(1)).addCookie(argThat(cookie ->
                OAuth2AuthorizationRequestBasedOnCookieRepository.REDIRECT_URI_PARAM_COOKIE_NAME.equals(cookie.getName()) &&
                        cookie.getValue().isEmpty() &&
                        cookie.getMaxAge() == 0
        ));
        verify(response, times(1)).addCookie(argThat(cookie ->
                OAuth2AuthorizationRequestBasedOnCookieRepository.REFRESH_TOKEN.equals(cookie.getName()) &&
                        cookie.getValue().isEmpty() &&
                        cookie.getMaxAge() == 0
        ));
    }

    @Test
    public void testRemoveAuthorizationRequestCookies() {
        // given
        Cookie oauthCookie = new Cookie(OAuth2AuthorizationRequestBasedOnCookieRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, "test-value");
        Cookie redirectCookie = new Cookie(OAuth2AuthorizationRequestBasedOnCookieRepository.REDIRECT_URI_PARAM_COOKIE_NAME, "test-redirect-uri");
        Cookie refreshTokenCookie = new Cookie(OAuth2AuthorizationRequestBasedOnCookieRepository.REFRESH_TOKEN, "test-refresh-token");
        when(request.getCookies()).thenReturn(new Cookie[]{oauthCookie, redirectCookie, refreshTokenCookie});

        // when
        repository.removeAuthorizationRequestCookies(request, response);

        // then
        verify(response, times(1)).addCookie(argThat(cookie ->
                OAuth2AuthorizationRequestBasedOnCookieRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME.equals(cookie.getName()) &&
                        cookie.getValue().isEmpty() &&
                        cookie.getMaxAge() == 0
        ));
        verify(response, times(1)).addCookie(argThat(cookie ->
                OAuth2AuthorizationRequestBasedOnCookieRepository.REDIRECT_URI_PARAM_COOKIE_NAME.equals(cookie.getName()) &&
                        cookie.getValue().isEmpty() &&
                        cookie.getMaxAge() == 0
        ));
        verify(response, times(1)).addCookie(argThat(cookie ->
                OAuth2AuthorizationRequestBasedOnCookieRepository.REFRESH_TOKEN.equals(cookie.getName()) &&
                        cookie.getValue().isEmpty() &&
                        cookie.getMaxAge() == 0
        ));
    }
}