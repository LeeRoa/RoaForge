package com.roa.forge.handler;

import com.roa.forge.dto.TokenResponse;
import com.roa.forge.entity.UserAccount;
import com.roa.forge.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class GoogleSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        Object principal = authentication.getPrincipal();

        String email = null;
        String sub   = null;
        String name  = null;

        if (principal instanceof OidcUser oidc) {
            email = oidc.getEmail();
            sub   = oidc.getSubject();     // 구글 고유 ID
            name  = oidc.getFullName();
        } else if (principal instanceof OAuth2User ou) {
            email = (String) ou.getAttributes().get("email");
            sub   = (String) ou.getAttributes().get("sub");
            name  = (String) ou.getAttributes().get("name");
        }

        if (sub == null) {
            // 최소한의 방어 (scope가 잘못됐거나 응답 불완전)
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Google account id (sub) is missing");
            return;
        }

        // 1) (provider=GOOGLE, providerId=sub) 기준 upsert → 우리 DB 유저 확보
        UserAccount user = authService.upsertGoogleUser(sub, email, name);

        // 2) userId 기반 토큰 발급 (sub = user.getId())
        TokenResponse tokens = authService.issueTokensFor(user);

        // 3) 프론트 콜백으로 리다이렉트
        String target = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("access_token", tokens.getAccessToken())
                .queryParam("refresh_token", tokens.getRefreshToken())
                .build(true)
                .toUriString();

        response.sendRedirect(target);
    }
}