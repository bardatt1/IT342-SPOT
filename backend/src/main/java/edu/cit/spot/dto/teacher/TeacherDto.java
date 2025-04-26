package edu.cit.spot.dto.teacher;

import edu.cit.spot.entity.Teacher;

public record TeacherDto(
    Long id,
    String firstName,
    String middleName,
    String lastName,
    String email,
    String teacherPhysicalId,
    boolean googleLinked,
    java.util.List<Long> assignedSectionIds
) {
    public TeacherDto {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be blank");
        }
        if (teacherPhysicalId == null || teacherPhysicalId.isBlank()) {
            throw new IllegalArgumentException("Teacher physical ID cannot be blank");
        }
    }
    
    public static TeacherDto fromEntity(Teacher teacher) {
        java.util.List<Long> sectionIds = teacher.getSections() != null ? 
            teacher.getSections().stream()
                .map(section -> section.getId())
                .collect(java.util.stream.Collectors.toList()) : 
            new java.util.ArrayList<>();
        
        return new TeacherDto(
            teacher.getId(),
            teacher.getFirstName(),
            teacher.getMiddleName(),
            teacher.getLastName(),
            teacher.getEmail(),
            teacher.getTeacherPhysicalId(),
            teacher.isGoogleLinked(),
            sectionIds
        );
    }
}
