package com.certificate.platform.service;

import com.certificate.platform.model.Certificate;
import com.certificate.platform.model.User;
import com.certificate.platform.repository.CertificateRepository;
import com.certificate.platform.util.PdfGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.UUID;
import com.certificate.platform.util.LogoStorageService;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;

@Service
@RequiredArgsConstructor
public class CertificateService {


    private final CertificateRepository certificateRepository;
    private final EmailService emailService;
    private final PdfGeneratorService pdfGeneratorService;
    private final BlockchainService blockchainService;
    private final LogoStorageService logoStorageService;

    public Certificate issueCertificate(String recipientName, String recipientEmail,
                                        String courseTitle, String enrollmentNo,
                                        String passingYear, User issuer,
                                        MultipartFile logoFile) {

        if (certificateRepository.existsByEnrollmentNoAndIssuer(enrollmentNo, issuer)) {
            throw new RuntimeException("Certificate already issued for enrollment: " + enrollmentNo);
        }

        Certificate cert = new Certificate();
        cert.setCertificateId(UUID.randomUUID().toString());
        cert.setRecipientName(recipientName);
        cert.setRecipientEmail(recipientEmail);
        cert.setCourseTitle(courseTitle);
        cert.setEnrollmentNo(enrollmentNo);
        cert.setPassingYear(passingYear);
        cert.setIssuer(issuer);
        cert.setStatus(Certificate.CertificateStatus.ACTIVE);
        cert.setHash(generateHash(recipientName, enrollmentNo, courseTitle, passingYear));

        Certificate saved = certificateRepository.save(cert);

        // Save logo if uploaded
        String logoPath = null;
        if (logoFile != null && !logoFile.isEmpty()) {
            logoPath = logoStorageService.saveLogo(logoFile);
        }

        // Generate PDF
        File pdf = pdfGeneratorService.generateCertificate(saved, logoPath);

        // Send email
        emailService.sendCertificateEmail(
                saved.getRecipientEmail(),
                saved.getRecipientName(),
                saved.getCourseTitle(),
                pdf
        );

        // Register on blockchain
        try {
            String txHash = blockchainService.registerCertificate(
                    saved.getCertificateId(), saved.getHash());
            System.out.println("Blockchain TX Hash: " + txHash);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Blockchain registration failed: " + e.getMessage());
        }

        return saved;
    }

    public Certificate findByCertificateId(String certificateId) {
        return certificateRepository.findByCertificateId(certificateId)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));
    }

    public boolean verifyOnBlockchain(String hash) {
        return blockchainService.verifyCertificateOnChain(hash);
    }

    public List<Certificate> getCertificatesByIssuer(User issuer) {
        return certificateRepository.findByIssuer(issuer);
    }

    public List<Certificate> getAllCertificates() {
        return certificateRepository.findAll();
    }

    public void revokeCertificate(Long id) {
        Certificate cert = certificateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));
        cert.setStatus(Certificate.CertificateStatus.REVOKED);
        certificateRepository.save(cert);
    }

    private String generateHash(String name, String enrollment, String course, String year) {
        String raw = name + enrollment + course + year;
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating hash");
        }
    }
}