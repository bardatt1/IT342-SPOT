package edu.cit.spot.service;

import edu.cit.spot.dto.attendance.AttendanceDto;
import edu.cit.spot.dto.attendance.LogAttendanceRequest;
import edu.cit.spot.dto.attendance.QRCodeResponse;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    
    /**
     * Log attendance for the current authenticated student
     * 
     * @param request The request containing QR code data
     * @return The logged attendance record
     */
    AttendanceDto logAttendance(LogAttendanceRequest request);
    
    /**
     * Generate QR code for the current authenticated teacher's section
     * 
     * @param sectionId The ID of the section to generate QR code for
     * @return The generated QR code response
     */
    QRCodeResponse generateQRCode(Long sectionId);
    
    List<AttendanceDto> getAttendanceByStudentId(Long studentId);
    
    List<AttendanceDto> getAttendanceBySectionId(Long sectionId);
    
    List<AttendanceDto> getAttendanceBySectionAndDate(Long sectionId, LocalDate date);
    
    boolean deleteAttendance(Long id);
}
