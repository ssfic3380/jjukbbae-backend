package com.jjukbbae.oauth.service;

import com.jjukbbae.api.entity.user.User;
import com.jjukbbae.api.repository.user.UserRepository;
import com.jjukbbae.oauth.entity.ProviderType;
import com.jjukbbae.oauth.entity.RoleType;
import com.jjukbbae.oauth.exception.OAuthProviderMissMatchException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testProcess_WhenUserExist_ThenReturnUserPrincipal() {
        // given
        OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
        ClientRegistration clientRegistration = mock(ClientRegistration.class);
        when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn("google");
        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority(RoleType.USER.getCode())),
                Map.of("sub", "testUserId", "name", "Test User", "email", "test@example.com", "picture", "http://test.com/profile.jpg"),
                "sub"
        );
        User savedUser = new User(
                "testUserId",
                "Test User",
                "test@example.com",
                "Y",
                "http://test.com/profile.jpg",
                ProviderType.GOOGLE,
                RoleType.USER,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(userRepository.findByUserId("testUserId")).thenReturn(savedUser);

        // when
        OAuth2User result = customOAuth2UserService.process(userRequest, oAuth2User);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("testUserId");
    }

    @Test
    public void testProcess_WhenUserNotExist_ThenCreateAndReturnUserPrincipal() {
        // given
        OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
        ClientRegistration clientRegistration = mock(ClientRegistration.class);
        when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn("google");
        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority(RoleType.USER.getCode())),
                Map.of("sub", "newUserId", "name", "New User", "email", "new@example.com", "picture", "http://test.com/profile.jpg"),
                "sub"
        );
        when(userRepository.findByUserId("newUserId")).thenReturn(null);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        OAuth2User result = customOAuth2UserService.process(userRequest, oAuth2User);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("newUserId");
    }

    @Test
    public void testProcess_WhenProviderMismatched_ThenThrowException() {
        // given
        OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
        ClientRegistration clientRegistration = mock(ClientRegistration.class);
        when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn("google");
        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority(RoleType.USER.getCode())),
                Map.of("sub", "testUserId", "name", "Test User", "email", "test@example.com", "picture", "http://test.com/profile.jpg"),
                "sub"
        );
        User savedUser = new User(
                "testUserId",
                "Test User",
                "test@example.com",
                "Y",
                "http://test.com/profile.jpg",
                ProviderType.FACEBOOK, // 다른 ProviderType으로 설정
                RoleType.USER,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(userRepository.findByUserId("testUserId")).thenReturn(savedUser);

        // when & then
        assertThatThrownBy(() -> customOAuth2UserService.process(userRequest, oAuth2User))
                .isInstanceOf(OAuthProviderMissMatchException.class)
                .hasMessageContaining("이미 FACEBOOK 계정으로 가입된 상태입니다");
    }

    @Test
    public void testProcess_WhenProviderMismatchedAndUserUpdated_ThenUpdateUserSuccessfully() {
        // given
        OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
        ClientRegistration clientRegistration = mock(ClientRegistration.class);
        when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn("google");
        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority(RoleType.USER.getCode())),
                Map.of("sub", "testUserId", "name", "Updated User", "email", "test@example.com", "picture", "http://test.com/updated_profile.jpg"),
                "sub"
        );
        User savedUser = new User(
                "testUserId",
                "Old User",
                "test@example.com",
                "Y",
                "http://test.com/old_profile.jpg",
                ProviderType.GOOGLE,
                RoleType.USER,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(userRepository.findByUserId("testUserId")).thenReturn(savedUser);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        OAuth2User result = customOAuth2UserService.process(userRequest, oAuth2User);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("testUserId");
        verify(userRepository, times(1)).saveAndFlush(savedUser);
        assertThat(savedUser.getUsername()).isEqualTo("Updated User");
        assertThat(savedUser.getProfileImageUrl()).isEqualTo("http://test.com/updated_profile.jpg");
    }
}