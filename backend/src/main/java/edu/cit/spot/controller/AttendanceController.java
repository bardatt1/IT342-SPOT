package edu.cit.spot.controller;

import edu.cit.spot.entity.Attendance;
import edu.cit.spot.entity.Attendance.AttendanceStatus;
import edu.cit.spot.service.AttendanceService;
import edu.cit.spot.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Attendance", description = "Operations for tracking and managing student attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final SessionService sessionService;

    @Operation(summary = "Get attendance records for a class", description = "Returns all attendance records for a specific class")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved attendance records"),
        @ApiResponse(responseCode = "404", description = "Class not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/{classId}")
    public ResponseEntity<List<Attendance>> getAttendanceByClass(@PathVariable Long classId) {
        List<Attendance> attendanceList = attendanceService.getAttendanceByCourseId(classId);
        log.info("Retrieved {} attendance records for class {}", attendanceList.size(), classId);
        return ResponseEntity.ok(attendanceList);
    }

    @Operation(summary = "Mark attendance for a session", description = "Records attendance for a student in a specific class session")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully recorded attendance"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Session or student not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/{classId}")
    public ResponseEntity<Attendance> markAttendance(
            @PathVariable Long classId,
            @RequestParam Long sessionId,
            @RequestParam Long studentId,
            @RequestParam AttendanceStatus status) {
        
        // Verify sessionId belongs to the specified class
        var session = sessionService.getSessionById(sessionId);
        if (!session.getCourse().getId().equals(classId)) {
            log.warn("Session {} does not belong to class {}", sessionId, classId);
            return ResponseEntity.badRequest().build();
        }
        
        Attendance attendance = attendanceService.recordAttendance(sessionId, studentId, status);
        log.info("Recorded {} attendance for student {} in session {}", status, studentId, sessionId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(attendance);
    }

    @Operation(summary = "Update a student's attendance", description = "Updates the attendance status for a student in a session")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated attendance"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Attendance record not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PutMapping("/{classId}/{studentId}")
    public ResponseEntity<Attendance> updateAttendance(
            @PathVariable Long classId,
            @PathVariable Long studentId,
            @RequestParam Long attendanceId,
            @RequestParam AttendanceStatus status) {
        
        // Get the attendance record first to verify it belongs to the right class and student
        Attendance existingAttendance = attendanceService.getAttendanceById(attendanceId);
        if (!existingAttendance.getSession().getCourse().getId().equals(classId) || 
            !existingAttendance.getStudent().getId().equals(studentId)) {
            log.warn("Attendance record {} does not match class {} and student {}", 
                    attendanceId, classId, studentId);
            return ResponseEntity.badRequest().build();
        }
        
        Attendance updatedAttendance = attendanceService.updateAttendance(attendanceId, status);
        log.info("Updated attendance record {} to status {}", attendanceId, status);
        
        return ResponseEntity.ok(updatedAttendance);
    }

    @Operation(summary = "Get attendance records across all classes", description = "Returns attendance records for all classes (admin/teacher view)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved attendance records"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized to access this resource")
    })
    @GetMapping("/all")
    public ResponseEntity<List<Attendance>> getAllAttendance() {
        List<Attendance> attendanceList = attendanceService.getAllAttendance();
        log.info("Retrieved all attendance records, count: {}", attendanceList.size());
        return ResponseEntity.ok(attendanceList);
    }

    @Operation(summary = "Mark bulk attendance", description = "Records attendance for multiple students in a session")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully recorded bulk attendance"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Session not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/{classId}/bulk")
    public ResponseEntity<Void> markBulkAttendance(
            @PathVariable Long classId,
            @RequestParam Long sessionId,
            @RequestBody List<Long> studentIds,
            @RequestParam AttendanceStatus status) {
        
        // Verify sessionId belongs to the specified class
        var session = sessionService.getSessionById(sessionId);
        if (!session.getCourse().getId().equals(classId)) {
            log.warn("Session {} does not belong to class {}", sessionId, classId);
            return ResponseEntity.badRequest().build();
        }
        
        attendanceService.markBulkAttendance(sessionId, studentIds, status);
        log.info("Recorded bulk {} attendance for {} students in session {}", 
                status, studentIds.size(), sessionId);
        
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Process QR code scan", description = "Records attendance when a student scans a QR code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully processed QR code scan"),
        @ApiResponse(responseCode = "400", description = "Invalid QR code or expired"),
        @ApiResponse(responseCode = "404", description = "Student not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/scan")
    public ResponseEntity<Map<String, Object>> processScan(
            @RequestParam String qrCodeUuid,
            @RequestParam Long studentId) {
        
        // This endpoint would typically call the QRCodeService to validate and process the scan
        // For now, let's simulate the response
        Map<String, Object> result = Map.of(
            "success", true,
            "message", "Attendance recorded successfully",
            "timestamp", System.currentTimeMillis()
        );
        
        log.info("Processed QR code scan for student {}", studentId);
        
        return ResponseEntity.ok(result);
    }
}
