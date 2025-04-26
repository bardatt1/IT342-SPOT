package edu.cit.spot.dto.student;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record StudentUpdateRequest(
    @Size(max = 50, message = "First name must be less than 50 characters")
    String firstName,
    
    @Size(max = 50, message = "Middle name must be less than 50 characters")
    String middleName,
    
    @Size(max = 50, message = "Last name must be less than 50 characters")
    String lastName,
    
    @Size(max = 10, message = "Year must be less than 10 characters")
    String year,
    
    @Size(max = 100, message = "Program must be less than 100 characters")
    String program,
    
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must be less than 100 characters")
    String email,
    
    @Size(max = 50, message = "Student physical ID must be less than 50 characters")
    String studentPhysicalId,
    
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    String password
) {
    public StudentUpdateRequest {
        if (firstName != null) {
            firstName = firstName.trim();
        }
        if (middleName != null) {
            middleName = middleName.trim();
        }
        if (lastName != null) {
            lastName = lastName.trim();
        }
        if (year != null) {
            year = year.trim();
        }
        if (program != null) {
            program = program.trim();
        }
        if (email != null) {
            email = email.trim().toLowerCase();
        }
        if (studentPhysicalId != null) {
            studentPhysicalId = studentPhysicalId.trim();
        }
    }
}
