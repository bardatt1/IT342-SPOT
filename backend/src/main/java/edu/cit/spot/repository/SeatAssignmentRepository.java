package edu.cit.spot.repository;

import edu.cit.spot.entity.SeatAssignment;
import edu.cit.spot.entity.SeatPlan;
import edu.cit.spot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatAssignmentRepository extends JpaRepository<SeatAssignment, Long> {
    List<SeatAssignment> findBySeatPlan(SeatPlan seatPlan);
    Optional<SeatAssignment> findBySeatPlanAndStudent(SeatPlan seatPlan, User student);
    List<SeatAssignment> findBySeatPlanAndRowIndexAndColumnIndex(SeatPlan seatPlan, Integer rowIndex, Integer columnIndex);
    void deleteBySeatPlanAndStudent(SeatPlan seatPlan, User student);
}
