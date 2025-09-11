package io.github.jspinak.brobot.runner.session.lifecycle;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.session.Session;
import io.github.jspinak.brobot.runner.session.context.SessionContext;
import io.github.jspinak.brobot.runner.session.context.SessionOptions;

import lombok.extern.slf4j.Slf4j;

/**
 * Manages the lifecycle of sessions including creation, state transitions, and termination.
 *
 * <p>This service is responsible for managing session lifecycle operations including starting new
 * sessions, ending sessions, and tracking session state.
 *
 * <p>Thread Safety: This class is thread-safe.
 *
 * @since 1.0.0
 */
@Slf4j
@Service("legacySessionLifecycleService")
public class SessionLifecycleService implements DiagnosticCapable {

    // Active sessions tracking
    private final Map<String, Session> activeSessions = new ConcurrentHashMap<>();

    // Session state tracking
    private final Map<String, SessionState> sessionStates = new ConcurrentHashMap<>();

    // Diagnostic mode
    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);

    /**
     * Starts a new session with the given parameters.
     *
     * @param sessionName human-readable session name
     * @param projectName project associated with the session
     * @param configPath path to configuration file
     * @param imagePath path to image resources
     * @param options session configuration options
     * @return created session context
     */
    public SessionContext startSession(
            String sessionName,
            String projectName,
            String configPath,
            String imagePath,
            SessionOptions options) {
        log.info("Starting new session: {} for project: {}", sessionName, projectName);

        // Create session context
        SessionContext context =
                SessionContext.builder()
                        .sessionName(sessionName)
                        .projectName(projectName)
                        .configPath(configPath)
                        .imagePath(imagePath)
                        .options(options != null ? options : SessionOptions.defaultOptions())
                        .correlationId(generateCorrelationId())
                        .build();

        // Create session object
        Session session = new Session();
        session.setId(context.getSessionId());
        session.setProjectName(projectName);
        session.setConfigPath(configPath);
        session.setImagePath(imagePath);
        session.setStartTime(LocalDateTime.now());
        session.setActive(true);

        // Store session
        activeSessions.put(context.getSessionId(), session);
        sessionStates.put(context.getSessionId(), SessionState.ACTIVE);

        if (diagnosticMode.get()) {
            log.info(
                    "[DIAGNOSTIC] Session started - ID: {}, Name: {}, Project: {}",
                    context.getSessionId(),
                    sessionName,
                    projectName);
        }

        return context;
    }

    /**
     * Ends a session and marks it as inactive.
     *
     * @param sessionId the session ID to end
     */
    public void endSession(String sessionId) {
        log.info("Ending session: {}", sessionId);

        Session session = activeSessions.get(sessionId);
        if (session == null) {
            log.warn("Cannot end session {} - not found", sessionId);
            return;
        }

        // Update session state
        session.setActive(false);
        session.setEndTime(LocalDateTime.now());
        sessionStates.put(sessionId, SessionState.ENDED);

        // Remove from active sessions
        activeSessions.remove(sessionId);

        if (diagnosticMode.get()) {
            log.info(
                    "[DIAGNOSTIC] Session ended - ID: {}, Duration: {}",
                    sessionId,
                    java.time.Duration.between(session.getStartTime(), session.getEndTime()));
        }
    }

    /**
     * Checks if a session is currently active.
     *
     * @param sessionId the session ID to check
     * @return true if the session is active
     */
    public boolean isSessionActive(String sessionId) {
        return activeSessions.containsKey(sessionId)
                && sessionStates.get(sessionId) == SessionState.ACTIVE;
    }

    /**
     * Starts a new session from a context.
     *
     * @param context the session context
     * @return the created session
     */
    public Session startSession(SessionContext context) {
        log.info(
                "Starting new session from context: {} for project: {}",
                context.getSessionName(),
                context.getProjectName());

        // Create session object
        Session session = new Session();
        session.setId(context.getSessionId());
        session.setProjectName(context.getProjectName());
        session.setConfigPath(context.getConfigPath());
        session.setImagePath(context.getImagePath());
        session.setStartTime(LocalDateTime.now());
        session.setActive(true);

        // Store session
        activeSessions.put(context.getSessionId(), session);
        sessionStates.put(context.getSessionId(), SessionState.ACTIVE);

        if (diagnosticMode.get()) {
            log.info(
                    "[DIAGNOSTIC] Session started - ID: {}, Name: {}, Project: {}",
                    context.getSessionId(),
                    context.getSessionName(),
                    context.getProjectName());
        }

        return session;
    }

    /**
     * Gets the current active session.
     *
     * @return the current session or empty if none active
     */
    public Optional<Session> getCurrentSession() {
        // Return the most recently created active session
        return activeSessions.values().stream()
                .filter(Session::isActive)
                .max((s1, s2) -> s1.getStartTime().compareTo(s2.getStartTime()));
    }

    /**
     * Checks if any session is currently active.
     *
     * @return true if there is an active session
     */
    public boolean isSessionActive() {
        return !activeSessions.isEmpty()
                && activeSessions.values().stream().anyMatch(Session::isActive);
    }

    /**
     * Activates a previously loaded session.
     *
     * @param session the session to activate
     */
    public void activateSession(Session session) {
        if (session == null || session.getId() == null) {
            throw new IllegalArgumentException("Session and session ID must not be null");
        }

        log.info("Activating session: {}", session.getId());

        // Clear any current sessions
        activeSessions.clear();

        // Add this session as active
        session.setActive(true);
        activeSessions.put(session.getId(), session);
        sessionStates.put(session.getId(), SessionState.ACTIVE);

        if (diagnosticMode.get()) {
            log.info("[DIAGNOSTIC] Session activated - ID: {}", session.getId());
        }
    }

    /**
     * Transitions a session to a new state.
     *
     * @param sessionId the session ID
     * @param newState the new state
     * @return transition result
     */
    public SessionTransition transitionTo(String sessionId, SessionState newState) {
        SessionState currentState = sessionStates.get(sessionId);
        if (currentState == null) {
            return new SessionTransition(false, "Session not found");
        }

        // Validate transition
        if (!isValidTransition(currentState, newState)) {
            return new SessionTransition(
                    false,
                    String.format("Invalid transition from %s to %s", currentState, newState));
        }

        // Perform transition
        sessionStates.put(sessionId, newState);

        if (diagnosticMode.get()) {
            log.info(
                    "[DIAGNOSTIC] Session state transition - ID: {}, {} -> {}",
                    sessionId,
                    currentState,
                    newState);
        }

        return new SessionTransition(true, "Transition successful");
    }

    /**
     * Gets the current state of a session.
     *
     * @param sessionId the session ID
     * @return current session state or null if not found
     */
    public SessionState getSessionState(String sessionId) {
        return sessionStates.get(sessionId);
    }

    /**
     * Gets all active sessions.
     *
     * @return map of active sessions
     */
    public Map<String, Session> getActiveSessions() {
        return new ConcurrentHashMap<>(activeSessions);
    }

    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> states = new ConcurrentHashMap<>();
        states.put("activeSessions", activeSessions.size());
        states.put("totalSessions", sessionStates.size());

        // Count sessions by state
        Map<SessionState, Long> stateCounts =
                sessionStates.values().stream()
                        .collect(
                                java.util.stream.Collectors.groupingBy(
                                        state -> state, java.util.stream.Collectors.counting()));

        stateCounts.forEach(
                (state, count) -> states.put("sessions." + state.name().toLowerCase(), count));

        return DiagnosticInfo.builder().component("SessionLifecycleService").states(states).build();
    }

    @Override
    public boolean isDiagnosticModeEnabled() {
        return diagnosticMode.get();
    }

    @Override
    public void enableDiagnosticMode(boolean enabled) {
        diagnosticMode.set(enabled);
        log.info("Diagnostic mode {}", enabled ? "enabled" : "disabled");
    }

    /** Validates if a state transition is allowed. */
    private boolean isValidTransition(SessionState from, SessionState to) {
        // Define valid transitions
        return switch (from) {
            case ACTIVE -> to == SessionState.PAUSED || to == SessionState.ENDED;
            case PAUSED -> to == SessionState.ACTIVE || to == SessionState.ENDED;
            case ENDED -> false; // Cannot transition from ended state
        };
    }

    /** Generates a correlation ID for session tracing. */
    private String generateCorrelationId() {
        return "session-" + UUID.randomUUID().toString();
    }

    /** Enum representing possible session states. */
    public enum SessionState {
        ACTIVE,
        PAUSED,
        ENDED
    }

    /** Result of a session state transition. */
    public record SessionTransition(boolean success, String message) {}
}
