package edu.cit.spot.service.impl;

import edu.cit.spot.entity.Attendance;
import edu.cit.spot.entity.QRCode;
import edu.cit.spot.entity.Session;
import edu.cit.spot.entity.User;
import edu.cit.spot.exception.ResourceNotFoundException;
import edu.cit.spot.repository.QRCodeRepository;
import edu.cit.spot.repository.SessionRepository;
import edu.cit.spot.repository.UserRepository;
import edu.cit.spot.service.AttendanceService;
import edu.cit.spot.service.QRCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

@Service
@RequiredArgsConstructor
public class QRCodeServiceImpl implements QRCodeService {
    private final QRCodeRepository qrCodeRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final AttendanceService attendanceService;

    @Override
    public List<QRCode> getAllQRCodes() {
        return qrCodeRepository.findAll();
    }

    @Override
    public QRCode getQRCodeById(Long id) {
        return qrCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QR Code not found with id: " + id));
    }

    @Override
    public QRCode getQRCodeByUuid(String uuid) {
        return qrCodeRepository.findByUuidAndIsActiveTrue(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("QR Code not found with uuid: " + uuid));
    }

    @Override
    public List<QRCode> getActiveQRCodesBySessionId(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));
        return qrCodeRepository.findBySessionAndIsActiveTrue(session);
    }

    @Override
    @Transactional
    public QRCode generateQRCode(Long sessionId, int expirationMinutes) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));
        
        // Deactivate any existing QR codes for this session
        List<QRCode> existingCodes = qrCodeRepository.findBySessionAndIsActiveTrue(session);
        existingCodes.forEach(code -> {
            code.setActive(false);
            qrCodeRepository.save(code);
        });
        
        // Create new QR code
        QRCode qrCode = new QRCode();
        qrCode.setSession(session);
        qrCode.setGeneratedAt(LocalDateTime.now());
        qrCode.setExpiresAt(LocalDateTime.now().plusMinutes(expirationMinutes));
        qrCode.setActive(true);
        
        return qrCodeRepository.save(qrCode);
    }

    @Override
    @Transactional
    public void deactivateQRCode(Long id) {
        QRCode qrCode = getQRCodeById(id);
        qrCode.setActive(false);
        qrCodeRepository.save(qrCode);
    }

    @Override
    @Transactional
    public Map<String, Object> processScannedQRCode(String uuid, Long studentId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Find the QR code
            QRCode qrCode = getQRCodeByUuid(uuid);
            
            // Check if expired
            if (LocalDateTime.now().isAfter(qrCode.getExpiresAt())) {
                qrCode.setActive(false);
                qrCodeRepository.save(qrCode);
                result.put("success", false);
                result.put("message", "QR code has expired");
                return result;
            }
            
            // Find the student
            User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
            
            // Record attendance
            Attendance attendance = attendanceService.recordAttendance(
                qrCode.getSession().getId(), 
                studentId, 
                Attendance.AttendanceStatus.PRESENT
            );
            
            result.put("success", true);
            result.put("message", "Attendance recorded successfully");
            result.put("attendance", attendance);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        
        return result;
    }

    @Override
    public byte[] generateQRCodeImage(String uuid) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(uuid, BarcodeFormat.QR_CODE, 250, 250);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code image", e);
        }
    }

    @Override
    @Transactional
    public void cleanupExpiredQRCodes() {
        List<QRCode> expiredCodes = qrCodeRepository.findByExpiresAtBeforeAndIsActiveTrue(LocalDateTime.now());
        expiredCodes.forEach(code -> {
            code.setActive(false);
            qrCodeRepository.save(code);
        });
    }
}
