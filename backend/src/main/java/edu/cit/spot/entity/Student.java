package edu.cit.spot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
@Table(name = "students")
public class Student {
    
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
    @Size(max = 10)
    private String year;
    
    @NotBlank
    @Size(max = 100)
    private String program;
    
    @NotBlank
    @Email
    @Size(max = 100)
    @Column(unique = true)
    private String email;
    
    @NotBlank
    @Size(max = 50)
    private String studentPhysicalId;
    
    @NotBlank
    @Size(max = 100)
    private String password;
    
    private String googleId;
    
    private boolean googleLinked = false;
}
