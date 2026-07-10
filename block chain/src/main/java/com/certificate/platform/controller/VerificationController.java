package com.certificate.platform.controller;

import com.certificate.platform.model.Certificate;
import com.certificate.platform.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class VerificationController {

    private final CertificateService certificateService;

    @GetMapping("/verify")
    public String verifyPage(@RequestParam(required = false) String certificateId, Model model) {
        if (certificateId != null && !certificateId.isBlank()) {
            try {
                Certificate cert = certificateService.findByCertificateId(certificateId);
                boolean dbValid = cert.getStatus() == Certificate.CertificateStatus.ACTIVE;
                boolean chainValid = certificateService.verifyOnBlockchain(cert.getHash());

                model.addAttribute("certificate", cert);
                model.addAttribute("valid", dbValid);
                model.addAttribute("chainValid", chainValid);
            } catch (RuntimeException e) {
                model.addAttribute("error", "No certificate found with this ID");
            }
        }
        return "verify";
    }

    @GetMapping("/verify/{certificateId}")
    public String verifyCertificateByPath(@PathVariable String certificateId, Model model) {
        try {
            Certificate cert = certificateService.findByCertificateId(certificateId);
            model.addAttribute("certificate", cert);
            model.addAttribute("valid", cert.getStatus() == Certificate.CertificateStatus.ACTIVE);
        } catch (RuntimeException e) {
            model.addAttribute("error", "No certificate found with this ID");
        }
        return "verify";
    }
}