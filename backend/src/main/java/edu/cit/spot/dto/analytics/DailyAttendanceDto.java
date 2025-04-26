package edu.cit.spot.dto.analytics;

import java.time.LocalDate;

public record DailyAttendanceDto(
    LocalDate date,
    int presentCount,
    int totalStudents,
    double attendanceRate
) {
    public DailyAttendanceDto {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (presentCount < 0) {
            throw new IllegalArgumentException("Present count cannot be negative");
        }
        if (totalStudents < 0) {
            throw new IllegalArgumentException("Total students cannot be negative");
        }
        if (totalStudents < presentCount) {
            throw new IllegalArgumentException("Total students cannot be less than present count");
        }
        if (attendanceRate < 0 || attendanceRate > 100) {
            throw new IllegalArgumentException("Attendance rate must be between 0 and 100");
        }
    }
}
