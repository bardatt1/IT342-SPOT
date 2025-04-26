package edu.cit.spot.controller;

import edu.cit.spot.dto.ApiResponse;
import edu.cit.spot.dto.schedule.CreateScheduleRequest;
import edu.cit.spot.dto.schedule.ScheduleDto;
import edu.cit.spot.dto.schedule.ScheduleUpdateRequest;
import edu.cit.spot.exception.GlobalExceptionHandler;
import edu.cit.spot.service.ScheduleService;
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
@RequestMapping("/api/schedules")
@Tag(name = "Schedules", description = "APIs for schedule management")
@SecurityRequirement(name = "bearerAuth")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;
    
    @GetMapping
    @Operation(summary = "Get schedules by section ID", description = "Get schedules for a specific section")
    public ResponseEntity<ApiResponse<List<ScheduleDto>>> getSchedulesBySectionId(@RequestParam Long sectionId) {
        try {
            List<ScheduleDto> schedules = scheduleService.getSchedulesBySectionId(sectionId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Schedules retrieved successfully", schedules));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get schedule by ID", description = "Get a schedule by its ID")
    public ResponseEntity<ApiResponse<ScheduleDto>> getScheduleById(@PathVariable Long id) {
        try {
            ScheduleDto scheduleDto = scheduleService.getScheduleById(id);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Schedule retrieved successfully", scheduleDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping
    @Operation(summary = "Create schedule", description = "Create a new schedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<ScheduleDto>> createSchedule(@Valid @RequestBody CreateScheduleRequest request) {
        try {
            ScheduleDto scheduleDto = scheduleService.createSchedule(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("SUCCESS", "Schedule created successfully", scheduleDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update schedule", description = "Update an existing schedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<ScheduleDto>> updateSchedule(
            @PathVariable Long id, 
            @Valid @RequestBody ScheduleUpdateRequest request) {
        try {
            ScheduleDto scheduleDto = scheduleService.updateSchedule(id, request);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Schedule updated successfully", scheduleDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete schedule", description = "Delete a schedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Boolean>> deleteSchedule(@PathVariable Long id) {
        try {
            boolean result = scheduleService.deleteSchedule(id);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Schedule deleted successfully", result));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
