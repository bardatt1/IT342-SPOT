package edu.cit.spot.dto.enrollment;

import edu.cit.spot.dto.section.SectionDto;
import edu.cit.spot.dto.student.StudentDto;
import edu.cit.spot.entity.Enrollment;

import java.time.LocalDateTime;

public record EnrollmentDto(
    Long id,
    SectionDto section,
    StudentDto student,
    LocalDateTime enrolledAt
) {
    public EnrollmentDto {
        if (section == null) {
            throw new IllegalArgumentException("Section cannot be null");
        }
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null");
        }
        if (enrolledAt == null) {
            throw new IllegalArgumentException("Enrollment timestamp cannot be null");
        }
    }
    
    public static EnrollmentDto fromEntity(Enrollment enrollment) {
        return new EnrollmentDto(
            enrollment.getId(),
            SectionDto.fromEntity(enrollment.getSection()),
            StudentDto.fromEntity(enrollment.getStudent()),
            enrollment.getEnrolledAt()
        );
    }
}
