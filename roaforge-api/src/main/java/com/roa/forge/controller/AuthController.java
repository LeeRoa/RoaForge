package com.roa.forge.controller;

import com.roa.forge.dto.LoginRequest;
import com.roa.forge.dto.RegisterRequest;
import com.roa.forge.dto.TokenResponse;
import com.roa.forge.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "username/email/password로 회원가입")
    public TokenResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.registerLocal(request); // save 후 userId 기반으로 토큰 발급
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "username/password로 로그인 후 JWT 발급 (sub=userId)")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "refreshToken(sub=userId)로 accessToken 재발급")
    public TokenResponse refresh(@RequestParam String refreshToken) {
        return authService.refresh(refreshToken);
    }

    @PostMapping("/logout")
    public void logout(@RequestParam(required = false) String refreshToken) {
        authService.logout(refreshToken);
    }
}