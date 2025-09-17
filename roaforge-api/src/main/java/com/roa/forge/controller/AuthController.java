package com.roa.forge.controller;

import com.roa.forge.dto.LoginRequest;
import com.roa.forge.dto.TokenResponse;
import com.roa.forge.dto.RegisterRequest;
import com.roa.forge.entity.UserAccount;
import com.roa.forge.repository.UserAccountRepository;
import com.roa.forge.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
    private final UserAccountRepository userAccountRepository;

    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "username/email/password로 회원가입")
    public TokenResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.registerLocal(request); // save 후 userId 기반으로 토큰 발급
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "username/password로 로그인 후 JWT 발급 (sub=userId)")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        // 1) 인증
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 2) DB에서 사용자 로드 (username -> 엔티티)
        UserAccount user = userAccountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        // 3) userId 기반 토큰 발급 (sub=userId)
        String access  = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getUsername(), user.getProvider());
        String refresh = jwtTokenProvider.createRefreshToken(user.getId());

        return new TokenResponse(access, refresh);
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "refreshToken(sub=userId)로 accessToken 재발급")
    public TokenResponse refresh(@RequestParam String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("재발급 된 토큰이 유효하지 않습니다.");
        }
        // 1) refresh에서 userId 추출
        Long userId = jwtTokenProvider.getUserId(refreshToken);

        // 2) 최신 사용자 정보 조회(클레임 업데이트 목적)
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("사용자를 찾을 수 없습니다."));

        // 3) 새로운 accessToken 발급 (refreshToken은 기존 것 재사용)
        String newAccess = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getUsername(), user.getProvider());
        return new TokenResponse(newAccess, refreshToken);
    }
}