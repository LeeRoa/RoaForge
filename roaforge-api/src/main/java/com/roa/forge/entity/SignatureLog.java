package com.roa.forge.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "signature_log",
        indexes = {
                @Index(name = "idx_signature_doc", columnList = "document_id"),
                @Index(name = "idx_signature_signed_at", columnList = "signed_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignatureLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "field_id")
    private Long fieldId;

    @Column(nullable = false, length = 100)
    private String signer;

    @Column(name = "cert_subject", length = 200)
    private String certSubject;

    @Column(name = "cert_issuer", length = 200)
    private String certIssuer;

    @Column(name = "cert_serial", length = 100)
    private String certSerial;

    @Column(name = "signed_at", nullable = false, updatable = false)
    private LocalDateTime signedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Builder
    public SignatureLog(Long documentId, Long fieldId, String signer,
                        String certSubject, String certIssuer, String certSerial,
                        String ipAddress, String userAgent) {
        this.documentId = documentId;
        this.fieldId = fieldId;
        this.signer = signer;
        this.certSubject = certSubject;
        this.certIssuer = certIssuer;
        this.certSerial = certSerial;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.signedAt = LocalDateTime.now();
    }
}