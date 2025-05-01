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
@RequestMapping("/api/teacher-profile")
@Tag(name = "Teacher Profile", description = "APIs for teacher's own profile management")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherProfileController {

    @Autowired
    private TeacherService teacherService;
    
    @GetMapping("/me")
    @Operation(summary = "Get current teacher profile", description = "Get the profile of the currently authenticated teacher")
    public ResponseEntity<ApiResponse<TeacherDto>> getCurrentTeacher() {
        try {
            TeacherDto teacherDto = teacherService.getCurrentTeacher();
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Current teacher profile retrieved successfully", teacherDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @PutMapping("/me")
    @Operation(summary = "Update current teacher profile", description = "Update the profile of the currently authenticated teacher")
    public ResponseEntity<ApiResponse<TeacherDto>> updateCurrentTeacher(@Valid @RequestBody TeacherUpdateRequest request) {
        try {
            TeacherDto currentTeacher = teacherService.getCurrentTeacher();
            // Use the id field directly since TeacherDto doesn't have a getId() method
            TeacherDto updatedTeacher = teacherService.updateTeacher(currentTeacher.id(), request);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Profile updated successfully", updatedTeacher));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
