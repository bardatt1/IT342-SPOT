package edu.cit.spot.dto.course;

import jakarta.validation.constraints.Size;

public record CourseUpdateRequest(
    @Size(max = 100, message = "Course name must be less than 100 characters")
    String courseName,
    
    @Size(max = 500, message = "Course description must be less than 500 characters")
    String courseDescription,
    
    @Size(max = 20, message = "Course code must be less than 20 characters")
    String courseCode
) {
    public CourseUpdateRequest {
        if (courseName != null) {
            courseName = courseName.trim();
        }
        if (courseDescription != null) {
            courseDescription = courseDescription.trim();
        }
        if (courseCode != null) {
            courseCode = courseCode.trim().toUpperCase();
        }
    }
}
