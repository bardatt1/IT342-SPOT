package edu.cit.spot.controller;

import edu.cit.spot.entity.Course;
import edu.cit.spot.entity.Session;
import edu.cit.spot.entity.User;
import edu.cit.spot.service.CourseService;
import edu.cit.spot.service.SessionService;
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

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sessions", description = "Operations for managing class sessions")
public class SessionController {

    private final SessionService sessionService;
    private final CourseService courseService;
    private final UserService userService;

    @Operation(summary = "Get all sessions for a course", description = "Returns all sessions for a specific course")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved sessions"),
        @ApiResponse(responseCode = "404", description = "Course not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Session>> getSessionsByCourse(@PathVariable Long courseId) {
        List<Session> sessions = sessionService.getSessionsByCourse(courseId);
        log.info("Retrieved {} sessions for course {}", sessions.size(), courseId);
        return ResponseEntity.ok(sessions);
    }

    @Operation(summary = "Get session by ID", description = "Returns a specific session by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved session"),
        @ApiResponse(responseCode = "404", description = "Session not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Session> getSessionById(@PathVariable Long id) {
        Session session = sessionService.getSessionById(id);
        log.info("Retrieved session with ID: {}", id);
        return ResponseEntity.ok(session);
    }

    @Operation(summary = "Create new session", description = "Creates a new session for a course")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully created session"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Course not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized to modify this course")
    })
    @PostMapping("/course/{courseId}")
    public ResponseEntity<Session> createSession(@PathVariable Long courseId, @RequestBody Session session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        
        // Verify ownership
        Course course = courseService.getCourseById(courseId);
        if (!course.getTeacher().getId().equals(user.getId())) {
            log.warn("User {} attempted to create session for course owned by {}", 
                    user.getEmail(), course.getTeacher().getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        session.setCourse(course);
        Session createdSession = sessionService.createSession(session);
        log.info("Created new session with ID: {} for course {}", createdSession.getId(), courseId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSession);
    }

    @Operation(summary = "Update session", description = "Updates an existing session")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated session"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Session not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized to modify this session")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Session> updateSession(@PathVariable Long id, @RequestBody Session session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        
        // Verify ownership
        Session existingSession = sessionService.getSessionById(id);
        if (!existingSession.getCourse().getTeacher().getId().equals(user.getId())) {
            log.warn("User {} attempted to update session for course owned by {}", 
                    user.getEmail(), existingSession.getCourse().getTeacher().getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        session.setId(id);
        Session updatedSession = sessionService.updateSession(session);
        log.info("Updated session with ID: {}", id);
        
        return ResponseEntity.ok(updatedSession);
    }

    @Operation(summary = "Delete session", description = "Deletes an existing session")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted session"),
        @ApiResponse(responseCode = "404", description = "Session not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized to delete this session")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        
        // Verify ownership
        Session session = sessionService.getSessionById(id);
        if (!session.getCourse().getTeacher().getId().equals(user.getId())) {
            log.warn("User {} attempted to delete session for course owned by {}", 
                    user.getEmail(), session.getCourse().getTeacher().getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        sessionService.deleteSession(id);
        log.info("Deleted session with ID: {}", id);
        
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Start session", description = "Marks a session as active and sets the start time")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully started session"),
        @ApiResponse(responseCode = "400", description = "Invalid operation (e.g., session already started)"),
        @ApiResponse(responseCode = "404", description = "Session not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized to modify this session")
    })
    @PostMapping("/{id}/start")
    public ResponseEntity<Session> startSession(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        
        // Verify ownership
        Session session = sessionService.getSessionById(id);
        if (!session.getCourse().getTeacher().getId().equals(user.getId())) {
            log.warn("User {} attempted to start session for course owned by {}", 
                    user.getEmail(), session.getCourse().getTeacher().getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        if (session.getStartTime() != null && session.isActive()) {
            log.warn("Attempted to start already active session: {}", id);
            return ResponseEntity.badRequest().build();
        }
        
        session.setStartTime(LocalDateTime.now());
        session.setActive(true);
        Session updatedSession = sessionService.updateSession(session);
        log.info("Started session with ID: {}", id);
        
        return ResponseEntity.ok(updatedSession);
    }

    @Operation(summary = "End session", description = "Marks a session as inactive and sets the end time")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully ended session"),
        @ApiResponse(responseCode = "400", description = "Invalid operation (e.g., session not started)"),
        @ApiResponse(responseCode = "404", description = "Session not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized to modify this session")
    })
    @PostMapping("/{id}/end")
    public ResponseEntity<Session> endSession(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        
        // Verify ownership
        Session session = sessionService.getSessionById(id);
        if (!session.getCourse().getTeacher().getId().equals(user.getId())) {
            log.warn("User {} attempted to end session for course owned by {}", 
                    user.getEmail(), session.getCourse().getTeacher().getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        if (session.getStartTime() == null || !session.isActive()) {
            log.warn("Attempted to end inactive session: {}", id);
            return ResponseEntity.badRequest().build();
        }
        
        session.setEndTime(LocalDateTime.now());
        session.setActive(false);
        Session updatedSession = sessionService.updateSession(session);
        log.info("Ended session with ID: {}", id);
        
        return ResponseEntity.ok(updatedSession);
    }

    @Operation(summary = "Get active sessions", description = "Returns all active sessions for a course")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved active sessions"),
        @ApiResponse(responseCode = "404", description = "Course not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/course/{courseId}/active")
    public ResponseEntity<List<Session>> getActiveSessions(@PathVariable Long courseId) {
        List<Session> activeSessions = sessionService.getActiveSessions(courseId);
        log.info("Retrieved {} active sessions for course {}", activeSessions.size(), courseId);
        return ResponseEntity.ok(activeSessions);
    }
}
