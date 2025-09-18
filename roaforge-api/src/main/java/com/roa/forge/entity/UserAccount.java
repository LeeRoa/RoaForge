package com.roa.forge.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@ToString(exclude = {"roles", "password", "providerId"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_account",
        uniqueConstraints = {
                @UniqueConstraint(name = "uni_user_username", columnNames = "username"),
                @UniqueConstraint(name = "uni_user_email", columnNames = "email")
        }
)
public class UserAccount extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String username;

    @NotBlank
    @Size(min = 8, max = 200)
    @Column(nullable = false, length = 200)
    private String password;

    @Email
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private Boolean active = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            uniqueConstraints = @UniqueConstraint(name = "uk_user_role", columnNames = {"user_id", "role_id"})
    )
    private Set<Role> roles = new HashSet<>();

    @Builder
    public UserAccount(String username, String password, String email, Boolean active, String provider, String providerId) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.active = active != null ? active : true;
        this.provider = provider;
        this.providerId = providerId;
    }

    @Size(max = 20)
    @Column(length = 20)              // LOCAL / GOOGLE / KAKAO / NAVER
    private String provider;

    @Size(max = 100)
    @Column(name = "provider_id", length = 100) // 구글 sub 등
    private String providerId;

    public void addRole(Role role) {
        if (role != null) {
            this.roles.add(role);
        }
    }

    public void removeRole(Role role) {
        if (role != null) {
            this.roles.remove(role);
        }
    }

    /** 소셜 계정으로 링크 (로컬→소셜 연동/최초 소셜 가입 공통) */
    public void linkProvider(String provider, String providerId) {
        this.provider = provider;
        this.providerId = providerId;
    }

    /** 패스워드 변경(이미 인코딩된 값 전달) */
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    /** 활성/비활성 전환 */
    public void activate() { this.active = true; }
    public void deactivate() { this.active = false; }

    public void changeUsername(String username) { this.username = username; }
    public void changeEmail(String email) { this.email = email; }
}