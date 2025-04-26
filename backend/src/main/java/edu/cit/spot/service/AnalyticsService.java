package edu.cit.spot.service;

import edu.cit.spot.dto.analytics.AttendanceAnalyticsDto;
import edu.cit.spot.dto.analytics.StudentAttendanceDto;

import java.time.LocalDate;
import java.util.List;

public interface AnalyticsService {
    
    AttendanceAnalyticsDto getSectionAnalytics(Long sectionId);
    
    AttendanceAnalyticsDto getSectionAnalyticsForDateRange(Long sectionId, LocalDate startDate, LocalDate endDate);
    
    List<StudentAttendanceDto> getStudentAttendanceStats(Long sectionId);
    
    StudentAttendanceDto getStudentAttendanceStatsInSection(Long sectionId, Long studentId);
}
