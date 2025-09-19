package com.roa.forge.exception;

import com.roa.forge.dto.ErrorCode;
import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final ErrorCode code;

    public AppException(ErrorCode code) {
        super(code.message());
        this.code = code;
    }

    public AppException(ErrorCode code, String message) {
        super(message == null ? code.message() : message);
        this.code = code;
    }

    // 메시지 포맷팅 지원 (e.g. "필드 %s 가 유효하지 않습니다.", field)
    public static AppException of(ErrorCode code, String fmt, Object... args) {
        return new AppException(code, fmt == null ? null : String.format(fmt, args));
    }
}