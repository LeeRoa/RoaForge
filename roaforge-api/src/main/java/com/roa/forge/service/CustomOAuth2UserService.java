package com.roa.forge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final CustomUserDetailsService customUserDetailsService;
    // 필요하면 UserRepository 주입해서 신규 회원가입 처리

    @Override
    public OAuth2User loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(req);

        String registrationId = req.getClientRegistration().getRegistrationId();
        Map<String, Object> attr = oAuth2User.getAttributes();

        // 구글은 OpenID scope 쓰면 "sub"가 고유키
        String providerId = (String) attr.get("sub");
        String email = (String) attr.get("email");
        String name = (String) attr.get("name");

        // 1) provider + providerId로 기존 유저 조회
        // 2) 없으면 이메일 기반으로 묶거나 신규 생성 (선호 정책대로)
        // 예: customUserDetailsService.linkOrCreateOAuthUser(registrationId, providerId, email, name);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attr,
                "sub"
        );
    }
}