package edu.cit.spot.controller;

import edu.cit.spot.entity.Course;
import edu.cit.spot.entity.User;
import edu.cit.spot.service.CourseService;
import edu.cit.spot.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Courses", description = "Operations for managing courses")
public class CourseController {

    private final CourseService courseService;
    private final UserService userService;

    @Operation(summary = "Get all courses", description = "Returns all courses that the authenticated user has access to")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved courses"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        log.info("Retrieved all courses, count: {}", courses.size());
        return ResponseEntity.ok(courses);
    }

    @Operation(summary = "Get course by ID", description = "Returns a specific course by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved course"),
        @ApiResponse(responseCode = "404", description = "Course not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        log.info("Retrieved course with ID: {}", id);
        return ResponseEntity.ok(course);
    }

    @Operation(summary = "Create new course", description = "Creates a new course with the authenticated teacher as owner")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully created course"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User teacher = userService.findByEmail(auth.getName());
        
        course.setTeacher(teacher);
        Course createdCourse = courseService.createCourse(course);
        
        log.info("Created new course with ID: {}, teacher: {}", createdCourse.getId(), teacher.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCourse);
    }

    @Operation(summary = "Update course", description = "Updates an existing course")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated course"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Course not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized to update this course")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @RequestBody Course course) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        
        // Verify ownership (only the teacher can update their own course)
        Course existingCourse = courseService.getCourseById(id);
        if (!existingCourse.getTeacher().getId().equals(user.getId())) {
            log.warn("User {} attempted to update course {} owned by {}", 
                    user.getEmail(), id, existingCourse.getTeacher().getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Course updatedCourse = courseService.updateCourse(id, course);
        log.info("Updated course with ID: {}", id);
        
        return ResponseEntity.ok(updatedCourse);
    }

    @Operation(summary = "Delete course", description = "Deletes an existing course")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted course"),
        @ApiResponse(responseCode = "404", description = "Course not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized to delete this course")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        
        // Verify ownership (only the teacher can delete their own course)
        Course existingCourse = courseService.getCourseById(id);
        if (!existingCourse.getTeacher().getId().equals(user.getId())) {
            log.warn("User {} attempted to delete course {} owned by {}", 
                    user.getEmail(), id, existingCourse.getTeacher().getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        courseService.deleteCourse(id);
        log.info("Deleted course with ID: {}", id);
        
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Enroll student in course", description = "Enrolls a student in a course")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully enrolled student"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Course or student not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized to modify this course")
    })
    @PostMapping("/{courseId}/enroll/{studentId}")
    public ResponseEntity<Void> enrollStudent(@PathVariable Long courseId, @PathVariable Long studentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        
        // Verify ownership (only the teacher can enroll students in their own course)
        Course existingCourse = courseService.getCourseById(courseId);
        if (!existingCourse.getTeacher().getId().equals(user.getId())) {
            log.warn("User {} attempted to enroll student in course {} owned by {}", 
                    user.getEmail(), courseId, existingCourse.getTeacher().getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        courseService.enrollStudent(courseId, studentId);
        log.info("Enrolled student {} in course {}", studentId, courseId);
        
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Unenroll student from course", description = "Removes a student from a course")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully unenrolled student"),
        @ApiResponse(responseCode = "404", description = "Course or student not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized to modify this course")
    })
    @DeleteMapping("/{courseId}/enroll/{studentId}")
    public ResponseEntity<Void> unenrollStudent(@PathVariable Long courseId, @PathVariable Long studentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        
        // Verify ownership (only the teacher can unenroll students from their own course)
        Course existingCourse = courseService.getCourseById(courseId);
        if (!existingCourse.getTeacher().getId().equals(user.getId())) {
            log.warn("User {} attempted to unenroll student from course {} owned by {}", 
                    user.getEmail(), courseId, existingCourse.getTeacher().getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        courseService.unenrollStudent(courseId, studentId);
        log.info("Unenrolled student {} from course {}", studentId, courseId);
        
        return ResponseEntity.noContent().build();
    }
}
