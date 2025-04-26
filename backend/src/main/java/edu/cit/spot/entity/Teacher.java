package edu.cit.spot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "teachers")
public class Teacher {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 50)
    private String firstName;
    
    @Size(max = 50)
    private String middleName;
    
    @NotBlank
    @Size(max = 50)
    private String lastName;
    
    @NotBlank
    @Email
    @Size(max = 100)
    @Column(unique = true)
    private String email;
    
    @NotBlank
    @Size(max = 50)
    private String teacherPhysicalId;
    
    @NotBlank
    @Size(max = 100)
    private String password;
    
    private String googleId;
    
    private boolean googleLinked = false;
    
    @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY)
    private List<Section> sections = new ArrayList<>();
}
