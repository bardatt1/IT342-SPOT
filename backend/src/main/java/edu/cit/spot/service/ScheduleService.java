package edu.cit.spot.service;

import edu.cit.spot.dto.schedule.CreateScheduleRequest;
import edu.cit.spot.dto.schedule.ScheduleDto;
import edu.cit.spot.dto.schedule.ScheduleUpdateRequest;

import java.util.List;

public interface ScheduleService {
    
    ScheduleDto createSchedule(CreateScheduleRequest request);
    
    ScheduleDto getScheduleById(Long id);
    
    List<ScheduleDto> getSchedulesBySectionId(Long sectionId);
    
    ScheduleDto updateSchedule(Long id, ScheduleUpdateRequest request);
    
    boolean deleteSchedule(Long id);
}
