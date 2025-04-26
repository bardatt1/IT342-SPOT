# SPOT - School Presence and Orientation Tracker

A comprehensive Spring Boot application for tracking student attendance at educational institutions, featuring Google OAuth integration, QR code-based attendance logging, and JWT authentication. SPOT provides a modern, secure, and efficient solution for managing student presence and classroom seating arrangements.

## Features

- Admin provisioning of student and teacher accounts
- Course and section management
- Teacher and student Google OAuth binding
- Enrollment via enrollment key
- Seat selection and teacher seat override
- QR code-based attendance logging
- Attendance logs and analytics for students and teachers

## Technical Stack

- **Spring Boot 3.x** with **Java 21**
- **Spring Data JPA** for database operations
- **Spring Security** with JWT and OAuth2
- **MySQL** for data persistence
- **Lombok** for reducing boilerplate code
- **Swagger/OpenAPI** for API documentation
- **Google ZXing** for QR code generation

## Project Structure

The project follows a clean architecture design:

- `entity`: JPA entities representing database tables
- `repository`: JPA repositories for database operations
- `service`: Service interfaces defining business operations
- `service.impl`: Service implementations containing business logic
- `controller`: REST controllers handling HTTP requests
- `dto`: Data Transfer Objects for data exchange
- `config`: Configuration classes for Spring Boot
- `security`: Security-related classes for JWT and OAuth
- `exception`: Exception handling classes

## Setup Instructions

1. **Clone the repository**

2. **Configure the application**
   
   Edit `src/main/resources/application.yml` to set up:
   - Database connection details
   - JWT secret key
   - Google OAuth credentials

3. **Create the MySQL database**

   ```sql
   CREATE DATABASE attendance_db;
   ```

4. **Build and run the application**

   ```bash
   ./gradlew bootRun
   ```

5. **Access the API documentation**

   Navigate to `http://localhost:8080/swagger-ui.html`

## API Endpoints

The application exposes the following main API endpoints:

- `/api/auth`: Authentication endpoints (login, OAuth)
- `/api/admin`: Admin management endpoints
- `/api/students`: Student management endpoints
- `/api/teachers`: Teacher management endpoints
- `/api/courses`: Course management endpoints
- `/api/sections`: Section management endpoints
- `/api/attendance`: Attendance logging and retrieval endpoints
- `/api/analytics`: Analytics and reporting endpoints
- `/api/seats`: Seat management endpoints

## API Response Format

All API endpoints follow a consistent response format using the `ApiResponse` wrapper class:

```json
{
  "result": "SUCCESS" | "ERROR",
  "message": "Human-readable message about the result",
  "data": { ... } | null
}
```

## Error Codes and Responses

The SPOT system uses standard HTTP status codes along with descriptive error messages:

| Status Code | Error Type | Description | Example |
|-------------|------------|-------------|---------|
| 400 | Bad Request | Validation errors, malformed request | `{"result":"ERROR","message":"Invalid input parameters: Student ID must be a positive number","data":null}` |
| 401 | Unauthorized | Authentication required | `{"result":"ERROR","message":"Authentication required. Please login or provide a valid JWT token","data":null}` |
| 403 | Forbidden | Insufficient permissions | `{"result":"ERROR","message":"You do not have permission to access this section's attendance data","data":null}` |
| 404 | Not Found | Resource not found | `{"result":"ERROR","message":"Student with ID 24601 not found in the system","data":null}` |
| 409 | Conflict | Resource conflict | `{"result":"ERROR","message":"Student is already enrolled in this section","data":null}` |
| 422 | Unprocessable Entity | Valid request but cannot be processed | `{"result":"ERROR","message":"Cannot mark attendance for a future date","data":null}` |
| 500 | Internal Server Error | Unexpected server error | `{"result":"ERROR","message":"An unexpected error occurred while processing your request","data":null}` |
- `/api/schedules`: Schedule management endpoints
- `/api/seats`: Seat management endpoints
- `/api/enrollments`: Enrollment management endpoints
- `/api/attendance`: Attendance logging endpoints
- `/api/analytics`: Attendance analytics endpoints

For detailed API documentation, please refer to the Swagger UI.

## Security

- JWT authentication for all API endpoints
- Google OAuth integration for account binding
- Role-based access control for endpoints
- Password encryption with BCrypt

## License

This project is licensed under the MIT License - see the LICENSE file for details.
