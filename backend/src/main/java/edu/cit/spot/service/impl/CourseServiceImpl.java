package edu.cit.spot.service.impl;

import edu.cit.spot.entity.Course;
import edu.cit.spot.entity.User;
import edu.cit.spot.exception.ResourceNotFoundException;
import edu.cit.spot.repository.CourseRepository;
import edu.cit.spot.repository.UserRepository;
import edu.cit.spot.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Override
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    @Override
    public List<Course> getCoursesByTeacher(User teacher) {
        return courseRepository.findByTeacher(teacher);
    }

    @Override
    public List<Course> getCoursesByStudent(User student) {
        return courseRepository.findByStudentsContains(student);
    }

    @Override
    @Transactional
    public Course createCourse(Course course) {
        if (courseRepository.existsByCourseCode(course.getCourseCode())) {
            throw new IllegalArgumentException("Course with code " + course.getCourseCode() + " already exists");
        }
        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public Course updateCourse(Long id, Course courseDetails) {
        Course course = getCourseById(id);
        
        // Only update fields that are provided (not null)
        if (courseDetails.getName() != null) {
            course.setName(courseDetails.getName());
        }
        
        if (courseDetails.getDescription() != null) {
            course.setDescription(courseDetails.getDescription());
        }
        
        if (courseDetails.getSchedule() != null) {
            course.setSchedule(courseDetails.getSchedule());
        }
        
        if (courseDetails.getRoom() != null) {
            course.setRoom(courseDetails.getRoom());
        }
        
        // Don't update course code as it's usually fixed
        // Only update if provided and different from current
        if (courseDetails.getCourseCode() != null && 
            !courseDetails.getCourseCode().equals(course.getCourseCode()) && 
            !courseRepository.existsByCourseCode(courseDetails.getCourseCode())) {
            course.setCourseCode(courseDetails.getCourseCode());
        }
        
        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = getCourseById(id);
        courseRepository.delete(course);
    }

    @Override
    @Transactional
    public void enrollStudent(Long courseId, Long studentId) {
        Course course = getCourseById(courseId);
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        course.getStudents().add(student);
        courseRepository.save(course);
    }

    @Override
    @Transactional
    public void unenrollStudent(Long courseId, Long studentId) {
        Course course = getCourseById(courseId);
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        course.getStudents().remove(student);
        courseRepository.save(course);
    }
}
