package edu.cit.spot.dto.student;

import edu.cit.spot.entity.Student;

public record StudentDto(
    Long id,
    String firstName,
    String middleName,
    String lastName,
    String year,
    String program,
    String email,
    String studentPhysicalId,
    boolean googleLinked
) {
    public StudentDto {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be blank");
        }
        if (year == null || year.isBlank()) {
            throw new IllegalArgumentException("Year cannot be blank");
        }
        if (program == null || program.isBlank()) {
            throw new IllegalArgumentException("Program cannot be blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be blank");
        }
        if (studentPhysicalId == null || studentPhysicalId.isBlank()) {
            throw new IllegalArgumentException("Student physical ID cannot be blank");
        }
    }
    
    public static StudentDto fromEntity(Student student) {
        return new StudentDto(
            student.getId(),
            student.getFirstName(),
            student.getMiddleName(),
            student.getLastName(),
            student.getYear(),
            student.getProgram(),
            student.getEmail(),
            student.getStudentPhysicalId(),
            student.isGoogleLinked()
        );
    }
}
