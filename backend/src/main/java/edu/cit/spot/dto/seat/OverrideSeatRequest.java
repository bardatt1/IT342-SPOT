package edu.cit.spot.dto.seat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public record OverrideSeatRequest(
    @NotNull(message = "Student ID cannot be null")
    Long studentId,
    
    @NotNull(message = "Section ID cannot be null")
    Long sectionId,
    
    @NotNull(message = "Row cannot be null")
    @Min(value = 0, message = "Row must be a non-negative integer")
    Integer row,
    
    @NotNull(message = "Column cannot be null")
    @Min(value = 0, message = "Column must be a non-negative integer")
    Integer column,
    
    @NotNull(message = "Teacher ID cannot be null")
    Long teacherId
) {
    public OverrideSeatRequest {
        if (studentId == null || studentId <= 0) {
            throw new IllegalArgumentException("Invalid student ID");
        }
        if (sectionId == null || sectionId <= 0) {
            throw new IllegalArgumentException("Invalid section ID");
        }
        if (row == null || row < 0) {
            throw new IllegalArgumentException("Row must be a non-negative integer");
        }
        if (column == null || column < 0) {
            throw new IllegalArgumentException("Column must be a non-negative integer");
        }
        if (teacherId == null || teacherId <= 0) {
            throw new IllegalArgumentException("Invalid teacher ID");
        }
    }
}
