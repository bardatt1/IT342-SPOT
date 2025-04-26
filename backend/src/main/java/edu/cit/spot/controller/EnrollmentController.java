package edu.cit.spot.controller;

import edu.cit.spot.dto.ApiResponse;
import edu.cit.spot.dto.enrollment.EnrollRequest;
import edu.cit.spot.dto.enrollment.EnrollmentDto;
import edu.cit.spot.exception.GlobalExceptionHandler;
import edu.cit.spot.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@Tag(name = "Enrollments", description = "APIs for enrollment management")
@SecurityRequirement(name = "bearerAuth")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;
    
    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get enrollments by student ID", description = "Get all enrollments for a specific student")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<EnrollmentDto>>> getEnrollmentsByStudentId(@PathVariable Long studentId) {
        try {
            List<EnrollmentDto> enrollments = enrollmentService.getEnrollmentsByStudentId(studentId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Enrollments retrieved successfully", enrollments));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/section/{sectionId}")
    @Operation(summary = "Get enrollments by section ID", description = "Get all enrollments for a specific section")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<EnrollmentDto>>> getEnrollmentsBySectionId(@PathVariable Long sectionId) {
        try {
            List<EnrollmentDto> enrollments = enrollmentService.getEnrollmentsBySectionId(sectionId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Enrollments retrieved successfully", enrollments));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("/enroll")
    @Operation(summary = "Enroll student", description = "Enroll a student in a section using an enrollment key")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<EnrollmentDto>> enrollStudent(@Valid @RequestBody EnrollRequest request) {
        try {
            EnrollmentDto enrollmentDto = enrollmentService.enrollStudent(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("SUCCESS", "Student enrolled successfully", enrollmentDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/status")
    @Operation(summary = "Check enrollment status", description = "Check if a student is enrolled in a section")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Boolean>> isStudentEnrolled(
            @RequestParam Long studentId, 
            @RequestParam Long sectionId) {
        try {
            boolean isEnrolled = enrollmentService.isStudentEnrolled(studentId, sectionId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Enrollment status checked", isEnrolled));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete enrollment", description = "Delete an enrollment")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Boolean>> deleteEnrollment(@PathVariable Long id) {
        try {
            boolean result = enrollmentService.deleteEnrollment(id);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Enrollment deleted successfully", result));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/section/{sectionId}")
    @Operation(summary = "Delete enrollments by section", description = "Delete all enrollments in a section")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Boolean>> deleteEnrollmentsBySectionId(@PathVariable Long sectionId) {
        try {
            boolean result = enrollmentService.deleteEnrollmentsBySectionId(sectionId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "All enrollments in section deleted successfully", result));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
