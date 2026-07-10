package com.certificate.platform.dto;

import lombok.Data;

@Data
public class CertificateDTO {
    private String recipientName;
    private String recipientEmail;
    private String courseTitle;
    private String enrollmentNo;
    private String passingYear;
}