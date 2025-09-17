package com.roa.forge.service;

import com.roa.forge.dto.RegisterRequest;
import com.roa.forge.dto.TokenResponse;
import com.roa.forge.entity.Role;
import com.roa.forge.entity.UserAccount;
import com.roa.forge.provider.JwtTokenProvider;
import com.roa.forge.repository.RoleRepository;
import com.roa.forge.repository.UserAccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAccountRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwt;

    private Role ensureUserRole() {
        return roleRepo.findByName("ROLE_USER")
                .orElseGet(() -> roleRepo.save(Role.builder().name("ROLE_USER").build()));
    }

    /** 로컬 회원가입 */
    @Transactional
    public TokenResponse registerLocal(RegisterRequest req) {
        if (userRepo.existsByEmail(req.getEmail())) throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        if (userRepo.existsByUsername(req.getUsername())) throw new IllegalArgumentException("이미 사용 중인 사용자명입니다.");

        UserAccount user = UserAccount.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword())) // BCrypt
                .active(true)
                .provider("LOCAL")
                .build();
        user.addRole(ensureUserRole());
        userRepo.save(user);

        String subject = user.getEmail(); // JWT subject는 email로 통일
        return new TokenResponse(jwt.createAccessToken(subject), jwt.createRefreshToken(subject));
    }

    /** 구글 최초 로그인: 없으면 생성/연결 */
    @Transactional
    public UserAccount upsertGoogleUser(String sub, String email, String nameOrNull) {
        // provider+providerId 일치 시 바로 반환
        return userRepo.findByProviderAndProviderId("GOOGLE", sub)
                .orElseGet(() -> userRepo.findByEmail(email)
                        .map(existing -> { // 기존 로컬 계정과 연동
                            existing.linkProvider("GOOGLE", sub);
                            return existing;
                        })
                        .orElseGet(() -> { // 신규 유저 생성
                            String username = deriveUniqueUsername(email, nameOrNull);
                            // 소셜용 패스워드(검증용 아님). @NotBlank/@Size 때문에 랜덤 비번 저장
                            String fakePwd = passwordEncoder.encode(UUID.randomUUID().toString());
                            UserAccount u = UserAccount.builder()
                                    .username(username)
                                    .email(email)
                                    .password(fakePwd)
                                    .active(true)
                                    .provider("GOOGLE")
                                    .providerId(sub)
                                    .build();
                            u.addRole(ensureUserRole());
                            return userRepo.save(u);
                        }));
    }

    private String deriveUniqueUsername(String email, String fallback) {
        String base = (email != null && email.contains("@"))
                ? email.substring(0, email.indexOf('@'))
                : (fallback != null && !fallback.isBlank() ? fallback : "user");
        String candidate = base;
        int i = 1;
        while (userRepo.existsByUsername(candidate)) candidate = base + i++;
        return candidate;
    }

    public TokenResponse issueTokens(String subjectEmail) {
        return new TokenResponse(jwt.createAccessToken(subjectEmail), jwt.createRefreshToken(subjectEmail));
    }
}