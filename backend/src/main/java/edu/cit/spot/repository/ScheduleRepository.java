package edu.cit.spot.repository;

import edu.cit.spot.entity.Schedule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    
    @EntityGraph(attributePaths = {"section"})
    @Query("SELECT s FROM Schedule s WHERE s.section.id = :sectionId")
    List<Schedule> findBySectionId(@Param("sectionId") Long sectionId);
    
    @Query("SELECT s FROM Schedule s WHERE s.dayOfWeek = :dayOfWeek AND s.section.id = :sectionId")
    List<Schedule> findByDayOfWeekAndSectionId(@Param("dayOfWeek") Integer dayOfWeek, @Param("sectionId") Long sectionId);
}
