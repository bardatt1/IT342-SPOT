package edu.cit.spot.service.impl;

import edu.cit.spot.dto.analytics.AttendanceAnalyticsDto;
import edu.cit.spot.dto.analytics.DailyAttendanceDto;
import edu.cit.spot.dto.analytics.StudentAttendanceDto;
import edu.cit.spot.entity.Attendance;
import edu.cit.spot.entity.Enrollment;
import edu.cit.spot.entity.Schedule;
import edu.cit.spot.entity.Section;
import edu.cit.spot.entity.Student;
import edu.cit.spot.exception.ResourceNotFoundException;
import edu.cit.spot.repository.AttendanceRepository;
import edu.cit.spot.repository.EnrollmentRepository;
import edu.cit.spot.repository.ScheduleRepository;
import edu.cit.spot.repository.SectionRepository;
import edu.cit.spot.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @Autowired
    private SectionRepository sectionRepository;
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private ScheduleRepository scheduleRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Override
    public AttendanceAnalyticsDto getSectionAnalytics(Long sectionId) {
        // Get the first and last attendance dates for the section
        Section section = sectionRepository.findByIdWithCourseAndTeacher(sectionId)
            .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));
        
        List<Attendance> allAttendances = attendanceRepository.findBySectionIdWithStudent(sectionId);
        
        if (allAttendances.isEmpty()) {
            return createEmptyAnalytics(section);
        }
        
        // Find the earliest and latest attendance dates
        LocalDate firstAttendance = allAttendances.stream()
            .map(Attendance::getDate)
            .min(LocalDate::compareTo)
            .orElse(LocalDate.now());
            
        LocalDate lastAttendance = allAttendances.stream()
            .map(Attendance::getDate)
            .max(LocalDate::compareTo)
            .orElse(LocalDate.now());
        
        return getSectionAnalyticsForDateRange(sectionId, firstAttendance, lastAttendance);
    }

    @Override
    public AttendanceAnalyticsDto getSectionAnalyticsForDateRange(Long sectionId, LocalDate startDate, LocalDate endDate) {
        Section section = sectionRepository.findByIdWithCourseAndTeacher(sectionId)
            .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));
        
        // Get all enrolled students
        List<Enrollment> enrollments = enrollmentRepository.findBySectionIdWithStudents(sectionId);
        int totalEnrolledStudents = enrollments.size();
        
        if (totalEnrolledStudents == 0) {
            return createEmptyAnalytics(section);
        }
        
        // Get all class days within the date range based on schedule
        List<Schedule> schedules = scheduleRepository.findBySectionId(sectionId);
        Set<Integer> classDaysOfWeek = schedules.stream()
            .map(Schedule::getDayOfWeek)
            .collect(Collectors.toSet());
        
        List<LocalDate> classDates = getClassDatesInRange(startDate, endDate, classDaysOfWeek);
        int totalClassDays = classDates.size();
        
        if (totalClassDays == 0) {
            return createEmptyAnalytics(section);
        }
        
        // Get all attendances for the section in the date range
        List<Attendance> attendances = attendanceRepository.findBySectionIdWithStudent(sectionId).stream()
            .filter(a -> !a.getDate().isBefore(startDate) && !a.getDate().isAfter(endDate))
            .collect(Collectors.toList());
        
        // Calculate attendance by date
        Map<LocalDate, List<Attendance>> attendancesByDate = attendances.stream()
            .collect(Collectors.groupingBy(Attendance::getDate));
        
        Map<LocalDate, Integer> attendanceCountByDate = new HashMap<>();
        List<DailyAttendanceDto> dailyAttendances = new ArrayList<>();
        
        double totalAttendanceRate = 0.0;
        
        for (LocalDate classDate : classDates) {
            List<Attendance> dailyAttendance = attendancesByDate.getOrDefault(classDate, Collections.emptyList());
            int presentCount = dailyAttendance.size();
            
            attendanceCountByDate.put(classDate, presentCount);
            
            double attendanceRate = (double) presentCount / totalEnrolledStudents * 100.0;
            totalAttendanceRate += attendanceRate;
            
            dailyAttendances.add(new DailyAttendanceDto(
                classDate,
                presentCount,
                totalEnrolledStudents,
                attendanceRate
            ));
        }
        
        // Calculate average attendance rate
        double averageAttendanceRate = totalClassDays > 0 ? totalAttendanceRate / totalClassDays : 0.0;
        
        // Get the first schedule's room for analytics if available
        String roomInfo = "No Room";
        List<Schedule> sectionSchedules = scheduleRepository.findBySectionId(sectionId);
        if (!sectionSchedules.isEmpty()) {
            roomInfo = sectionSchedules.get(0).getRoom(); // Using the first schedule's room for simplicity
        }
        
        return new AttendanceAnalyticsDto(
            sectionId,
            section.getCourse().getCourseName(),
            roomInfo,
            totalEnrolledStudents,
            averageAttendanceRate,
            totalClassDays,
            attendanceCountByDate,
            dailyAttendances
        );
    }

    @Override
    public List<StudentAttendanceDto> getStudentAttendanceStats(Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
            .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));
        
        List<Enrollment> enrollments = enrollmentRepository.findBySectionIdWithStudents(sectionId);
        
        return enrollments.stream()
            .map(enrollment -> getStudentAttendanceStatsInSection(sectionId, enrollment.getStudent().getId()))
            .collect(Collectors.toList());
    }

    @Override
    public StudentAttendanceDto getStudentAttendanceStatsInSection(Long sectionId, Long studentId) {
        Section section = sectionRepository.findById(sectionId)
            .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));
        
        // Verify student exists and is enrolled
        Enrollment enrollment = enrollmentRepository.findBySectionIdAndStudentId(sectionId, studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student is not enrolled in this section"));
        
        Student student = enrollment.getStudent();
        
        // Get class schedule
        List<Schedule> schedules = scheduleRepository.findBySectionId(sectionId);
        Set<Integer> classDaysOfWeek = schedules.stream()
            .map(Schedule::getDayOfWeek)
            .collect(Collectors.toSet());
        
        // Determine the date range from the first enrollment to present
        LocalDate enrollmentDate = enrollment.getEnrolledAt().toLocalDate();
        LocalDate currentDate = LocalDate.now();
        
        List<LocalDate> classDates = getClassDatesInRange(enrollmentDate, currentDate, classDaysOfWeek);
        int totalClassDays = classDates.size();
        
        // Get all attendances for this student in this section
        List<Attendance> attendances = attendanceRepository.findBySectionIdWithStudent(sectionId).stream()
                .filter(a -> a.getStudent().getId().equals(studentId))
                .collect(Collectors.toList());
        
        Set<LocalDate> attendanceDates = attendances.stream()
            .map(Attendance::getDate)
            .collect(Collectors.toSet());
        
        int daysPresent = (int) classDates.stream()
            .filter(attendanceDates::contains)
            .count();
        
        double attendanceRate = totalClassDays > 0 
            ? (double) daysPresent / totalClassDays * 100.0 
            : 0.0;
        
        // Create attendance by date map
        Map<LocalDate, Boolean> attendanceByDate = classDates.stream()
            .collect(Collectors.toMap(
                date -> date,
                date -> attendanceDates.contains(date),
                (existing, replacement) -> existing,
                TreeMap::new
            ));
        
        return new StudentAttendanceDto(
            studentId,
            student.getFirstName() + " " + student.getLastName(),
            totalClassDays,
            daysPresent,
            attendanceRate,
            attendanceByDate
        );
    }
    
    private AttendanceAnalyticsDto createEmptyAnalytics(Section section) {
        // Get the first schedule's room for analytics if available
        String roomInfo = "No Room";
        List<Schedule> sectionSchedules = scheduleRepository.findBySectionId(section.getId());
        if (!sectionSchedules.isEmpty()) {
            roomInfo = sectionSchedules.get(0).getRoom(); // Using the first schedule's room for simplicity
        }
        
        return new AttendanceAnalyticsDto(
            section.getId(),
            section.getCourse().getCourseName(),
            roomInfo,
            0,
            0.0,
            0,
            Collections.emptyMap(),
            Collections.emptyList()
        );
    }
    
    private List<LocalDate> getClassDatesInRange(LocalDate startDate, LocalDate endDate, Set<Integer> classDaysOfWeek) {
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        
        return Stream.iterate(startDate, date -> date.plusDays(1))
            .limit(daysBetween)
            .filter(date -> classDaysOfWeek.contains(date.getDayOfWeek().getValue()))
            .collect(Collectors.toList());
    }
}
