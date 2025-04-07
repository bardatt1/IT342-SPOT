# SPOT API Documentation

## Authentication Endpoints

### Register a new user
```http
POST /api/auth/signup
Content-Type: application/json

{
  "email": "string",
  "password": "string",
  "firstName": "string",
  "lastName": "string",
  "role": "TEACHER" | "STUDENT",
  "platformType": "WEB" | "MOBILE"
}
```

### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "string",
  "password": "string",
  "platformType": "WEB" | "MOBILE"
}
```

### Google OAuth Login/Signup
```http
POST /api/auth/google
Content-Type: application/x-www-form-urlencoded

idToken=string&platformType=WEB|MOBILE
```

### Get User Profile
```http
GET /api/auth/profile
Authorization: Bearer <token>
```

### Logout
```http
POST /api/auth/logout
Authorization: Bearer <token>
```

## Platform-Specific Rules

### Web Platform
- Only allows teacher registration and login
- Teachers must use web platform for administrative tasks

### Mobile Platform
- Allows both student and teacher registration
- Only allows student login
- Teachers are redirected to web platform

## Response Format

### Success Response
```json
{
  "token": "string",
  "email": "string",
  "firstName": "string",
  "lastName": "string",
  "role": "TEACHER" | "STUDENT",
  "profilePicture": "string"
}
```

### Error Response
```json
{
  "error": "string"
}
```

## Authentication
All protected endpoints require a valid JWT token in the Authorization header:
```http
Authorization: Bearer <token>
```
