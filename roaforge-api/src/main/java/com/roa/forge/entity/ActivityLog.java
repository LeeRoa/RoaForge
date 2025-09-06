package com.roa.forge.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String action;
    private String detail;

    private LocalDateTime createdAt;

    @Builder
    public ActivityLog(String username, String action, String detail) {
        this.username = username;
        this.action = action;
        this.detail = detail;
        this.createdAt = LocalDateTime.now();
    }
}
