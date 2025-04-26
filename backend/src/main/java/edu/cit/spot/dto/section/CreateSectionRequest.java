package edu.cit.spot.dto.section;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSectionRequest(
    @NotNull(message = "Course ID cannot be null")
    Long courseId,
    
    @NotBlank(message = "Section name cannot be blank")
    @Size(max = 100, message = "Section name must be less than 100 characters")
    String sectionName
) {
    public CreateSectionRequest {
        if (courseId == null || courseId <= 0) {
            throw new IllegalArgumentException("Invalid course ID");
        }
        if (sectionName != null) {
            sectionName = sectionName.trim();
        }
    }
}
