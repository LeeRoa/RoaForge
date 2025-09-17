package com.roa.forge.controller;

import com.roa.forge.dto.TokenResponse;
import com.roa.forge.dto.RegisterRequest;
import com.roa.forge.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import com.roa.forge.provider.JwtTokenProvider;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "username/email/password로 회원가입")
    public TokenResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.registerLocal(request);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "username/password로 로그인 후 JWT 토큰 발급")
    public Map<String, Object> login(@RequestParam String username, @RequestParam String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            String accessToken = jwtTokenProvider.createAccessToken(authentication.getName());
            String refreshToken = jwtTokenProvider.createRefreshToken(authentication.getName());

            return Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken
            );
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid credentials");
        }
    }

    @PostMapping("/refresh")
    public Map<String, String> refresh(@RequestParam String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String username = jwtTokenProvider.getUsername(refreshToken);
        String newAccessToken = jwtTokenProvider.createAccessToken(username);

        return Map.of("accessToken", newAccessToken);
    }
}