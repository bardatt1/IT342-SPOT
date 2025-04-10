package edu.cit.spot.service;

import edu.cit.spot.entity.Session;

import java.time.LocalDateTime;
import java.util.List;

public interface SessionService {
    // Original methods
    List<Session> getAllSessions();
    Session getSessionById(Long id);
    List<Session> getSessionsByCourseId(Long courseId);
    List<Session> getActiveSessions(Long courseId);
    Session createSession(Long courseId, Session session);
    Session updateSession(Long id, Session session);
    void deleteSession(Long id);
    Session startSession(Long id);
    Session endSession(Long id);
    Session cancelSession(Long id);
    
    // Additional methods needed by controllers
    List<Session> getSessionsByCourse(Long courseId);
    Session createSession(Session session);
    Session updateSession(Session session);
    List<Session> getUpcomingSessionsByTeacher(Long teacherId, LocalDateTime currentTime);
    List<Session> getActiveSessionsForStudent(Long studentId);
}
