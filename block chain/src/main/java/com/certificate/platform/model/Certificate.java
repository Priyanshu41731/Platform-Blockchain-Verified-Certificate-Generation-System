package com.certificate.platform.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "certificates")
@Data
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String certificateId;

    private String recipientName;

    private String recipientEmail;

    private String courseTitle;

    private String enrollmentNo;

    private String passingYear;

    private String hash;

    private String blockchainTxHash;

    @Enumerated(EnumType.STRING)
    private CertificateStatus status;

    @ManyToOne
    @JoinColumn(name = "issuer_id")
    private User issuer;

    private LocalDateTime issuedAt;

    @PrePersist
    public void prePersist() {
        this.issuedAt = LocalDateTime.now();
    }

    public enum CertificateStatus {
        ACTIVE, REVOKED
    }
}
