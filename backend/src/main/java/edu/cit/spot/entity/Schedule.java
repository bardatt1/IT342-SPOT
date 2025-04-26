package edu.cit.spot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalTime;

@Entity
@Data
@Table(name = "schedules")
public class Schedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;
    
    @NotNull
    @Column(name = "day_of_week")
    private Integer dayOfWeek; // 1 = Monday, 7 = Sunday
    
    @NotNull
    @Column(name = "time_start")
    private LocalTime timeStart;
    
    @NotNull
    @Column(name = "time_end")
    private LocalTime timeEnd;
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "room")
    private String room;
    
    @NotBlank
    @Column(name = "schedule_type")
    private String scheduleType; // LEC, LAB
}
