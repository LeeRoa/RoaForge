package com.roa.forge.dto;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // ==== 공통 (1xxx) ====
    INVALID_ARGUMENT("1000", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("1001", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("1003", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    NOT_FOUND("1004", "리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    RATE_LIMITED("1029", "요청이 너무 많습니다.", HttpStatus.TOO_MANY_REQUESTS),
    INTERNAL_ERROR("1500", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // ==== 사용자/권한 (2xxx) ====
    USER_NOT_FOUND("2004", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DUPLICATE_USERNAME("2009", "이미 사용 중인 사용자명입니다.", HttpStatus.CONFLICT),

    // ==== PDF/서명 (3xxx) ====
    PDF_PARSE_ERROR("3001", "PDF 파싱에 실패했습니다.", HttpStatus.BAD_REQUEST),
    PDF_SIGNING_ERROR("3002", "PDF 전자서명에 실패했습니다.", HttpStatus.BAD_REQUEST),
    CERT_LOAD_FAILED("3003", "서명 인증서를 불러오지 못했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    TSA_REQUEST_FAILED("3004", "타임스탬프 서버 요청에 실패했습니다.", HttpStatus.BAD_GATEWAY),
    OCSP_REQUEST_FAILED("3005", "OCSP 검증 요청에 실패했습니다.", HttpStatus.BAD_GATEWAY),
    STORAGE_IO_ERROR("3006", "저장소 I/O 오류가 발생했습니다.", HttpStatus.SERVICE_UNAVAILABLE);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String defaultMessage, HttpStatus httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }

    public String code() { return code; }
    public String message() { return defaultMessage; }
    public HttpStatus status() { return httpStatus; }
}