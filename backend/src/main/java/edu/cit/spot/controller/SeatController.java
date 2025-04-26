package edu.cit.spot.controller;

import edu.cit.spot.dto.ApiResponse;
import edu.cit.spot.dto.seat.OverrideSeatRequest;
import edu.cit.spot.dto.seat.PickSeatRequest;
import edu.cit.spot.dto.seat.SeatDto;
import edu.cit.spot.exception.GlobalExceptionHandler;
import edu.cit.spot.service.SeatService;
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
@RequestMapping("/api/seats")
@Tag(name = "Seats", description = "APIs for seat management")
@SecurityRequirement(name = "bearerAuth")
public class SeatController {

    @Autowired
    private SeatService seatService;
    
    @GetMapping
    @Operation(summary = "Get seats by section ID", description = "Get all seats for a specific section. Only accessible by teachers and students linked to the section.")
    @PreAuthorize("hasRole('TEACHER') or (hasRole('STUDENT') and @sectionService.isStudentEnrolledInSection(authentication.principal.username, #sectionId))")
    public ResponseEntity<ApiResponse<List<SeatDto>>> getSeatsBySectionId(@RequestParam Long sectionId) {
        try {
            List<SeatDto> seats = seatService.getSeatsBySectionId(sectionId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Seats retrieved successfully", seats));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/student")
    @Operation(summary = "Get seat by student and section", description = "Get a seat for a specific student in a section")
    public ResponseEntity<ApiResponse<SeatDto>> getSeatByStudentAndSectionId(
            @RequestParam Long studentId, 
            @RequestParam Long sectionId) {
        try {
            SeatDto seatDto = seatService.getSeatByStudentAndSectionId(studentId, sectionId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Seat retrieved successfully", seatDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping("/pick")
    @Operation(summary = "Pick seat", description = "Student picks a seat in a section")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<SeatDto>> pickSeat(@Valid @RequestBody PickSeatRequest request) {
        try {
            SeatDto seatDto = seatService.pickSeat(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("SUCCESS", "Seat picked successfully", seatDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @PostMapping("/override")
    @Operation(summary = "Override seat", description = "Teacher overrides a student's seat")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<SeatDto>> overrideSeat(@Valid @RequestBody OverrideSeatRequest request) {
        try {
            SeatDto seatDto = seatService.overrideSeat(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("SUCCESS", "Seat overridden successfully", seatDto));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete seat", description = "Delete a seat")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Boolean>> deleteSeat(@PathVariable Long id) {
        try {
            boolean result = seatService.deleteSeat(id);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Seat deleted successfully", result));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/section/{sectionId}")
    @Operation(summary = "Delete seats by section", description = "Delete all seats in a section")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Boolean>> deleteSeatsBySection(@PathVariable Long sectionId) {
        try {
            boolean result = seatService.deleteSeatsBySection(sectionId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "All seats in section deleted successfully", result));
        } catch (Exception e) {
            return GlobalExceptionHandler.errorResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
