package com.jjukbbae.api.repository.user;

import com.jjukbbae.api.entity.user.User;
import com.jjukbbae.oauth.entity.ProviderType;
import com.jjukbbae.oauth.entity.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
    }

    @Test
    public void testSaveUser() {
        // given
        User user = new User("testUserId", "testUsername", "test@example.com", "Y", "http://test.com/profile.jpg", ProviderType.GOOGLE, RoleType.USER, LocalDateTime.now(), LocalDateTime.now());

        // when
        User savedUser = userRepository.save(user);

        // then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUserSeq()).isNotNull();
        assertThat(savedUser.getUserId()).isEqualTo("testUserId");
    }

    @Test
    public void testSaveUser_NonUnique_ShouldThrowException() {
        // given
        User user1 = new User("testUserId", "testUsername", "test@example.com", "Y", "http://test.com/profile.jpg", ProviderType.GOOGLE, RoleType.USER, LocalDateTime.now(), LocalDateTime.now());
        User user2 = new User("testUserId", "testUsername", "test@example.com", "Y", "http://test.com/profile.jpg", ProviderType.GOOGLE, RoleType.USER, LocalDateTime.now(), LocalDateTime.now());
        userRepository.save(user1);

        // when & then
        assertThatThrownBy(() -> userRepository.save(user2))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    public void testFindByUserId() {
        // given
        User user = new User("testUserId", "testUsername", "test@example.com", "Y", "http://test.com/profile.jpg", ProviderType.GOOGLE, RoleType.USER, LocalDateTime.now(), LocalDateTime.now());
        userRepository.save(user);

        // when
        User foundUser = userRepository.findByUserId("testUserId");

        // then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUserId()).isEqualTo("testUserId");
    }
}