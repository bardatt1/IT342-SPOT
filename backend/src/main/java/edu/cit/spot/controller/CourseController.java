package edu.cit.spot.controller;

import edu.cit.spot.dto.ApiResponse;
import edu.cit.spot.dto.course.CourseDto;
import edu.cit.spot.dto.course.CourseUpdateRequest;
import edu.cit.spot.dto.course.CreateCourseRequest;
import edu.cit.spot.exception.GlobalExceptionHandler;
import edu.cit.spot.service.CourseService;
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
@RequestMapping("/api/courses")
@Tag(name = "Courses", description = "APIs for course management")
@SecurityRequirement(name = "bearerAuth")
public class CourseController {

    @Autowired
    private CourseService courseService;
    
    @GetMapping
    @Operation(summary = "Get all courses", description = "Get a list of all courses")
    public ResponseEntity<ApiResponse<List<CourseDto>>> getAllCourses() {
        try {
            List<CourseDto> courses = courseService.getAllCourses();
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Courses retrieved successfully", courses));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get course by ID", description = "Get a course by its ID")
    public ResponseEntity<ApiResponse<CourseDto>> getCourseById(@PathVariable Long id) {
        try {
            CourseDto courseDto = courseService.getCourseById(id);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Course retrieved successfully", courseDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/code/{courseCode}")
    @Operation(summary = "Get course by code", description = "Get a course by its course code")
    public ResponseEntity<ApiResponse<CourseDto>> getCourseByCourseCode(@PathVariable String courseCode) {
        try {
            CourseDto courseDto = courseService.getCourseByCourseCode(courseCode);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Course retrieved successfully", courseDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping
    @Operation(summary = "Create course", description = "Create a new course")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CourseDto>> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        try {
            CourseDto courseDto = courseService.createCourse(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("SUCCESS", "Course created successfully", courseDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update course", description = "Update an existing course")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CourseDto>> updateCourse(
            @PathVariable Long id, 
            @Valid @RequestBody CourseUpdateRequest request) {
        try {
            CourseDto courseDto = courseService.updateCourse(id, request);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Course updated successfully", courseDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete course", description = "Delete a course")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> deleteCourse(@PathVariable Long id) {
        try {
            boolean result = courseService.deleteCourse(id);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Course deleted successfully", result));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
