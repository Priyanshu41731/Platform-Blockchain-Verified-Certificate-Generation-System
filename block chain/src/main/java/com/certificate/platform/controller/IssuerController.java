package com.certificate.platform.controller;

import com.certificate.platform.model.Certificate;
import com.certificate.platform.model.User;
import com.certificate.platform.repository.UserRepository;
import com.certificate.platform.service.CertificateService;
import com.certificate.platform.service.BulkCertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/issuer")
@RequiredArgsConstructor
public class IssuerController {

    private final CertificateService certificateService;
    private final BulkCertificateService bulkCertificateService;
    private final UserRepository userRepository;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User issuer = getIssuer(userDetails);
        List<Certificate> certificates = certificateService.getCertificatesByIssuer(issuer);

        long activeCount = certificates.stream()
                .filter(c -> c.getStatus() == Certificate.CertificateStatus.ACTIVE).count();
        long revokedCount = certificates.stream()
                .filter(c -> c.getStatus() == Certificate.CertificateStatus.REVOKED).count();

        model.addAttribute("certificates", certificates);
        model.addAttribute("issuer", issuer);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("revokedCount", revokedCount);
        return "issuer/dashboard";
    }

    @GetMapping("/issue")
    public String issuePage() {
        return "issuer/issue";
    }


    @PostMapping("/issue")
    public String issueSingle(@RequestParam String recipientName,
                              @RequestParam String recipientEmail,
                              @RequestParam String courseTitle,
                              @RequestParam String enrollmentNo,
                              @RequestParam String passingYear,
                              @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        try {
            User issuer = getIssuer(userDetails);
            certificateService.issueCertificate(
                    recipientName, recipientEmail, courseTitle,
                    enrollmentNo, passingYear, issuer, logoFile);
            model.addAttribute("success", "Certificate issued successfully!");
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "issuer/issue";
    }

    @GetMapping("/bulk")
    public String bulkPage() {
        return "issuer/bulk";
    }

    @PostMapping("/bulk")
    public String bulkUpload(@RequestParam("file") MultipartFile file,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        try {
            User issuer = getIssuer(userDetails);
            bulkCertificateService.processBulkUpload(file, issuer);
            model.addAttribute("success", "Bulk upload processed successfully!");
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "issuer/bulk";
    }

    @PostMapping("/revoke/{id}")
    public String revoke(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails) {
        certificateService.revokeCertificate(id);
        return "redirect:/issuer/dashboard";
    }

    private User getIssuer(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Issuer not found"));
    }
}