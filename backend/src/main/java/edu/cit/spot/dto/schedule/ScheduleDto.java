package edu.cit.spot.dto.schedule;

import edu.cit.spot.entity.Schedule;
import java.time.LocalTime;

public record ScheduleDto(
    Long id,
    Long sectionId,
    Integer dayOfWeek,
    LocalTime timeStart,
    LocalTime timeEnd,
    String scheduleType,
    String room
) {
    public ScheduleDto {
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
        if (scheduleType == null || scheduleType.isBlank()) {
            throw new IllegalArgumentException("Schedule type cannot be blank");
        }
        if (room == null || room.isBlank()) {
            throw new IllegalArgumentException("Room cannot be blank");
        }
    }
    
    public static ScheduleDto fromEntity(Schedule schedule) {
        return new ScheduleDto(
            schedule.getId(),
            schedule.getSection().getId(),
            schedule.getDayOfWeek(),
            schedule.getTimeStart(),
            schedule.getTimeEnd(),
            schedule.getScheduleType(),
            schedule.getRoom()
        );
    }
}
