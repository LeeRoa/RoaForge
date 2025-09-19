package com.roa.forge.exception;

import com.roa.forge.dto.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    record ApiResponse<T>(String code, String msg, T data) {
        static <T> ApiResponse<T> fail(String code, String msg) { return new ApiResponse<>(code, msg, null); }
    }

    @ExceptionHandler(AppException.class) // 도메인 예외(에러코드 내장)
    public ResponseEntity<ApiResponse<Void>> handleApp(AppException ex) {
        var ec = ex.getCode(); // ErrorCode(enum): code()/message()/status()
        return ResponseEntity.status(ec.status())
                .body(ApiResponse.fail(ec.code(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class) // @Valid 실패
    public ResponseEntity<ApiResponse<Void>> handleValid(MethodArgumentNotValidException ex) {
        var msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField()+": "+e.getDefaultMessage())
                .findFirst().orElse("validation error");
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(ErrorCode.INVALID_ARGUMENT.code(), msg));
    }

    @ExceptionHandler(Exception.class) // 그 외 미처리
    public ResponseEntity<ApiResponse<Void>> handleAny(Exception ex) {
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.status())
                .body(ApiResponse.fail(ErrorCode.INTERNAL_ERROR.code(), ErrorCode.INTERNAL_ERROR.message()));
    }
}