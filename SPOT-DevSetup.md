# SPOT Developer Setup Guide

**School Presence and Orientation Tracker**

## Table of Contents

1. [Introduction](#introduction)
2. [System Architecture](#system-architecture)
3. [Backend Setup](#backend-setup)
   - [Prerequisites](#backend-prerequisites)
   - [Database Setup](#database-setup)
   - [Configuration](#backend-configuration)
   - [Running the Backend](#running-the-backend)
4. [Frontend Web Setup](#frontend-web-setup)
   - [Prerequisites](#frontend-web-prerequisites)
   - [Configuration](#frontend-web-configuration)
   - [Running the Web Application](#running-the-web-application)
5. [Frontend Mobile Setup](#frontend-mobile-setup)
   - [Prerequisites](#frontend-mobile-prerequisites)
   - [Configuration](#frontend-mobile-configuration)
   - [Running the Mobile Application](#running-the-mobile-application)
6. [Development Workflow](#development-workflow)

## Introduction

This guide provides detailed instructions for setting up the development environment for the SPOT (School Presence and Orientation Tracker) system. The system comprises three main components:

1. A Spring Boot backend
2. A React TypeScript web frontend
3. A Kotlin Android mobile application

## System Architecture

### Tech Stack Overview

**Backend:**
- Java 21
- Spring Boot 3.2.3
- Spring Data JPA
- Spring Security with JWT
- MySQL Database
- Gradle build system

**Frontend Web:**
- TypeScript
- React 19.0.0
- React Router DOM 7.5.2
- Vite 6.3.1
- TailwindCSS 3.4.1
- Axios for API communication

**Frontend Mobile:**
- Kotlin
- Jetpack Compose
- Material 3 Design
- Retrofit for API communication
- Kotlin Coroutines

## Backend Setup

### Backend Prerequisites

- JDK 21 or higher
- MySQL 8.0 or higher
- Gradle 8.0 or higher
- An IDE (IntelliJ IDEA recommended)

### Database Setup

1. Install MySQL if not already installed.

2. Create a new MySQL database for the SPOT system:

   ```sql
   CREATE DATABASE spot_db;
   CREATE USER 'spot_user'@'localhost' IDENTIFIED BY 'your_password';
   GRANT ALL PRIVILEGES ON spot_db.* TO 'spot_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

### Backend Configuration

1. Clone the repository:

   ```bash
   git clone https://github.com/your-organization/IT342-SPOT.git
   cd IT342-SPOT/backend
   ```

2. Configure the application properties:

   Create or edit `src/main/resources/application.yml` with the following configuration:

   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/spot_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
       username: spot_user
       password: your_password
     jpa:
       hibernate:
         ddl-auto: update
       show-sql: true
       properties:
         hibernate:
           dialect: org.hibernate.dialect.MySQLDialect
           format_sql: true

   # JWT Configuration
   app:
     jwt:
       secret: your_jwt_secret_key_here_make_it_long_and_secure
       expiration-ms: 86400000  # 24 hours

   # Server configuration
   server:
     port: 8080
     servlet:
       context-path: /api
   ```

### Running the Backend

1. Build the application:

   ```bash
   ./gradlew build
   ```

2. Run the application:

   ```bash
   ./gradlew bootRun
   ```

3. Verify the application is running by accessing:
   - Swagger UI: http://localhost:8080/api/swagger-ui.html

## Frontend Web Setup

### Frontend Web Prerequisites

- Node.js 18.x or higher
- npm 9.x or higher

### Frontend Web Configuration

1. Navigate to the frontend web directory:

   ```bash
   cd IT342-SPOT/frontend_web
   ```

2. Install dependencies:

   ```bash
   npm install
   ```

3. Configure the API endpoint:

   Create or edit `.env` file in the root of the frontend_web directory:

   ```env
   VITE_API_URL=http://localhost:8080/api
   ```

### Running the Web Application

1. Start the development server:

   ```bash
   npm run dev
   ```

2. Access the web application:
   - Default URL: http://localhost:5173

## Frontend Mobile Setup

### Frontend Mobile Prerequisites

- Android Studio Iguana or higher
- Android SDK 24 or higher
- Kotlin 1.9.0 or higher

### Frontend Mobile Configuration

1. Open Android Studio and select "Open an existing Android Studio project"

2. Navigate to `IT342-SPOT/frontend_mobile` and select it

3. Configure the API endpoint:

   Open `app/src/main/java/com/example/spot/network/RetrofitClient.kt` and update the `BASE_URL` value:

   ```kotlin
   private const val BASE_URL = "http://10.0.2.2:8080/api/"
   ```

   Note: `10.0.2.2` is the special IP for accessing the host machine's localhost from an Android emulator.

### Running the Mobile Application

1. Connect an Android device or start an emulator

2. Click the "Run" button in Android Studio

## Development Workflow

### Backend Development

1. Follow Spring Boot best practices for development:
   - Use appropriate layers (Controller, Service, Repository)
   - Use DTOs for data transfer between layers
   - Follow SOLID principles

2. API Documentation:
   - Document all API endpoints with Swagger annotations
   - Access Swagger UI at: http://localhost:8080/api/swagger-ui.html

3. Testing:
   - Write unit tests for services and controllers
   - Run tests with: `./gradlew test`

### Frontend Web Development

1. Component Structure:
   - Follow React best practices
   - Organize components by feature/page
   - Use TypeScript for type safety

2. Styling:
   - Use TailwindCSS utility classes
   - Follow the design system in `tailwind.config.js`

3. Testing:
   - Write unit tests with Jest and React Testing Library
   - Run tests with: `npm run test`

### Frontend Mobile Development

1. UI Development:
   - Use Jetpack Compose for UI components
   - Follow Material 3 design guidelines

2. Architecture:
   - Use MVVM (Model-View-ViewModel) architecture
   - Separate concerns between UI, logic, and data

3. Testing:
   - Write unit tests for ViewModels and repositories
   - Run tests from Android Studio

### API Integration

All components communicate through the backend API. Key endpoints include:

- Authentication: `/api/auth/*`
- User Management: `/api/admin/*`, `/api/students/*`, `/api/teachers/*`
- Course Management: `/api/courses/*`, `/api/sections/*`
- Attendance: `/api/attendance/*`
- Analytics: `/api/analytics/*`

---

## Troubleshooting

### Common Backend Issues

- **Database Connection Problems**: Verify MySQL is running and credentials are correct
- **Port Conflicts**: Change port in `application.yml` if 8080 is in use

### Common Frontend Web Issues

- **Module Not Found Errors**: Run `npm install` to ensure all dependencies are installed
- **API Connection Errors**: Verify backend is running and API URL is correctly set in `.env`

### Common Frontend Mobile Issues

- **Build Failures**: Update Gradle and Android SDK versions
- **API Connection Errors**: Verify the correct IP address in `RetrofitClient.kt`

---

*Last Updated: May 10, 2025*
