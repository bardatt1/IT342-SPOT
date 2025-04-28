package edu.cit.spot.controller;

import edu.cit.spot.config.GlobalApiResponseConfig.StandardApiResponses;
import edu.cit.spot.dto.student.StudentDto;
import edu.cit.spot.dto.student.StudentUpdateRequest;
import edu.cit.spot.exception.GlobalExceptionHandler;
import edu.cit.spot.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Students", description = "APIs for student management")
@SecurityRequirement(name = "bearerAuth")
public class StudentController {

    @Autowired
    private StudentService studentService;
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Get student by ID", 
        description = "Retrieves a student's profile information by their ID in the SPOT system"
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER') or @securityUtil.isCurrentUser(#id)")
    @StandardApiResponses
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Student successfully retrieved",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = edu.cit.spot.dto.ApiResponse.class),
            examples = @ExampleObject(value = 
                "{\n" +
                "  \"result\": \"SUCCESS\",\n" +
                "  \"message\": \"Student retrieved successfully\",\n" +
                "  \"data\": {\n" +
                "    \"id\": 24601,\n" +
                "    \"name\": \"Jane Smith\",\n" +
                "    \"email\": \"jane.smith@students.spot.edu\",\n" +
                "    \"studentId\": \"ST24601\",\n" +
                "    \"googleEmail\": \"jane.smith@gmail.com\",\n" +
                "    \"enrollmentYear\": 2024,\n" +
                "    \"active\": true\n" +
                "  }\n" +
                "}"
            )
        )
    )
    public ResponseEntity<edu.cit.spot.dto.ApiResponse<StudentDto>> getStudentById(@PathVariable Long id) {
        try {
            StudentDto studentDto = studentService.getStudentById(id);
            return ResponseEntity.ok(new edu.cit.spot.dto.ApiResponse<>("SUCCESS", "Student retrieved successfully", studentDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/email/{email}")
    @Operation(summary = "Get student by email", description = "Get a student by their email")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER') or @securityService.isCurrentUserEmail(#email)")
    public ResponseEntity<edu.cit.spot.dto.ApiResponse<StudentDto>> getStudentByEmail(@PathVariable String email) {
        try {
            StudentDto studentDto = studentService.getStudentByEmail(email);
            return ResponseEntity.ok(new edu.cit.spot.dto.ApiResponse<>("SUCCESS", "Student retrieved successfully", studentDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update student", description = "Update a student's details")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id)")
    public ResponseEntity<edu.cit.spot.dto.ApiResponse<StudentDto>> updateStudent(
            @PathVariable Long id, 
            @Valid @RequestBody StudentUpdateRequest request) {
        try {
            StudentDto studentDto = studentService.updateStudent(id, request);
            return ResponseEntity.ok(new edu.cit.spot.dto.ApiResponse<>("SUCCESS", "Student updated successfully", studentDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @PostMapping("/{id}/bind-oauth")
    @Operation(summary = "Bind Google account to student", description = "Bind a Google account to a student account")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id)")
    public ResponseEntity<edu.cit.spot.dto.ApiResponse<StudentDto>> bindGoogleAccount(
            @PathVariable Long id, 
            @RequestParam String googleId) {
        try {
            StudentDto studentDto = studentService.bindGoogleAccount(id, googleId);
            return ResponseEntity.ok(new edu.cit.spot.dto.ApiResponse<>("SUCCESS", "Google account bound successfully", studentDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
