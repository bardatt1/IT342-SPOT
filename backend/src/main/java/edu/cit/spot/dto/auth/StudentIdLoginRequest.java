package edu.cit.spot.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for student physical ID login requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentIdLoginRequest {

    @NotEmpty(message = "Student physical ID is required")
    @NotBlank(message = "Student physical ID cannot be blank")
    private String studentPhysicalId;

    @NotEmpty(message = "Password is required")
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
