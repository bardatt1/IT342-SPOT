package edu.cit.spot.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SignupRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Pattern(regexp = "^(TEACHER|STUDENT)$", message = "Role must be either TEACHER or STUDENT")
    private String role;

    @NotBlank
    @Pattern(regexp = "^(WEB|MOBILE)$", message = "Platform type must be either WEB or MOBILE")
    private String platformType;
}
