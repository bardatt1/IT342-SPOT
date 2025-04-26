package edu.cit.spot.dto.section;

import edu.cit.spot.dto.course.CourseDto;
import edu.cit.spot.dto.teacher.TeacherDto;
import edu.cit.spot.entity.Section;

public record SectionDto(
    Long id,
    CourseDto course,
    TeacherDto teacher,
    String sectionName,
    String enrollmentKey,
    boolean enrollmentOpen,
    int enrollmentCount
) {
    public SectionDto {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null");
        }
        if (sectionName == null || sectionName.isBlank()) {
            throw new IllegalArgumentException("Section name cannot be blank");
        }
        if (enrollmentCount < 0) {
            enrollmentCount = 0;
        }
    }
    
    public static SectionDto fromEntity(Section section) {
        CourseDto courseDto = section.getCourse() != null ? 
            CourseDto.fromEntity(section.getCourse()) : null;
            
        TeacherDto teacherDto = section.getTeacher() != null ?
            TeacherDto.fromEntity(section.getTeacher()) : null;
            
        return new SectionDto(
            section.getId(),
            courseDto,
            teacherDto,
            section.getSectionName(),
            section.getEnrollmentKey(),
            section.isEnrollmentOpen(),
            section.getEnrollments() != null ? section.getEnrollments().size() : 0
        );
    }
}
