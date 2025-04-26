package edu.cit.spot.service;

import edu.cit.spot.dto.course.CourseDto;
import edu.cit.spot.dto.course.CreateCourseRequest;
import edu.cit.spot.dto.course.CourseUpdateRequest;

import java.util.List;

public interface CourseService {
    
    CourseDto createCourse(CreateCourseRequest request);
    
    CourseDto getCourseById(Long id);
    
    CourseDto getCourseByCourseCode(String courseCode);
    
    CourseDto updateCourse(Long id, CourseUpdateRequest request);
    
    List<CourseDto> getAllCourses();
    
    boolean deleteCourse(Long id);
}
