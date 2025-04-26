package edu.cit.spot.service;

import edu.cit.spot.dto.section.CreateSectionRequest;
import edu.cit.spot.dto.section.SectionDto;
import edu.cit.spot.dto.section.SectionUpdateRequest;

import java.util.List;

public interface SectionService {
    
    SectionDto createSection(CreateSectionRequest request);
    
    SectionDto getSectionById(Long id);
    
    List<SectionDto> getSectionsByCourseId(Long courseId);
    
    SectionDto updateSection(Long id, SectionUpdateRequest request);
    
    boolean deleteSection(Long id);
    
    SectionDto assignTeacher(Long sectionId, Long teacherId);
    
    SectionDto removeTeacher(Long sectionId);
    
    SectionDto openEnrollment(Long sectionId, String enrollmentKey);
    
    SectionDto closeEnrollment(Long sectionId);
    
    SectionDto endSection(Long sectionId);
    
    SectionDto getSectionByEnrollmentKey(String enrollmentKey);
}
