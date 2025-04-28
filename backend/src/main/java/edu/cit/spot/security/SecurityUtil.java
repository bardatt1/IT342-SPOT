package edu.cit.spot.security;

import edu.cit.spot.entity.Student;
import edu.cit.spot.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Utility class for Spring Security related operations
 */
@Component("securityUtil")
public class SecurityUtil {
    
    @Autowired
    private StudentRepository studentRepository;
    
    /**
     * Check if the current authenticated user has the same ID as the provided student ID
     * Used in @PreAuthorize expressions to restrict access to a user's own data
     * 
     * @param studentId The student ID to check against the current authenticated user
     * @return true if the current user's email matches the student's email with the provided ID
     */
    public boolean isCurrentUser(Long studentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        // Get the authenticated user's email
        String email = authentication.getName();
        
        // Find the student by ID
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            return false;
        }
        
        // Compare emails to determine if this is the current user
        return studentOpt.get().getEmail().equals(email);
    }
}
