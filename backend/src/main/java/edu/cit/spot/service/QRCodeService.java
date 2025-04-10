package edu.cit.spot.service;

import edu.cit.spot.entity.QRCode;
import java.util.List;
import java.util.Map;

public interface QRCodeService {
    List<QRCode> getAllQRCodes();
    QRCode getQRCodeById(Long id);
    QRCode getQRCodeByUuid(String uuid);
    List<QRCode> getActiveQRCodesBySessionId(Long sessionId);
    QRCode generateQRCode(Long sessionId, int expirationMinutes);
    void deactivateQRCode(Long id);
    Map<String, Object> processScannedQRCode(String uuid, Long studentId);
    byte[] generateQRCodeImage(String uuid);
    void cleanupExpiredQRCodes();
}
