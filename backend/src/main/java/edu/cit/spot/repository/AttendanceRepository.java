package edu.cit.spot.repository;

import edu.cit.spot.entity.Attendance;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    @EntityGraph(attributePaths = {"student"})
    @Query("SELECT a FROM Attendance a WHERE a.section.id = :sectionId AND a.date = :date")
    List<Attendance> findBySectionIdAndDateWithStudent(@Param("sectionId") Long sectionId, @Param("date") LocalDate date);
    
    @EntityGraph(attributePaths = {"section", "section.course"})
    @Query("SELECT a FROM Attendance a WHERE a.student.id = :studentId")
    List<Attendance> findByStudentIdWithSections(@Param("studentId") Long studentId);
    
    @EntityGraph(attributePaths = {"student"})
    @Query("SELECT a FROM Attendance a WHERE a.section.id = :sectionId")
    List<Attendance> findBySectionIdWithStudent(@Param("sectionId") Long sectionId);
    
    @Query("SELECT a FROM Attendance a WHERE a.section.id = :sectionId AND a.student.id = :studentId AND a.date = :date")
    Optional<Attendance> findBySectionIdAndStudentIdAndDate(
            @Param("sectionId") Long sectionId, 
            @Param("studentId") Long studentId, 
            @Param("date") LocalDate date);
    
    @Query("SELECT COUNT(DISTINCT a.student.id) FROM Attendance a WHERE a.section.id = :sectionId AND a.date = :date")
    Long countUniqueStudentsBySectionAndDate(@Param("sectionId") Long sectionId, @Param("date") LocalDate date);
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.section.id = :sectionId AND a.student.id = :studentId")
    Long countByStudentIdAndSectionId(@Param("studentId") Long studentId, @Param("sectionId") Long sectionId);
}
