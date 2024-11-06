package com.jjukbbae.oauth.handler;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TokenAccessDeniedHandlerTest {

    @Mock
    private HandlerExceptionResolver handlerExceptionResolver;
    private TokenAccessDeniedHandler tokenAccessDeniedHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tokenAccessDeniedHandler = new TokenAccessDeniedHandler(handlerExceptionResolver);
    }

    @Test
    void testHandle_ShouldResolveException() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AccessDeniedException accessDeniedException = new AccessDeniedException("Access Denied");

        // when
        tokenAccessDeniedHandler.handle(request, response, accessDeniedException);

        // then
        verify(handlerExceptionResolver, times(1)).resolveException(request, response, null, accessDeniedException);
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK); // 상태가 403이 아닌 다른 이유로 변경되지 않았는지 확인
    }
}