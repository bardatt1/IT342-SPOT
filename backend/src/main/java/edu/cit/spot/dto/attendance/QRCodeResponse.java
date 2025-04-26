package edu.cit.spot.dto.attendance;

import java.time.LocalDate;

public record QRCodeResponse(
    String qrCodeUrl,
    String qrCodeImageBase64,
    Long sectionId,
    String courseName,
    LocalDate date,
    Long expiresInSeconds
) {
    public QRCodeResponse {
        if (qrCodeUrl == null || qrCodeUrl.isBlank()) {
            throw new IllegalArgumentException("QR code URL cannot be blank");
        }
        if (qrCodeImageBase64 == null || qrCodeImageBase64.isBlank()) {
            throw new IllegalArgumentException("QR code image cannot be blank");
        }
        if (sectionId == null || sectionId <= 0) {
            throw new IllegalArgumentException("Invalid section ID");
        }
        if (courseName == null || courseName.isBlank()) {
            throw new IllegalArgumentException("Course name cannot be blank");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (expiresInSeconds == null || expiresInSeconds <= 0) {
            throw new IllegalArgumentException("Expiration time must be positive");
        }
    }
}
