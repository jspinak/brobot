package io.github.jspinak.brobot.runner.service;

import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.runner.event.ActionRecordedEvent;
import io.github.jspinak.brobot.runner.event.RecordingStartedEvent;
import io.github.jspinak.brobot.runner.event.RecordingStoppedEvent;
import io.github.jspinak.brobot.runner.persistence.entity.ActionRecordEntity;
import io.github.jspinak.brobot.runner.persistence.entity.RecordingSessionEntity;
import io.github.jspinak.brobot.runner.persistence.entity.RecordingSessionEntity.SessionStatus;
import io.github.jspinak.brobot.runner.persistence.repository.ActionRecordRepository;
import io.github.jspinak.brobot.runner.persistence.repository.RecordingSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service for recording ActionRecords during automation execution.
 * Manages recording sessions and persists action data to the database.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ActionRecordingService {
    
    private final ActionRecordRepository actionRecordRepository;
    private final RecordingSessionRepository sessionRepository;
    private final ActionRecordMapper mapper;
    private final ApplicationEventPublisher eventPublisher;
    
    private RecordingSessionEntity activeSession;
    private final AtomicBoolean recordingEnabled = new AtomicBoolean(false);
    private final ConcurrentLinkedQueue<ActionRecordEntity> recordBuffer = new ConcurrentLinkedQueue<>();
    private static final int BATCH_SIZE = 100;
    
    /**
     * Start a new recording session
     */
    @Transactional
    public RecordingSessionEntity startRecording(String sessionName, String application, String description) {
        if (recordingEnabled.get()) {
            log.warn("Recording already in progress, stopping current session");
            stopRecording();
        }
        
        log.info("Starting recording session: {}", sessionName);
        
        activeSession = new RecordingSessionEntity();
        activeSession.setName(sessionName);
        activeSession.setApplication(application);
        activeSession.setDescription(description);
        activeSession.setStartTime(LocalDateTime.now());
        activeSession.setStatus(SessionStatus.RECORDING);
        
        activeSession = sessionRepository.save(activeSession);
        recordingEnabled.set(true);
        
        eventPublisher.publishEvent(new RecordingStartedEvent(activeSession));
        
        return activeSession;
    }
    
    /**
     * Stop the current recording session
     */
    @Transactional
    public RecordingSessionEntity stopRecording() {
        if (!recordingEnabled.get() || activeSession == null) {
            log.warn("No active recording session to stop");
            return null;
        }
        
        log.info("Stopping recording session: {}", activeSession.getName());
        
        // Flush any remaining buffered records
        flushBuffer();
        
        activeSession.setEndTime(LocalDateTime.now());
        activeSession.complete();
        RecordingSessionEntity stoppedSession = sessionRepository.save(activeSession);
        
        recordingEnabled.set(false);
        eventPublisher.publishEvent(new RecordingStoppedEvent(stoppedSession));
        
        activeSession = null;
        return stoppedSession;
    }
    
    /**
     * Record an action asynchronously
     */
    @Async
    public CompletableFuture<Void> recordAction(ActionRecord actionRecord, StateImage stateImage) {
        if (!recordingEnabled.get() || activeSession == null) {
            return CompletableFuture.completedFuture(null);
        }
        
        try {
            ActionRecordEntity entity = mapper.toEntity(actionRecord);
            entity.setSession(activeSession);
            
            if (stateImage != null) {
                entity.setStateName(stateImage.getOwnerStateName());
                entity.setObjectName(stateImage.getName());
            }
            
            entity.setTimestamp(LocalDateTime.now());
            entity.setApplicationUnderTest(activeSession.getApplication());
            
            recordBuffer.offer(entity);
            
            // Flush buffer if it reaches batch size
            if (recordBuffer.size() >= BATCH_SIZE) {
                flushBuffer();
            }
            
            // Update session statistics
            updateSessionStats(actionRecord.isActionSuccess());
            
            eventPublisher.publishEvent(new ActionRecordedEvent(entity, activeSession));
            
        } catch (Exception e) {
            log.error("Error recording action: {}", e.getMessage(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Record an action synchronously
     */
    @Transactional
    public void recordActionSync(ActionRecord actionRecord, StateImage stateImage) {
        if (!recordingEnabled.get() || activeSession == null) {
            return;
        }
        
        ActionRecordEntity entity = mapper.toEntity(actionRecord);
        entity.setSession(activeSession);
        
        if (stateImage != null) {
            entity.setStateName(stateImage.getOwnerStateName());
            entity.setObjectName(stateImage.getName());
        }
        
        entity.setTimestamp(LocalDateTime.now());
        entity.setApplicationUnderTest(activeSession.getApplication());
        
        actionRecordRepository.save(entity);
        activeSession.addActionRecord(entity);
        sessionRepository.save(activeSession);
        
        eventPublisher.publishEvent(new ActionRecordedEvent(entity, activeSession));
    }
    
    /**
     * Record multiple actions in batch
     */
    @Transactional
    public void recordBatch(List<ActionRecord> actionRecords) {
        if (!recordingEnabled.get() || activeSession == null) {
            return;
        }
        
        List<ActionRecordEntity> entities = new ArrayList<>();
        
        for (ActionRecord record : actionRecords) {
            ActionRecordEntity entity = mapper.toEntity(record);
            entity.setSession(activeSession);
            entity.setTimestamp(LocalDateTime.now());
            entity.setApplicationUnderTest(activeSession.getApplication());
            entities.add(entity);
        }
        
        actionRecordRepository.saveAll(entities);
        
        // Update session statistics
        int successful = (int) actionRecords.stream()
            .filter(ActionRecord::isActionSuccess)
            .count();
        
        activeSession.setTotalActions(activeSession.getTotalActions() + actionRecords.size());
        activeSession.setSuccessfulActions(activeSession.getSuccessfulActions() + successful);
        activeSession.setFailedActions(activeSession.getFailedActions() + (actionRecords.size() - successful));
        
        sessionRepository.save(activeSession);
    }
    
    /**
     * Export a session to ActionHistory
     */
    @Transactional(readOnly = true)
    public ActionHistory exportSession(Long sessionId) {
        RecordingSessionEntity session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        
        ActionHistory history = new ActionHistory();
        List<ActionRecordEntity> records = actionRecordRepository.findBySessionId(sessionId);
        
        for (ActionRecordEntity entity : records) {
            try {
                ActionRecord record = mapper.fromEntity(entity);
                history.addSnapshot(record);
            } catch (Exception e) {
                log.error("Error converting entity to ActionRecord: {}", e.getMessage());
            }
        }
        
        log.info("Exported session {} with {} records", session.getName(), records.size());
        
        return history;
    }
    
    /**
     * Import ActionHistory as a new session
     */
    @Transactional
    public RecordingSessionEntity importSession(ActionHistory history, String sessionName, String application) {
        RecordingSessionEntity session = new RecordingSessionEntity();
        session.setName(sessionName);
        session.setApplication(application);
        session.setStartTime(LocalDateTime.now());
        session.setImported(true);
        session.setStatus(SessionStatus.COMPLETED);
        
        session = sessionRepository.save(session);
        
        List<ActionRecordEntity> entities = new ArrayList<>();
        
        for (ActionRecord record : history.getSnapshots()) {
            ActionRecordEntity entity = mapper.toEntity(record);
            entity.setSession(session);
            entity.setApplicationUnderTest(application);
            entities.add(entity);
        }
        
        actionRecordRepository.saveAll(entities);
        
        session.setTotalActions(entities.size());
        session.setSuccessfulActions((int) entities.stream()
            .filter(ActionRecordEntity::isActionSuccess)
            .count());
        session.setFailedActions(session.getTotalActions() - session.getSuccessfulActions());
        session.setEndTime(LocalDateTime.now());
        
        return sessionRepository.save(session);
    }
    
    /**
     * Get all recording sessions
     */
    @Transactional(readOnly = true)
    public List<RecordingSessionEntity> getAllSessions() {
        return sessionRepository.findAll();
    }
    
    /**
     * Get a specific session
     */
    @Transactional(readOnly = true)
    public Optional<RecordingSessionEntity> getSession(Long sessionId) {
        return sessionRepository.findById(sessionId);
    }
    
    /**
     * Get action records for a session
     */
    @Transactional(readOnly = true)
    public List<ActionRecordEntity> getSessionRecords(Long sessionId) {
        return actionRecordRepository.findBySessionId(sessionId);
    }
    
    /**
     * Check if recording is active
     */
    public boolean isRecording() {
        return recordingEnabled.get();
    }
    
    /**
     * Get the active session
     */
    public Optional<RecordingSessionEntity> getActiveSession() {
        return Optional.ofNullable(activeSession);
    }
    
    /**
     * Pause recording
     */
    public void pauseRecording() {
        recordingEnabled.set(false);
        log.info("Recording paused");
    }
    
    /**
     * Resume recording
     */
    public void resumeRecording() {
        if (activeSession != null) {
            recordingEnabled.set(true);
            log.info("Recording resumed");
        } else {
            log.warn("Cannot resume - no active session");
        }
    }
    
    /**
     * Delete a session and all its records
     */
    @Transactional
    public void deleteSession(Long sessionId) {
        sessionRepository.deleteById(sessionId);
        log.info("Deleted session: {}", sessionId);
    }
    
    /**
     * Mark session as exported
     */
    @Transactional
    public void markSessionExported(Long sessionId, String exportPath, String format) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.setExported(true);
            session.setExportPath(exportPath);
            session.setExportFormat(format);
            sessionRepository.save(session);
        });
    }
    
    /**
     * Flush buffered records to database
     */
    @Transactional
    private void flushBuffer() {
        if (recordBuffer.isEmpty()) {
            return;
        }
        
        List<ActionRecordEntity> toSave = new ArrayList<>();
        ActionRecordEntity entity;
        
        while ((entity = recordBuffer.poll()) != null) {
            toSave.add(entity);
        }
        
        if (!toSave.isEmpty()) {
            actionRecordRepository.saveAll(toSave);
            log.debug("Flushed {} records to database", toSave.size());
        }
    }
    
    /**
     * Update session statistics
     */
    private void updateSessionStats(boolean success) {
        if (activeSession != null) {
            activeSession.setTotalActions(activeSession.getTotalActions() + 1);
            if (success) {
                activeSession.setSuccessfulActions(activeSession.getSuccessfulActions() + 1);
            } else {
                activeSession.setFailedActions(activeSession.getFailedActions() + 1);
            }
        }
    }
}