package io.github.jspinak.brobot.runner.session;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.resources.ResourceManager;
import io.github.jspinak.brobot.runner.session.autosave.SessionAutosaveService;
import io.github.jspinak.brobot.runner.session.context.SessionContext;
import io.github.jspinak.brobot.runner.session.context.SessionOptions;
import io.github.jspinak.brobot.runner.session.discovery.SessionDiscoveryService;
import io.github.jspinak.brobot.runner.session.lifecycle.SessionLifecycleService;
import io.github.jspinak.brobot.runner.session.persistence.SessionPersistenceService;
import io.github.jspinak.brobot.runner.session.state.SessionStateService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages automation sessions with persistence capabilities.
 * 
 * <p>This class acts as a thin orchestrator that delegates responsibilities to specialized services:
 * <ul>
 *   <li>{@link SessionLifecycleService} - Manages session lifecycle (start, end, active sessions)</li>
 *   <li>{@link SessionPersistenceService} - Handles all file I/O operations</li>
 *   <li>{@link SessionStateService} - Captures and restores application state</li>
 *   <li>{@link SessionAutosaveService} - Manages automatic periodic saving</li>
 *   <li>{@link SessionDiscoveryService} - Provides session search and discovery</li>
 * </ul>
 * </p>
 * 
 * <p>Thread Safety: This class is thread-safe through delegation to thread-safe services.</p>
 * 
 * @see Session
 * @see SessionContext
 * @see SessionOptions
 * @since 1.0.0
 */
@Slf4j
@Component
public class SessionManager implements AutoCloseable, DiagnosticCapable {

    private final EventBus eventBus;
    private final ResourceManager resourceManager;
    
    // Delegated services
    private final SessionLifecycleService lifecycleService;
    private final SessionPersistenceService persistenceService;
    private final SessionStateService stateService;
    private final SessionAutosaveService autosaveService;
    private final SessionDiscoveryService discoveryService;
    
    // Diagnostic mode
    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);

    @Autowired
    public SessionManager(EventBus eventBus,
                          ResourceManager resourceManager,
                          SessionLifecycleService lifecycleService,
                          SessionPersistenceService persistenceService,
                          SessionStateService stateService,
                          SessionAutosaveService autosaveService,
                          SessionDiscoveryService discoveryService) {
        this.eventBus = eventBus;
        this.resourceManager = resourceManager;
        this.lifecycleService = lifecycleService;
        this.persistenceService = persistenceService;
        this.stateService = stateService;
        this.autosaveService = autosaveService;
        this.discoveryService = discoveryService;

        resourceManager.registerResource(this, "SessionManager");
    }

    @PostConstruct
    public void initialize() {
        log.info("SessionManager initialized with all services");
        eventBus.publish(LogEvent.info(this, "Session management initialized", "Session"));
    }

    /**
     * Starts a new session with the given configuration.
     * 
     * @param projectName the project name
     * @param configPath the configuration path
     * @param imagePath the image path
     * @return the created session
     */
    public Session startNewSession(String projectName, String configPath, String imagePath) {
        log.info("Starting new session for project: {}", projectName);
        
        // Build session context
        SessionContext context = SessionContext.builder()
                .projectName(projectName)
                .configPath(configPath)
                .imagePath(imagePath)
                .options(SessionOptions.defaultOptions())
                .build();
        
        // Start session through lifecycle service
        Session session = lifecycleService.startSession(context);
        
        // Save initial session state
        try {
            persistenceService.saveSession(session);
        } catch (IOException e) {
            log.error("Failed to save initial session state", e);
        }
        
        // Enable autosave if configured
        if (context.getOptions().isAutosaveEnabled()) {
            autosaveService.enableAutosave(context, this::autosaveSession);
        }
        
        eventBus.publish(LogEvent.info(this, 
                "Session started: " + session.getId(), "Session"));
        
        return session;
    }

    /**
     * Ends the current session.
     */
    public void endCurrentSession() {
        Optional<Session> currentSession = lifecycleService.getCurrentSession();
        
        if (currentSession.isEmpty()) {
            log.warn("No active session to end");
            return;
        }
        
        Session session = currentSession.get();
        String sessionId = session.getId();
        
        // Disable autosave
        autosaveService.disableAutosave(sessionId);
        
        // End session
        lifecycleService.endSession(sessionId);
        
        // Save final state
        try {
            persistenceService.saveSession(session);
        } catch (IOException e) {
            log.error("Failed to save final session state", e);
        }
        
        eventBus.publish(LogEvent.info(this, 
                "Session ended: " + sessionId, "Session"));
    }

    /**
     * Checks if there's an active session.
     * 
     * @return true if a session is active
     */
    public boolean isSessionActive() {
        return lifecycleService.isSessionActive();
    }

    /**
     * Gets the current session.
     * 
     * @return the current session or null
     */
    public Session getCurrentSession() {
        return lifecycleService.getCurrentSession().orElse(null);
    }

    /**
     * Autosaves the current session.
     */
    public void autosaveCurrentSession() {
        Optional<Session> currentSession = lifecycleService.getCurrentSession();
        
        if (currentSession.isEmpty()) {
            log.debug("No active session to autosave");
            return;
        }
        
        autosaveSession(currentSession.get());
    }

    /**
     * Gets the last autosave time.
     * 
     * @return the last autosave time or null
     */
    public LocalDateTime getLastAutosaveTime() {
        return lifecycleService.getCurrentSession()
                .map(Session::getId)
                .map(autosaveService::getStatus)
                .map(status -> status.getLastSaveTime())
                .orElse(null);
    }

    /**
     * Saves a session.
     * 
     * @param session the session to save
     */
    public void saveSession(Session session) {
        if (session == null) {
            return;
        }
        
        try {
            // Capture current state
            stateService.captureState(session);
            
            // Save to disk
            persistenceService.saveSession(session);
            
            log.debug("Session saved: {}", session.getId());
            
        } catch (Exception e) {
            log.error("Failed to save session", e);
            eventBus.publish(LogEvent.error(this,
                    "Failed to save session: " + e.getMessage(), "Session", e));
        }
    }

    /**
     * Loads a session by ID.
     * 
     * @param sessionId the session ID
     * @return the loaded session or empty
     */
    public Optional<Session> loadSession(String sessionId) {
        log.info("Loading session: {}", sessionId);
        
        try {
            Optional<Session> session = persistenceService.loadSession(sessionId);
            
            if (session.isPresent()) {
                eventBus.publish(LogEvent.info(this,
                        "Session loaded: " + sessionId, "Session"));
            } else {
                eventBus.publish(LogEvent.warning(this,
                        "Session not found: " + sessionId, "Session"));
            }
            
            return session;
            
        } catch (Exception e) {
            log.error("Failed to load session", e);
            eventBus.publish(LogEvent.error(this,
                    "Failed to load session: " + e.getMessage(), "Session", e));
            return Optional.empty();
        }
    }

    /**
     * Gets all session summaries.
     * 
     * @return list of session summaries
     */
    public List<SessionSummary> getAllSessionSummaries() {
        return discoveryService.listAvailableSessions();
    }

    /**
     * Restores a session.
     * 
     * @param sessionId the session ID to restore
     * @return true if restored successfully
     */
    public boolean restoreSession(String sessionId) {
        log.info("Restoring session: {}", sessionId);
        
        Optional<Session> loadedSession = loadSession(sessionId);
        if (loadedSession.isEmpty()) {
            return false;
        }
        
        try {
            Session session = loadedSession.get();
            
            // End current session if active
            if (isSessionActive()) {
                endCurrentSession();
            }
            
            // Activate the loaded session
            lifecycleService.activateSession(session);
            
            // Restore application state
            stateService.restoreState(session);
            
            // Re-enable autosave if the session was active
            if (session.isActive()) {
                SessionContext context = SessionContext.builder()
                        .sessionId(session.getId())
                        .projectName(session.getProjectName())
                        .configPath(session.getConfigPath())
                        .imagePath(session.getImagePath())
                        .options(SessionOptions.defaultOptions())
                        .build();
                        
                autosaveService.enableAutosave(context, this::autosaveSession);
            }
            
            // Add restoration event
            session.addEvent(new SessionEvent("RESTORED",
                    "Session restored",
                    "Application state restored from saved session"));
            
            eventBus.publish(LogEvent.info(this,
                    "Session restored: " + sessionId, "Session"));
            
            return true;
            
        } catch (Exception e) {
            log.error("Failed to restore session", e);
            eventBus.publish(LogEvent.error(this,
                    "Failed to restore session: " + e.getMessage(), "Session", e));
            return false;
        }
    }

    /**
     * Deletes a session.
     * 
     * @param sessionId the session ID to delete
     * @return true if deleted successfully
     */
    public boolean deleteSession(String sessionId) {
        log.info("Deleting session: {}", sessionId);
        
        // Make sure we're not deleting the active session
        Optional<Session> currentSession = lifecycleService.getCurrentSession();
        if (currentSession.isPresent() && currentSession.get().getId().equals(sessionId)) {
            log.warn("Cannot delete active session");
            return false;
        }
        
        boolean deleted = persistenceService.deleteSession(sessionId);
        
        if (deleted) {
            eventBus.publish(LogEvent.info(this,
                    "Session deleted: " + sessionId, "Session"));
        }
        
        return deleted;
    }

    @Override
    public void close() {
        log.info("Closing SessionManager");
        
        // Ensure any active session is saved
        if (isSessionActive()) {
            endCurrentSession();
        }
        
        // Shutdown autosave service (it has its own cleanup)
        autosaveService.shutdown();
        
        eventBus.publish(LogEvent.info(this, "Session manager closed", "Resources"));
    }

    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> states = new ConcurrentHashMap<>();
        
        // Aggregate diagnostic info from all services
        states.put("lifecycle", lifecycleService.getDiagnosticInfo());
        states.put("persistence", persistenceService.getDiagnosticInfo());
        states.put("state", stateService.getDiagnosticInfo());
        states.put("autosave", autosaveService.getDiagnosticInfo());
        states.put("discovery", discoveryService.getDiagnosticInfo());
        
        // Add orchestrator-specific info
        states.put("activeSession", isSessionActive());
        states.put("currentSessionId", getCurrentSession() != null ? getCurrentSession().getId() : null);
        
        return DiagnosticInfo.builder()
                .component("SessionManager")
                .states(states)
                .build();
    }

    @Override
    public boolean isDiagnosticModeEnabled() {
        return diagnosticMode.get();
    }

    @Override
    public void enableDiagnosticMode(boolean enabled) {
        diagnosticMode.set(enabled);
        
        // Propagate to all services
        lifecycleService.enableDiagnosticMode(enabled);
        persistenceService.enableDiagnosticMode(enabled);
        stateService.enableDiagnosticMode(enabled);
        autosaveService.enableDiagnosticMode(enabled);
        discoveryService.enableDiagnosticMode(enabled);
        
        log.info("Diagnostic mode {} for SessionManager and all services", 
                enabled ? "enabled" : "disabled");
    }

    /**
     * Helper method for autosave callback.
     */
    private void autosaveSession(Session session) {
        try {
            // Capture current state
            stateService.captureState(session);
            
            // Save to disk
            persistenceService.saveSession(session);
            
            log.debug("Session autosaved: {}", session.getId());
            
            if (diagnosticMode.get()) {
                log.info("[DIAGNOSTIC] Autosave completed for session: {}", session.getId());
            }
            
        } catch (Exception e) {
            log.error("Autosave failed for session: {}", session.getId(), e);
        }
    }
}