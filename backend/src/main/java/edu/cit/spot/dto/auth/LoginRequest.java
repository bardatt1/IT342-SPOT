package edu.cit.spot.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    String email,
    
    @NotBlank(message = "Password cannot be blank")
    String password
) {
    public LoginRequest {
        if (email != null) {
            email = email.trim().toLowerCase();
        }
        
        if (password != null) {
            password = password.trim();
        }
    }
}
