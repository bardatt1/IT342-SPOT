-- Drop tables if they exist (in reverse order of dependencies)
DROP TABLE IF EXISTS attendance;
DROP TABLE IF EXISTS qr_codes;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS seat_assignments;
DROP TABLE IF EXISTS sessions;
DROP TABLE IF EXISTS seat_plans;
DROP TABLE IF EXISTS course_enrollment;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(191) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    platform_type VARCHAR(20) NOT NULL,
    google_id VARCHAR(100),
    profile_picture VARCHAR(255),
    enabled BOOLEAN DEFAULT TRUE,
    active BOOLEAN DEFAULT TRUE,
    phone_number VARCHAR(20),
    INDEX idx_user_email (email),
    INDEX idx_user_role (role)
);

-- Create courses table
CREATE TABLE courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(191) NOT NULL,
    course_code VARCHAR(30) NOT NULL,
    description TEXT,
    schedule VARCHAR(100),
    room VARCHAR(50),
    teacher_id BIGINT NOT NULL,
    FOREIGN KEY (teacher_id) REFERENCES users(id),
    INDEX idx_course_teacher (teacher_id),
    INDEX idx_course_code (course_code)
);

-- Create course_enrollment table for many-to-many relationship
CREATE TABLE course_enrollment (
    course_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    PRIMARY KEY (course_id, student_id),
    FOREIGN KEY (course_id) REFERENCES courses(id),
    FOREIGN KEY (student_id) REFERENCES users(id)
);

-- Create seat_plans table
CREATE TABLE seat_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    name VARCHAR(191) NOT NULL,
    layout_json TEXT,
    `rows` INT NOT NULL,
    `columns` INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- Create sessions table
CREATE TABLE sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME,
    title VARCHAR(191),
    description TEXT,
    status VARCHAR(20) NOT NULL,
    active BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (course_id) REFERENCES courses(id),
    INDEX idx_session_course (course_id),
    INDEX idx_session_time (start_time, end_time),
    INDEX idx_session_status (status)
);

-- Create seat_assignments table
CREATE TABLE seat_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seat_plan_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    row_index INT NOT NULL,
    column_index INT NOT NULL,
    FOREIGN KEY (seat_plan_id) REFERENCES seat_plans(id),
    FOREIGN KEY (student_id) REFERENCES users(id)
);

-- Create notifications table
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(191) NOT NULL,
    message TEXT NOT NULL,
    created_at DATETIME NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    type VARCHAR(20) NOT NULL,
    link VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create qr_codes table
CREATE TABLE qr_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    session_id BIGINT NOT NULL,
    generated_at DATETIME NOT NULL,
    expires_at DATETIME NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (session_id) REFERENCES sessions(id)
);

-- Create attendance table
CREATE TABLE attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    recorded_at DATETIME NOT NULL,
    notes TEXT,
    updated_at DATETIME,
    FOREIGN KEY (session_id) REFERENCES sessions(id),
    FOREIGN KEY (student_id) REFERENCES users(id),
    INDEX idx_attendance_session (session_id),
    INDEX idx_attendance_student (student_id),
    INDEX idx_attendance_status (status),
    UNIQUE INDEX idx_attendance_session_student (session_id, student_id)
);
