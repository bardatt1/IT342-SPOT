package edu.cit.spot.repository;

import edu.cit.spot.entity.Attendance;
import edu.cit.spot.entity.Session;
import edu.cit.spot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findBySession(Session session);
    Optional<Attendance> findBySessionAndStudent(Session session, User student);
    List<Attendance> findByStudent(User student);
    List<Attendance> findBySessionAndStatus(Session session, Attendance.AttendanceStatus status);
    
    @Query("SELECT a FROM Attendance a WHERE a.session.course.id = :courseId AND a.student.id = :studentId")
    List<Attendance> findByCourseIdAndStudentId(Long courseId, Long studentId);
    
    @Query("SELECT a FROM Attendance a WHERE a.session.course.id = :courseId")
    List<Attendance> findByCourseId(Long courseId);
    
    @Query("SELECT a FROM Attendance a WHERE a.session.course.id = :courseId AND a.recordedAt BETWEEN :startDate AND :endDate")
    List<Attendance> findByCourseIdAndDateRange(Long courseId, LocalDateTime startDate, LocalDateTime endDate);
}
