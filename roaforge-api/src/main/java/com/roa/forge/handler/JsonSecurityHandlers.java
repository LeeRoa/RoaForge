package com.roa.forge.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roa.forge.dto.ApiResponse;
import com.roa.forge.dto.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import java.io.IOException;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class JsonSecurityHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper mapper;
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) throws IOException {
        write(res, ErrorCode.UNAUTHORIZED, localeResolver.resolveLocale(req));
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex) throws IOException {
        write(res, ErrorCode.FORBIDDEN, localeResolver.resolveLocale(req));
    }

    private void write(HttpServletResponse res, ErrorCode ec, Locale locale) throws IOException {
        res.setStatus(ec.status().value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        String m = messageSource.getMessage(ec.key(), null, ec.message(), locale);
        mapper.writeValue(res.getWriter(), ApiResponse.fail(ec.code(), m));
    }
}