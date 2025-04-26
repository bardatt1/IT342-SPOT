package edu.cit.spot.dto.enrollment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EnrollRequest(
    @NotBlank(message = "Enrollment key cannot be blank")
    String enrollmentKey
) {
    public EnrollRequest {
        if (enrollmentKey != null) {
            enrollmentKey = enrollmentKey.trim();
        }
    }
}
