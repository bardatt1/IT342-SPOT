package edu.cit.spot.service;

import edu.cit.spot.entity.Course;
import edu.cit.spot.entity.User;

import java.util.List;

public interface CourseService {
    List<Course> getAllCourses();
    Course getCourseById(Long id);
    List<Course> getCoursesByTeacher(User teacher);
    List<Course> getCoursesByStudent(User student);
    Course createCourse(Course course);
    Course updateCourse(Long id, Course course);
    void deleteCourse(Long id);
    void enrollStudent(Long courseId, Long studentId);
    void unenrollStudent(Long courseId, Long studentId);
}
