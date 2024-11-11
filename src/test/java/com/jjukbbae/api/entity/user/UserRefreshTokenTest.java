package com.jjukbbae.api.entity.user;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserRefreshTokenTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testUserRefreshToken_WhenValid_ThenCreateSuccessfully() {
        // given
        UserRefreshToken refreshToken = new UserRefreshToken("testUserId", "sampleRefreshToken");

        // when
        Set<ConstraintViolation<UserRefreshToken>> violations = validator.validate(refreshToken);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    public void testUserRefreshToken_WhenFieldsEmpty_ThenInvalid() {
        // given
        UserRefreshToken refreshToken = new UserRefreshToken();

        // when
        Set<ConstraintViolation<UserRefreshToken>> violations = validator.validate(refreshToken);

        // then
        assertThat(violations).isNotEmpty();
    }

    @Test
    public void testUserRefreshToken_WhenMaxSizeExceeded_ThenInvalid() {
        // given
        String longUserId = "a".repeat(65);
        String longRefreshToken = "b".repeat(257);
        UserRefreshToken refreshToken = new UserRefreshToken(longUserId, longRefreshToken);

        // when
        Set<ConstraintViolation<UserRefreshToken>> violations = validator.validate(refreshToken);

        // then
        assertThat(violations).isNotEmpty();
    }
}