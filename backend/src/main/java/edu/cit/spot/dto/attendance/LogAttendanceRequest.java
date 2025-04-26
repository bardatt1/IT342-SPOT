package edu.cit.spot.dto.attendance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LogAttendanceRequest(
    @NotNull(message = "Section ID cannot be null")
    Long sectionId
) {
    public LogAttendanceRequest {
        if (sectionId == null || sectionId <= 0) {
            throw new IllegalArgumentException("Invalid section ID");
        }
    }
}
