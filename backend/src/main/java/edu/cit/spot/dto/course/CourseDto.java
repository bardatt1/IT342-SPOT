package edu.cit.spot.dto.course;

import edu.cit.spot.entity.Course;

public record CourseDto(
    Long id,
    String courseName,
    String courseDescription,
    String courseCode,
    int sectionCount
) {
    public CourseDto {
        if (courseName == null || courseName.isBlank()) {
            throw new IllegalArgumentException("Course name cannot be blank");
        }
        if (courseCode == null || courseCode.isBlank()) {
            throw new IllegalArgumentException("Course code cannot be blank");
        }
        if (sectionCount < 0) {
            sectionCount = 0;
        }
    }
    
    public static CourseDto fromEntity(Course course) {
        return new CourseDto(
            course.getId(),
            course.getCourseName(),
            course.getCourseDescription(),
            course.getCourseCode(),
            course.getSections() != null ? course.getSections().size() : 0
        );
    }
}
