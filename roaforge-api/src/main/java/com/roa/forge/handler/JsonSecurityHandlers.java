package com.roa.forge.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roa.forge.dto.ApiResponse;
import com.roa.forge.dto.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JsonSecurityHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper mapper;

    @Override
    public void commence(jakarta.servlet.http.HttpServletRequest req,
                         HttpServletResponse res,
                         org.springframework.security.core.AuthenticationException ex) throws IOException {
        write(res, ErrorCode.UNAUTHORIZED);
    }

    @Override
    public void handle(jakarta.servlet.http.HttpServletRequest req,
                       HttpServletResponse res,
                       org.springframework.security.access.AccessDeniedException ex) throws IOException {
        write(res, ErrorCode.FORBIDDEN);
    }

    private void write(HttpServletResponse res, ErrorCode ec) throws IOException {
        res.setStatus(ec.status().value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        var body = ApiResponse.fail(ec.code(), ec.message());
        mapper.writeValue(res.getWriter(), body);
    }
}