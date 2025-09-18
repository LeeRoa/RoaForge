package com.roa.forge.service;

import com.roa.forge.dto.MeResponse;
import com.roa.forge.dto.ProfileUpdateRequest;
import com.roa.forge.dto.UserPrincipal;
import com.roa.forge.entity.UserAccount;
import com.roa.forge.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserAccount> getAllUsers() {
        return userAccountRepository.findAll();
    }

    public UserAccount getUser(Long id) {
        return userAccountRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("사용자를 찾을 수 없습니다. : " + id));
    }

    @Transactional
    public UserAccount createUser(UserAccount user) {
        String encodedPw = passwordEncoder.encode(user.getPassword());
        user = UserAccount.builder()
                .username(user.getUsername())
                .password(encodedPw)
                .email(user.getEmail())
                .active(user.getActive())
                .build();
        return userAccountRepository.save(user);
    }

    @Transactional
    public UserAccount updateUser(Long id, UserAccount updated) {
        UserAccount existing = userAccountRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("User not found: " + id));

        // 업데이트할 값만 반영
        existing = UserAccount.builder()
                .username(updated.getUsername())
                .password(updated.getPassword())
                .email(updated.getEmail())
                .active(updated.getActive())
                .build();

        return userAccountRepository.save(existing);
    }

    @Transactional
    public void deleteUser(Long id) {
        userAccountRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public MeResponse getMe(UserPrincipal me) {
        return new MeResponse(me.getId(), me.getUsername(), me.getEmail(), me.getProvider(), me.getAuthorities());
    }

    @Transactional
    public MeResponse updateMe(UserPrincipal me, ProfileUpdateRequest req) {
        UserAccount user = userAccountRepository.findById(me.getId()).orElseThrow();

        if (req.username() != null && !req.username().isBlank() && !req.username().equals(user.getUsername())) {
            if (userAccountRepository.existsByUsername(req.username())) {
                throw new IllegalArgumentException("이미 사용 중인 사용자명입니다.");
            }
            user.changeUsername(req.username());
        }

        if (req.email() != null && !req.email().isBlank() && !req.email().equals(user.getEmail())) {
            if (userAccountRepository.existsByEmail(req.email())) {
                throw new IllegalArgumentException("이미 가입된 이메일입니다.");
            }
            user.changeEmail(req.email());
        }

        // 변경사항은 영속 상태에서 flush되므로 별도 save() 불필요하지만,
        // 명시적으로 호출하고 싶다면 아래 라인 유지해도 무방
        // userRepo.save(user);

        // 최신 값으로 응답
        return new MeResponse(user.getId(), user.getUsername(), user.getEmail(), user.getProvider(), me.getAuthorities());
    }
}