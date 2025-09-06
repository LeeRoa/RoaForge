package com.roa.forge.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "license",
        uniqueConstraints = {
                @UniqueConstraint(name = "uni_license_key", columnNames = "license_key")
        },
        indexes = {
                @Index(name = "idx_license_start_date", columnList = "start_date"),
                @Index(name = "idx_license_end_date", columnList = "end_date")
        }
)
public class License extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "license_key", nullable = false, length = 128)
    private String licenseKey;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Min(0)
    @Column(name = "max_users", nullable = false)
    private Integer maxUsers;

    @Min(0)
    @Column(name = "max_documents", nullable = false)
    private Integer maxDocuments;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Builder
    public License(
            String licenseKey,
            LocalDate startDate,
            LocalDate endDate,
            Integer maxUsers,
            Integer maxDocuments
    ) {
        this.licenseKey = licenseKey;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxUsers = (maxUsers == null ? 0 : maxUsers);
        this.maxDocuments = (maxDocuments == null ? 0 : maxDocuments);
    }

    @jakarta.validation.constraints.AssertTrue(message = "시작일은 종료일보다 같거나 이전이어야 합니다.")
    private boolean isDateRangeValid() {
        return endDate == null || (startDate != null && !startDate.isAfter(endDate));
    }

    public boolean isActiveOn(LocalDate date) {
        if (date == null) return false;
        boolean afterStart = (startDate == null) || !date.isBefore(startDate);
        boolean beforeEnd  = (endDate == null)   || !date.isAfter(endDate);
        return afterStart && beforeEnd;
    }
}