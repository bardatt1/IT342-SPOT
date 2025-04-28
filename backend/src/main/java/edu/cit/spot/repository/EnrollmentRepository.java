package edu.cit.spot.repository;

import edu.cit.spot.entity.Enrollment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    
    @EntityGraph(attributePaths = {"section", "student"})
    @Query("SELECT e FROM Enrollment e WHERE e.section.id = :sectionId")
    List<Enrollment> findBySectionIdWithStudents(@Param("sectionId") Long sectionId);
    
    @EntityGraph(attributePaths = {"section", "section.course"})
    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId")
    List<Enrollment> findByStudentIdWithSections(@Param("studentId") Long studentId);
    
    @Query("SELECT COUNT(e) > 0 FROM Enrollment e WHERE e.section.id = :sectionId AND e.student.id = :studentId")
    boolean existsBySectionIdAndStudentId(@Param("sectionId") Long sectionId, @Param("studentId") Long studentId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.section.id = :sectionId AND e.student.id = :studentId")
    Optional<Enrollment> findBySectionIdAndStudentId(@Param("sectionId") Long sectionId, @Param("studentId") Long studentId);
    
    @Query("DELETE FROM Enrollment e WHERE e.section.id = :sectionId")
    void deleteBySectionId(@Param("sectionId") Long sectionId);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.section.id = :sectionId")
    Long countBySectionId(@Param("sectionId") Long sectionId);
    
    /**
     * Check if a student is enrolled in a section by email
     * Used for security authorization
     */
    @Query("SELECT COUNT(e) > 0 FROM Enrollment e WHERE e.section.id = :sectionId AND e.student.email = :email")
    boolean existsByStudentEmailAndSectionId(@Param("email") String email, @Param("sectionId") Long sectionId);
}
