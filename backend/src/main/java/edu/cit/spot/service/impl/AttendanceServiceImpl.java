package edu.cit.spot.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import edu.cit.spot.dto.attendance.AttendanceDto;
import edu.cit.spot.dto.attendance.LogAttendanceRequest;
import edu.cit.spot.dto.attendance.QRCodeResponse;
import edu.cit.spot.entity.Attendance;
import edu.cit.spot.entity.Enrollment;
import edu.cit.spot.entity.Section;
import edu.cit.spot.entity.Student;
import edu.cit.spot.entity.Teacher;
import edu.cit.spot.exception.ResourceNotFoundException;
import edu.cit.spot.repository.AttendanceRepository;
import edu.cit.spot.repository.EnrollmentRepository;
import edu.cit.spot.repository.SectionRepository;
import edu.cit.spot.repository.StudentRepository;
import edu.cit.spot.repository.TeacherRepository;
import edu.cit.spot.security.SecurityService;
import edu.cit.spot.service.AttendanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImpl implements AttendanceService {
    
    private static final ZoneId MANILA_ZONE = ZoneId.of("Asia/Manila"); // GMT+8
    
    private static final Logger logger = LoggerFactory.getLogger(AttendanceServiceImpl.class);
    private static final long QR_CODE_EXPIRATION_SECONDS = 300; // 5 minutes
    private static final int QR_CODE_WIDTH = 300;
    private static final int QR_CODE_HEIGHT = 300;
    private static final String QR_ATTENDANCE_PREFIX = "attend:"; // Match mobile app's expected format

    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private SectionRepository sectionRepository;
    
    @Autowired
    private TeacherRepository teacherRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private SecurityService securityService;
    
    @org.springframework.beans.factory.annotation.Value("${application.frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public AttendanceDto logAttendance(LogAttendanceRequest request) {
        // Get current authenticated student
        String currentUserEmail = securityService.getCurrentUserEmail();
        if (currentUserEmail == null) {
            throw new IllegalStateException("No authenticated user found");
        }
        
        // Find student by email
        Student student = studentRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Student", "email", currentUserEmail));
        
        // Get section ID directly from the request
        Long sectionId = request.sectionId();
        
        if (sectionId == null || sectionId <= 0) {
            throw new IllegalArgumentException("Invalid section ID");
        }
        
        // Find section
        Section section = sectionRepository.findById(sectionId)
            .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));
        
        // Check if student is enrolled in the section
        if (!enrollmentRepository.existsBySectionIdAndStudentId(sectionId, student.getId())) {
            throw new IllegalArgumentException("You are not enrolled in this section");
        }
        
        // Check if attendance for today already exists
        // Use Manila timezone (GMT+8) for date and time
        ZonedDateTime manilaDateTime = ZonedDateTime.now(MANILA_ZONE);
        LocalDate today = manilaDateTime.toLocalDate();
        LocalTime now = manilaDateTime.toLocalTime();
        
        attendanceRepository.findBySectionIdAndStudentIdAndDate(sectionId, student.getId(), today)
            .ifPresent(existingAttendance -> {
                if (existingAttendance.getEndTime() == null) {
                    // Update end time for existing attendance
                    existingAttendance.setEndTime(now);
                    attendanceRepository.save(existingAttendance);
                }
                throw new IllegalArgumentException("Attendance already recorded for today");
            });
        
        // Create new attendance record
        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setSection(section);
        attendance.setDate(today);
        attendance.setStartTime(now);
        
        Attendance savedAttendance = attendanceRepository.save(attendance);
        
        return AttendanceDto.fromEntity(savedAttendance);
    }

    @Override
    public QRCodeResponse generateQRCode(Long sectionId) {
        // Get current authenticated teacher
        String currentUserEmail = securityService.getCurrentUserEmail();
        if (currentUserEmail == null) {
            throw new IllegalStateException("No authenticated user found");
        }
        
        // Find teacher by email
        Teacher teacher = teacherRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Teacher", "email", currentUserEmail));
        
        Section section = sectionRepository.findByIdWithCourseAndTeacher(sectionId)
            .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));
        
        if (section.getTeacher() == null || !section.getTeacher().getId().equals(teacher.getId())) {
            throw new IllegalArgumentException("You are not assigned to this section");
        }
        
        // Generate QR code with simple format for mobile app
        String qrCodeData = QR_ATTENDANCE_PREFIX + sectionId;
        
        // Also keep the URL for web frontend reference
        String qrCodeUrl = String.format("%s/attendance/log/%d", frontendUrl, sectionId);
        
        // Generate QR code image with the format expected by mobile app
        String qrCodeImageBase64;
        try {
            qrCodeImageBase64 = generateQRCodeImage(qrCodeData);
        } catch (Exception e) {
            logger.error("Failed to generate QR code", e);
            throw new RuntimeException("Failed to generate QR code", e);
        }
        
        // Use Manila timezone (GMT+8) for date
        ZonedDateTime manilaDateTime = ZonedDateTime.now(MANILA_ZONE);
        LocalDate today = manilaDateTime.toLocalDate();
        
        return new QRCodeResponse(
            qrCodeUrl,
            qrCodeImageBase64,
            sectionId,
            section.getCourse().getCourseName(),
            today,
            QR_CODE_EXPIRATION_SECONDS
        );
    }

    private String generateQRCodeImage(String qrCodeData) throws WriterException, IOException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(
            qrCodeData, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    @Override
    public List<AttendanceDto> getAttendanceByStudentId(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student", "id", studentId);
        }
        
        List<Attendance> attendances = attendanceRepository.findByStudentIdWithSections(studentId);
        
        return attendances.stream()
            .map(AttendanceDto::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDto> getAttendanceBySectionId(Long sectionId) {
        if (!sectionRepository.existsById(sectionId)) {
            throw new ResourceNotFoundException("Section", "id", sectionId);
        }
        
        List<Attendance> attendances = attendanceRepository.findBySectionIdWithStudent(sectionId);
        
        return attendances.stream()
            .map(AttendanceDto::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDto> getAttendanceBySectionAndDate(Long sectionId, LocalDate date) {
        if (!sectionRepository.existsById(sectionId)) {
            throw new ResourceNotFoundException("Section", "id", sectionId);
        }
        
        List<Attendance> attendances = attendanceRepository.findBySectionIdAndDateWithStudent(sectionId, date);
        
        return attendances.stream()
            .map(AttendanceDto::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteAttendance(Long id) {
        if (!attendanceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Attendance", "id", id);
        }
        
        attendanceRepository.deleteById(id);
        return true;
    }
}
