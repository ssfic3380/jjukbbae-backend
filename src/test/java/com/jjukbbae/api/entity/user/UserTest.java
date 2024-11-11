package com.jjukbbae.api.entity.user;

import com.jjukbbae.oauth.entity.ProviderType;
import com.jjukbbae.oauth.entity.RoleType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testUser_WhenValid_ThenCreateSuccessfully() {
        // given
        User user = new User("testUserId", "testUsername", "test@example.com", "Y", "http://test.com/profile.jpg", ProviderType.GOOGLE, RoleType.USER, LocalDateTime.now(), LocalDateTime.now());

        // when
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    public void testUser_WhenFieldsEmpty_ThenInvalid() {
        // given
        User user = new User();

        // when
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // then
        assertThat(violations).isNotEmpty();
    }

    @Test
    public void testUser_WhenMaxSizeExceeded_ThenInvalid() {
        // given
        String longUserId = "a".repeat(65);
        String longUsername = "b".repeat(101);
        String longPassword = "c".repeat(129);
        String longEmail = "d".repeat(513);
        String longProfileImageUrl = "e".repeat(513);
        User user = new User(longUserId, longUsername, longEmail, "Y", longProfileImageUrl, ProviderType.GOOGLE, RoleType.USER, LocalDateTime.now(), LocalDateTime.now());
        user.setPassword(longPassword);

        // when
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // then
        assertThat(violations).isNotEmpty();
    }
}