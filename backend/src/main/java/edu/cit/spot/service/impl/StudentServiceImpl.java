package edu.cit.spot.service.impl;

import edu.cit.spot.dto.student.CreateStudentRequest;
import edu.cit.spot.dto.student.StudentDto;
import edu.cit.spot.dto.student.StudentUpdateRequest;
import edu.cit.spot.entity.Student;
import edu.cit.spot.exception.ResourceNotFoundException;
import edu.cit.spot.repository.StudentRepository;
import edu.cit.spot.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public StudentDto createStudent(CreateStudentRequest request) {
        // Check if email is already in use
        if (studentRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already in use: " + request.email());
        }
        
        // Check if student physical ID is already in use
        if (studentRepository.existsByStudentPhysicalId(request.studentPhysicalId())) {
            throw new IllegalArgumentException("Student physical ID is already in use: " + request.studentPhysicalId());
        }
        
        // Create new student
        Student student = new Student();
        student.setFirstName(request.firstName());
        student.setMiddleName(request.middleName());
        student.setLastName(request.lastName());
        student.setYear(request.year());
        student.setProgram(request.program());
        student.setEmail(request.email());
        student.setStudentPhysicalId(request.studentPhysicalId());
        student.setPassword(passwordEncoder.encode(request.password()));
        
        Student savedStudent = studentRepository.save(student);
        
        return StudentDto.fromEntity(savedStudent);
    }

    @Override
    public StudentDto getStudentById(Long id) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
        
        return StudentDto.fromEntity(student);
    }

    @Override
    public StudentDto getStudentByEmail(String email) {
        Student student = studentRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Student", "email", email));
        
        return StudentDto.fromEntity(student);
    }

    @Override
    @Transactional
    public StudentDto updateStudent(Long id, StudentUpdateRequest request) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
        
        // Update email if it's changed and not in use by another student
        if (request.email() != null && !request.email().equals(student.getEmail())) {
            if (studentRepository.existsByEmail(request.email())) {
                throw new IllegalArgumentException("Email is already in use: " + request.email());
            }
            student.setEmail(request.email());
        }
        
        // Update physical ID if it's changed and not in use by another student
        if (request.studentPhysicalId() != null && !request.studentPhysicalId().equals(student.getStudentPhysicalId())) {
            if (studentRepository.existsByStudentPhysicalId(request.studentPhysicalId())) {
                throw new IllegalArgumentException("Student physical ID is already in use: " + request.studentPhysicalId());
            }
            student.setStudentPhysicalId(request.studentPhysicalId());
        }
        
        // Update other fields if provided
        if (request.firstName() != null) {
            student.setFirstName(request.firstName());
        }
        if (request.middleName() != null) {
            student.setMiddleName(request.middleName());
        }
        if (request.lastName() != null) {
            student.setLastName(request.lastName());
        }
        if (request.year() != null) {
            student.setYear(request.year());
        }
        if (request.program() != null) {
            student.setProgram(request.program());
        }
        if (request.password() != null) {
            student.setPassword(passwordEncoder.encode(request.password()));
        }
        
        Student updatedStudent = studentRepository.save(student);
        
        return StudentDto.fromEntity(updatedStudent);
    }

    @Override
    public List<StudentDto> getAllStudents() {
        List<Student> students = studentRepository.findAll();
        
        return students.stream()
            .map(StudentDto::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
        
        studentRepository.delete(student);
        return true;
    }

    @Override
    @Transactional
    public StudentDto bindGoogleAccount(Long id, String googleId) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
        
        student.setGoogleId(googleId);
        student.setGoogleLinked(true);
        
        Student updatedStudent = studentRepository.save(student);
        
        return StudentDto.fromEntity(updatedStudent);
    }
}
