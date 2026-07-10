package com.certificate.platform.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import java.io.File;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendCertificateEmail(String toEmail, String recipientName,
                                     String courseName, File certificatePdf) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Your Certificate - " + courseName);
            helper.setText("Dear " + recipientName + ",\n\n" +
                    "Congratulations! Please find your certificate attached.\n\n" +
                    "You can verify your certificate on our platform.\n\n" +
                    "Regards,\nCertificate Platform Team");

            helper.addAttachment(certificatePdf.getName(), certificatePdf);
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
}