package com.jjukbbae.oauth.handler;

import com.jjukbbae.oauth.repository.OAuth2AuthorizationRequestBasedOnCookieRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.RedirectStrategy;

import java.io.IOException;

import static com.jjukbbae.oauth.repository.OAuth2AuthorizationRequestBasedOnCookieRepository.REDIRECT_URI_PARAM_COOKIE_NAME;
import static org.mockito.Mockito.*;

class OAuth2AuthenticationFailureHandlerTest {

    @Mock
    private OAuth2AuthorizationRequestBasedOnCookieRepository authorizationRequestRepository;

    @Mock
    private RedirectStrategy redirectStrategy;

    private OAuth2AuthenticationFailureHandler failureHandler;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        failureHandler = new OAuth2AuthenticationFailureHandler(authorizationRequestRepository);
        failureHandler.setRedirectStrategy(redirectStrategy);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void testOnAuthenticationFailure() throws IOException, ServletException {
        // given
        String redirectUri = "http://localhost:8080/loginFailure";
        Cookie redirectCookie = new Cookie(REDIRECT_URI_PARAM_COOKIE_NAME, redirectUri);
        request.setCookies(redirectCookie);

        AuthenticationException exception = mock(AuthenticationException.class);
        when(exception.getLocalizedMessage()).thenReturn("Authentication failed");

        // when
        failureHandler.onAuthenticationFailure(request, response, exception);

        // then
        String expectedUrl = redirectUri + "?error=Authentication failed";
        verify(authorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
        verify(redirectStrategy).sendRedirect(request, response, expectedUrl);
    }

    @Test
    void testOnAuthenticationFailure_whenRedirectUriIsMissing_thenRedirectToDefaultUriWithErrorMessage() throws IOException, ServletException {
        // given
        AuthenticationException exception = mock(AuthenticationException.class);
        when(exception.getLocalizedMessage()).thenReturn("Default error");

        // when
        failureHandler.onAuthenticationFailure(request, response, exception);

        // then
        String expectedUrl = "/?error=Default error";
        verify(authorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
        verify(redirectStrategy).sendRedirect(request, response, expectedUrl);
    }
}