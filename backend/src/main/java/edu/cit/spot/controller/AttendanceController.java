package edu.cit.spot.controller;

import edu.cit.spot.dto.ApiResponse;
import edu.cit.spot.dto.attendance.AttendanceDto;
import edu.cit.spot.dto.attendance.LogAttendanceRequest;
import edu.cit.spot.dto.attendance.QRCodeResponse;
import edu.cit.spot.exception.GlobalExceptionHandler;
import edu.cit.spot.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@Tag(name = "Attendance", description = "APIs for attendance management")
@SecurityRequirement(name = "bearerAuth")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;
    
    @PostMapping("/log")
    @Operation(
        summary = "Log attendance", 
        description = "Student logs attendance for a section. Uses the current authenticated student and server time (GMT+8)."
    )
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<AttendanceDto>> logAttendance(@Valid @RequestBody LogAttendanceRequest request) {
        try {
            AttendanceDto attendanceDto = attendanceService.logAttendance(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("SUCCESS", "Attendance logged successfully", attendanceDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @PostMapping("/generate-qr")
    @Operation(
        summary = "Generate QR code", 
        description = "Teacher generates QR code for attendance that links to the frontend URL. Uses current authenticated teacher."
    )
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<QRCodeResponse>> generateQRCode(@RequestParam Long sectionId) {
        try {
            QRCodeResponse qrCodeResponse = attendanceService.generateQRCode(sectionId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "QR code generated successfully", qrCodeResponse));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get student attendance", description = "Get attendance records for a specific student")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<AttendanceDto>>> getAttendanceByStudentId(@PathVariable Long studentId) {
        try {
            List<AttendanceDto> attendances = attendanceService.getAttendanceByStudentId(studentId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Attendance records retrieved successfully", attendances));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/section/{sectionId}")
    @Operation(summary = "Get section attendance", description = "Get all attendance records for a specific section")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<AttendanceDto>>> getAttendanceBySectionId(@PathVariable Long sectionId) {
        try {
            List<AttendanceDto> attendances = attendanceService.getAttendanceBySectionId(sectionId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Attendance records retrieved successfully", attendances));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/section/{sectionId}/date/{date}")
    @Operation(summary = "Get section attendance by date", description = "Get attendance records for a specific section on a specific date")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<AttendanceDto>>> getAttendanceBySectionAndDate(
            @PathVariable Long sectionId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<AttendanceDto> attendances = attendanceService.getAttendanceBySectionAndDate(sectionId, date);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Attendance records retrieved successfully", attendances));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete attendance", description = "Delete an attendance record")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Boolean>> deleteAttendance(@PathVariable Long id) {
        try {
            boolean result = attendanceService.deleteAttendance(id);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Attendance record deleted successfully", result));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
