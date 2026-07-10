package com.certificate.platform.controller;

import com.certificate.platform.service.CertificateService;
import com.certificate.platform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.certificate.platform.model.User;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final CertificateService certificateService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<User> issuers = userService.getAllIssuers();
        long pendingCount = issuers.stream()
                .filter(i -> i.getStatus() == User.Status.PENDING).count();
        long approvedCount = issuers.stream()
                .filter(i -> i.getStatus() == User.Status.APPROVED).count();

        model.addAttribute("issuers", issuers);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("approvedCount", approvedCount);
        return "admin/dashboard";
    }

    @PostMapping("/approve/{id}")
    public String approveIssuer(@PathVariable Long id) {
        userService.approveIssuer(id);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/reject/{id}")
    public String rejectIssuer(@PathVariable Long id) {
        userService.rejectIssuer(id);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/certificates")
    public String allCertificates(Model model) {
        model.addAttribute("certificates", certificateService.getAllCertificates());
        return "admin/certificates";
    }

    @PostMapping("/revoke/{id}")
    public String revokeCertificate(@PathVariable Long id) {
        certificateService.revokeCertificate(id);
        return "redirect:/admin/certificates";
    }
}