package edu.cit.spot.service.impl;

import edu.cit.spot.entity.Attendance;
import edu.cit.spot.entity.Session;
import edu.cit.spot.entity.User;
import edu.cit.spot.exception.ResourceNotFoundException;
import edu.cit.spot.repository.AttendanceRepository;
import edu.cit.spot.repository.SessionRepository;
import edu.cit.spot.repository.UserRepository;
import edu.cit.spot.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Override
    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    @Override
    public Attendance getAttendanceById(Long id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found with id: " + id));
    }

    @Override
    public List<Attendance> getAttendanceBySessionId(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));
        return attendanceRepository.findBySession(session);
    }

    @Override
    public List<Attendance> getAttendanceByCourseId(Long courseId) {
        return attendanceRepository.findByCourseId(courseId);
    }

    @Override
    public List<Attendance> getAttendanceByStudentId(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        return attendanceRepository.findByStudent(student);
    }

    @Override
    @Transactional
    public Attendance recordAttendance(Long sessionId, Long studentId, Attendance.AttendanceStatus status) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));
        
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        // Check if attendance is already recorded
        Optional<Attendance> existingAttendance = attendanceRepository.findBySessionAndStudent(session, student);
        
        Attendance attendance;
        if (existingAttendance.isPresent()) {
            // Update existing attendance
            attendance = existingAttendance.get();
            attendance.setStatus(status);
            attendance.setUpdatedAt(LocalDateTime.now());
        } else {
            // Create new attendance record
            attendance = new Attendance();
            attendance.setSession(session);
            attendance.setStudent(student);
            attendance.setStatus(status);
            attendance.setRecordedAt(LocalDateTime.now());
        }
        
        return attendanceRepository.save(attendance);
    }

    @Override
    @Transactional
    public Attendance updateAttendance(Long id, Attendance.AttendanceStatus status) {
        Attendance attendance = getAttendanceById(id);
        attendance.setStatus(status);
        attendance.setUpdatedAt(LocalDateTime.now());
        
        return attendanceRepository.save(attendance);
    }

    @Override
    @Transactional
    public void deleteAttendance(Long id) {
        Attendance attendance = getAttendanceById(id);
        attendanceRepository.delete(attendance);
    }

    @Override
    public Map<String, Object> getAttendanceStatsByCourse(Long courseId) {
        List<Attendance> attendanceList = attendanceRepository.findByCourseId(courseId);
        
        // Calculate stats
        long totalAttendance = attendanceList.size();
        long presentCount = countByStatus(attendanceList, Attendance.AttendanceStatus.PRESENT);
        long absentCount = countByStatus(attendanceList, Attendance.AttendanceStatus.ABSENT);
        long lateCount = countByStatus(attendanceList, Attendance.AttendanceStatus.LATE);
        long excusedCount = countByStatus(attendanceList, Attendance.AttendanceStatus.EXCUSED);
        
        // Prepare stats map
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRecords", totalAttendance);
        stats.put("presentCount", presentCount);
        stats.put("presentPercentage", calculatePercentage(presentCount, totalAttendance));
        stats.put("absentCount", absentCount);
        stats.put("absentPercentage", calculatePercentage(absentCount, totalAttendance));
        stats.put("lateCount", lateCount);
        stats.put("latePercentage", calculatePercentage(lateCount, totalAttendance));
        stats.put("excusedCount", excusedCount);
        stats.put("excusedPercentage", calculatePercentage(excusedCount, totalAttendance));
        
        return stats;
    }

    @Override
    public Map<String, Object> getAttendanceStatsByStudent(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        List<Attendance> attendanceList = attendanceRepository.findByStudent(student);
        
        // Calculate stats
        long totalAttendance = attendanceList.size();
        long presentCount = countByStatus(attendanceList, Attendance.AttendanceStatus.PRESENT);
        long absentCount = countByStatus(attendanceList, Attendance.AttendanceStatus.ABSENT);
        long lateCount = countByStatus(attendanceList, Attendance.AttendanceStatus.LATE);
        long excusedCount = countByStatus(attendanceList, Attendance.AttendanceStatus.EXCUSED);
        
        // Prepare stats map
        Map<String, Object> stats = new HashMap<>();
        stats.put("studentId", studentId);
        stats.put("studentName", student.getFirstName() + " " + student.getLastName());
        stats.put("totalSessions", totalAttendance);
        stats.put("presentCount", presentCount);
        stats.put("presentPercentage", calculatePercentage(presentCount, totalAttendance));
        stats.put("absentCount", absentCount);
        stats.put("absentPercentage", calculatePercentage(absentCount, totalAttendance));
        stats.put("lateCount", lateCount);
        stats.put("latePercentage", calculatePercentage(lateCount, totalAttendance));
        stats.put("excusedCount", excusedCount);
        stats.put("excusedPercentage", calculatePercentage(excusedCount, totalAttendance));
        
        return stats;
    }

    @Override
    @Transactional
    public void markBulkAttendance(Long sessionId, List<Long> studentIds, Attendance.AttendanceStatus status) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));
        
        for (Long studentId : studentIds) {
            User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
            
            Optional<Attendance> existingAttendance = attendanceRepository.findBySessionAndStudent(session, student);
            
            Attendance attendance;
            if (existingAttendance.isPresent()) {
                attendance = existingAttendance.get();
                attendance.setStatus(status);
                attendance.setUpdatedAt(LocalDateTime.now());
            } else {
                attendance = new Attendance();
                attendance.setSession(session);
                attendance.setStudent(student);
                attendance.setStatus(status);
                attendance.setRecordedAt(LocalDateTime.now());
            }
            
            attendanceRepository.save(attendance);
        }
    }
    
    // Helper methods
    private long countByStatus(List<Attendance> attendanceList, Attendance.AttendanceStatus status) {
        return attendanceList.stream()
                .filter(a -> a.getStatus() == status)
                .count();
    }
    
    private double calculatePercentage(long count, long total) {
        if (total == 0) return 0.0;
        return Math.round((double) count / total * 100 * 10.0) / 10.0;  // Round to 1 decimal place
    }
}
