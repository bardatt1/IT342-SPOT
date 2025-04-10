package edu.cit.spot.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seat_plans")
@Data
@NoArgsConstructor
public class SeatPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "students"})
    private Course course;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String layoutJson;
    
    @Column(nullable = false)
    private Integer rows;
    
    @Column(nullable = false)
    private Integer columns;
    
    private boolean isActive = true;
}
