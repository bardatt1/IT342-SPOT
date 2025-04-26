package edu.cit.spot.service;

import edu.cit.spot.dto.enrollment.EnrollmentDto;
import edu.cit.spot.dto.enrollment.EnrollRequest;

import java.util.List;

public interface EnrollmentService {
    
    EnrollmentDto enrollStudent(EnrollRequest request);
    
    List<EnrollmentDto> getEnrollmentsByStudentId(Long studentId);
    
    List<EnrollmentDto> getEnrollmentsBySectionId(Long sectionId);
    
    boolean isStudentEnrolled(Long studentId, Long sectionId);
    
    boolean deleteEnrollment(Long id);
    
    boolean deleteEnrollmentsBySectionId(Long sectionId);
}
