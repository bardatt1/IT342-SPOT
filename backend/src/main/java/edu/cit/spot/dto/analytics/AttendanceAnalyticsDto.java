package edu.cit.spot.dto.analytics;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record AttendanceAnalyticsDto(
    Long sectionId,
    String courseName,
    String sectionRoom,
    int totalEnrolledStudents,
    double averageAttendanceRate,
    int totalClassDays,
    Map<LocalDate, Integer> attendanceByDate,
    List<DailyAttendanceDto> dailyAttendance
) {
    public AttendanceAnalyticsDto {
        if (sectionId == null || sectionId <= 0) {
            throw new IllegalArgumentException("Invalid section ID");
        }
        if (courseName == null || courseName.isBlank()) {
            throw new IllegalArgumentException("Course name cannot be blank");
        }
        if (sectionRoom == null || sectionRoom.isBlank()) {
            throw new IllegalArgumentException("Section room cannot be blank");
        }
        if (totalEnrolledStudents < 0) {
            throw new IllegalArgumentException("Total enrolled students cannot be negative");
        }
        if (averageAttendanceRate < 0 || averageAttendanceRate > 100) {
            throw new IllegalArgumentException("Average attendance rate must be between 0 and 100");
        }
        if (totalClassDays < 0) {
            throw new IllegalArgumentException("Total class days cannot be negative");
        }
        if (attendanceByDate == null) {
            throw new IllegalArgumentException("Attendance by date cannot be null");
        }
        if (dailyAttendance == null) {
            throw new IllegalArgumentException("Daily attendance cannot be null");
        }
    }
}
