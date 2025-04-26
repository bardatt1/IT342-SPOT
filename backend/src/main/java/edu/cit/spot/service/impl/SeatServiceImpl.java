package edu.cit.spot.service.impl;

import edu.cit.spot.dto.seat.OverrideSeatRequest;
import edu.cit.spot.dto.seat.PickSeatRequest;
import edu.cit.spot.dto.seat.SeatDto;
import edu.cit.spot.entity.Enrollment;
import edu.cit.spot.entity.Seat;
import edu.cit.spot.entity.Section;
import edu.cit.spot.entity.Student;
import edu.cit.spot.entity.Teacher;
import edu.cit.spot.exception.ResourceNotFoundException;
import edu.cit.spot.repository.EnrollmentRepository;
import edu.cit.spot.repository.SeatRepository;
import edu.cit.spot.repository.SectionRepository;
import edu.cit.spot.repository.StudentRepository;
import edu.cit.spot.repository.TeacherRepository;
import edu.cit.spot.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SeatServiceImpl implements SeatService {

    @Autowired
    private SeatRepository seatRepository;
    
    @Autowired
    private SectionRepository sectionRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private TeacherRepository teacherRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public SeatDto pickSeat(PickSeatRequest request) {
        // Verify section exists
        Section section = sectionRepository.findById(request.sectionId())
            .orElseThrow(() -> new ResourceNotFoundException("Section", "id", request.sectionId()));
        
        // Verify student exists
        Student student = studentRepository.findById(request.studentId())
            .orElseThrow(() -> new ResourceNotFoundException("Student", "id", request.studentId()));
        
        // Verify student is enrolled in the section
        if (!enrollmentRepository.existsBySectionIdAndStudentId(request.sectionId(), request.studentId())) {
            throw new IllegalArgumentException("Student is not enrolled in this section");
        }
        
        // Check if the seat position is already taken
        if (seatRepository.existsBySectionIdAndPosition(request.sectionId(), request.row(), request.column())) {
            throw new IllegalArgumentException("Seat position is already taken");
        }
        
        // Check if student already has a seat in this section and remove it
        Optional<Seat> existingSeat = seatRepository.findBySectionIdAndStudentId(request.sectionId(), request.studentId());
        existingSeat.ifPresent(seatRepository::delete);
        
        // Create new seat
        Seat seat = new Seat();
        seat.setSection(section);
        seat.setStudent(student);
        seat.setRow(request.row());
        seat.setColumn(request.column());
        
        Seat savedSeat = seatRepository.save(seat);
        
        return SeatDto.fromEntity(savedSeat);
    }

    @Override
    @Transactional
    public SeatDto overrideSeat(OverrideSeatRequest request) {
        // Verify section exists
        Section section = sectionRepository.findById(request.sectionId())
            .orElseThrow(() -> new ResourceNotFoundException("Section", "id", request.sectionId()));
        
        // Verify student exists
        Student student = studentRepository.findById(request.studentId())
            .orElseThrow(() -> new ResourceNotFoundException("Student", "id", request.studentId()));
        
        // Verify teacher exists and is assigned to this section
        Teacher teacher = teacherRepository.findById(request.teacherId())
            .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", request.teacherId()));
        
        if (section.getTeacher() == null || !section.getTeacher().getId().equals(teacher.getId())) {
            throw new IllegalArgumentException("Teacher is not assigned to this section");
        }
        
        // Verify student is enrolled in the section
        if (!enrollmentRepository.existsBySectionIdAndStudentId(request.sectionId(), request.studentId())) {
            throw new IllegalArgumentException("Student is not enrolled in this section");
        }
        
        // Check if the seat position is already taken by another student
        Optional<Seat> existingSeatAtPosition = seatRepository.findBySectionIdAndPosition(
                request.sectionId(), request.row(), request.column());
        
        existingSeatAtPosition.ifPresent(existingSeat -> {
            if (!existingSeat.getStudent().getId().equals(request.studentId())) {
                seatRepository.delete(existingSeat);
            }
        });
        
        // Check if student already has a seat in this section and remove it
        Optional<Seat> existingSeatForStudent = seatRepository.findBySectionIdAndStudentId(
                request.sectionId(), request.studentId());
        
        existingSeatForStudent.ifPresent(seatRepository::delete);
        
        // Create new seat
        Seat seat = new Seat();
        seat.setSection(section);
        seat.setStudent(student);
        seat.setRow(request.row());
        seat.setColumn(request.column());
        
        Seat savedSeat = seatRepository.save(seat);
        
        return SeatDto.fromEntity(savedSeat);
    }

    @Override
    public List<SeatDto> getSeatsBySectionId(Long sectionId) {
        if (!sectionRepository.existsById(sectionId)) {
            throw new ResourceNotFoundException("Section", "id", sectionId);
        }
        
        List<Seat> seats = seatRepository.findBySectionIdWithStudent(sectionId);
        
        return seats.stream()
            .map(SeatDto::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    public SeatDto getSeatByStudentAndSectionId(Long studentId, Long sectionId) {
        Seat seat = seatRepository.findBySectionIdAndStudentId(sectionId, studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Seat", "student and section", 
                    studentId + ", " + sectionId));
        
        return SeatDto.fromEntity(seat);
    }

    @Override
    @Transactional
    public boolean deleteSeat(Long id) {
        if (!seatRepository.existsById(id)) {
            throw new ResourceNotFoundException("Seat", "id", id);
        }
        
        seatRepository.deleteById(id);
        return true;
    }

    @Override
    @Transactional
    public boolean deleteSeatsBySection(Long sectionId) {
        if (!sectionRepository.existsById(sectionId)) {
            throw new ResourceNotFoundException("Section", "id", sectionId);
        }
        
        seatRepository.deleteBySectionId(sectionId);
        return true;
    }
}
