package edu.cit.spot.dto.auth;

public record JwtResponse(
    String accessToken,
    String tokenType,
    String userType,
    Long id,
    String email,
    String name,
    boolean googleLinked
) {
    public JwtResponse {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("Access token cannot be null or blank");
        }
        if (tokenType == null || tokenType.isBlank()) {
            tokenType = "Bearer";
        }
        if (userType == null || userType.isBlank()) {
            throw new IllegalArgumentException("User type cannot be null or blank");
        }
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
    }
}
