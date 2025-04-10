package edu.cit.spot.service;

import edu.cit.spot.entity.SeatPlan;
import edu.cit.spot.entity.SeatAssignment;

import java.util.List;
import java.util.Map;

public interface SeatPlanService {
    List<SeatPlan> getAllSeatPlans();
    SeatPlan getSeatPlanById(Long id);
    List<SeatPlan> getSeatPlansByCourseId(Long courseId);
    SeatPlan createSeatPlan(Long courseId, SeatPlan seatPlan);
    SeatPlan updateSeatPlan(Long id, SeatPlan seatPlan);
    void deleteSeatPlan(Long id);
    void setActiveSeatPlan(Long id);
    
    // Seat assignment methods
    List<SeatAssignment> getSeatAssignments(Long seatPlanId);
    SeatAssignment assignSeat(Long seatPlanId, Long studentId, Integer row, Integer column);
    void removeSeatAssignment(Long seatPlanId, Long studentId);
    Map<String, Object> getSeatPlanWithAssignments(Long seatPlanId);
}
