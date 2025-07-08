package io.github.jspinak.brobot.runner.session.services;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.session.Session;
import io.github.jspinak.brobot.runner.session.SessionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service responsible for managing the lifecycle of sessions.
 * Handles session creation, activation, deactivation, and state management.
 */
@Slf4j
@Service
public class SessionLifecycleService {
    
    private final EventBus eventBus;
    private final SessionPersistenceService persistenceService;
    private final SessionStateService stateService;
    
    private final AtomicReference<Session> currentSession = new AtomicReference<>();
    
    @Autowired
    public SessionLifecycleService(
            EventBus eventBus,
            SessionPersistenceService persistenceService,
            SessionStateService stateService) {
        
        this.eventBus = eventBus;
        this.persistenceService = persistenceService;
        this.stateService = stateService;
    }
    
    /**
     * Starts a new session with the given configuration.
     */
    public Session startNewSession(String projectName, String configPath, String imagePath) {
        // End current session if active
        if (isSessionActive()) {
            endCurrentSession();
        }
        
        // Create new session
        Session session = createSession(projectName, configPath, imagePath);
        
        // Set as current session
        currentSession.set(session);
        
        // Publish event
        publishSessionEvent("Session started", session.getId());
        
        // Save initial state
        persistenceService.saveSession(session);
        
        return session;
    }
    
    /**
     * Ends the current session.
     */
    public void endCurrentSession() {
        Session session = currentSession.get();
        if (session != null && session.isActive()) {
            // Capture final state
            stateService.captureApplicationState(session);
            
            // Mark session as ended
            session.setEndTime(LocalDateTime.now());
            session.setActive(false);
            session.addEvent(new SessionEvent("ENDED", "Session ended"));
            
            // Save final state
            persistenceService.saveSession(session);
            
            // Publish event
            publishSessionEvent("Session ended", session.getId());
            
            // Clear current session
            currentSession.set(null);
        }
    }
    
    /**
     * Checks if there's an active session.
     */
    public boolean isSessionActive() {
        Session session = currentSession.get();
        return session != null && session.isActive();
    }
    
    /**
     * Gets the current session.
     */
    public Session getCurrentSession() {
        return currentSession.get();
    }
    
    /**
     * Saves the current session state.
     */
    public void saveCurrentSession() {
        Session session = currentSession.get();
        if (session != null && session.isActive()) {
            try {
                // Capture current state
                stateService.captureApplicationState(session);
                
                // Save to disk
                persistenceService.saveSession(session);
                
                log.debug("Current session saved: {}", session.getId());
            } catch (Exception e) {
                log.error("Failed to save current session", e);
                eventBus.publish(LogEvent.error(this,
                        "Failed to save session: " + e.getMessage(), "Session", e));
            }
        }
    }
    
    /**
     * Restores a session by ID.
     */
    public boolean restoreSession(String sessionId) {
        return persistenceService.loadSession(sessionId)
            .map(this::activateSession)
            .orElse(false);
    }
    
    /**
     * Creates a new session instance.
     */
    private Session createSession(String projectName, String configPath, String imagePath) {
        String sessionId = UUID.randomUUID().toString();
        
        Session session = new Session();
        session.setId(sessionId);
        session.setStartTime(LocalDateTime.now());
        session.setActive(true);
        session.setProjectName(projectName);
        session.setConfigPath(configPath);
        session.setImagePath(imagePath);
        
        // Add creation event
        session.addEvent(new SessionEvent("CREATED", "Session created"));
        
        log.info("New session created: {}", sessionId);
        
        return session;
    }
    
    /**
     * Activates a loaded session.
     */
    private boolean activateSession(Session session) {
        try {
            // End current session if active
            if (isSessionActive()) {
                endCurrentSession();
            }
            
            // Mark session as active if it wasn't explicitly ended
            if (session.getEndTime() == null) {
                session.setActive(true);
            }
            
            // Set as current session
            currentSession.set(session);
            
            // Restore application state
            stateService.restoreApplicationState(session);
            
            // Add restoration event
            session.addEvent(new SessionEvent(
                "RESTORED",
                "Session restored",
                "Application state restored from saved session"
            ));
            
            // Save the restored session
            persistenceService.saveSession(session);
            
            // Publish event
            publishSessionEvent("Session restored", session.getId());
            
            log.info("Session activated: {}", session.getId());
            return true;
            
        } catch (Exception e) {
            log.error("Failed to activate session: {}", session.getId(), e);
            eventBus.publish(LogEvent.error(this,
                    "Failed to restore session: " + e.getMessage(), "Session", e));
            return false;
        }
    }
    
    /**
     * Updates the current session with new data.
     */
    public void updateSessionData(String key, Object value) {
        Session session = currentSession.get();
        if (session != null) {
            session.addStateData(key, value);
            log.debug("Session data updated: {} = {}", key, value);
        }
    }
    
    /**
     * Adds an event to the current session.
     */
    public void addSessionEvent(String type, String description) {
        Session session = currentSession.get();
        if (session != null) {
            session.addEvent(new SessionEvent(type, description));
        }
    }
    
    /**
     * Adds an event with details to the current session.
     */
    public void addSessionEvent(String type, String description, String details) {
        Session session = currentSession.get();
        if (session != null) {
            session.addEvent(new SessionEvent(type, description, details));
        }
    }
    
    /**
     * Publishes a session event to the event bus.
     */
    private void publishSessionEvent(String message, String sessionId) {
        eventBus.publish(LogEvent.info(this, message + ": " + sessionId, "Session"));
    }
    
    /**
     * Checks if a session exists.
     */
    public boolean sessionExists(String sessionId) {
        return persistenceService.loadSession(sessionId).isPresent();
    }
    
    /**
     * Deletes a session.
     */
    public boolean deleteSession(String sessionId) {
        // Don't delete the current session
        Session current = currentSession.get();
        if (current != null && current.getId().equals(sessionId)) {
            log.warn("Cannot delete the current active session");
            return false;
        }
        
        return persistenceService.deleteSession(sessionId);
    }
}