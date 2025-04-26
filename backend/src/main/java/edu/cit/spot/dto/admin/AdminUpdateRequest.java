package edu.cit.spot.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record AdminUpdateRequest(
    @Size(max = 50, message = "First name must be less than 50 characters")
    String firstName,
    
    @Size(max = 50, message = "Middle name must be less than 50 characters")
    String middleName,
    
    @Size(max = 50, message = "Last name must be less than 50 characters")
    String lastName,
    
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must be less than 100 characters")
    String email,
    
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    String password
) {
    public AdminUpdateRequest {
        if (firstName != null) {
            firstName = firstName.trim();
        }
        if (middleName != null) {
            middleName = middleName.trim();
        }
        if (lastName != null) {
            lastName = lastName.trim();
        }
        if (email != null) {
            email = email.trim().toLowerCase();
        }
    }
}
