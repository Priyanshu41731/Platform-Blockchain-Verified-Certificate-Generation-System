package com.certificate.platform.service;

import com.certificate.platform.model.Certificate;
import com.certificate.platform.model.User;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class BulkCertificateService {

    private final CertificateService certificateService;
    private final EmailService emailService;

    public void processBulkUpload(MultipartFile file, User issuer) {
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int success = 0;
            int failed = 0;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String recipientName  = getCellValue(row, 0);
                    String recipientEmail = getCellValue(row, 1);
                    String courseTitle    = getCellValue(row, 2);
                    String enrollmentNo   = getCellValue(row, 3);
                    String passingYear    = getCellValue(row, 4);

                    Certificate cert = certificateService.issueCertificate(
                            recipientName, recipientEmail, courseTitle, enrollmentNo, passingYear, issuer , null);

                    success++;
                } catch (Exception e) {
                    failed++;
                }
            }

            System.out.println("Bulk upload done. Success: " + success + " Failed: " + failed);

        } catch (Exception e) {
            throw new RuntimeException("Failed to process Excel file: " + e.getMessage());
        }
    }

    private String getCellValue(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
    }
}