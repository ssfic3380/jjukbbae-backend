package com.jjukbbae.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CookieUtilTest {

    @Test
    public void testGetCookie_whenCookieExists() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie cookie = new Cookie("testName", "testValue");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        // when
        Optional<Cookie> result = CookieUtil.getCookie(request, "testName");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getValue()).isEqualTo("testValue");
    }

    @Test
    public void testGetCookie_whenCookieDoesNotExist() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{});

        // when
        Optional<Cookie> result = CookieUtil.getCookie(request, "testName");

        // then
        assertThat(result).isNotPresent();
    }

    @Test
    public void testAddCookie() {
        // given
        HttpServletResponse response = mock(HttpServletResponse.class);

        // when
        CookieUtil.addCookie(response, "testName", "testValue", 3600);

        // then
        verify(response, times(1)).addCookie(Mockito.argThat(cookie ->
                "testName".equals(cookie.getName()) &&
                        "testValue".equals(cookie.getValue()) &&
                        cookie.getMaxAge() == 3600 &&
                        cookie.isHttpOnly() &&
                        "/".equals(cookie.getPath())
        ));
    }

    @Test
    public void testDeleteCookie_whenCookieExists() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Cookie cookie = new Cookie("testName", "testValue");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        // when
        CookieUtil.deleteCookie(request, response, "testName");

        // then
        verify(response, times(1)).addCookie(Mockito.argThat(c ->
                "testName".equals(c.getName()) &&
                        c.getValue().isEmpty() &&
                        c.getMaxAge() == 0 &&
                        "/".equals(c.getPath())
        ));
    }

    @Test
    public void testDeleteCookie_whenCookieDoesNotExist() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getCookies()).thenReturn(new Cookie[]{});

        // when
        CookieUtil.deleteCookie(request, response, "testName");

        // then
        verify(response, never()).addCookie(any(Cookie.class));
    }

    @Test
    public void testSerializeAndDeserialize() {
        // given
        String originalValue = "testValue";

        // when
        String serialized = CookieUtil.serialize(originalValue);
        Cookie cookie = new Cookie("testCookie", serialized);

        String deserialized = CookieUtil.deserialize(cookie, String.class);

        // then
        assertThat(deserialized).isEqualTo(originalValue);
    }
}