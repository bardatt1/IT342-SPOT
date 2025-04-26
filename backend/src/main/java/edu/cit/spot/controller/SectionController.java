package edu.cit.spot.controller;

import edu.cit.spot.dto.ApiResponse;
import edu.cit.spot.dto.section.CreateSectionRequest;
import edu.cit.spot.dto.section.SectionDto;
import edu.cit.spot.dto.section.SectionUpdateRequest;
import edu.cit.spot.exception.GlobalExceptionHandler;
import edu.cit.spot.security.SecurityService;
import edu.cit.spot.service.SectionService;
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
@RequestMapping("/api/sections")
@Tag(name = "Sections", description = "APIs for section management")
@SecurityRequirement(name = "bearerAuth")
public class SectionController {

    @Autowired
    private SectionService sectionService;
    
    @Autowired
    private SecurityService securityService;
    
    @GetMapping
    @Operation(summary = "Get sections by course ID", description = "Get sections for a specific course")
    public ResponseEntity<ApiResponse<List<SectionDto>>> getSectionsByCourseId(@RequestParam Long courseId) {
        try {
            List<SectionDto> sections = sectionService.getSectionsByCourseId(courseId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Sections retrieved successfully", sections));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get section by ID", description = "Get a section by its ID")
    public ResponseEntity<ApiResponse<SectionDto>> getSectionById(@PathVariable Long id) {
        try {
            SectionDto sectionDto = sectionService.getSectionById(id);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Section retrieved successfully", sectionDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping
    @Operation(summary = "Create section", description = "Create a new section")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SectionDto>> createSection(@Valid @RequestBody CreateSectionRequest request) {
        try {
            SectionDto sectionDto = sectionService.createSection(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("SUCCESS", "Section created successfully", sectionDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update section", description = "Update an existing section")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<SectionDto>> updateSection(
            @PathVariable Long id, 
            @Valid @RequestBody SectionUpdateRequest request) {
        try {
            SectionDto sectionDto = sectionService.updateSection(id, request);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Section updated successfully", sectionDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete section", description = "Delete a section")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> deleteSection(@PathVariable Long id) {
        try {
            boolean result = sectionService.deleteSection(id);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Section deleted successfully", result));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping("/{id}/assign")
    @Operation(summary = "Assign teacher to section", description = "Assign a teacher to a section (Admin can assign any teacher, teachers can assign themselves)")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TEACHER') and @securityService.isCurrentTeacher(#teacherId))")
    public ResponseEntity<ApiResponse<SectionDto>> assignTeacher(
            @PathVariable Long id, 
            @RequestParam Long teacherId) {
        try {
            SectionDto sectionDto = sectionService.assignTeacher(id, teacherId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Teacher assigned successfully", sectionDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @PostMapping("/{id}/open")
    @Operation(summary = "Open enrollment", description = "Open enrollment for a section")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<SectionDto>> openEnrollment(
            @PathVariable Long id, 
            @RequestParam String enrollmentKey) {
        try {
            SectionDto sectionDto = sectionService.openEnrollment(id, enrollmentKey);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Enrollment opened successfully", sectionDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @PostMapping("/{id}/close")
    @Operation(summary = "Close enrollment", description = "Close enrollment for a section")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<SectionDto>> closeEnrollment(@PathVariable Long id) {
        try {
            SectionDto sectionDto = sectionService.closeEnrollment(id);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Enrollment closed successfully", sectionDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @PostMapping("/{id}/end")
    @Operation(summary = "End section", description = "End a section, clearing everything including seats and enrollments")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<SectionDto>> endSection(@PathVariable Long id) {
        try {
            SectionDto sectionDto = sectionService.endSection(id);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Section ended successfully", sectionDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
