package edu.cit.spot.controller;

import edu.cit.spot.dto.ApiResponse;
import edu.cit.spot.dto.analytics.AttendanceAnalyticsDto;
import edu.cit.spot.dto.analytics.StudentAttendanceDto;
import edu.cit.spot.entity.Student;
import edu.cit.spot.exception.GlobalExceptionHandler;
import edu.cit.spot.repository.StudentRepository;
import edu.cit.spot.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "API for attendance analytics")
public class AnalyticsController {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);

    @Autowired
    private AnalyticsService analyticsService;
    
    @Autowired
    private StudentRepository studentRepository;

    @GetMapping("/{sectionId}")
    @Operation(summary = "Get section analytics", description = "Get attendance analytics for a section")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<AttendanceAnalyticsDto>> getSectionAnalytics(@PathVariable Long sectionId) {
        try {
            AttendanceAnalyticsDto analytics = analyticsService.getSectionAnalytics(sectionId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Section analytics retrieved successfully", analytics));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{sectionId}/range")
    @Operation(summary = "Get section analytics for date range", description = "Get attendance analytics for a section within a date range")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<AttendanceAnalyticsDto>> getSectionAnalyticsForDateRange(
            @PathVariable Long sectionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            AttendanceAnalyticsDto analytics = analyticsService.getSectionAnalyticsForDateRange(sectionId, startDate, endDate);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Section analytics retrieved successfully", analytics));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{sectionId}/students")
    @Operation(summary = "Get student attendance stats", description = "Get attendance statistics for all students in a section")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentAttendanceDto>> getStudentAttendanceStatsInSection(
            @PathVariable Long sectionId,
            @PathVariable Long studentId) {
        try {
            // Get current authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // Additional security check for students - they can only access their own data
            if (authentication != null && authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))) {
                
                // Extract username (email) from authentication principal 
                String email = null;
                Object principal = authentication.getPrincipal();
                
                if (principal instanceof UserDetails) {
                    email = ((UserDetails) principal).getUsername();
                    logger.debug("Extracted email from UserDetails: {}", email);
                } else {
                    email = principal.toString();
                    logger.debug("Extracted email from principal.toString(): {}", email);
                }
                
                logger.debug("Authenticated email: {}", email);
                
                // Find student by email to get their ID - Use try-catch to handle potential errors
                try {
                    Optional<Student> studentOpt = studentRepository.findByEmail(email);
                    
                    if (studentOpt.isPresent()) {
                        Student student = studentOpt.get();
                        logger.debug("Found student with ID: {} for email: {}", student.getId(), email);
                        
                        // Verify the authenticated user is requesting their own data
                        if (!student.getId().equals(studentId)) {
                            logger.warn("Student with ID {} attempted to access data for student ID {}", student.getId(), studentId);
                            return GlobalExceptionHandler.errorResponseEntity(
                                    "Access denied: Students can only view their own attendance records", 
                                    HttpStatus.FORBIDDEN);
                        }
                    } else {
                        logger.error("No student found with email: {}", email);
                        return GlobalExceptionHandler.errorResponseEntity(
                                "Student not found with the authenticated credentials", 
                                HttpStatus.UNAUTHORIZED);
                    }
                } catch (Exception e) {
                    logger.error("Error retrieving student by email: {}", email, e);
                    return GlobalExceptionHandler.errorResponseEntity(
                            "Error authenticating student", 
                            HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            
            StudentAttendanceDto studentStats = analyticsService.getStudentAttendanceStatsInSection(sectionId, studentId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Student attendance stats retrieved successfully", studentStats));
        } catch (Exception e) {
            logger.error("Error retrieving student attendance stats", e);
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
