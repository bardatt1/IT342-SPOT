# IT342-SPOT Backend

## Overview
This is the backend API for the Student Position and Orientation Tracking (SPOT) system.

## Prerequisites
- Java 21 (Eclipse Temurin/Adoptium)
- Gradle
- PostgreSQL (for production) or H2 (for development)

## Development Setup

### 1. Database Setup
```bash
# Start PostgreSQL container
docker-compose up -d
```

### 2. Google OAuth Setup
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the Google+ API
4. Create OAuth 2.0 credentials
   - Authorized JavaScript origins:
     - http://localhost:5173 (Vite dev server)
     - http://localhost:8080 (Production)
   - Authorized redirect URIs:
     - http://localhost:8080/api/auth/google/callback
5. Copy the Client ID and Client Secret

### 3. Environment Setup
Create a `.env` file in the root directory:
```properties
JWT_SECRET=your-256-bit-secret-key
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GOOGLE_REDIRECT_URI=http://localhost:8080/api/auth/google/callback
```

### 4. Build and Run
```bash
# Run with development profile
.\gradlew.bat bootRun

```

The server will start at `http://localhost:8080`

## API Documentation
See [API.md](./docs/API.md) for detailed API documentation.

## Testing
```bash
# Run tests
mvn test
```