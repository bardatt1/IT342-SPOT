package edu.cit.spot.dto.schedule;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record ScheduleUpdateRequest(
    @Min(value = 1, message = "Day of week must be between 1 (Monday) and 7 (Sunday)")
    @Max(value = 7, message = "Day of week must be between 1 (Monday) and 7 (Sunday)")
    Integer dayOfWeek,
    
    LocalTime timeStart,
    
    LocalTime timeEnd,
    
    @Size(max = 10, message = "Schedule type must be less than 10 characters")
    String scheduleType,
    
    @Size(max = 50, message = "Room must be less than 50 characters")
    String room
) {
    public ScheduleUpdateRequest {
        if (dayOfWeek != null && (dayOfWeek < 1 || dayOfWeek > 7)) {
            throw new IllegalArgumentException("Day of week must be between 1 (Monday) and 7 (Sunday)");
        }
        
        if (timeStart != null && timeEnd != null && timeStart.isAfter(timeEnd)) {
            throw new IllegalArgumentException("Start time cannot be after end time");
        }
        
        if (scheduleType != null) {
            scheduleType = scheduleType.trim().toUpperCase();
        }
        
        if (room != null && room.trim().isEmpty()) {
            room = null;
        }
    }
}
