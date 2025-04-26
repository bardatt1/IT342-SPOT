package edu.cit.spot.service.impl;

import edu.cit.spot.dto.schedule.CreateScheduleRequest;
import edu.cit.spot.dto.schedule.ScheduleDto;
import edu.cit.spot.dto.schedule.ScheduleUpdateRequest;
import edu.cit.spot.entity.Schedule;
import edu.cit.spot.entity.Section;
import edu.cit.spot.exception.ResourceNotFoundException;
import edu.cit.spot.repository.ScheduleRepository;
import edu.cit.spot.repository.SectionRepository;
import edu.cit.spot.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;
    
    @Autowired
    private SectionRepository sectionRepository;

    @Override
    @Transactional
    public ScheduleDto createSchedule(CreateScheduleRequest request) {
        Section section = sectionRepository.findById(request.sectionId())
            .orElseThrow(() -> new ResourceNotFoundException("Section", "id", request.sectionId()));
        
        Schedule schedule = new Schedule();
        schedule.setSection(section);
        schedule.setDayOfWeek(request.dayOfWeek());
        schedule.setTimeStart(request.timeStart());
        schedule.setTimeEnd(request.timeEnd());
        schedule.setScheduleType(request.scheduleType());
        schedule.setRoom(request.room());
        
        Schedule savedSchedule = scheduleRepository.save(schedule);
        
        return ScheduleDto.fromEntity(savedSchedule);
    }

    @Override
    public ScheduleDto getScheduleById(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Schedule", "id", id));
        
        return ScheduleDto.fromEntity(schedule);
    }

    @Override
    public List<ScheduleDto> getSchedulesBySectionId(Long sectionId) {
        if (!sectionRepository.existsById(sectionId)) {
            throw new ResourceNotFoundException("Section", "id", sectionId);
        }
        
        List<Schedule> schedules = scheduleRepository.findBySectionId(sectionId);
        
        return schedules.stream()
            .map(ScheduleDto::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ScheduleDto updateSchedule(Long id, ScheduleUpdateRequest request) {
        Schedule schedule = scheduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Schedule", "id", id));
        
        if (request.dayOfWeek() != null) {
            schedule.setDayOfWeek(request.dayOfWeek());
        }
        
        if (request.timeStart() != null) {
            schedule.setTimeStart(request.timeStart());
        }
        
        if (request.timeEnd() != null) {
            schedule.setTimeEnd(request.timeEnd());
        }
        
        if (request.scheduleType() != null) {
            schedule.setScheduleType(request.scheduleType());
        }
        
        if (request.room() != null) {
            schedule.setRoom(request.room());
        }
        
        // Validate that start time is before end time
        if (schedule.getTimeStart().isAfter(schedule.getTimeEnd())) {
            throw new IllegalArgumentException("Start time cannot be after end time");
        }
        
        Schedule updatedSchedule = scheduleRepository.save(schedule);
        
        return ScheduleDto.fromEntity(updatedSchedule);
    }

    @Override
    @Transactional
    public boolean deleteSchedule(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Schedule", "id", id));
        
        scheduleRepository.delete(schedule);
        return true;
    }
}
