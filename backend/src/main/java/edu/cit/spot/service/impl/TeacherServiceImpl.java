package edu.cit.spot.service.impl;

import edu.cit.spot.dto.teacher.CreateTeacherRequest;
import edu.cit.spot.dto.teacher.TeacherDto;
import edu.cit.spot.dto.teacher.TeacherUpdateRequest;
import edu.cit.spot.entity.Section;
import edu.cit.spot.entity.Teacher;
import edu.cit.spot.exception.ResourceNotFoundException;
import edu.cit.spot.repository.SectionRepository;
import edu.cit.spot.repository.TeacherRepository;
import edu.cit.spot.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeacherServiceImpl implements TeacherService {

    @Autowired
    private TeacherRepository teacherRepository;
    
    @Autowired
    private SectionRepository sectionRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public TeacherDto createTeacher(CreateTeacherRequest request) {
        // Check if email is already in use
        if (teacherRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already in use: " + request.email());
        }
        
        // Check if teacher physical ID is already in use
        if (teacherRepository.existsByTeacherPhysicalId(request.teacherPhysicalId())) {
            throw new IllegalArgumentException("Teacher physical ID is already in use: " + request.teacherPhysicalId());
        }
        
        // Create new teacher
        Teacher teacher = new Teacher();
        teacher.setFirstName(request.firstName());
        teacher.setMiddleName(request.middleName());
        teacher.setLastName(request.lastName());
        teacher.setEmail(request.email());
        teacher.setTeacherPhysicalId(request.teacherPhysicalId());
        teacher.setPassword(passwordEncoder.encode(request.password()));
        
        Teacher savedTeacher = teacherRepository.save(teacher);
        
        return TeacherDto.fromEntity(savedTeacher);
    }

    @Override
    public TeacherDto getTeacherById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));
        
        return TeacherDto.fromEntity(teacher);
    }

    @Override
    public TeacherDto getTeacherByEmail(String email) {
        Teacher teacher = teacherRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Teacher", "email", email));
        
        return TeacherDto.fromEntity(teacher);
    }

    @Override
    @Transactional
    public TeacherDto updateTeacher(Long id, TeacherUpdateRequest request) {
        Teacher teacher = teacherRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));
        
        // Update email if it's changed and not in use by another teacher
        if (request.email() != null && !request.email().equals(teacher.getEmail())) {
            if (teacherRepository.existsByEmail(request.email())) {
                throw new IllegalArgumentException("Email is already in use: " + request.email());
            }
            teacher.setEmail(request.email());
        }
        
        // Update physical ID if it's changed and not in use by another teacher
        if (request.teacherPhysicalId() != null && !request.teacherPhysicalId().equals(teacher.getTeacherPhysicalId())) {
            if (teacherRepository.existsByTeacherPhysicalId(request.teacherPhysicalId())) {
                throw new IllegalArgumentException("Teacher physical ID is already in use: " + request.teacherPhysicalId());
            }
            teacher.setTeacherPhysicalId(request.teacherPhysicalId());
        }
        
        // Update other fields if provided
        if (request.firstName() != null) {
            teacher.setFirstName(request.firstName());
        }
        if (request.middleName() != null) {
            teacher.setMiddleName(request.middleName());
        }
        if (request.lastName() != null) {
            teacher.setLastName(request.lastName());
        }
        if (request.password() != null) {
            teacher.setPassword(passwordEncoder.encode(request.password()));
        }
        
        Teacher updatedTeacher = teacherRepository.save(teacher);
        
        return TeacherDto.fromEntity(updatedTeacher);
    }

    @Override
    public List<TeacherDto> getAllTeachers() {
        List<Teacher> teachers = teacherRepository.findAll();
        
        return teachers.stream()
            .map(TeacherDto::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteTeacher(Long id) {
        Teacher teacher = teacherRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));
        
        teacherRepository.delete(teacher);
        return true;
    }

    @Override
    @Transactional
    public TeacherDto bindGoogleAccount(Long id, String googleId) {
        Teacher teacher = teacherRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));
        
        teacher.setGoogleId(googleId);
        teacher.setGoogleLinked(true);
        
        Teacher updatedTeacher = teacherRepository.save(teacher);
        
        return TeacherDto.fromEntity(updatedTeacher);
    }

    @Override
    @Transactional
    public TeacherDto assignToSection(Long teacherId, Long sectionId) {
        Teacher teacher = teacherRepository.findById(teacherId)
            .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));
            
        Section section = sectionRepository.findById(sectionId)
            .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));
            
        // Check if section already has a teacher
        if (section.getTeacher() != null && !section.getTeacher().getId().equals(teacherId)) {
            throw new IllegalArgumentException("Section is already assigned to another teacher");
        }
        
        // Assign teacher to section
        section.setTeacher(teacher);
        
        // Add this section to teacher's sections collection
        if (!teacher.getSections().contains(section)) {
            teacher.getSections().add(section);
        }
        
        sectionRepository.save(section);
        Teacher updatedTeacher = teacherRepository.save(teacher);
        
        return TeacherDto.fromEntity(updatedTeacher);
    }

    @Override
    @Transactional
    public TeacherDto removeFromSection(Long teacherId) {
        Teacher teacher = teacherRepository.findByIdWithSections(teacherId)
            .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));
            
        if (!teacher.getSections().isEmpty()) {
            // Get all sections that this teacher teaches
            java.util.List<Section> sectionsToUpdate = new java.util.ArrayList<>(teacher.getSections());
            
            // Remove teacher from each section
            for (Section section : sectionsToUpdate) {
                section.setTeacher(null);
                sectionRepository.save(section);
            }
            
            // Clear the teacher's sections
            teacher.getSections().clear();
            Teacher updatedTeacher = teacherRepository.save(teacher);
            
            return TeacherDto.fromEntity(updatedTeacher);
        }
        
        return TeacherDto.fromEntity(teacher);
    }
}
