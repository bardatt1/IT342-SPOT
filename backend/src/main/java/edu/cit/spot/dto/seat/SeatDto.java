package edu.cit.spot.dto.seat;

import edu.cit.spot.dto.student.StudentDto;
import edu.cit.spot.entity.Seat;

public record SeatDto(
    Long id,
    Long sectionId,
    StudentDto student,
    Integer column,
    Integer row
) {
    public SeatDto {
        if (sectionId == null || sectionId <= 0) {
            throw new IllegalArgumentException("Invalid section ID");
        }
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null");
        }
        if (column == null || column < 0) {
            throw new IllegalArgumentException("Column must be a non-negative integer");
        }
        if (row == null || row < 0) {
            throw new IllegalArgumentException("Row must be a non-negative integer");
        }
    }
    
    public static SeatDto fromEntity(Seat seat) {
        return new SeatDto(
            seat.getId(),
            seat.getSection().getId(),
            StudentDto.fromEntity(seat.getStudent()),
            seat.getColumn(),
            seat.getRow()
        );
    }
}
