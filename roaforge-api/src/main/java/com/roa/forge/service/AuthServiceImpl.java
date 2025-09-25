package com.roa.forge.service;

import com.roa.forge.config.RefreshTokenDenylist;
import com.roa.forge.dto.*;
import com.roa.forge.entity.Role;
import com.roa.forge.entity.UserAccount;
import com.roa.forge.exception.AppException;
import com.roa.forge.provider.JwtTokenProvider;
import com.roa.forge.repository.RoleRepository;
import com.roa.forge.repository.UserAccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final MessageSource messageSource;
    private final UserAccountRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwt;
    private final UserAccountRepository userAccountRepository;
    private final RefreshTokenDenylist refreshDenylist;

    private Role ensureUserRole() {
        return roleRepo.findByName("ROLE_USER")
                .orElseGet(() -> roleRepo.save(Role.builder().name("ROLE_USER").build()));
    }

    @Override
    public TokenResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("재발급 된 토큰이 유효하지 않습니다.");
        }

        String rjti = jwtTokenProvider.getJti(refreshToken);
        if (refreshDenylist.isRevoked(rjti)) {
            throw new BadCredentialsException("이미 로그아웃된 리프레시 토큰입니다.");
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

    @Override
    public TokenResponse login(LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserPrincipal p = (UserPrincipal) auth.getPrincipal();

            String access  = jwtTokenProvider.createAccessToken(p.getId(), p.getEmail(), p.getUsername(), p.getProvider());
            String refresh = jwtTokenProvider.createRefreshToken(p.getId());
            return new TokenResponse(access, refresh);

        } catch (BadCredentialsException e) {
            throw new AppException(ErrorCode.UNAUTHORIZED,
                    messageSource.getMessage("error.bad_credentials", null, "Invalid", LocaleContextHolder.getLocale()));
        } catch (LockedException e) {
            throw new AppException(ErrorCode.FORBIDDEN,
                    messageSource.getMessage("error.account_locked", null, "Locked", LocaleContextHolder.getLocale()));
        } catch (DisabledException e) {
            throw new AppException(ErrorCode.FORBIDDEN,
                    messageSource.getMessage("error.account_disabled", null, "Disabled", LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void logout(String refreshToken) {
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            String jti = jwtTokenProvider.getJti(refreshToken);
            var exp = jwtTokenProvider.getExpiration(refreshToken);
            if (jti != null && exp != null) {
                refreshDenylist.revoke(jti, exp.getTime()); // 만료까지 deny
            }
        }
    }

    /** 로컬 회원가입: sub=userId 로 발급 */
    @Override
    @Transactional
    public TokenResponse registerLocal(RegisterRequest req) {
        if (userRepo.existsByEmail(req.getEmail())) throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        if (userRepo.existsByUsername(req.getUsername())) throw new IllegalArgumentException("이미 사용 중인 사용자명입니다.");

        UserAccount user = UserAccount.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword())) // BCrypt
                .active(true)
                .build();

        user.addRole(ensureUserRole());
        user.linkProvider("LOCAL", null); // 빌더가 provider를 받지 않으면 도메인 메서드로 세팅
        userRepo.save(user);              // save 후에 id 채워짐

        return issueTokensFor(user);
    }

    /** 구글 최초 로그인: 없으면 생성/연결 후 엔티티 반환 (발급은 핸들러에서 issueTokensFor 호출) */
    @Override
    @Transactional
    public UserAccount upsertGoogleUser(String sub, String email, String nameOrNull) {
        return userRepo.findByProviderAndProviderId("GOOGLE", sub)
                .orElseGet(() -> userRepo.findByEmail(email)
                        .map(existing -> {                // 기존 로컬 계정과 연동
                            existing.linkProvider("GOOGLE", sub);
                            return existing;
                        })
                        .orElseGet(() -> {                // 신규 유저 생성
                            String username = deriveUniqueUsername(email, nameOrNull);
                            String fakePwd = passwordEncoder.encode(UUID.randomUUID().toString()); // 제약 충족용
                            UserAccount u = UserAccount.builder()
                                    .username(username)
                                    .email(email)
                                    .password(fakePwd)
                                    .active(true)
                                    .build();
                            u.addRole(ensureUserRole());
                            u.linkProvider("GOOGLE", sub);
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

    /** userId 기반 토큰 발급 */
    @Override
    public TokenResponse issueTokensFor(UserAccount user) {
        String access  = jwt.createAccessToken(user.getId(), user.getEmail(), user.getUsername(), user.getProvider());
        String refresh = jwt.createRefreshToken(user.getId());
        return new TokenResponse(access, refresh);
    }

    /** (구방식) 이메일로 발급 */
    @Deprecated
    public TokenResponse issueTokens(String subjectEmail) {
        // 남겨두려면 JwtTokenProvider에 레거시 오버로드(createAccessToken(String))가 있어야 함
        return new TokenResponse(jwt.createAccessToken(subjectEmail), jwt.createRefreshToken(subjectEmail));
    }
}