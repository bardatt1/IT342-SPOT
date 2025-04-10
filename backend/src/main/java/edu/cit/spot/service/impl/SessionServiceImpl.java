package edu.cit.spot.service.impl;

import edu.cit.spot.entity.Course;
import edu.cit.spot.entity.Session;
import edu.cit.spot.entity.Session.SessionStatus;
import edu.cit.spot.exception.ResourceNotFoundException;
import edu.cit.spot.repository.CourseRepository;
import edu.cit.spot.repository.SessionRepository;
import edu.cit.spot.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {
    private final SessionRepository sessionRepository;
    private final CourseRepository courseRepository;

    @Override
    public List<Session> getAllSessions() {
        return sessionRepository.findAll();
    }

    @Override
    public Session getSessionById(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));
    }

    @Override
    public List<Session> getSessionsByCourseId(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        return sessionRepository.findByCourse(course);
    }

    @Override
    public List<Session> getActiveSessions(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        return sessionRepository.findByCourseAndStatus(course, SessionStatus.ACTIVE);
    }

    @Override
    @Transactional
    public Session createSession(Long courseId, Session session) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        
        session.setCourse(course);
        
        // If no start time is set, use the current time
        if (session.getStartTime() == null) {
            session.setStartTime(LocalDateTime.now());
        }
        
        // Default to SCHEDULED status if not specified
        if (session.getStatus() == null) {
            session.setStatus(SessionStatus.SCHEDULED);
        }
        
        return sessionRepository.save(session);
    }

    @Override
    @Transactional
    public Session updateSession(Long id, Session sessionDetails) {
        Session session = getSessionById(id);
        
        // Only allow updates to non-completed sessions
        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new IllegalStateException("Cannot update a completed session");
        }
        
        if (sessionDetails.getTitle() != null) {
            session.setTitle(sessionDetails.getTitle());
        }
        
        if (sessionDetails.getDescription() != null) {
            session.setDescription(sessionDetails.getDescription());
        }
        
        if (sessionDetails.getStartTime() != null) {
            session.setStartTime(sessionDetails.getStartTime());
        }
        
        if (sessionDetails.getEndTime() != null) {
            session.setEndTime(sessionDetails.getEndTime());
        }
        
        return sessionRepository.save(session);
    }

    @Override
    @Transactional
    public void deleteSession(Long id) {
        Session session = getSessionById(id);
        sessionRepository.delete(session);
    }

    @Override
    @Transactional
    public Session startSession(Long id) {
        Session session = getSessionById(id);
        
        if (session.getStatus() != SessionStatus.SCHEDULED && session.getStatus() != SessionStatus.CANCELLED) {
            throw new IllegalStateException("Cannot start a session with status: " + session.getStatus());
        }
        
        session.setStatus(SessionStatus.ACTIVE);
        session.setStartTime(LocalDateTime.now());
        
        return sessionRepository.save(session);
    }

    @Override
    @Transactional
    public Session endSession(Long id) {
        Session session = getSessionById(id);
        
        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new IllegalStateException("Cannot end a session with status: " + session.getStatus());
        }
        
        session.setStatus(SessionStatus.COMPLETED);
        session.setEndTime(LocalDateTime.now());
        
        return sessionRepository.save(session);
    }

    @Override
    @Transactional
    public Session cancelSession(Long id) {
        Session session = getSessionById(id);
        
        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed session");
        }
        
        session.setStatus(SessionStatus.CANCELLED);
        
        return sessionRepository.save(session);
    }
    
    // Additional methods needed by controllers
    
    @Override
    public List<Session> getSessionsByCourse(Long courseId) {
        return sessionRepository.findByCourseId(courseId);
    }
    
    @Override
    @Transactional
    public Session createSession(Session session) {
        // If no start time is set, use the current time
        if (session.getStartTime() == null) {
            session.setStartTime(LocalDateTime.now());
        }
        
        // Default to SCHEDULED status if not specified
        if (session.getStatus() == null) {
            session.setStatus(SessionStatus.SCHEDULED);
        }
        
        return sessionRepository.save(session);
    }
    
    @Override
    @Transactional
    public Session updateSession(Session session) {
        // Verify the session exists
        getSessionById(session.getId());
        
        // Only allow updates to non-completed sessions
        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new IllegalStateException("Cannot update a completed session");
        }
        
        return sessionRepository.save(session);
    }
    
    @Override
    public List<Session> getUpcomingSessionsByTeacher(Long teacherId, LocalDateTime currentTime) {
        return sessionRepository.findUpcomingSessionsByTeacher(teacherId, currentTime);
    }
    
    @Override
    public List<Session> getActiveSessionsForStudent(Long studentId) {
        return sessionRepository.findActiveSessionsForStudent(studentId);
    }
}
