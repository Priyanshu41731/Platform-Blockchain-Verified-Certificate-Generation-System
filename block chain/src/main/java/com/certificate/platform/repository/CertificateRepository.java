package com.certificate.platform.repository;

import com.certificate.platform.model.Certificate;
import com.certificate.platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    Optional<Certificate> findByCertificateId(String certificateId);
    List<Certificate> findByIssuer(User issuer);
    boolean existsByEnrollmentNoAndIssuer(String enrollmentNo, User issuer);
}
