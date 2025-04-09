package edu.cit.spot.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class GoogleAuthRequest {
    @NotBlank
    private String idToken;

    @NotBlank
    @Pattern(regexp = "^(WEB|MOBILE)$", message = "Platform type must be either WEB or MOBILE")
    private String platformType;
}
