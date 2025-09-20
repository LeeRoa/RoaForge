package com.roa.forge.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;

@RestControllerAdvice
@RequiredArgsConstructor
public class ResponseWrapAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper mapper;
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface NoWrap {}

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        if (returnType.getContainingClass().isAnnotationPresent(NoWrap.class)) return false;
        if (returnType.hasMethodAnnotation(NoWrap.class)) return false;
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType contentType,
                                  Class converterType, ServerHttpRequest request, ServerHttpResponse response) {

        String path = request.getURI().getPath();
        // 문서/정적 또는 /api/가 아닌 경로는 래핑 제외
        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")
                || path.startsWith("/webjars") || !path.startsWith("/api/")) {
            return body;
        }

        if (body instanceof ApiResponse<?> ar) return ar; // 이미 래핑됨
        if (body instanceof Resource) return body;
        if (MediaType.APPLICATION_OCTET_STREAM.equals(contentType) || MediaType.APPLICATION_PDF.equals(contentType)) return body;

        Locale locale = (request instanceof ServletServerHttpRequest sr)
                ? localeResolver.resolveLocale(sr.getServletRequest())
                : Locale.KOREAN;
        String okMsg = messageSource.getMessage("ok", null, "OK", locale);

        if (body instanceof String s) {
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            try {
                return mapper.writeValueAsString(ApiResponse.ok(s));
            } catch (Exception e) {
                // 최후의 보루
                return "{\"code\":\"0\",\"msg\":\"" + okMsg + "\",\"data\":\"" + s.replace("\"","\\\"") + "\"}";
            }
        }
        return ApiResponse.ok(body);
    }
}