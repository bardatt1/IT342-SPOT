package edu.cit.spot.controller;

import edu.cit.spot.entity.Course;
import edu.cit.spot.entity.Notification;
import edu.cit.spot.entity.Session;
import edu.cit.spot.entity.User;
import edu.cit.spot.service.AttendanceService;
import edu.cit.spot.service.CourseService;
import edu.cit.spot.service.NotificationService;
import edu.cit.spot.service.SessionService;
import edu.cit.spot.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard", description = "Dashboard operations for home page")
public class DashboardController {

    private final CourseService courseService;
    private final NotificationService notificationService;
    private final UserService userService;
    private final SessionService sessionService;
    private final AttendanceService attendanceService;

    @Operation(summary = "Get current user's classes", description = "Returns list of classes for the authenticated teacher")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved classes"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized for this resource")
    })
    @GetMapping("/classes")
    public ResponseEntity<List<Course>> getClasses() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        
        List<Course> courses = courseService.getCoursesByTeacher(user);
        log.info("Retrieved {} courses for teacher {}", courses.size(), user.getEmail());
        
        return ResponseEntity.ok(courses);
    }

    @Operation(summary = "Get current user's notifications", description = "Returns list of notifications for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized for this resource")
    })
    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        
        List<Notification> notifications = notificationService.getNotificationsByUserId(user.getId());
        log.info("Retrieved {} notifications for user {}", notifications.size(), user.getEmail());
        
        return ResponseEntity.ok(notifications);
    }
    
    @Operation(summary = "Get dashboard summary", description = "Returns summary data for the dashboard")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dashboard summary"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        
        Map<String, Object> summary = new HashMap<>();
        
        // Get basic statistics for the user
        List<Course> courses = courseService.getCoursesByTeacher(user);
        int totalStudents = 0;
        for (Course course : courses) {
            totalStudents += course.getStudents().size();
        }
        
        // Count active sessions
        int activeSessionsCount = 0;
        for (Course course : courses) {
            List<Session> activeSessions = sessionService.getActiveSessions(course.getId());
            activeSessionsCount += activeSessions.size();
        }
        
        // Get unread notification count
        int unreadNotifications = notificationService.getUnreadNotificationCount(user.getId());
        
        // Populate summary data
        summary.put("totalCourses", courses.size());
        summary.put("totalStudents", totalStudents);
        summary.put("activeSessionsCount", activeSessionsCount);
        summary.put("unreadNotifications", unreadNotifications);
        
        log.info("Retrieved dashboard summary for user {}", user.getEmail());
        return ResponseEntity.ok(summary);
    }
    
    @Operation(summary = "Get upcoming sessions", description = "Returns upcoming scheduled sessions for the authenticated teacher")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved upcoming sessions"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized for this resource")
    })
    @GetMapping("/upcoming-sessions")
    public ResponseEntity<List<Session>> getUpcomingSessions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        
        LocalDateTime now = LocalDateTime.now();
        
        // Get upcoming sessions across all courses taught by this teacher
        List<Session> upcomingSessions = sessionService.getUpcomingSessionsByTeacher(user.getId(), now);
        log.info("Retrieved {} upcoming sessions for teacher {}", upcomingSessions.size(), user.getEmail());
        
        return ResponseEntity.ok(upcomingSessions);
    }
    
    @Operation(summary = "Get course statistics", description = "Returns attendance statistics for a specific course")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved course statistics"),
        @ApiResponse(responseCode = "404", description = "Course not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized for this resource")
    })
    @GetMapping("/course/{courseId}/stats")
    public ResponseEntity<Map<String, Object>> getCourseStatistics(@PathVariable Long courseId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        
        // Verify ownership
        Course course = courseService.getCourseById(courseId);
        if (!course.getTeacher().getId().equals(user.getId())) {
            log.warn("User {} attempted to access stats for course owned by {}", 
                    user.getEmail(), course.getTeacher().getEmail());
            return ResponseEntity.status(403).build();
        }
        
        Map<String, Object> stats = attendanceService.getAttendanceStatsByCourse(courseId);
        log.info("Retrieved statistics for course {}", courseId);
        
        return ResponseEntity.ok(stats);
    }
    
    @Operation(summary = "Get student dashboard data", description = "Returns dashboard data for student view")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved student dashboard data"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/student")
    public ResponseEntity<Map<String, Object>> getStudentDashboard() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User student = userService.findByEmail(auth.getName());
        
        Map<String, Object> dashboard = new HashMap<>();
        
        // Get courses the student is enrolled in
        List<Course> enrolledCourses = courseService.getCoursesByStudent(student);
        
        // Get active sessions the student can attend
        List<Session> activeSessions = sessionService.getActiveSessionsForStudent(student.getId());
        
        // Get recent attendance records
        Map<String, Object> attendanceStats = attendanceService.getAttendanceStatsByStudent(student.getId());
        
        // Get unread notifications
        int unreadNotifications = notificationService.getUnreadNotificationCount(student.getId());
        
        // Populate dashboard data
        dashboard.put("enrolledCourses", enrolledCourses);
        dashboard.put("activeSessions", activeSessions);
        dashboard.put("attendanceStats", attendanceStats);
        dashboard.put("unreadNotifications", unreadNotifications);
        
        log.info("Retrieved dashboard data for student {}", student.getEmail());
        return ResponseEntity.ok(dashboard);
    }
}
