package edu.cit.spot.repository;

import edu.cit.spot.entity.Course;
import edu.cit.spot.entity.SeatPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatPlanRepository extends JpaRepository<SeatPlan, Long> {
    List<SeatPlan> findByCourse(Course course);
    List<SeatPlan> findByCourseAndIsActiveTrue(Course course);
    Optional<SeatPlan> findByCourseAndIsActiveTrueAndName(Course course, String name);
}
