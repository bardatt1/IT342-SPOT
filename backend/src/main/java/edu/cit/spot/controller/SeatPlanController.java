package edu.cit.spot.controller;

import edu.cit.spot.entity.SeatAssignment;
import edu.cit.spot.entity.SeatPlan;
import edu.cit.spot.service.SeatPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Seat Plans", description = "Operations for managing classroom seat plans")
public class SeatPlanController {

    private final SeatPlanService seatPlanService;

    @Operation(summary = "Get seat plan and assignments for a class", description = "Returns the seat plan layout and student assignments for a specific class")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved seat plan"),
        @ApiResponse(responseCode = "404", description = "Class or seat plan not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/{classId}")
    public ResponseEntity<Map<String, Object>> getSeatPlan(@PathVariable Long classId) {
        List<SeatPlan> seatPlans = seatPlanService.getSeatPlansByCourseId(classId);
        
        // If no seat plan exists, return empty
        if (seatPlans.isEmpty()) {
            log.info("No seat plans found for class with ID: {}", classId);
            return ResponseEntity.noContent().build();
        }
        
        // Get the active seat plan, or the first one if none is active
        SeatPlan activePlan = seatPlans.stream()
                .filter(SeatPlan::isActive)
                .findFirst()
                .orElse(seatPlans.get(0));
        
        Map<String, Object> result = seatPlanService.getSeatPlanWithAssignments(activePlan.getId());
        log.info("Retrieved seat plan {} for class {}", activePlan.getId(), classId);
        
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Create a new seat plan", description = "Creates a new seat plan layout for a class")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully created seat plan"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Class not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/{classId}")
    public ResponseEntity<SeatPlan> createSeatPlan(@PathVariable Long classId, @RequestBody SeatPlan seatPlan) {
        SeatPlan createdSeatPlan = seatPlanService.createSeatPlan(classId, seatPlan);
        log.info("Created new seat plan with ID: {} for class {}", createdSeatPlan.getId(), classId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSeatPlan);
    }

    @Operation(summary = "Update seat plan", description = "Updates an existing seat plan layout")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated seat plan"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Seat plan not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PutMapping("/{classId}")
    public ResponseEntity<SeatPlan> updateSeatPlan(@PathVariable Long classId, @RequestBody SeatPlan seatPlan) {
        // Ensure the seat plan exists for this class
        List<SeatPlan> existingPlans = seatPlanService.getSeatPlansByCourseId(classId);
        SeatPlan targetPlan = existingPlans.stream()
                .filter(plan -> plan.getId().equals(seatPlan.getId()))
                .findFirst()
                .orElse(null);
                
        if (targetPlan == null) {
            log.warn("Attempted to update non-existent seat plan for class {}", classId);
            return ResponseEntity.notFound().build();
        }
        
        SeatPlan updatedPlan = seatPlanService.updateSeatPlan(targetPlan.getId(), seatPlan);
        log.info("Updated seat plan with ID: {}", updatedPlan.getId());
        
        return ResponseEntity.ok(updatedPlan);
    }

    @Operation(summary = "Delete seat plan", description = "Deletes a seat plan layout and all its assignments")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted seat plan"),
        @ApiResponse(responseCode = "404", description = "Seat plan not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @DeleteMapping("/{classId}")
    public ResponseEntity<Void> deleteSeatPlan(@PathVariable Long classId, @RequestParam Long planId) {
        // Check if plan belongs to this class
        List<SeatPlan> existingPlans = seatPlanService.getSeatPlansByCourseId(classId);
        boolean belongsToClass = existingPlans.stream()
                .anyMatch(plan -> plan.getId().equals(planId));
                
        if (!belongsToClass) {
            log.warn("Attempted to delete seat plan {} that doesn't belong to class {}", planId, classId);
            return ResponseEntity.notFound().build();
        }
        
        seatPlanService.deleteSeatPlan(planId);
        log.info("Deleted seat plan with ID: {}", planId);
        
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Assign seat to student", description = "Assigns a student to a specific seat in the seat plan")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully assigned seat"),
        @ApiResponse(responseCode = "400", description = "Invalid input or seat already occupied"),
        @ApiResponse(responseCode = "404", description = "Seat plan or student not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/{seatPlanId}/assign")
    public ResponseEntity<SeatAssignment> assignSeat(
            @PathVariable Long seatPlanId,
            @RequestParam Long studentId,
            @RequestParam Integer row,
            @RequestParam Integer column) {
        
        SeatAssignment assignment = seatPlanService.assignSeat(seatPlanId, studentId, row, column);
        log.info("Assigned student {} to seat at position ({},{}) in plan {}", 
                studentId, row, column, seatPlanId);
        
        return ResponseEntity.ok(assignment);
    }

    @Operation(summary = "Remove student from seat", description = "Removes a student's seat assignment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully removed assignment"),
        @ApiResponse(responseCode = "404", description = "Seat plan or assignment not found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @DeleteMapping("/{seatPlanId}/assign")
    public ResponseEntity<Void> removeSeatAssignment(
            @PathVariable Long seatPlanId,
            @RequestParam Long studentId) {
        
        seatPlanService.removeSeatAssignment(seatPlanId, studentId);
        log.info("Removed seat assignment for student {} in plan {}", studentId, seatPlanId);
        
        return ResponseEntity.noContent().build();
    }
}
