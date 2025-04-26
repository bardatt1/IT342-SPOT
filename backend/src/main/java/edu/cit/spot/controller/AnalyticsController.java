package edu.cit.spot.controller;

import edu.cit.spot.dto.ApiResponse;
import edu.cit.spot.dto.analytics.AttendanceAnalyticsDto;
import edu.cit.spot.dto.analytics.StudentAttendanceDto;
import edu.cit.spot.exception.GlobalExceptionHandler;
import edu.cit.spot.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "APIs for attendance analytics")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;
    
    @GetMapping("/{sectionId}")
    @Operation(summary = "Get section analytics", description = "Get attendance analytics for a specific section")
    public ResponseEntity<ApiResponse<AttendanceAnalyticsDto>> getSectionAnalytics(@PathVariable Long sectionId) {
        try {
            AttendanceAnalyticsDto analytics = analyticsService.getSectionAnalytics(sectionId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Analytics retrieved successfully", analytics));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/{sectionId}/range")
    @Operation(summary = "Get section analytics for date range", description = "Get attendance analytics for a specific section within a date range")
    public ResponseEntity<ApiResponse<AttendanceAnalyticsDto>> getSectionAnalyticsForDateRange(
            @PathVariable Long sectionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            AttendanceAnalyticsDto analytics = analyticsService.getSectionAnalyticsForDateRange(sectionId, startDate, endDate);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Analytics retrieved successfully", analytics));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/{sectionId}/students")
    @Operation(summary = "Get student attendance stats", description = "Get attendance statistics for all students in a section")
    public ResponseEntity<ApiResponse<List<StudentAttendanceDto>>> getStudentAttendanceStats(@PathVariable Long sectionId) {
        try {
            List<StudentAttendanceDto> studentStats = analyticsService.getStudentAttendanceStats(sectionId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Student attendance stats retrieved successfully", studentStats));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/{sectionId}/students/{studentId}")
    @Operation(summary = "Get student attendance stats in section", description = "Get attendance statistics for a specific student in a section")
    public ResponseEntity<ApiResponse<StudentAttendanceDto>> getStudentAttendanceStatsInSection(
            @PathVariable Long sectionId,
            @PathVariable Long studentId) {
        try {
            StudentAttendanceDto studentStats = analyticsService.getStudentAttendanceStatsInSection(sectionId, studentId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Student attendance stats retrieved successfully", studentStats));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
