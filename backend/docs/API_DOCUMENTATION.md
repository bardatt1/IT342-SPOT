# SPOT API Documentation Guide

## Overview

This document provides detailed information about the SPOT (School Presence and Orientation Tracker) REST API. This guide is intended for developers integrating with the SPOT system or extending its functionality.

## Base URL

For local development:
```
http://localhost:8080
```

## Authentication

SPOT uses JWT (JSON Web Token) for API authentication.

### Obtaining a Token

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "user@school.edu",
  "password": "password"
}
```

**Successful Response:**
```json
{
  "result": "SUCCESS",
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": 1,
      "username": "user@school.edu",
      "role": "TEACHER"
    }
  }
}
```

### Using the Token

Include the token in the Authorization header for all authenticated requests:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Error Handling

All API errors follow a standard format:

```json
{
  "result": "ERROR",
  "message": "Descriptive error message",
  "data": null
}
```

For validation errors, the data field may contain additional details:

```json
{
  "result": "ERROR",
  "message": "Validation failed",
  "data": {
    "errors": [
      {
        "field": "email",
        "message": "Invalid email format"
      }
    ]
  }
}
```

## Common Status Codes

| Status | Description |
|--------|-------------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request - Invalid input |
| 401 | Unauthorized - Authentication required |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource does not exist |
| 409 | Conflict - Resource conflict |
| 422 | Unprocessable Entity - Valid request but cannot be processed |
| 500 | Internal Server Error |

## Student API

### Get Student by ID

```http
GET /api/students/{id}
Authorization: Bearer {token}
```

**Successful Response:**
```json
{
  "result": "SUCCESS",
  "message": "Student retrieved successfully",
  "data": {
    "id": 24601,
    "name": "Jane Smith",
    "email": "jane.smith@students.spot.edu",
    "studentId": "ST24601",
    "googleEmail": "jane.smith@gmail.com",
    "enrollmentYear": 2024,
    "active": true
  }
}
```

**Error Responses:**

- Student not found:
```json
{
  "result": "ERROR",
  "message": "Student with ID 24601 not found in the system",
  "data": null
}
```

- Unauthorized access:
```json
{
  "result": "ERROR",
  "message": "You do not have permission to view this student's information",
  "data": null
}
```

### Bind Google Account

```http
PUT /api/students/{id}/google-account
Authorization: Bearer {token}
Content-Type: application/json

{
  "googleEmail": "jane.smith@gmail.com"
}
```

**Successful Response:**
```json
{
  "result": "SUCCESS",
  "message": "Google account successfully bound to student profile",
  "data": {
    "id": 24601,
    "googleEmail": "jane.smith@gmail.com"
  }
}
```

## Teacher API

### Get Teacher by ID

```http
GET /api/teachers/{id}
Authorization: Bearer {token}
```

**Successful Response:**
```json
{
  "result": "SUCCESS",
  "message": "Teacher retrieved successfully",
  "data": {
    "id": 12345,
    "name": "Professor Johnson",
    "email": "johnson@faculty.spot.edu",
    "teacherId": "TC12345",
    "googleEmail": "johnson@gmail.com",
    "department": "Computer Science",
    "active": true
  }
}
```

## Attendance API

### Generate Attendance QR Code

```http
POST /api/attendance/generate-qr
Authorization: Bearer {token}
Content-Type: application/json

{
  "sectionId": 123,
  "expirationMinutes": 15
}
```

**Successful Response:**
```json
{
  "result": "SUCCESS",
  "message": "QR code generated successfully",
  "data": {
    "qrCodeId": "abc123",
    "qrCodeImageUrl": "/api/attendance/qr-image/abc123",
    "expiresAt": "2023-10-20T14:30:00Z"
  }
}
```

### Log Attendance via QR Code

```http
POST /api/attendance/log
Authorization: Bearer {token}
Content-Type: application/json

{
  "qrCodeId": "abc123",
  "studentId": 24601
}
```

**Successful Response:**
```json
{
  "result": "SUCCESS",
  "message": "Attendance recorded successfully",
  "data": {
    "attendanceId": 87654,
    "studentId": 24601,
    "sectionId": 123,
    "timestamp": "2023-10-20T14:22:30Z",
    "status": "PRESENT"
  }
}
```

**Error Responses:**

- Expired QR Code:
```json
{
  "result": "ERROR",
  "message": "This attendance QR code has expired. Please request a new code from your teacher.",
  "data": null
}
```

- Already Logged:
```json
{
  "result": "ERROR",
  "message": "Attendance already logged for this student in this section today",
  "data": null
}
```

## Course and Section API

### List Sections by Course

```http
GET /api/courses/{courseId}/sections
Authorization: Bearer {token}
```

**Successful Response:**
```json
{
  "result": "SUCCESS",
  "message": "Sections retrieved successfully",
  "data": [
    {
      "id": 123,
      "name": "CS101-A",
      "enrollmentKey": "spring2023cs101a",
      "schedule": "MWF 10:00 AM - 11:30 AM",
      "room": "Science Building 305",
      "enrollmentOpen": true,
      "teacherId": 12345,
      "enrollmentCount": 28,
      "maxEnrollment": 35
    },
    {
      "id": 124,
      "name": "CS101-B",
      "enrollmentKey": "spring2023cs101b",
      "schedule": "TTh 1:00 PM - 2:30 PM",
      "room": "Science Building 305",
      "enrollmentOpen": false,
      "teacherId": 12345,
      "enrollmentCount": 35,
      "maxEnrollment": 35
    }
  ]
}
```

## Enrollment API

### Enroll in Section

```http
POST /api/enrollments
Authorization: Bearer {token}
Content-Type: application/json

{
  "studentId": 24601,
  "sectionId": 123,
  "enrollmentKey": "spring2023cs101a"
}
```

**Successful Response:**
```json
{
  "result": "SUCCESS",
  "message": "Successfully enrolled in CS101-A",
  "data": {
    "enrollmentId": 9876,
    "studentId": 24601,
    "sectionId": 123,
    "enrollmentDate": "2023-09-05T09:22:30Z",
    "status": "ACTIVE"
  }
}
```

**Error Responses:**

- Invalid Enrollment Key:
```json
{
  "result": "ERROR",
  "message": "Invalid enrollment key for the specified section",
  "data": null
}
```

- Section Full:
```json
{
  "result": "ERROR",
  "message": "Section has reached maximum enrollment capacity",
  "data": null
}
```

- Enrollment Closed:
```json
{
  "result": "ERROR",
  "message": "Enrollment for this section is currently closed",
  "data": null
}
```

## Seat Management API

### Select a Seat

```http
POST /api/seats/select
Authorization: Bearer {token}
Content-Type: application/json

{
  "studentId": 24601,
  "sectionId": 123,
  "seatRow": "B",
  "seatColumn": 5
}
```

**Successful Response:**
```json
{
  "result": "SUCCESS",
  "message": "Seat B5 successfully selected",
  "data": {
    "seatId": 456,
    "studentId": 24601,
    "sectionId": 123,
    "seatRow": "B",
    "seatColumn": 5,
    "assignedDate": "2023-09-05T10:15:30Z"
  }
}
```

**Error Responses:**

- Seat Already Taken:
```json
{
  "result": "ERROR",
  "message": "Seat B5 is already assigned to another student",
  "data": null
}
```

- Invalid Seat:
```json
{
  "result": "ERROR",
  "message": "The selected seat does not exist in the classroom layout",
  "data": null
}
```

## Analytics API

### Get Attendance Statistics

```http
GET /api/analytics/attendance?sectionId=123&startDate=2023-09-01&endDate=2023-12-15
Authorization: Bearer {token}
```

**Successful Response:**
```json
{
  "result": "SUCCESS",
  "message": "Attendance statistics retrieved successfully",
  "data": {
    "sectionId": 123,
    "sectionName": "CS101-A",
    "totalClasses": 42,
    "averageAttendanceRate": 89.5,
    "attendanceByDate": [
      {
        "date": "2023-09-05",
        "presentCount": 32,
        "absentCount": 3,
        "attendanceRate": 91.4
      },
      // Additional dates...
    ],
    "attendanceByStudent": [
      {
        "studentId": 24601,
        "studentName": "Jane Smith",
        "presentCount": 40,
        "absentCount": 2,
        "attendanceRate": 95.2
      },
      // Additional students...
    ]
  }
}
```

## Pagination

Many endpoints that return collections support pagination:

```http
GET /api/students?page=0&size=10
```

- `page`: Zero-based page index (default: 0)
- `size`: Page size (default: 20)

Response format for paginated results:

```json
{
  "result": "SUCCESS",
  "message": "Students retrieved successfully",
  "data": {
    "content": [
      // array of items
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": {
        "sorted": true,
        "unsorted": false
      }
    },
    "totalElements": 135,
    "totalPages": 14,
    "last": false,
    "first": true,
    "size": 10,
    "number": 0,
    "numberOfElements": 10,
    "empty": false
  }
}
```

## Rate Limiting

To ensure system stability, the SPOT API implements rate limiting:

- 60 requests per minute for authenticated users
- 10 requests per minute for unauthenticated requests

When rate limits are exceeded, the API will return:

```
Status: 429 Too Many Requests
```

```json
{
  "result": "ERROR",
  "message": "Rate limit exceeded. Please try again later.",
  "data": null
}
```

## Additional Resources

For complete API specifications and interactive documentation, visit the Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```
