package edu.cit.spot.controller;

import edu.cit.spot.dto.ApiResponse;
import edu.cit.spot.dto.admin.AdminDto;
import edu.cit.spot.dto.admin.CreateAdminRequest;
import edu.cit.spot.dto.admin.CreateSystemAdminRequest;
import edu.cit.spot.dto.student.StudentDto;
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
@RequestMapping("/api/system-admin")
@Tag(name = "System Admin", description = "APIs for system admin operations")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('SYSTEMADMIN')")
public class SystemAdminController {

    @Autowired
    private AdminService adminService;
    
    @Autowired
    private TeacherService teacherService;
    
    @Autowired
    private StudentService studentService;
    
    @PostMapping("/create-admin")
    @Operation(summary = "Create admin", description = "Create a new admin account")
    public ResponseEntity<ApiResponse<AdminDto>> createAdmin(@Valid @RequestBody CreateAdminRequest request) {
        try {
            AdminDto adminDto = adminService.createAdmin(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("SUCCESS", "Admin created successfully", adminDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @PostMapping("/create-system-admin")
    @Operation(summary = "Create system admin", description = "Create a new system admin account")
    public ResponseEntity<ApiResponse<AdminDto>> createSystemAdmin(@Valid @RequestBody CreateSystemAdminRequest request) {
        try {
            AdminDto adminDto = adminService.createSystemAdmin(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("SUCCESS", "System Admin created successfully", adminDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/admins")
    @Operation(summary = "Get all admins", description = "Get a list of all admins including system admins")
    public ResponseEntity<ApiResponse<List<AdminDto>>> getAllAdmins() {
        try {
            List<AdminDto> admins = adminService.getAllAdmins();
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Admins retrieved successfully", admins));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/system-admins")
    @Operation(summary = "Get all system admins", description = "Get a list of all system admins")
    public ResponseEntity<ApiResponse<List<AdminDto>>> getAllSystemAdmins() {
        try {
            List<AdminDto> systemAdmins = adminService.getAllSystemAdmins();
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "System Admins retrieved successfully", systemAdmins));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PutMapping("/promote/{id}")
    @Operation(summary = "Promote to system admin", description = "Promote an admin to system admin")
    public ResponseEntity<ApiResponse<AdminDto>> promoteToSystemAdmin(@PathVariable Long id) {
        try {
            AdminDto adminDto = adminService.promoteToSystemAdmin(id);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Admin promoted to System Admin successfully", adminDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @PutMapping("/demote/{id}")
    @Operation(summary = "Demote from system admin", description = "Demote a system admin to regular admin")
    public ResponseEntity<ApiResponse<AdminDto>> demoteFromSystemAdmin(@PathVariable Long id) {
        try {
            AdminDto adminDto = adminService.demoteFromSystemAdmin(id);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "System Admin demoted to Admin successfully", adminDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @DeleteMapping("/admin/{id}")
    @Operation(summary = "Delete admin", description = "Delete an admin by their ID")
    public ResponseEntity<ApiResponse<Boolean>> deleteAdmin(@PathVariable Long id) {
        try {
            boolean result = adminService.deleteAdmin(id);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Admin deleted successfully", result));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    // Teacher endpoints for System Admin
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
            TeacherDto teacher = teacherService.getTeacherById(id);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Teacher retrieved successfully", teacher));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    // Student endpoints for System Admin
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
            StudentDto student = studentService.getStudentById(id);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Student retrieved successfully", student));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
