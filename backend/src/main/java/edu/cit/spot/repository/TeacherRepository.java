package edu.cit.spot.repository;

import edu.cit.spot.entity.Teacher;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    
    Optional<Teacher> findByEmail(String email);
    
    Optional<Teacher> findByTeacherPhysicalId(String teacherPhysicalId);
    
    boolean existsByEmail(String email);
    
    boolean existsByTeacherPhysicalId(String teacherPhysicalId);
    
    @Query("SELECT t FROM Teacher t WHERE t.googleId = :googleId")
    Optional<Teacher> findByGoogleId(@Param("googleId") String googleId);
    
    @EntityGraph(attributePaths = {"sections"})
    @Query("SELECT t FROM Teacher t WHERE t.id = :id")
    Optional<Teacher> findByIdWithSections(@Param("id") Long id);
}
