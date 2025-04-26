package edu.cit.spot.service.impl;

import edu.cit.spot.dto.course.CourseDto;
import edu.cit.spot.dto.course.CourseUpdateRequest;
import edu.cit.spot.dto.course.CreateCourseRequest;
import edu.cit.spot.entity.Course;
import edu.cit.spot.exception.ResourceNotFoundException;
import edu.cit.spot.repository.CourseRepository;
import edu.cit.spot.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Override
    @Transactional
    public CourseDto createCourse(CreateCourseRequest request) {
        // Check if course code is already in use
        if (courseRepository.existsByCourseCode(request.courseCode())) {
            throw new IllegalArgumentException("Course code is already in use: " + request.courseCode());
        }
        
        // Create new course
        Course course = new Course();
        course.setCourseName(request.courseName());
        course.setCourseDescription(request.courseDescription());
        course.setCourseCode(request.courseCode());
        
        Course savedCourse = courseRepository.save(course);
        
        return CourseDto.fromEntity(savedCourse);
    }

    @Override
    public CourseDto getCourseById(Long id) {
        Course course = courseRepository.findByIdWithSections(id)
            .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        
        return CourseDto.fromEntity(course);
    }

    @Override
    public CourseDto getCourseByCourseCode(String courseCode) {
        Course course = courseRepository.findByCourseCode(courseCode)
            .orElseThrow(() -> new ResourceNotFoundException("Course", "courseCode", courseCode));
        
        return CourseDto.fromEntity(course);
    }

    @Override
    @Transactional
    public CourseDto updateCourse(Long id, CourseUpdateRequest request) {
        Course course = courseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        
        // Update course code if it's changed and not in use by another course
        if (request.courseCode() != null && !request.courseCode().equals(course.getCourseCode())) {
            if (courseRepository.existsByCourseCode(request.courseCode())) {
                throw new IllegalArgumentException("Course code is already in use: " + request.courseCode());
            }
            course.setCourseCode(request.courseCode());
        }
        
        // Update other fields if provided
        if (request.courseName() != null) {
            course.setCourseName(request.courseName());
        }
        if (request.courseDescription() != null) {
            course.setCourseDescription(request.courseDescription());
        }
        
        Course updatedCourse = courseRepository.save(course);
        
        return CourseDto.fromEntity(updatedCourse);
    }

    @Override
    public List<CourseDto> getAllCourses() {
        List<Course> courses = courseRepository.findAllWithSections();
        
        return courses.stream()
            .map(CourseDto::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        
        courseRepository.delete(course);
        return true;
    }
}
