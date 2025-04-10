package edu.cit.spot.repository;

import edu.cit.spot.entity.Course;
import edu.cit.spot.entity.Session;
import edu.cit.spot.entity.Session.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    // Course-based queries
    List<Session> findByCourse(Course course);
    List<Session> findByCourseId(Long courseId);
    List<Session> findByCourseAndStartTimeBetween(Course course, LocalDateTime start, LocalDateTime end);
    List<Session> findByCourseAndStatus(Course course, SessionStatus status);
    
    // Teacher-based queries
    List<Session> findByCourseTeacherIdAndStartTimeGreaterThanOrderByStartTime(Long teacherId, LocalDateTime currentTime);
    
    // Status-based queries
    List<Session> findByCourseStudentsIdAndStatus(Long studentId, SessionStatus status);
    
    // Alias methods with simpler names that will be used by service classes
    default List<Session> findUpcomingSessionsByTeacher(Long teacherId, LocalDateTime currentTime) {
        return findByCourseTeacherIdAndStartTimeGreaterThanOrderByStartTime(teacherId, currentTime);
    }
    
    default List<Session> findActiveSessionsForStudent(Long studentId) {
        return findByCourseStudentsIdAndStatus(studentId, SessionStatus.ACTIVE);
    }
}
