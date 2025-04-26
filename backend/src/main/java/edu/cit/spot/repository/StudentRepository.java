package edu.cit.spot.repository;

import edu.cit.spot.entity.Student;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    Optional<Student> findByEmail(String email);
    
    Optional<Student> findByStudentPhysicalId(String studentPhysicalId);
    
    boolean existsByEmail(String email);
    
    boolean existsByStudentPhysicalId(String studentPhysicalId);
    
    @Query("SELECT s FROM Student s WHERE s.googleId = :googleId")
    Optional<Student> findByGoogleId(@Param("googleId") String googleId);
}
