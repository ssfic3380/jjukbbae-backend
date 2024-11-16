package com.jjukbbae.oauth.handler;

import com.jjukbbae.api.entity.user.UserRefreshToken;
import com.jjukbbae.api.repository.user.UserRefreshTokenRepository;
import com.jjukbbae.config.properties.AppProperties;
import com.jjukbbae.oauth.entity.ProviderType;
import com.jjukbbae.oauth.info.OAuth2UserInfo;
import com.jjukbbae.oauth.info.OAuth2UserInfoFactory;
import com.jjukbbae.oauth.repository.OAuth2AuthorizationRequestBasedOnCookieRepository;
import com.jjukbbae.oauth.token.AuthToken;
import com.jjukbbae.oauth.token.AuthTokenProvider;
import com.jjukbbae.utils.CookieUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static com.jjukbbae.oauth.repository.OAuth2AuthorizationRequestBasedOnCookieRepository.REDIRECT_URI_PARAM_COOKIE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private AuthTokenProvider tokenProvider;

    @Mock
    private AppProperties appProperties;

    @Mock
    private UserRefreshTokenRepository userRefreshTokenRepository;

    @Mock
    private OAuth2AuthorizationRequestBasedOnCookieRepository authorizationRequestRepository;

    @InjectMocks
    private OAuth2AuthenticationSuccessHandler successHandler;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private Authentication authentication;

    @BeforeEach
    public void setUp() {
        openMocks(this);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        authentication = mock(OAuth2AuthenticationToken.class);
    }

    @Test
    public void testDetermineTargetUrl() {
        // given
        String redirectUri = "http://localhost:8080/oauth2/redirect";
        request.setCookies(new Cookie(REDIRECT_URI_PARAM_COOKIE_NAME, redirectUri));

        // AppProperties의 Auth, OAuth2 클래스 모킹
        AppProperties.Auth authProperties = mock(AppProperties.Auth.class);
        when(appProperties.getAuth()).thenReturn(authProperties);
        when(authProperties.getTokenSecret()).thenReturn("testSecret");
        when(authProperties.getTokenExpiry()).thenReturn(1800000L);
        when(authProperties.getRefreshTokenExpiry()).thenReturn(604800000L);

        AppProperties.OAuth2 oauth2Properties = mock(AppProperties.OAuth2.class);
        when(appProperties.getOauth2()).thenReturn(oauth2Properties);
        when(oauth2Properties.getAuthorizedRedirectUris()).thenReturn(Collections.singletonList(redirectUri));

        // ProviderType 반환
        when(((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId()).thenReturn("google");

        // OAuth2UserInfo 설정
        OidcUser user = mock(OidcUser.class);
        OAuth2UserInfo userInfo = mock(OAuth2UserInfo.class);
        Map<String, Object> attributes = Map.of(
                "sub", "test_user",
                "name", "Test User",
                "email", "testuser@example.com",
                "picture", "http://example.com/user.jpg"
        );
        when(authentication.getPrincipal()).thenReturn(user);
        when(user.getAttributes()).thenReturn(attributes);

        // accessToken
        AuthToken accessToken = mock(AuthToken.class);
        when(accessToken.getToken()).thenReturn("testAccessToken");
        when(tokenProvider.createAuthToken(anyString(), anyString(), any(Date.class))).thenReturn(accessToken);

        // refreshToken
        AuthToken refreshToken = mock(AuthToken.class);
        when(refreshToken.getToken()).thenReturn("testRefreshToken");
        when(tokenProvider.createAuthToken(anyString(), any(Date.class))).thenReturn(refreshToken);

        // userRefreshToken
        UserRefreshToken userRefreshToken = mock(UserRefreshToken.class);
        when(userRefreshTokenRepository.findByUserId(anyString())).thenReturn(userRefreshToken);
        doAnswer(invocation -> {
            String newRefreshToken = invocation.getArgument(0);
            return null;
        }).when(userRefreshToken).setRefreshToken(anyString());

        // when
        String result = successHandler.determineTargetUrl(request, response, authentication);

        // then
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", "testAccessToken")
                .build().toUriString();
        assertThat(result).isEqualTo(targetUrl);
    }

    @Test
    public void testDetermineTargetUrl_WhenUnauthorizedRedirectUri_ThenThrowIllegalArgumentException() {
        // given
        String unauthorizedUri = "http://unauthorized.com";
        request.setCookies(new Cookie(REDIRECT_URI_PARAM_COOKIE_NAME, unauthorizedUri));
        AppProperties.OAuth2 oauth2Properties = mock(AppProperties.OAuth2.class);
        when(appProperties.getOauth2()).thenReturn(oauth2Properties);
        when(oauth2Properties.getAuthorizedRedirectUris()).thenReturn(Collections.singletonList("http://localhost:8080/oauth2/redirect"));

        // when & then
        assertThatThrownBy(() -> successHandler.determineTargetUrl(request, response, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unauthorized Redirect URI");
    }
}
