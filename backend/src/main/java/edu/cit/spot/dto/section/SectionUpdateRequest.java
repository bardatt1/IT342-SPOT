package edu.cit.spot.dto.section;

import jakarta.validation.constraints.Size;

public record SectionUpdateRequest(
    @Size(max = 100, message = "Section name must be less than 100 characters")
    String sectionName,
    
    @Size(max = 20, message = "Enrollment key must be less than 20 characters")
    String enrollmentKey
) {
    public SectionUpdateRequest {
        if (sectionName != null) {
            sectionName = sectionName.trim();
        }
        if (enrollmentKey != null) {
            enrollmentKey = enrollmentKey.trim();
        }
    }
}
