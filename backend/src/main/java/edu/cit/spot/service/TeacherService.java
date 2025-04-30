package edu.cit.spot.service;

import edu.cit.spot.dto.teacher.CreateTeacherRequest;
import edu.cit.spot.dto.teacher.TeacherDto;
import edu.cit.spot.dto.teacher.TeacherUpdateRequest;

import java.util.List;

public interface TeacherService {
    
    TeacherDto createTeacher(CreateTeacherRequest request);
    
    TeacherDto getTeacherById(Long id);
    
    TeacherDto getTeacherByEmail(String email);
    
    TeacherDto updateTeacher(Long id, TeacherUpdateRequest request);
    
    List<TeacherDto> getAllTeachers();
    
    boolean deleteTeacher(Long id);
    
    TeacherDto bindGoogleAccount(Long id, String googleId);
    
    TeacherDto assignToSection(Long teacherId, Long sectionId);
    
    TeacherDto removeFromSection(Long teacherId);
    
    /**
     * Get the current authenticated teacher
     * @return the current teacher or throws exception if not found
     */
    TeacherDto getCurrentTeacher();
}
