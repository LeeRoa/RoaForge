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
@ToString(exclude = "roles")
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
    public UserAccount(String username, String password, String email, Boolean active) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.active = active != null ? active : true;
    }

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
}