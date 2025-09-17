package com.roa.forge.handler;

import com.roa.forge.provider.JwtTokenProvider;
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

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        Object principal = authentication.getPrincipal();
        String email = null;
        String sub = null;

        if (principal instanceof OidcUser oidc) {           // 구글 OIDC면 여기로 옴
            email = oidc.getEmail();
            sub = oidc.getSubject();                        // 고유 ID
        } else if (principal instanceof OAuth2User ou) {    // 혹시 OIDC scope 빠진 경우
            email = (String) ou.getAttributes().get("email");
            sub = (String) ou.getAttributes().get("sub");
        }

        String subject = (email != null) ? email : sub;
        String accessToken = jwtTokenProvider.createAccessToken(subject);
        String refreshToken = jwtTokenProvider.createRefreshToken(subject);

        String target = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("access_token", accessToken)
                .queryParam("refresh_token", refreshToken)
                .build(true)
                .toUriString();

        response.sendRedirect(target);
    }
}