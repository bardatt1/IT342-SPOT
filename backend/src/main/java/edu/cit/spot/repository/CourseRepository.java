package edu.cit.spot.repository;

import edu.cit.spot.entity.Course;
import edu.cit.spot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTeacher(User teacher);
    List<Course> findByStudentsContains(User student);
    boolean existsByCourseCode(String courseCode);
}
