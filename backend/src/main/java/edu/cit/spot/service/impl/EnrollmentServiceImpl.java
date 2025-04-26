package edu.cit.spot.service.impl;

import edu.cit.spot.dto.enrollment.EnrollmentDto;
import edu.cit.spot.dto.enrollment.EnrollRequest;
import edu.cit.spot.entity.Enrollment;
import edu.cit.spot.entity.Section;
import edu.cit.spot.entity.Student;
import edu.cit.spot.exception.ResourceNotFoundException;
import edu.cit.spot.repository.EnrollmentRepository;
import edu.cit.spot.repository.SectionRepository;
import edu.cit.spot.repository.StudentRepository;
import edu.cit.spot.security.SecurityService;
import edu.cit.spot.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private SecurityService securityService;

    @Override
    @Transactional
    public EnrollmentDto enrollStudent(EnrollRequest request) {
        // Get current authenticated student
        String currentUserEmail = securityService.getCurrentUserEmail();
        if (currentUserEmail == null) {
            throw new IllegalStateException("No authenticated user found");
        }
        
        // Find student by email
        Student student = studentRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Student", "email", currentUserEmail));
        
        // Find section by enrollment key
        Section section = sectionRepository.findByEnrollmentKeyAndEnrollmentOpen(request.enrollmentKey())
            .orElseThrow(() -> new IllegalArgumentException("Invalid enrollment key or enrollment is closed"));
        
        // Check if student is already enrolled
        if (enrollmentRepository.existsBySectionIdAndStudentId(section.getId(), student.getId())) {
            throw new IllegalArgumentException("You are already enrolled in this section");
        }
        
        // Create enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setSection(section);
        enrollment.setEnrolledAt(LocalDateTime.now());
        
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        
        return EnrollmentDto.fromEntity(savedEnrollment);
    }

    @Override
    public List<EnrollmentDto> getEnrollmentsByStudentId(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student", "id", studentId);
        }
        
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdWithSections(studentId);
        
        return enrollments.stream()
            .map(EnrollmentDto::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentDto> getEnrollmentsBySectionId(Long sectionId) {
        if (!sectionRepository.existsById(sectionId)) {
            throw new ResourceNotFoundException("Section", "id", sectionId);
        }
        
        List<Enrollment> enrollments = enrollmentRepository.findBySectionIdWithStudents(sectionId);
        
        return enrollments.stream()
            .map(EnrollmentDto::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    public boolean isStudentEnrolled(Long studentId, Long sectionId) {
        return enrollmentRepository.existsBySectionIdAndStudentId(sectionId, studentId);
    }

    @Override
    @Transactional
    public boolean deleteEnrollment(Long id) {
        if (!enrollmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Enrollment", "id", id);
        }
        
        enrollmentRepository.deleteById(id);
        return true;
    }

    @Override
    @Transactional
    public boolean deleteEnrollmentsBySectionId(Long sectionId) {
        if (!sectionRepository.existsById(sectionId)) {
            throw new ResourceNotFoundException("Section", "id", sectionId);
        }
        
        enrollmentRepository.deleteBySectionId(sectionId);
        return true;
    }
}
