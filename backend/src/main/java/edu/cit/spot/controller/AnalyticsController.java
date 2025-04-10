package edu.cit.spot.controller;

import edu.cit.spot.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analytics", description = "Operations for attendance analytics and reporting")
public class AnalyticsController {

    private final AttendanceService attendanceService;

    @Operation(summary = "Get analytics for a class", description = "Returns attendance statistics for a specific class")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved analytics"),
        @ApiResponse(responseCode = "404", description = "Class not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/{classId}")
    public ResponseEntity<Map<String, Object>> getClassAnalytics(@PathVariable Long classId) {
        Map<String, Object> stats = attendanceService.getAttendanceStatsByCourse(classId);
        log.info("Retrieved analytics for class {}", classId);
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get global analytics", description = "Returns attendance statistics across all classes (admin view)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved global analytics"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized to access this resource")
    })
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getGlobalAnalytics() {
        // In a real implementation, you would aggregate data from multiple classes
        // For now, we'll return a simple placeholder
        Map<String, Object> globalStats = new HashMap<>();
        globalStats.put("totalClasses", 0);
        globalStats.put("totalStudents", 0);
        globalStats.put("totalSessions", 0);
        globalStats.put("averageAttendanceRate", 0.0);
        
        log.info("Retrieved global analytics");
        return ResponseEntity.ok(globalStats);
    }

    @Operation(summary = "Get student analytics", description = "Returns attendance statistics for a specific student in a class")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved student analytics"),
        @ApiResponse(responseCode = "404", description = "Class or student not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/{classId}/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getStudentAnalytics(
            @PathVariable Long classId, 
            @PathVariable Long studentId) {
        
        Map<String, Object> studentStats = attendanceService.getAttendanceStatsByStudent(studentId);
        log.info("Retrieved analytics for student {} in class {}", studentId, classId);
        return ResponseEntity.ok(studentStats);
    }

    @Operation(summary = "Export attendance report", description = "Generates an exportable attendance report for a class")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully generated report"),
        @ApiResponse(responseCode = "404", description = "Class not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/{classId}/export")
    public ResponseEntity<Map<String, Object>> exportAttendanceReport(@PathVariable Long classId) {
        // In a real implementation, this would generate a CSV or PDF file
        // For now, we'll return a simple placeholder response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Export functionality will be implemented in future versions");
        
        log.info("Received request to export attendance report for class {}", classId);
        return ResponseEntity.ok(response);
    }
}
