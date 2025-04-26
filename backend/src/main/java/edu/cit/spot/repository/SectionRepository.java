package edu.cit.spot.repository;

import edu.cit.spot.entity.Section;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {
    
    // Methods to check uniqueness constraints
    boolean existsByCourseIdAndSectionNameIgnoreCase(Long courseId, String sectionName);
    
    boolean existsByCourseIdAndSectionNameIgnoreCaseAndIdNot(Long courseId, String sectionName, Long id);
    
    @EntityGraph(attributePaths = {"course", "teacher"})
    @Query("SELECT s FROM Section s WHERE s.id = :id")
    Optional<Section> findByIdWithCourseAndTeacher(@Param("id") Long id);
    
    @EntityGraph(attributePaths = {"course", "schedules"})
    @Query("SELECT s FROM Section s WHERE s.id = :id")
    Optional<Section> findByIdWithCourseAndSchedules(@Param("id") Long id);
    
    @EntityGraph(attributePaths = {"enrollments", "enrollments.student"})
    @Query("SELECT s FROM Section s WHERE s.id = :id")
    Optional<Section> findByIdWithEnrollments(@Param("id") Long id);
    
    @EntityGraph(attributePaths = {"teacher"})
    @Query("SELECT s FROM Section s WHERE s.course.id = :courseId")
    List<Section> findByCourseIdWithTeacher(@Param("courseId") Long courseId);
    
    @Query("SELECT s FROM Section s WHERE s.enrollmentKey = :key AND s.enrollmentOpen = true")
    Optional<Section> findByEnrollmentKeyAndEnrollmentOpen(@Param("key") String key);
    
    @Query("SELECT s FROM Section s WHERE s.teacher.id = :teacherId")
    List<Section> findByTeacherId(@Param("teacherId") Long teacherId);
}
