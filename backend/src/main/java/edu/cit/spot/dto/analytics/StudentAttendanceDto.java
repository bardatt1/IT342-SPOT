package edu.cit.spot.dto.analytics;

import java.util.Map;
import java.time.LocalDate;

public record StudentAttendanceDto(
    Long studentId,
    String studentName,
    int totalClassDays,
    int daysPresent,
    double attendanceRate,
    Map<LocalDate, Boolean> attendanceByDate
) {
    public StudentAttendanceDto {
        if (studentId == null || studentId <= 0) {
            throw new IllegalArgumentException("Invalid student ID");
        }
        if (studentName == null || studentName.isBlank()) {
            throw new IllegalArgumentException("Student name cannot be blank");
        }
        if (totalClassDays < 0) {
            throw new IllegalArgumentException("Total class days cannot be negative");
        }
        if (daysPresent < 0) {
            throw new IllegalArgumentException("Days present cannot be negative");
        }
        if (daysPresent > totalClassDays) {
            throw new IllegalArgumentException("Days present cannot be greater than total class days");
        }
        if (attendanceRate < 0 || attendanceRate > 100) {
            throw new IllegalArgumentException("Attendance rate must be between 0 and 100");
        }
        if (attendanceByDate == null) {
            throw new IllegalArgumentException("Attendance by date cannot be null");
        }
    }
}
