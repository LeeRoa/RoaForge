package com.roa.forge.bootstrap;

import com.roa.forge.entity.Role;
import com.roa.forge.entity.UserAccount;
import com.roa.forge.repository.RoleRepository;
import com.roa.forge.repository.UserAccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final UserAccountRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Role userRole = roleRepo.findByName("ROLE_USER")
                .orElseGet(() -> roleRepo.save(Role.builder().name("ROLE_USER").build()));

        if (!userRepo.existsByUsername("testuser")) {
            UserAccount u = UserAccount.builder()
                    .username("testuser")
                    .email("test@local.dev")
                    .password(passwordEncoder.encode("pass1234"))
                    .active(true)
                    .build();
            u.addRole(userRole);
            u.linkProvider("LOCAL", null); // 소셜 컬럼 관리 중이면
            userRepo.save(u);
        }

        if (!userRepo.existsByUsername("admin")) {
            Role adminRole = roleRepo.findByName("ROLE_ADMIN")
                    .orElseGet(() -> roleRepo.save(Role.builder().name("ROLE_ADMIN").build()));
            UserAccount a = UserAccount.builder()
                    .username("admin")
                    .email("admin@local.dev")
                    .password(passwordEncoder.encode("admin1234"))
                    .active(true)
                    .build();
            a.addRole(userRole);
            a.addRole(adminRole);
            a.linkProvider("LOCAL", null);
            userRepo.save(a);
        }
    }
}