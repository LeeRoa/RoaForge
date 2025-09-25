package com.roa.forge.dto;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // ==== 공통 (1xxx) ====
    INVALID_ARGUMENT("1000", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST, "error.invalid_argument"),
    UNAUTHORIZED("1001", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED, "error.unauthorized"),
    FORBIDDEN("1003", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN, "error.forbidden"),
    NOT_FOUND("1004", "리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "error.not_found"),
    RATE_LIMITED("1029", "요청이 너무 많습니다.", HttpStatus.TOO_MANY_REQUESTS, "error.rate_limited"),
    INTERNAL_ERROR("1500", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR, "error.internal_error"),

    // ==== 사용자/권한 (2xxx) ====
    USER_NOT_FOUND("2004", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "error.user_not_found"),
    DUPLICATE_USERNAME("2009", "이미 사용 중인 사용자명입니다.", HttpStatus.CONFLICT, "error.duplicate_username"),
    AUTH_BAD_CREDENTIALS("2010", "아이디 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED, "error.bad_credentials"),
    AUTH_ACCOUNT_LOCKED("2011", "계정이 잠겨 있습니다.", HttpStatus.FORBIDDEN, "error.account_locked"),
    AUTH_ACCOUNT_DISABLED("2011", "계정이 비활성화되어 있습니다.", HttpStatus.FORBIDDEN, "error.account_disabled"),

    // ==== PDF/서명 (3xxx) ====
    PDF_PARSE_ERROR("3001", "PDF 파싱에 실패했습니다.", HttpStatus.BAD_REQUEST, "error.pdf_parse"),
    PDF_SIGNING_ERROR("3002", "PDF 전자서명에 실패했습니다.", HttpStatus.BAD_REQUEST, "error.pdf_sign"),
    CERT_LOAD_FAILED("3003", "서명 인증서를 불러오지 못했습니다.", HttpStatus.INTERNAL_SERVER_ERROR, "error.cert_load"),
    TSA_REQUEST_FAILED("3004", "타임스탬프 서버 요청에 실패했습니다.", HttpStatus.BAD_GATEWAY, "error.tsa_request"),
    OCSP_REQUEST_FAILED("3005", "OCSP 검증 요청에 실패했습니다.", HttpStatus.BAD_GATEWAY, "error.ocsp_request"),
    STORAGE_IO_ERROR("3006", "저장소 I/O 오류가 발생했습니다.", HttpStatus.SERVICE_UNAVAILABLE, "error.storage_io"),
    PDF_COLOR_NULL("3007", "색상 값이 없습니다.", HttpStatus.BAD_REQUEST, "error.null_color"),

    // ==== JWT/보안 키 (4xxx) ====
    JWT_SECRET_TOO_SHORT("4001", "JWT 시크릿 길이가 부족합니다. 최소 256비트(=32바이트) 이상이어야 합니다.", HttpStatus.INTERNAL_SERVER_ERROR, "error.jwt_secret_too_short");

    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;
    private final String i18nKey;

    ErrorCode(String code, String defaultMessage, HttpStatus httpStatus, String i18nKey) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
        this.i18nKey = i18nKey;
    }

    public String code() { return code; }
    public String message() { return defaultMessage; }
    public HttpStatus status() { return httpStatus; }
    public String key() { return i18nKey; }
}