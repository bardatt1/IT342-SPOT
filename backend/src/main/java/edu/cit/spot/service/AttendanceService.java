package edu.cit.spot.service;

import edu.cit.spot.entity.Attendance;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AttendanceService {
    List<Attendance> getAllAttendance();
    Attendance getAttendanceById(Long id);
    List<Attendance> getAttendanceBySessionId(Long sessionId);
    List<Attendance> getAttendanceByCourseId(Long courseId);
    List<Attendance> getAttendanceByStudentId(Long studentId);
    Attendance recordAttendance(Long sessionId, Long studentId, Attendance.AttendanceStatus status);
    Attendance updateAttendance(Long id, Attendance.AttendanceStatus status);
    void deleteAttendance(Long id);
    Map<String, Object> getAttendanceStatsByCourse(Long courseId);
    Map<String, Object> getAttendanceStatsByStudent(Long studentId);
    void markBulkAttendance(Long sessionId, List<Long> studentIds, Attendance.AttendanceStatus status);
}
