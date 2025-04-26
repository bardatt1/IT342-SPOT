package edu.cit.spot.controller;

import edu.cit.spot.dto.ApiResponse;
import edu.cit.spot.dto.teacher.TeacherDto;
import edu.cit.spot.dto.teacher.TeacherUpdateRequest;
import edu.cit.spot.exception.GlobalExceptionHandler;
import edu.cit.spot.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teachers")
@Tag(name = "Teachers", description = "APIs for teacher management")
@SecurityRequirement(name = "bearerAuth")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;
    
    @GetMapping("/{id}")
    @Operation(summary = "Get teacher by ID", description = "Get a teacher by their ID")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id)")
    public ResponseEntity<ApiResponse<TeacherDto>> getTeacherById(@PathVariable Long id) {
        try {
            TeacherDto teacherDto = teacherService.getTeacherById(id);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Teacher retrieved successfully", teacherDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/email/{email}")
    @Operation(summary = "Get teacher by email", description = "Get a teacher by their email")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUserEmail(#email)")
    public ResponseEntity<ApiResponse<TeacherDto>> getTeacherByEmail(@PathVariable String email) {
        try {
            TeacherDto teacherDto = teacherService.getTeacherByEmail(email);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Teacher retrieved successfully", teacherDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update teacher", description = "Update a teacher's details")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id)")
    public ResponseEntity<ApiResponse<TeacherDto>> updateTeacher(
            @PathVariable Long id, 
            @Valid @RequestBody TeacherUpdateRequest request) {
        try {
            TeacherDto teacherDto = teacherService.updateTeacher(id, request);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Teacher updated successfully", teacherDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @PostMapping("/{id}/bind-oauth")
    @Operation(summary = "Bind Google account to teacher", description = "Bind a Google account to a teacher account")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id)")
    public ResponseEntity<ApiResponse<TeacherDto>> bindGoogleAccount(
            @PathVariable Long id, 
            @RequestParam String googleId) {
        try {
            TeacherDto teacherDto = teacherService.bindGoogleAccount(id, googleId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Google account bound successfully", teacherDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @PostMapping("/{teacherId}/assign/{sectionId}")
    @Operation(summary = "Assign teacher to section", description = "Assign a teacher to a section")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#teacherId)")
    public ResponseEntity<ApiResponse<TeacherDto>> assignToSection(
            @PathVariable Long teacherId, 
            @PathVariable Long sectionId) {
        try {
            TeacherDto teacherDto = teacherService.assignToSection(teacherId, sectionId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Teacher assigned to section successfully", teacherDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @PostMapping("/{teacherId}/remove-from-section")
    @Operation(summary = "Remove teacher from section", description = "Remove a teacher from their assigned section")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#teacherId)")
    public ResponseEntity<ApiResponse<TeacherDto>> removeFromSection(@PathVariable Long teacherId) {
        try {
            TeacherDto teacherDto = teacherService.removeFromSection(teacherId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Teacher removed from section successfully", teacherDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
