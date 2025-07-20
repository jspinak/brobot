package io.github.jspinak.brobot.runner.session;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.resources.ResourceManager;
import io.github.jspinak.brobot.runner.session.services.SessionAutosaveService;
import io.github.jspinak.brobot.runner.session.services.SessionLifecycleService;
import io.github.jspinak.brobot.runner.session.services.SessionPersistenceService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Refactored SessionManager that delegates responsibilities to specialized services.
 * 
 * <p>This class now acts as a facade that coordinates between different session services:
 * <ul>
 *   <li>SessionLifecycleService - Manages session lifecycle (create, end, restore)</li>
 *   <li>SessionPersistenceService - Handles file I/O operations</li>
 *   <li>SessionAutosaveService - Manages automatic periodic saves</li>
 *   <li>SessionStateService - Captures and restores application state</li>
 * </ul>
 * </p>
 * 
 * <p>This refactoring follows the Single Responsibility Principle by separating
 * concerns into focused, testable services.</p>
 * 
 * @see SessionLifecycleService
 * @see SessionPersistenceService
 * @see SessionAutosaveService
 */
@Slf4j
@Component
public class RefactoredSessionManager implements AutoCloseable {
    
    private final EventBus eventBus;
    private final ResourceManager resourceManager;
    private final SessionLifecycleService lifecycleService;
    private final SessionPersistenceService persistenceService;
    private final SessionAutosaveService autosaveService;
    
    @Autowired
    public RefactoredSessionManager(
            EventBus eventBus,
            ResourceManager resourceManager,
            SessionLifecycleService lifecycleService,
            SessionPersistenceService persistenceService,
            SessionAutosaveService autosaveService) {
        
        this.eventBus = eventBus;
        this.resourceManager = resourceManager;
        this.lifecycleService = lifecycleService;
        this.persistenceService = persistenceService;
        this.autosaveService = autosaveService;
    }
    
    @PostConstruct
    public void initialize() {
        // Register with resource manager
        resourceManager.registerResource(this, "SessionManager");
        
        log.info("SessionManager initialized");
        eventBus.publish(LogEvent.info(this, "Session manager initialized", "Session"));
    }
    
    /**
     * Starts a new session with the given configuration.
     */
    public Session startNewSession(String projectName, String configPath, String imagePath) {
        return lifecycleService.startNewSession(projectName, configPath, imagePath);
    }
    
    /**
     * Ends the current session.
     */
    public void endCurrentSession() {
        lifecycleService.endCurrentSession();
    }
    
    /**
     * Checks if there's an active session.
     */
    public boolean isSessionActive() {
        return lifecycleService.isSessionActive();
    }
    
    /**
     * Gets the current session or null if none exists.
     */
    public Session getCurrentSession() {
        return lifecycleService.getCurrentSession();
    }
    
    /**
     * Manually triggers an autosave of the current session.
     */
    public void autosaveCurrentSession() {
        autosaveService.triggerAutosave();
    }
    
    /**
     * Gets the last autosave time.
     */
    public LocalDateTime getLastAutosaveTime() {
        return autosaveService.getLastAutosaveTime();
    }
    
    /**
     * Saves a session to disk.
     */
    public void saveSession(Session session) {
        persistenceService.saveSession(session);
    }
    
    /**
     * Loads a session by ID.
     */
    public Optional<Session> loadSession(String sessionId) {
        return persistenceService.loadSession(sessionId);
    }
    
    /**
     * Gets a list of all available session summaries.
     */
    public List<SessionSummary> getAllSessionSummaries() {
        return persistenceService.getAllSessionSummaries();
    }
    
    /**
     * Restores a session by ID.
     */
    public boolean restoreSession(String sessionId) {
        return lifecycleService.restoreSession(sessionId);
    }
    
    /**
     * Deletes a session by ID.
     */
    public boolean deleteSession(String sessionId) {
        return lifecycleService.deleteSession(sessionId);
    }
    
    /**
     * Updates the current session with new data.
     */
    public void updateSessionData(String key, Object value) {
        lifecycleService.updateSessionData(key, value);
    }
    
    /**
     * Adds an event to the current session.
     */
    public void addSessionEvent(String type, String description) {
        lifecycleService.addSessionEvent(type, description);
    }
    
    /**
     * Adds an event with details to the current session.
     */
    public void addSessionEvent(String type, String description, String details) {
        lifecycleService.addSessionEvent(type, description, details);
    }
    
    /**
     * Sets the autosave interval in minutes.
     */
    public void setAutosaveInterval(long minutes) {
        autosaveService.setAutosaveInterval(minutes);
    }
    
    /**
     * Gets the current autosave interval in minutes.
     */
    public long getAutosaveInterval() {
        return autosaveService.getAutosaveIntervalMinutes();
    }
    
    /**
     * Enables autosave functionality.
     */
    public void enableAutosave() {
        autosaveService.enableAutosave();
    }
    
    /**
     * Disables autosave functionality.
     */
    public void disableAutosave() {
        autosaveService.disableAutosave();
    }
    
    /**
     * Checks if autosave is enabled.
     */
    public boolean isAutosaveEnabled() {
        return autosaveService.isAutosaveEnabled();
    }
    
    /**
     * Gets the time in seconds until the next autosave.
     */
    public long getTimeUntilNextAutosave() {
        return autosaveService.getTimeUntilNextAutosave();
    }
    
    /**
     * Gets the total number of autosaves performed.
     */
    public long getAutosaveCount() {
        return autosaveService.getAutosaveCount();
    }
    
    @Override
    public void close() {
        // Ensure any active session is saved
        if (isSessionActive()) {
            endCurrentSession();
        }
        
        // The autosave service will shut down automatically via @PreDestroy
        
        log.info("SessionManager closed");
        eventBus.publish(LogEvent.info(this, "Session manager closed", "Resources"));
    }
    
    /**
     * Creates a session summary for the current session.
     * Useful for UI display without exposing the full session object.
     */
    public SessionSummary getCurrentSessionSummary() {
        Session current = getCurrentSession();
        if (current == null) {
            return null;
        }
        
        SessionSummary summary = new SessionSummary();
        summary.setId(current.getId());
        summary.setProjectName(current.getProjectName());
        summary.setStartTime(current.getStartTime());
        summary.setEndTime(current.getEndTime());
        summary.setLastSaved(getLastAutosaveTime());
        summary.setActive(current.isActive());
        
        return summary;
    }
    
    /**
     * Checks if a session exists.
     */
    public boolean sessionExists(String sessionId) {
        return lifecycleService.sessionExists(sessionId);
    }
}