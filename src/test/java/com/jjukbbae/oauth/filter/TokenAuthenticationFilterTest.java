package com.jjukbbae.oauth.filter;

import com.jjukbbae.oauth.token.AuthToken;
import com.jjukbbae.oauth.token.AuthTokenProvider;
import com.jjukbbae.utils.HeaderUtil;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

class TokenAuthenticationFilterTest {

    @Mock
    private AuthTokenProvider tokenProvider;
    @Mock
    private AuthToken token;
    private TokenAuthenticationFilter tokenAuthenticationFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tokenAuthenticationFilter = new TokenAuthenticationFilter(tokenProvider);
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_ValidToken_ShouldSetAuthentication() throws ServletException, IOException {
        try (MockedStatic<HeaderUtil> mockedHeaderUtil = mockStatic(HeaderUtil.class)) {
            // given
            String validTokenString = "validToken";
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();

            request.addHeader("Authorization", "Bearer " + validTokenString);
            mockedHeaderUtil.when(() -> HeaderUtil.getAccessToken(request)).thenReturn(validTokenString);
            when(tokenProvider.convertAuthToken(validTokenString)).thenReturn(token);
            when(token.validate()).thenReturn(true);
            when(tokenProvider.getAuthentication(token)).thenReturn(mock(Authentication.class));

            // when
            tokenAuthenticationFilter.doFilter(request, response, filterChain);

            // then
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        }
    }

    @Test
    void testDoFilterInternal_InvalidToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        try (MockedStatic<HeaderUtil> mockedHeaderUtil = mockStatic(HeaderUtil.class)) {
            // given
            String invalidTokenString = "invalidToken";
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();

            request.addHeader("Authorization", "Bearer " + invalidTokenString);
            mockedHeaderUtil.when(() -> HeaderUtil.getAccessToken(request)).thenReturn(invalidTokenString);
            when(tokenProvider.convertAuthToken(invalidTokenString)).thenReturn(token);
            when(token.validate()).thenReturn(false);

            // when
            tokenAuthenticationFilter.doFilter(request, response, filterChain);

            // then
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }
}