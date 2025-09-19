package com.roa.forge.dto;

public record ApiResponse<T>(String code, String msg, T data) {
    public static <T> ApiResponse<T> ok(T data) { return new ApiResponse<>("0", "OK", data); }
    public static <T> ApiResponse<T> ok()        { return new ApiResponse<>("0", "OK", null); }
    public static <T> ApiResponse<T> fail(String code, String msg) { return new ApiResponse<>(code, msg, null); }
}