package com.roa.forge.service;

import com.roa.forge.entity.UserAccount;
import com.roa.forge.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;

    public List<UserAccount> getAllUsers() {
        return userAccountRepository.findAll();
    }

    public UserAccount getUser(Long id) {
        return userAccountRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("User not found: " + id));
    }

    @Transactional
    public UserAccount createUser(UserAccount user) {
        // TODO: 비밀번호는 반드시 암호화 후 저장 (MVP 단계에서는 raw도 가능)
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
}