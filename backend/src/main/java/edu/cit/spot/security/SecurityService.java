package edu.cit.spot.security;

import edu.cit.spot.repository.AdminRepository;
import edu.cit.spot.repository.StudentRepository;
import edu.cit.spot.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {

    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private TeacherRepository teacherRepository;
    
    @Autowired
    private AdminRepository adminRepository;

    /**
     * Check if the current user is the user with the given ID
     * 
     * @param userId The ID of the user to check
     * @return true if the current user is the user with the given ID, false otherwise
     */
    public boolean isCurrentUser(Long userId) {
        String currentUserEmail = getCurrentUserEmail();
        if (currentUserEmail == null) {
            return false;
        }
        
        // Check if the student with the given ID has the current user's email
        return studentRepository.findById(userId)
                .map(student -> student.getEmail().equals(currentUserEmail))
                .orElseGet(() -> 
                    // If not a student, check if the teacher with the given ID has the current user's email
                    teacherRepository.findById(userId)
                            .map(teacher -> teacher.getEmail().equals(currentUserEmail))
                            .orElseGet(() -> 
                                // If not a teacher, check if the admin with the given ID has the current user's email
                                adminRepository.findById(userId)
                                        .map(admin -> admin.getEmail().equals(currentUserEmail))
                                        .orElse(false)
                            )
                );
    }
    
    /**
     * Check if the current user has the given email
     * 
     * @param email The email to check
     * @return true if the current user has the given email, false otherwise
     */
    public boolean isCurrentUserEmail(String email) {
        String currentUserEmail = getCurrentUserEmail();
        return currentUserEmail != null && currentUserEmail.equals(email);
    }
    
    /**
     * Get the email of the current user
     * 
     * @return The email of the current user, or null if no user is authenticated
     */
    /**
     * Check if the current user is the teacher with the given ID
     * 
     * @param teacherId The ID of the teacher to check
     * @return true if the current user is the teacher with the given ID, false otherwise
     */
    public boolean isCurrentTeacher(Long teacherId) {
        String currentUserEmail = getCurrentUserEmail();
        if (currentUserEmail == null) {
            return false;
        }
        
        return teacherRepository.findByIdWithSections(teacherId)
                .map(teacher -> teacher.getEmail().equals(currentUserEmail))
                .orElse(false);
    }
    
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        return authentication.getName();
    }
}
