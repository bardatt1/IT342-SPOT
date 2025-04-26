package edu.cit.spot.repository;

import edu.cit.spot.entity.Seat;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    
    @EntityGraph(attributePaths = {"student"})
    @Query("SELECT s FROM Seat s WHERE s.section.id = :sectionId")
    List<Seat> findBySectionIdWithStudent(@Param("sectionId") Long sectionId);
    
    @Query("SELECT s FROM Seat s WHERE s.section.id = :sectionId AND s.student.id = :studentId")
    Optional<Seat> findBySectionIdAndStudentId(@Param("sectionId") Long sectionId, @Param("studentId") Long studentId);
    
    @Query("SELECT s FROM Seat s WHERE s.section.id = :sectionId AND s.row = :row AND s.column = :column")
    Optional<Seat> findBySectionIdAndPosition(@Param("sectionId") Long sectionId, @Param("row") Integer row, @Param("column") Integer column);
    
    @Query("SELECT COUNT(s) > 0 FROM Seat s WHERE s.section.id = :sectionId AND s.row = :row AND s.column = :column")
    boolean existsBySectionIdAndPosition(@Param("sectionId") Long sectionId, @Param("row") Integer row, @Param("column") Integer column);
    
    @Query("DELETE FROM Seat s WHERE s.section.id = :sectionId")
    void deleteBySectionId(@Param("sectionId") Long sectionId);
}
