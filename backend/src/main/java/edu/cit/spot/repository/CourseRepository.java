package edu.cit.spot.repository;

import edu.cit.spot.entity.Course;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    boolean existsByCourseCode(String courseCode);
    
    Optional<Course> findByCourseCode(String courseCode);
    
    @EntityGraph(attributePaths = {"sections"})
    @Query("SELECT c FROM Course c WHERE c.id = :id")
    Optional<Course> findByIdWithSections(@Param("id") Long id);
    
    @EntityGraph(attributePaths = {"sections"})
    @Query("SELECT c FROM Course c")
    List<Course> findAllWithSections();
}
