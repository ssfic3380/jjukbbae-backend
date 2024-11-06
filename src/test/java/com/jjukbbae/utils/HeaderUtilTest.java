package com.jjukbbae.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class HeaderUtilTest {

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
    }

    @Test
    void testGetAccessToken_WithBearerToken() {
        // given
        String token = "sampleToken";
        request.addHeader("Authorization", "Bearer " + token);

        // when
        String accessToken = HeaderUtil.getAccessToken(request);

        // then
        assertThat(accessToken).isEqualTo(token);
    }

    @Test
    void testGetAccessToken_WithoutBearerToken() {
        // given
        String token = "sampleToken";
        request.addHeader("Authorization", token);

        // when
        String accessToken = HeaderUtil.getAccessToken(request);

        // then
        assertThat(accessToken).isNull();
    }
}