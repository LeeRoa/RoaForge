package com.roa.forge.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.roa.forge.entity.UserAccount;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserPrincipal implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @EqualsAndHashCode.Include
    private final Long id;
    private final String email;
    private final String username;
    private final String provider;                     // LOCAL / GOOGLE 등
    @JsonIgnore
    private final String password;                     // 해시된 비번
    private final boolean active;
    private final Set<? extends GrantedAuthority> authorities;

    private UserPrincipal(Long id, String email, String username, String provider,
                          String password, boolean active,
                          Set<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.provider = provider;
        this.password = password;
        this.active = active;
        this.authorities = authorities;
    }

    /**
     * 엔티티 -> 프린시펄 변환
     */
    public static UserPrincipal from(UserAccount u) {
        Set<? extends GrantedAuthority> auths = u.getRoles().stream()
                .map(r -> (GrantedAuthority) () -> {
                    String name = r.getName();
                    return name.startsWith("ROLE_") ? name : "ROLE_" + name;
                })
                .collect(Collectors.toSet());

        return new UserPrincipal(
                u.getId(),
                u.getEmail(),
                u.getUsername(),
                u.getProvider(),
                u.getPassword(),
                Boolean.TRUE.equals(u.getActive()),
                auths
        );
    }

    // --- UserDetails 표준 구현 ---
    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    @Override
    public Set<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
}