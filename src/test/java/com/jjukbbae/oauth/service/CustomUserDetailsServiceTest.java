package com.jjukbbae.oauth.service;

import com.jjukbbae.api.entity.user.User;
import com.jjukbbae.api.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLoadUserByUsername_WhenUsernameExist_ThenReturnUserDetails() {
        // given
        User user = new User(
                "testUserId",
                "Test User",
                "test@example.com",
                "Y",
                "http://test.com/profile.jpg",
                null,
                null,
                null,
                null
        );
        when(userRepository.findByUserId("testUserId")).thenReturn(user);

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testUserId");

        // then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("testUserId");
    }

    @Test
    public void testLoadUserByUsername_WhenUsernameNotExist_ThenThrowUsernameNotFoundException() {
        // given
        when(userRepository.findByUserId(anyString())).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("invalidUser"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Can not find username.");
    }

    @Test
    public void testLoadUserByUsername_WhenUsernameNull_ThenThrowUsernameNotFoundException() {
        // given
        when(userRepository.findByUserId(null)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(null))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Can not find username.");
    }
}