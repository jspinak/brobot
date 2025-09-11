package io.github.jspinak.brobot.persistence;

import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.state.StateObject;

/**
 * Interface for pluggable persistence providers. Allows different persistence implementations
 * without forcing database dependencies.
 *
 * <p>Implementations can provide:
 *
 * <ul>
 *   <li>File-based persistence (JSON/CSV)
 *   <li>Database persistence (JPA/JDBC)
 *   <li>In-memory persistence (for testing)
 *   <li>Cloud storage persistence
 *   <li>Custom persistence solutions
 * </ul>
 *
 * @since 1.2.0
 */
public interface PersistenceProvider {

    /**
     * Start a new recording session.
     *
     * @param sessionName name of the session
     * @param application application being automated
     * @param metadata optional metadata for the session
     * @return session identifier
     */
    String startSession(String sessionName, String application, String metadata);

    /**
     * Stop the current recording session.
     *
     * @return session identifier of the stopped session
     */
    String stopSession();

    /** Pause recording without ending the session. */
    void pauseRecording();

    /** Resume a paused recording session. */
    void resumeRecording();

    /**
     * Check if currently recording.
     *
     * @return true if recording is active
     */
    boolean isRecording();

    /**
     * Record an action execution.
     *
     * @param record the ActionRecord to persist
     * @param stateObject the StateObject context (optional)
     */
    void recordAction(ActionRecord record, StateObject stateObject);

    /**
     * Record multiple actions in batch.
     *
     * @param records list of ActionRecords to persist
     */
    void recordBatch(java.util.List<ActionRecord> records);

    /**
     * Export a session as ActionHistory.
     *
     * @param sessionId the session identifier
     * @return ActionHistory containing all records from the session
     */
    ActionHistory exportSession(String sessionId);

    /**
     * Import ActionHistory as a new session.
     *
     * @param history the ActionHistory to import
     * @param sessionName name for the imported session
     * @return session identifier of the imported session
     */
    String importSession(ActionHistory history, String sessionName);

    /**
     * Get all available sessions.
     *
     * @return list of session identifiers
     */
    java.util.List<String> getAllSessions();

    /**
     * Delete a session and all its records.
     *
     * @param sessionId the session identifier
     */
    void deleteSession(String sessionId);

    /**
     * Get session metadata.
     *
     * @param sessionId the session identifier
     * @return session metadata
     */
    SessionMetadata getSessionMetadata(String sessionId);

    /**
     * Get the current active session identifier.
     *
     * @return current session ID or null if not recording
     */
    String getCurrentSessionId();

    /** Session metadata container. */
    class SessionMetadata {
        private String sessionId;
        private String name;
        private String application;
        private java.time.LocalDateTime startTime;
        private java.time.LocalDateTime endTime;
        private int totalActions;
        private int successfulActions;
        private String metadata;

        // Constructor
        public SessionMetadata() {}

        public SessionMetadata(String sessionId, String name, String application) {
            this.sessionId = sessionId;
            this.name = name;
            this.application = application;
            this.startTime = java.time.LocalDateTime.now();
            this.totalActions = 0;
            this.successfulActions = 0;
        }

        // Getters and setters
        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getApplication() {
            return application;
        }

        public void setApplication(String application) {
            this.application = application;
        }

        public java.time.LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(java.time.LocalDateTime startTime) {
            this.startTime = startTime;
        }

        public java.time.LocalDateTime getEndTime() {
            return endTime;
        }

        public void setEndTime(java.time.LocalDateTime endTime) {
            this.endTime = endTime;
        }

        public int getTotalActions() {
            return totalActions;
        }

        public void setTotalActions(int totalActions) {
            this.totalActions = totalActions;
        }

        public int getSuccessfulActions() {
            return successfulActions;
        }

        public void setSuccessfulActions(int successfulActions) {
            this.successfulActions = successfulActions;
        }

        public String getMetadata() {
            return metadata;
        }

        public void setMetadata(String metadata) {
            this.metadata = metadata;
        }

        @com.fasterxml.jackson.annotation.JsonIgnore
        public double getSuccessRate() {
            return totalActions > 0 ? (double) successfulActions / totalActions * 100 : 0;
        }
    }
}
