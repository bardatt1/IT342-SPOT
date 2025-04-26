package edu.cit.spot.dto.attendance;

import edu.cit.spot.dto.section.SectionDto;
import edu.cit.spot.dto.student.StudentDto;
import edu.cit.spot.entity.Attendance;

import java.time.LocalDate;
import java.time.LocalTime;

public record AttendanceDto(
    Long id,
    StudentDto student,
    SectionDto section,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime
) {
    public AttendanceDto {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null");
        }
        if (section == null) {
            throw new IllegalArgumentException("Section cannot be null");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }
    }
    
    public static AttendanceDto fromEntity(Attendance attendance) {
        return new AttendanceDto(
            attendance.getId(),
            StudentDto.fromEntity(attendance.getStudent()),
            SectionDto.fromEntity(attendance.getSection()),
            attendance.getDate(),
            attendance.getStartTime(),
            attendance.getEndTime()
        );
    }
}
