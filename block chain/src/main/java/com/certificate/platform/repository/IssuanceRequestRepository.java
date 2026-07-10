package com.certificate.platform.repository;

import com.certificate.platform.model.IssuanceRequest;
import com.certificate.platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IssuanceRequestRepository extends JpaRepository<IssuanceRequest, Long> {
    List<IssuanceRequest> findByIssuer(User issuer);
}
