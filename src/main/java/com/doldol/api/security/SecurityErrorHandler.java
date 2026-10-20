package com.doldol.api.security;

import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * 시큐리티에서 막힌 요청(401 · 403)도 GlobalExceptionHandler 와 같은 JSON 으로 응답한다.
 * 필터 단계라 @RestControllerAdvice 가 직접 못 잡으므로, 예외를 HandlerExceptionResolver 에 넘겨 처리하게 한다.
 */
@Component
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final HandlerExceptionResolver resolver;

    // @Qualifier 가 생성자 파라미터에 있어야 해서 롬복 대신 직접 생성자를 쓴다
    public SecurityErrorHandler(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    /** 로그인 안 됨 → 401 */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) {
        resolver.resolveException(request, response, null, new BusinessException(ErrorCode.UNAUTHORIZED));
    }

    /** 로그인은 됐지만 권한 없음 → 403 */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) {
        resolver.resolveException(request, response, null, new BusinessException(ErrorCode.FORBIDDEN));
    }

}