package edu.cit.spot.service.impl;

import edu.cit.spot.dto.section.CreateSectionRequest;
import edu.cit.spot.dto.section.SectionDto;
import edu.cit.spot.dto.section.SectionUpdateRequest;
import edu.cit.spot.entity.Course;
import edu.cit.spot.entity.Section;
import edu.cit.spot.entity.Teacher;
import edu.cit.spot.exception.ResourceNotFoundException;
import edu.cit.spot.repository.CourseRepository;
import edu.cit.spot.repository.EnrollmentRepository;
import edu.cit.spot.repository.SeatRepository;
import edu.cit.spot.repository.SectionRepository;
import edu.cit.spot.repository.TeacherRepository;
import edu.cit.spot.service.SectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SectionServiceImpl implements SectionService {

    @Autowired
    private SectionRepository sectionRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private TeacherRepository teacherRepository;
    
    @Autowired
    private SeatRepository seatRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public SectionDto createSection(CreateSectionRequest request) {
        Course course = courseRepository.findById(request.courseId())
            .orElseThrow(() -> new ResourceNotFoundException("Course", "id", request.courseId()));
        
        // Check if a section with the same name already exists for this course
        if (sectionRepository.existsByCourseIdAndSectionNameIgnoreCase(request.courseId(), request.sectionName())) {
            throw new IllegalArgumentException("A section with the name '" + request.sectionName() + "' already exists for this course");
        }
        
        Section section = new Section();
        section.setCourse(course);
        section.setSectionName(request.sectionName());
        // Room field moved to Schedule entity
        section.setEnrollmentOpen(false);
        
        try {
            Section savedSection = sectionRepository.save(section);
            return SectionDto.fromEntity(savedSection);
        } catch (Exception e) {
            // Handle potential constraint violations
            if (e.getMessage().contains("uk_course_section_name")) {
                throw new IllegalArgumentException("A section with the name '" + request.sectionName() + "' already exists for this course");
            }
            throw e;
        }
    }

    @Override
    public SectionDto getSectionById(Long id) {
        Section section = sectionRepository.findByIdWithCourseAndTeacher(id)
            .orElseThrow(() -> new ResourceNotFoundException("Section", "id", id));
        
        return SectionDto.fromEntity(section);
    }

    @Override
    public List<SectionDto> getSectionsByCourseId(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }
        
        List<Section> sections = sectionRepository.findByCourseIdWithTeacher(courseId);
        
        return sections.stream()
            .map(SectionDto::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SectionDto updateSection(Long id, SectionUpdateRequest request) {
        Section section = sectionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Section", "id", id));
        
        // Room field moved to Schedule entity
        
        if (request.sectionName() != null) {
            // Check if another section for the same course has this name (excluding current section)
            if (!request.sectionName().equals(section.getSectionName()) && 
                sectionRepository.existsByCourseIdAndSectionNameIgnoreCaseAndIdNot(
                    section.getCourse().getId(), request.sectionName(), id)) {
                throw new IllegalArgumentException("A section with the name '" + request.sectionName() + 
                                              "' already exists for this course");
            }
            section.setSectionName(request.sectionName());
        }
        
        if (request.enrollmentKey() != null) {
            section.setEnrollmentKey(request.enrollmentKey());
        }
        
        try {
            Section updatedSection = sectionRepository.save(section);
            return SectionDto.fromEntity(updatedSection);
        } catch (Exception e) {
            // Handle potential constraint violations
            if (e.getMessage().contains("uk_course_section_name")) {
                throw new IllegalArgumentException("A section with the name '" + 
                    (request.sectionName() != null ? request.sectionName() : section.getSectionName()) + 
                    "' already exists for this course");
            }
            throw e;
        }
    }

    @Override
    @Transactional
    public boolean deleteSection(Long id) {
        Section section = sectionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Section", "id", id));
        
        sectionRepository.delete(section);
        return true;
    }

    @Override
    @Transactional
    public SectionDto assignTeacher(Long sectionId, Long teacherId) {
        Section section = sectionRepository.findById(sectionId)
            .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));
        
        Teacher teacher = teacherRepository.findById(teacherId)
            .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));
        
        // No need to check if teacher is assigned to another section
        // since teachers can teach multiple sections
        
        section.setTeacher(teacher);
        // Add this section to the teacher's sections collection
        if (!teacher.getSections().contains(section)) {
            teacher.getSections().add(section);
        }
        
        teacherRepository.save(teacher);
        Section updatedSection = sectionRepository.save(section);
        
        return SectionDto.fromEntity(updatedSection);
    }

    @Override
    @Transactional
    public SectionDto removeTeacher(Long sectionId) {
        Section section = sectionRepository.findByIdWithCourseAndTeacher(sectionId)
            .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));
        
        if (section.getTeacher() != null) {
            Teacher teacher = section.getTeacher();
            // Remove this section from teacher's sections collection
            teacher.getSections().removeIf(s -> s.getId().equals(sectionId));
            teacherRepository.save(teacher);
            
            section.setTeacher(null);
            Section updatedSection = sectionRepository.save(section);
            
            return SectionDto.fromEntity(updatedSection);
        }
        
        return SectionDto.fromEntity(section);
    }

    @Override
    @Transactional
    public SectionDto openEnrollment(Long sectionId, String enrollmentKey) {
        Section section = sectionRepository.findById(sectionId)
            .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));
        
        if (section.getTeacher() == null) {
            throw new IllegalArgumentException("Section must have a teacher assigned before opening enrollment");
        }
        
        if (enrollmentKey == null || enrollmentKey.isBlank()) {
            throw new IllegalArgumentException("Enrollment key cannot be blank");
        }
        
        section.setEnrollmentKey(enrollmentKey);
        section.setEnrollmentOpen(true);
        
        Section updatedSection = sectionRepository.save(section);
        
        return SectionDto.fromEntity(updatedSection);
    }

    @Override
    @Transactional
    public SectionDto closeEnrollment(Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
            .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));
        
        section.setEnrollmentOpen(false);
        
        Section updatedSection = sectionRepository.save(section);
        
        return SectionDto.fromEntity(updatedSection);
    }

    @Override
    @Transactional
    public SectionDto endSection(Long sectionId) {
        Section section = sectionRepository.findByIdWithCourseAndTeacher(sectionId)
            .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));
        
        // Remove teacher from section
        if (section.getTeacher() != null) {
            Teacher teacher = section.getTeacher();
            // Remove this section from teacher's sections collection
            teacher.getSections().removeIf(s -> s.getId().equals(sectionId));
            teacherRepository.save(teacher);
            section.setTeacher(null);
        }
        
        // Clear enrollment key and close enrollment
        section.setEnrollmentKey(null);
        section.setEnrollmentOpen(false);
        
        // Delete all seats in the section
        seatRepository.deleteBySectionId(sectionId);
        
        // Delete all enrollments in the section
        enrollmentRepository.deleteBySectionId(sectionId);
        
        Section updatedSection = sectionRepository.save(section);
        
        return SectionDto.fromEntity(updatedSection);
    }

    @Override
    public SectionDto getSectionByEnrollmentKey(String enrollmentKey) {
        Section section = sectionRepository.findByEnrollmentKeyAndEnrollmentOpen(enrollmentKey)
            .orElseThrow(() -> new ResourceNotFoundException("Section", "enrollmentKey", enrollmentKey));
        
        return SectionDto.fromEntity(section);
    }
}
