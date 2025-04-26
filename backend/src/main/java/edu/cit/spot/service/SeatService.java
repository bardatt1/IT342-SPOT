package edu.cit.spot.service;

import edu.cit.spot.dto.seat.PickSeatRequest;
import edu.cit.spot.dto.seat.SeatDto;
import edu.cit.spot.dto.seat.OverrideSeatRequest;

import java.util.List;

public interface SeatService {
    
    SeatDto pickSeat(PickSeatRequest request);
    
    SeatDto overrideSeat(OverrideSeatRequest request);
    
    List<SeatDto> getSeatsBySectionId(Long sectionId);
    
    SeatDto getSeatByStudentAndSectionId(Long studentId, Long sectionId);
    
    boolean deleteSeat(Long id);
    
    boolean deleteSeatsBySection(Long sectionId);
}
