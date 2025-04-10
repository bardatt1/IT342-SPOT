package edu.cit.spot.auth;

import edu.cit.spot.entity.User;

public class SignupResponse {
    private String message;
    private String email;
    private String token;
    private UserDTO user;
    
    // For backward compatibility
    public SignupResponse(String message, String email) {
        this.message = message;
        this.email = email;
    }
    
    // Constructor that provides token and user info to match frontend expectations
    public SignupResponse(String message, String email, String token, User user) {
        this.message = message;
        this.email = email;
        this.token = token;
        this.user = user != null ? new UserDTO(user) : null;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public UserDTO getUser() {
        return user;
    }
    
    public void setUser(UserDTO user) {
        this.user = user;
    }
    
    // User DTO to match frontend expectations
    public static class UserDTO {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
        
        public UserDTO() {}
        
        public UserDTO(User user) {
            this.id = user.getId();
            this.email = user.getEmail();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.role = user.getRole().name();
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}
