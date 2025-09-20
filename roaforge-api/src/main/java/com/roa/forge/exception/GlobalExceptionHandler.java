package com.roa.forge.exception;

import com.roa.forge.dto.ApiResponse;
import com.roa.forge.dto.ErrorCode;
import com.roa.forge.support.JsonMediaTypes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.LocaleResolver;

import java.util.*;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;
    private static final Object[] NO_ARGS = new Object[0];

    private Locale locale(HttpServletRequest req) {
        return localeResolver.resolveLocale(req);
    }

    private String i18n(String key, String defaultMsg, Locale locale) {
        return messageSource.getMessage(key, NO_ARGS, defaultMsg, locale);
    }

    private String i18n(ErrorCode ec, Locale locale) {
        return i18n(ec.key(), ec.message(), locale);
    }

    private ResponseEntity<ApiResponse<Void>> fail(ErrorCode ec, Locale locale) {
        return ResponseEntity.status(ec.status())
                .contentType(JsonMediaTypes.APPLICATION_JSON_UTF8)
                .body(ApiResponse.fail(ec.code(), i18n(ec, locale)));
    }

    private ResponseEntity<ApiResponse<Void>> fail(ErrorCode ec, Locale locale, HttpStatus status) {
        return ResponseEntity.status(status)
                .contentType(JsonMediaTypes.APPLICATION_JSON_UTF8)
                .body(ApiResponse.fail(ec.code(), i18n(ec, locale)));
    }

    private ResponseEntity<ApiResponse<Map<String, Object>>> failWithErrors(Locale locale,
                                                                            List<Map<String, Object>> errors) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("errors", errors);
        return ResponseEntity.status(ErrorCode.INVALID_ARGUMENT.status())
                .contentType(JsonMediaTypes.APPLICATION_JSON_UTF8)
                .body(new ApiResponse<>(ErrorCode.INVALID_ARGUMENT.code(), i18n(ErrorCode.INVALID_ARGUMENT, locale), data));
    }

    private List<Map<String, Object>> fieldErrors(BindingResult br, Locale locale) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (FieldError fe : br.getFieldErrors()) {
            Map<String, Object> one = new LinkedHashMap<>();
            one.put("field", fe.getField());
            one.put("message", messageSource.getMessage(fe, locale));
            one.put("rejected", fe.getRejectedValue());
            list.add(one);
        }
        return list;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleConstraint(ConstraintViolationException ex,
                                                                             HttpServletRequest req) {
        Locale locale = locale(req);
        List<Map<String, Object>> details = ex.getConstraintViolations().stream()
                .map(v -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("field", v.getPropertyPath().toString());
                    m.put("message", v.getMessage());
                    m.put("rejected", v.getInvalidValue());
                    return m;
                })
                .toList();
        return failWithErrors(locale, details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpServletRequest req) {
        Locale locale = locale(req);
        String msg = i18n("error.malformed_json", "Malformed JSON", locale);
        return ResponseEntity.status(ErrorCode.INVALID_ARGUMENT.status())
                .contentType(JsonMediaTypes.APPLICATION_JSON_UTF8)
                .body(ApiResponse.fail(ErrorCode.INVALID_ARGUMENT.code(), msg));
    }

    @ExceptionHandler({ MethodArgumentNotValidException.class, BindException.class })
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleBind(Exception ex, HttpServletRequest req) {
        Locale locale = locale(req);
        BindingResult br = (ex instanceof MethodArgumentNotValidException manve)
                ? manve.getBindingResult()
                : ((BindException) ex).getBindingResult();
        return failWithErrors(locale, fieldErrors(br, locale));
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleApp(AppException ex, HttpServletRequest req) {
        Locale locale = locale(req);
        ErrorCode ec = ex.getCode();

        String msg = ex.getMessage();
        if (msg == null || msg.isBlank() || msg.equals(ec.message())) {
            msg = i18n(ec, locale);
        }

        return ResponseEntity.status(ec.status())
                .contentType(JsonMediaTypes.APPLICATION_JSON_UTF8)
                .body(ApiResponse.fail(ec.code(), msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAny(Exception ex, HttpServletRequest req) {
        Locale locale = locale(req);
        String traceId = UUID.randomUUID().toString();
        log.error("[{}] Unhandled exception on {} {}", traceId, req.getMethod(), req.getRequestURI(), ex);

        if (ex instanceof ResponseStatusException rse) {
            return ResponseEntity.status(rse.getStatusCode())
                    .header("X-Trace-Id", traceId)
                    .contentType(JsonMediaTypes.APPLICATION_JSON_UTF8)
                    .body(ApiResponse.fail(ErrorCode.INTERNAL_ERROR.code(), i18n(ErrorCode.INTERNAL_ERROR, locale)));
        }

        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.status())
                .header("X-Trace-Id", traceId)
                .contentType(JsonMediaTypes.APPLICATION_JSON_UTF8)
                .body(ApiResponse.fail(ErrorCode.INTERNAL_ERROR.code(), i18n(ErrorCode.INTERNAL_ERROR, locale)));
    }
}
