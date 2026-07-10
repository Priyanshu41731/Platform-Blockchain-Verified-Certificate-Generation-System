package com.certificate.platform.util;

import com.certificate.platform.model.Certificate;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGeneratorService {

    @Value("${app.certificate.output-dir:certificates}")
    private String outputDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public File generateCertificate(Certificate cert , String logoPath) {
        try {
            // Create output directory if not exists
            Path dirPath = Paths.get(outputDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            String fileName = cert.getCertificateId() + ".pdf";
            String filePath = outputDir + "/" + fileName;

            Document document = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Background color
            PdfContentByte canvas = writer.getDirectContentUnder();
            canvas.setColorFill(new BaseColor(245, 245, 250));
            canvas.rectangle(0, 0, document.getPageSize().getWidth(), document.getPageSize().getHeight());
            canvas.fill();

            // Border
            PdfContentByte border = writer.getDirectContent();
            border.setColorStroke(new BaseColor(26, 26, 46));
            border.setLineWidth(3f);
            border.rectangle(20, 20, document.getPageSize().getWidth() - 40,
                    document.getPageSize().getHeight() - 40);
            border.stroke();

            // Inner border
            border.setColorStroke(new BaseColor(76, 201, 240));
            border.setLineWidth(1.5f);
            border.rectangle(28, 28, document.getPageSize().getWidth() - 56,
                    document.getPageSize().getHeight() - 56);
            border.stroke();

            // Fonts
            Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 36, Font.BOLD,
                    new BaseColor(26, 26, 46));
            Font subtitleFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.ITALIC,
                    new BaseColor(100, 100, 120));
            Font bodyFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.NORMAL,
                    new BaseColor(60, 60, 80));
            Font nameFont = new Font(Font.FontFamily.TIMES_ROMAN, 28, Font.BOLD,
                    new BaseColor(26, 26, 46));
            Font courseFont = new Font(Font.FontFamily.TIMES_ROMAN, 20, Font.BOLDITALIC,
                    new BaseColor(76, 201, 240));
            Font smallFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL,
                    new BaseColor(120, 120, 140));
            Font orgFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD,
                    new BaseColor(26, 26, 46));

            // Organization logo if uploaded
            if (logoPath != null) {
                try {
                    String logoFullPath = "logos/" + logoPath;
                    Image logo = Image.getInstance(logoFullPath);
                    logo.setAlignment(Element.ALIGN_CENTER);
                    logo.scaleToFit(120, 80);
                    logo.setSpacingBefore(20);
                    document.add(logo);
                } catch (Exception e) {
                    // Logo not found skip it
                }
            } else if (cert.getIssuer().getLogoPath() != null) {
                // Use issuer default logo if no logo uploaded
                try {
                    String logoFullPath = "logos/" + cert.getIssuer().getLogoPath();
                    Image logo = Image.getInstance(logoFullPath);
                    logo.setAlignment(Element.ALIGN_CENTER);
                    logo.scaleToFit(100, 60);
                    logo.setSpacingBefore(20);
                    document.add(logo);
                } catch (Exception e) {
                    // Logo not found skip it
                }
            }

// Organization name
            Paragraph orgName = new Paragraph(
                    cert.getIssuer().getOrganizationName().toUpperCase(), orgFont);
            orgName.setAlignment(Element.ALIGN_CENTER);
            orgName.setSpacingBefore(15);
            document.add(orgName);

            // Divider line
            addDivider(document, writer);

            // Certificate title
            Paragraph title = new Paragraph("CERTIFICATE OF COMPLETION", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(15);
            document.add(title);

            // Subtitle
            Paragraph subtitle = new Paragraph("This is to proudly certify that", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingBefore(10);
            document.add(subtitle);

            // Recipient name
            Paragraph recipientName = new Paragraph(
                    cert.getRecipientName().toUpperCase(), nameFont);
            recipientName.setAlignment(Element.ALIGN_CENTER);
            recipientName.setSpacingBefore(5);
            document.add(recipientName);

            // Body text
            Paragraph bodyText = new Paragraph("has successfully completed the program of", bodyFont);
            bodyText.setAlignment(Element.ALIGN_CENTER);
            bodyText.setSpacingBefore(8);
            document.add(bodyText);

            // Course name
            Paragraph courseName = new Paragraph(cert.getCourseTitle(), courseFont);
            courseName.setAlignment(Element.ALIGN_CENTER);
            courseName.setSpacingBefore(5);
            document.add(courseName);

            // Details
            Paragraph details = new Paragraph(
                    "Enrollment No: " + cert.getEnrollmentNo() +
                            "     |     Passing Year: " + cert.getPassingYear(), bodyFont);
            details.setAlignment(Element.ALIGN_CENTER);
            details.setSpacingBefore(8);
            document.add(details);

            // Issue date
            String formattedDate = cert.getIssuedAt()
                    .format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
            Paragraph dateText = new Paragraph("Issued on: " + formattedDate, bodyFont);
            dateText.setAlignment(Element.ALIGN_CENTER);
            dateText.setSpacingBefore(5);
            document.add(dateText);

            // Certificate ID
            Paragraph certId = new Paragraph(
                    "Certificate ID: " + cert.getCertificateId(), smallFont);
            certId.setAlignment(Element.ALIGN_CENTER);
            certId.setSpacingBefore(3);
            document.add(certId);

            // QR Code
            String verifyUrl = baseUrl + "/verify/" + cert.getCertificateId();
            byte[] qrBytes = generateQRCode(verifyUrl, 100, 100);
            Image qrImage = Image.getInstance(qrBytes);
            qrImage.setAlignment(Element.ALIGN_CENTER);
            qrImage.setSpacingBefore(8);
            qrImage.scaleToFit(80, 80);
            document.add(qrImage);

            Paragraph scanText = new Paragraph("Scan to verify certificate", smallFont);
            scanText.setAlignment(Element.ALIGN_CENTER);
            scanText.setSpacingBefore(2);
            document.add(scanText);

            Paragraph poweredBy = new Paragraph("Powered by CertifyMe Platform", smallFont);
            poweredBy.setAlignment(Element.ALIGN_CENTER);
            poweredBy.setSpacingBefore(2);
            document.add(poweredBy);

            document.close();
            return new File(filePath);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage());
        }
    }

    private void addDivider(Document document, PdfWriter writer) throws DocumentException {
        PdfContentByte cb = writer.getDirectContent();
        cb.setColorStroke(new BaseColor(76, 201, 240));
        cb.setLineWidth(1f);
        float y = writer.getVerticalPosition(false);
        cb.moveTo(60, y - 5);
        cb.lineTo(document.getPageSize().getWidth() - 60, y - 5);
        cb.stroke();
    }

    private byte[] generateQRCode(String text, int width, int height) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        return outputStream.toByteArray();
    }
}