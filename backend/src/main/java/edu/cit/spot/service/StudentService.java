package edu.cit.spot.service;

import edu.cit.spot.dto.student.StudentDto;
import edu.cit.spot.dto.student.StudentUpdateRequest;
import edu.cit.spot.dto.student.CreateStudentRequest;

import java.util.List;

public interface StudentService {
    
    StudentDto createStudent(CreateStudentRequest request);
    
    StudentDto getStudentById(Long id);
    
    StudentDto getStudentByEmail(String email);
    
    StudentDto updateStudent(Long id, StudentUpdateRequest request);
    
    List<StudentDto> getAllStudents();
    
    boolean deleteStudent(Long id);
    
    StudentDto bindGoogleAccount(Long id, String googleId);
}
