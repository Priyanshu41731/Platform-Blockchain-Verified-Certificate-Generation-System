package com.certificate.platform.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "issuance_requests")
@Data
public class IssuanceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "issuer_id")
    private User issuer;

    private String fileName;

    private int totalRecords;

    private int successCount;

    private int failedCount;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    private LocalDateTime requestedAt;

    @PrePersist
    public void prePersist() {
        this.requestedAt = LocalDateTime.now();
    }

    public enum RequestStatus {
        PROCESSING, COMPLETED, FAILED
    }
}
