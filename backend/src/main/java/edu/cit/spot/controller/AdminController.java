package edu.cit.spot.controller;

import edu.cit.spot.dto.ApiResponse;
import edu.cit.spot.dto.admin.AdminDto;
import edu.cit.spot.dto.student.CreateStudentRequest;
import edu.cit.spot.dto.student.StudentDto;
import edu.cit.spot.dto.teacher.CreateTeacherRequest;
import edu.cit.spot.dto.teacher.TeacherDto;
import edu.cit.spot.exception.GlobalExceptionHandler;
import edu.cit.spot.service.AdminService;
import edu.cit.spot.service.StudentService;
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

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "APIs for admin operations")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private TeacherService teacherService;
    
    @PostMapping("/create-student")
    @Operation(summary = "Create student", description = "Create a new student account")
    public ResponseEntity<ApiResponse<StudentDto>> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        try {
            StudentDto studentDto = studentService.createStudent(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("SUCCESS", "Student created successfully", studentDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/students")
    @Operation(summary = "Get all students", description = "Get a list of all students")
    public ResponseEntity<ApiResponse<List<StudentDto>>> getAllStudents() {
        try {
            List<StudentDto> students = studentService.getAllStudents();
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Students retrieved successfully", students));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/students/{id}")
    @Operation(summary = "Get student by ID", description = "Get a student by their ID")
    public ResponseEntity<ApiResponse<StudentDto>> getStudentById(@PathVariable Long id) {
        try {
            StudentDto studentDto = studentService.getStudentById(id);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Student retrieved successfully", studentDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/students/{id}")
    @Operation(summary = "Delete student", description = "Delete a student by their ID")
    public ResponseEntity<ApiResponse<Boolean>> deleteStudent(@PathVariable Long id) {
        try {
            boolean result = studentService.deleteStudent(id);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Student deleted successfully", result));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping("/create-teacher")
    @Operation(summary = "Create teacher", description = "Create a new teacher account")
    public ResponseEntity<ApiResponse<TeacherDto>> createTeacher(@Valid @RequestBody CreateTeacherRequest request) {
        try {
            TeacherDto teacherDto = teacherService.createTeacher(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("SUCCESS", "Teacher created successfully", teacherDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/teachers")
    @Operation(summary = "Get all teachers", description = "Get a list of all teachers")
    public ResponseEntity<ApiResponse<List<TeacherDto>>> getAllTeachers() {
        try {
            List<TeacherDto> teachers = teacherService.getAllTeachers();
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Teachers retrieved successfully", teachers));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/teachers/{id}")
    @Operation(summary = "Get teacher by ID", description = "Get a teacher by their ID")
    public ResponseEntity<ApiResponse<TeacherDto>> getTeacherById(@PathVariable Long id) {
        try {
            TeacherDto teacherDto = teacherService.getTeacherById(id);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Teacher retrieved successfully", teacherDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/teachers/{id}")
    @Operation(summary = "Delete teacher", description = "Delete a teacher by their ID")
    public ResponseEntity<ApiResponse<Boolean>> deleteTeacher(@PathVariable Long id) {
        try {
            boolean result = teacherService.deleteTeacher(id);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Teacher deleted successfully", result));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
