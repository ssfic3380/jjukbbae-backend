package com.jjukbbae.api.repository.user;

import com.jjukbbae.api.entity.user.UserRefreshToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class UserRefreshTokenRepositoryTest {

    @Autowired
    private UserRefreshTokenRepository userRefreshTokenRepository;

    @BeforeEach
    public void setUp() {
        userRefreshTokenRepository.deleteAll();
    }

    @Test
    public void testSaveUserRefreshToken_WhenValid_ThenSaveSuccessfully() {
        // given
        UserRefreshToken refreshToken = new UserRefreshToken("testUserId", "sampleRefreshToken");

        // when
        UserRefreshToken savedRefreshToken = userRefreshTokenRepository.save(refreshToken);

        // then
        assertThat(savedRefreshToken).isNotNull();
        assertThat(savedRefreshToken.getRefreshTokenId()).isNotNull();
        assertThat(savedRefreshToken.getUserId()).isEqualTo("testUserId");
        assertThat(savedRefreshToken.getRefreshToken()).isEqualTo("sampleRefreshToken");
    }

    @Test
    public void testSaveUserRefreshToken_WhenNonUnique_ThenThrowException() {
        // given
        UserRefreshToken refreshToken1 = new UserRefreshToken("testUserId", "sampleRefreshToken1");
        UserRefreshToken refreshToken2 = new UserRefreshToken("testUserId", "sampleRefreshToken2");
        userRefreshTokenRepository.save(refreshToken1);

        // when & then
        assertThatThrownBy(() -> userRefreshTokenRepository.save(refreshToken2))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    public void testFindByUserId() {
        // given
        UserRefreshToken refreshToken = new UserRefreshToken("testUserId", "sampleRefreshToken");
        userRefreshTokenRepository.save(refreshToken);

        // when
        UserRefreshToken foundRefreshToken = userRefreshTokenRepository.findByUserId("testUserId");

        // then
        assertThat(foundRefreshToken).isNotNull();
        assertThat(foundRefreshToken.getUserId()).isEqualTo("testUserId");
        assertThat(foundRefreshToken.getRefreshToken()).isEqualTo("sampleRefreshToken");
    }

    @Test
    public void testFindByUserIdAndRefreshToken() {
        // given
        UserRefreshToken refreshToken = new UserRefreshToken("testUserId", "sampleRefreshToken");
        userRefreshTokenRepository.save(refreshToken);

        // when
        UserRefreshToken foundRefreshToken = userRefreshTokenRepository.findByUserIdAndRefreshToken("testUserId", "sampleRefreshToken");

        // then
        assertThat(foundRefreshToken).isNotNull();
        assertThat(foundRefreshToken.getUserId()).isEqualTo("testUserId");
        assertThat(foundRefreshToken.getRefreshToken()).isEqualTo("sampleRefreshToken");
    }
}