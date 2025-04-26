package edu.cit.spot.dto.schedule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record CreateScheduleRequest(
    @NotNull(message = "Section ID cannot be null")
    Long sectionId,
    
    @NotNull(message = "Day of week cannot be null")
    @Min(value = 1, message = "Day of week must be between 1 (Monday) and 7 (Sunday)")
    @Max(value = 7, message = "Day of week must be between 1 (Monday) and 7 (Sunday)")
    Integer dayOfWeek,
    
    @NotNull(message = "Start time cannot be null")
    LocalTime timeStart,
    
    @NotNull(message = "End time cannot be null")
    LocalTime timeEnd,
    
    @NotBlank(message = "Schedule type cannot be blank")
    @Size(max = 10, message = "Schedule type must be less than 10 characters")
    String scheduleType,
    
    @NotBlank(message = "Room cannot be blank")
    @Size(max = 50, message = "Room must be less than 50 characters")
    String room
) {
    public CreateScheduleRequest {
        if (sectionId == null || sectionId <= 0) {
            throw new IllegalArgumentException("Invalid section ID");
        }
        if (dayOfWeek == null || dayOfWeek < 1 || dayOfWeek > 7) {
            throw new IllegalArgumentException("Day of week must be between 1 (Monday) and 7 (Sunday)");
        }
        if (timeStart == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }
        if (timeEnd == null) {
            throw new IllegalArgumentException("End time cannot be null");
        }
        if (timeStart.isAfter(timeEnd)) {
            throw new IllegalArgumentException("Start time cannot be after end time");
        }
        if (scheduleType != null) {
            scheduleType = scheduleType.trim().toUpperCase();
        }
        if (room == null || room.trim().isEmpty()) {
            throw new IllegalArgumentException("Room cannot be blank");
        }
    }
}
