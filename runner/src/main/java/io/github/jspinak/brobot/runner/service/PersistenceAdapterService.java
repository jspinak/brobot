package io.github.jspinak.brobot.runner.service;

import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.persistence.PersistenceProvider;
import io.github.jspinak.brobot.persistence.PersistenceProvider.SessionMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Adapter service that bridges the runner UI with the persistence module.
 * This replaces the previous ActionRecordingService implementation with
 * the new modular persistence architecture.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PersistenceAdapterService {
    
    private final PersistenceProvider persistenceProvider;
    
    /**
     * Start a new recording session.
     * 
     * @param sessionName name of the session
     * @param application application being automated
     * @param description optional description
     * @return session ID
     */
    public String startRecording(String sessionName, String application, String description) {
        log.info("Starting recording session: {}", sessionName);
        return persistenceProvider.startSession(sessionName, application, description);
    }
    
    /**
     * Stop the current recording session.
     * 
     * @return session ID of the stopped session
     */
    public String stopRecording() {
        String sessionId = persistenceProvider.stopSession();
        log.info("Stopped recording session: {}", sessionId);
        return sessionId;
    }
    
    /**
     * Pause the current recording.
     */
    public void pauseRecording() {
        persistenceProvider.pauseRecording();
        log.debug("Recording paused");
    }
    
    /**
     * Resume the current recording.
     */
    public void resumeRecording() {
        persistenceProvider.resumeRecording();
        log.debug("Recording resumed");
    }
    
    /**
     * Check if currently recording.
     * 
     * @return true if recording is active
     */
    public boolean isRecording() {
        return persistenceProvider.isRecording();
    }
    
    /**
     * Get the current session ID.
     * 
     * @return current session ID or null if not recording
     */
    public String getCurrentSessionId() {
        return persistenceProvider.getCurrentSessionId();
    }
    
    /**
     * Record an action execution.
     * 
     * @param record the ActionRecord to persist
     * @param stateObject the StateObject context (optional)
     */
    public void recordAction(ActionRecord record, StateObject stateObject) {
        persistenceProvider.recordAction(record, stateObject);
    }
    
    /**
     * Record multiple actions in batch.
     * 
     * @param records list of ActionRecords to persist
     */
    public void recordBatch(List<ActionRecord> records) {
        persistenceProvider.recordBatch(records);
    }
    
    /**
     * Export a session as ActionHistory.
     * 
     * @param sessionId the session identifier
     * @return ActionHistory containing all records from the session
     */
    public ActionHistory exportSession(String sessionId) {
        return persistenceProvider.exportSession(sessionId);
    }
    
    /**
     * Import ActionHistory as a new session.
     * 
     * @param history the ActionHistory to import
     * @param sessionName name for the imported session
     * @param application application name
     * @return session identifier of the imported session
     */
    public String importSession(ActionHistory history, String sessionName, String application) {
        return persistenceProvider.importSession(history, sessionName);
    }
    
    /**
     * Get all available sessions.
     * 
     * @return list of session identifiers
     */
    public List<String> getAllSessions() {
        return persistenceProvider.getAllSessions();
    }
    
    /**
     * Delete a session and all its records.
     * 
     * @param sessionId the session identifier
     */
    public void deleteSession(String sessionId) {
        persistenceProvider.deleteSession(sessionId);
        log.info("Deleted session: {}", sessionId);
    }
    
    /**
     * Get session metadata.
     * 
     * @param sessionId the session identifier
     * @return session metadata
     */
    public SessionMetadata getSessionMetadata(String sessionId) {
        return persistenceProvider.getSessionMetadata(sessionId);
    }
    
    /**
     * Get metadata for all sessions.
     * 
     * @return list of session metadata
     */
    public List<SessionMetadata> getAllSessionMetadata() {
        return getAllSessions().stream()
            .map(this::getSessionMetadata)
            .toList();
    }
}