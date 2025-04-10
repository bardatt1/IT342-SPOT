package edu.cit.spot.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seat_assignments")
@Data
@NoArgsConstructor
public class SeatAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_plan_id", nullable = false)
    private SeatPlan seatPlan;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    
    @Column(nullable = false)
    private Integer rowIndex;
    
    @Column(nullable = false)
    private Integer columnIndex;
}
