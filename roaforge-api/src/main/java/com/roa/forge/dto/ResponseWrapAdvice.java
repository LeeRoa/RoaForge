package com.roa.forge.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@RestControllerAdvice
public class ResponseWrapAdvice implements ResponseBodyAdvice<Object> {

    // 특정 응답은 래핑 제외하는 어노테이션
    @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface NoWrap {}

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // NoWrap가 붙었으면 제외
        if (returnType.getContainingClass().isAnnotationPresent(NoWrap.class)) return false;
        if (returnType.hasMethodAnnotation(NoWrap.class)) return false;
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType contentType,
                                  Class converterType, ServerHttpRequest request, ServerHttpResponse response) {
        String path = request.getURI().getPath();
        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")
                || path.startsWith("/webjars") || !path.startsWith("/api/")) {
            return body;
        }

        // 이미 ApiResponse면 재래핑 금지
        if (body instanceof ApiResponse<?> ar) return ar;

        // 파일/스트림/바이너리 등은 래핑 금지
        if (body instanceof Resource) return body;
        if (MediaType.APPLICATION_OCTET_STREAM.equals(contentType)) return body;
        if (MediaType.APPLICATION_PDF.equals(contentType)) return body;

        // String을 반환하는 경우(텍스트)에는 직접 래핑해 문자열화
        if (body instanceof String s) {
            // Jackson이 아닌 StringHttpMessageConverter가 처리하므로 수동 직렬화 필요
            try {
                var mapper = new ObjectMapper();
                return mapper.writeValueAsString(ApiResponse.ok(s));
            } catch (Exception e) {
                return "{\"code\":\"0\",\"msg\":\"OK\",\"data\":\"" + s.replace("\"","\\\"") + "\"}";
            }
        }

        // 나머지는 전부 성공 래핑
        return ApiResponse.ok(body);
    }
}