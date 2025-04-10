package edu.cit.spot.service.impl;

import edu.cit.spot.entity.Course;
import edu.cit.spot.entity.SeatAssignment;
import edu.cit.spot.entity.SeatPlan;
import edu.cit.spot.entity.User;
import edu.cit.spot.exception.ResourceNotFoundException;
import edu.cit.spot.repository.CourseRepository;
import edu.cit.spot.repository.SeatAssignmentRepository;
import edu.cit.spot.repository.SeatPlanRepository;
import edu.cit.spot.repository.UserRepository;
import edu.cit.spot.service.SeatPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SeatPlanServiceImpl implements SeatPlanService {
    private final SeatPlanRepository seatPlanRepository;
    private final SeatAssignmentRepository seatAssignmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Override
    public List<SeatPlan> getAllSeatPlans() {
        return seatPlanRepository.findAll();
    }

    @Override
    public SeatPlan getSeatPlanById(Long id) {
        return seatPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat plan not found with id: " + id));
    }

    @Override
    public List<SeatPlan> getSeatPlansByCourseId(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        return seatPlanRepository.findByCourse(course);
    }

    @Override
    @Transactional
    public SeatPlan createSeatPlan(Long courseId, SeatPlan seatPlan) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        
        seatPlan.setCourse(course);
        
        // If this is the first seat plan for this course, make it active
        List<SeatPlan> existingSeatPlans = seatPlanRepository.findByCourse(course);
        if (existingSeatPlans.isEmpty()) {
            seatPlan.setActive(true);
        }
        
        return seatPlanRepository.save(seatPlan);
    }

    @Override
    @Transactional
    public SeatPlan updateSeatPlan(Long id, SeatPlan seatPlanDetails) {
        SeatPlan seatPlan = getSeatPlanById(id);
        
        // Handle partial updates - only update fields that are not null
        if (seatPlanDetails.getName() != null) {
            seatPlan.setName(seatPlanDetails.getName());
        }
        
        if (seatPlanDetails.getLayoutJson() != null) {
            seatPlan.setLayoutJson(seatPlanDetails.getLayoutJson());
        }
        
        if (seatPlanDetails.getRows() != null) {
            seatPlan.setRows(seatPlanDetails.getRows());
        }
        
        if (seatPlanDetails.getColumns() != null) {
            seatPlan.setColumns(seatPlanDetails.getColumns());
        }
        
        // Handle the active status if it's explicitly set in the request
        if (seatPlanDetails.isActive()) {
            // If setting to active, deactivate all other seat plans for this course
            Course course = seatPlan.getCourse();
            List<SeatPlan> courseSeatPlans = seatPlanRepository.findByCourse(course);
            for (SeatPlan plan : courseSeatPlans) {
                if (!plan.getId().equals(id)) {
                    plan.setActive(false);
                    seatPlanRepository.save(plan);
                }
            }
            seatPlan.setActive(true);
        }
        
        return seatPlanRepository.save(seatPlan);
    }

    @Override
    @Transactional
    public void deleteSeatPlan(Long id) {
        SeatPlan seatPlan = getSeatPlanById(id);
        
        // Delete all seat assignments first
        List<SeatAssignment> assignments = seatAssignmentRepository.findBySeatPlan(seatPlan);
        seatAssignmentRepository.deleteAll(assignments);
        
        // Then delete the seat plan
        seatPlanRepository.delete(seatPlan);
    }

    @Override
    @Transactional
    public void setActiveSeatPlan(Long id) {
        SeatPlan seatPlan = getSeatPlanById(id);
        Course course = seatPlan.getCourse();
        
        // Deactivate all other seat plans for this course
        List<SeatPlan> courseSeatPlans = seatPlanRepository.findByCourse(course);
        for (SeatPlan plan : courseSeatPlans) {
            plan.setActive(false);
            seatPlanRepository.save(plan);
        }
        
        // Activate the requested seat plan
        seatPlan.setActive(true);
        seatPlanRepository.save(seatPlan);
    }

    @Override
    public List<SeatAssignment> getSeatAssignments(Long seatPlanId) {
        SeatPlan seatPlan = getSeatPlanById(seatPlanId);
        return seatAssignmentRepository.findBySeatPlan(seatPlan);
    }

    @Override
    @Transactional
    public SeatAssignment assignSeat(Long seatPlanId, Long studentId, Integer row, Integer column) {
        SeatPlan seatPlan = getSeatPlanById(seatPlanId);
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        // Verify row and column are valid
        if (row < 0 || row >= seatPlan.getRows() || column < 0 || column >= seatPlan.getColumns()) {
            throw new IllegalArgumentException("Seat coordinates out of bounds");
        }
        
        // Check if seat is already occupied
        List<SeatAssignment> existingAssignments = seatAssignmentRepository
                .findBySeatPlanAndRowIndexAndColumnIndex(seatPlan, row, column);
        if (!existingAssignments.isEmpty()) {
            throw new IllegalArgumentException("Seat already occupied");
        }
        
        // Check if student is already assigned a seat in this plan
        seatAssignmentRepository.findBySeatPlanAndStudent(seatPlan, student)
                .ifPresent(assignment -> seatAssignmentRepository.delete(assignment));
        
        // Create new assignment
        SeatAssignment assignment = new SeatAssignment();
        assignment.setSeatPlan(seatPlan);
        assignment.setStudent(student);
        assignment.setRowIndex(row);
        assignment.setColumnIndex(column);
        
        return seatAssignmentRepository.save(assignment);
    }

    @Override
    @Transactional
    public void removeSeatAssignment(Long seatPlanId, Long studentId) {
        SeatPlan seatPlan = getSeatPlanById(seatPlanId);
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        seatAssignmentRepository.findBySeatPlanAndStudent(seatPlan, student)
                .ifPresent(seatAssignmentRepository::delete);
    }

    @Override
    public Map<String, Object> getSeatPlanWithAssignments(Long seatPlanId) {
        SeatPlan seatPlan = getSeatPlanById(seatPlanId);
        List<SeatAssignment> assignments = seatAssignmentRepository.findBySeatPlan(seatPlan);
        
        Map<String, Object> result = new HashMap<>();
        result.put("seatPlan", seatPlan);
        result.put("assignments", assignments);
        
        return result;
    }
}
