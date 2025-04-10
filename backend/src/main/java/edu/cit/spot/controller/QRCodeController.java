package edu.cit.spot.controller;

import edu.cit.spot.entity.QRCode;
import edu.cit.spot.entity.Session;
import edu.cit.spot.service.QRCodeService;
import edu.cit.spot.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/qrcode")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "QR Code", description = "Operations for QR code generation and management")
public class QRCodeController {

    private final QRCodeService qrCodeService;
    private final SessionService sessionService;

    @Operation(summary = "Generate new QR code for a class session", description = "Creates a new QR code that students can scan to mark attendance")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully generated QR code"),
        @ApiResponse(responseCode = "400", description = "Invalid input or no active session"),
        @ApiResponse(responseCode = "404", description = "Class not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/{classId}")
    public ResponseEntity<Map<String, Object>> generateQRCode(
            @PathVariable Long classId,
            @RequestParam(defaultValue = "15") int expirationMinutes) {
        
        // Find an active session for this class
        List<Session> activeSessions = sessionService.getActiveSessions(classId);
        
        if (activeSessions.isEmpty()) {
            log.warn("No active sessions found for class {}", classId);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "No active session found for this class",
                "message", "Please start a session before generating a QR code"
            ));
        }
        
        // Use the first active session
        Session activeSession = activeSessions.get(0);
        
        // Generate QR code
        QRCode qrCode = qrCodeService.generateQRCode(activeSession.getId(), expirationMinutes);
        log.info("Generated QR code for class {} session {}, expires in {} minutes", 
                classId, activeSession.getId(), expirationMinutes);
        
        // Return QR code details
        Map<String, Object> response = new HashMap<>();
        response.put("qrCode", qrCode);
        response.put("session", activeSession);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get existing QR code", description = "Returns the active QR code for a class session")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved QR code"),
        @ApiResponse(responseCode = "204", description = "No active QR code found"),
        @ApiResponse(responseCode = "404", description = "Class not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/{classId}")
    public ResponseEntity<Map<String, Object>> getQRCode(@PathVariable Long classId) {
        // Find an active session for this class
        List<Session> activeSessions = sessionService.getActiveSessions(classId);
        
        if (activeSessions.isEmpty()) {
            log.info("No active sessions found for class {}", classId);
            return ResponseEntity.noContent().build();
        }
        
        // Use the first active session
        Session activeSession = activeSessions.get(0);
        
        // Get active QR codes for this session
        List<QRCode> activeQRCodes = qrCodeService.getActiveQRCodesBySessionId(activeSession.getId());
        
        if (activeQRCodes.isEmpty()) {
            log.info("No active QR codes found for session {}", activeSession.getId());
            return ResponseEntity.noContent().build();
        }
        
        // Return the first active QR code
        QRCode qrCode = activeQRCodes.get(0);
        
        Map<String, Object> response = new HashMap<>();
        response.put("qrCode", qrCode);
        response.put("session", activeSession);
        
        log.info("Retrieved active QR code for class {} session {}", classId, activeSession.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get QR code image", description = "Returns the QR code image for scanning")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully generated QR code image"),
        @ApiResponse(responseCode = "404", description = "QR code not found or expired"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/{uuid}/image")
    public ResponseEntity<byte[]> getQRCodeImage(@PathVariable String uuid) {
        try {
            // Get QR code by UUID
            QRCode qrCode = qrCodeService.getQRCodeByUuid(uuid);
            
            // Generate QR code image
            byte[] qrCodeImage = qrCodeService.generateQRCodeImage(uuid);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            
            log.info("Generated QR code image for UUID: {}", uuid);
            return new ResponseEntity<>(qrCodeImage, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error generating QR code image for UUID: {}", uuid, e);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Deactivate QR code", description = "Manually deactivates a QR code before its expiration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deactivated QR code"),
        @ApiResponse(responseCode = "404", description = "QR code not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/{uuid}/deactivate")
    public ResponseEntity<Void> deactivateQRCode(@PathVariable String uuid) {
        try {
            QRCode qrCode = qrCodeService.getQRCodeByUuid(uuid);
            qrCodeService.deactivateQRCode(qrCode.getId());
            
            log.info("Deactivated QR code with UUID: {}", uuid);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deactivating QR code with UUID: {}", uuid, e);
            return ResponseEntity.notFound().build();
        }
    }
}
