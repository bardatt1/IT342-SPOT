package edu.cit.spot.dto.admin;

import edu.cit.spot.entity.Admin;

public record AdminDto(
    Long id,
    String firstName,
    String middleName,
    String lastName,
    String email,
    boolean systemAdmin
) {
    public AdminDto {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be blank");
        }
    }
    
    public static AdminDto fromEntity(Admin admin) {
        return new AdminDto(
            admin.getId(),
            admin.getFirstName(),
            admin.getMiddleName(),
            admin.getLastName(),
            admin.getEmail(),
            admin.isSystemAdmin()
        );
    }
}
