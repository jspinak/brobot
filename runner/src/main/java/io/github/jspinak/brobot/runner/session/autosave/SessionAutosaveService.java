package io.github.jspinak.brobot.runner.session.autosave;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.session.Session;
import io.github.jspinak.brobot.runner.session.context.SessionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Manages automatic saving of sessions at configured intervals.
 * 
 * This service handles scheduling and execution of periodic session saves,
 * ensuring data is not lost during long-running automation tasks.
 * 
 * Thread Safety: This class is thread-safe.
 * 
 * @since 1.0.0
 */
@Slf4j
@Service("legacySessionAutosaveService")
public class SessionAutosaveService implements DiagnosticCapable {
    
    private final ScheduledExecutorService scheduler;
    private final Map<String, ScheduledFuture<?>> autosaveTasks = new ConcurrentHashMap<>();
    private final Map<String, AutosaveStatus> autosaveStatuses = new ConcurrentHashMap<>();
    
    // Statistics
    private final AtomicInteger totalAutosaves = new AtomicInteger(0);
    private final AtomicInteger failedAutosaves = new AtomicInteger(0);
    
    // Diagnostic mode
    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);
    
    public SessionAutosaveService() {
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread thread = new Thread(r, "session-autosave-" + Thread.currentThread().getId());
            thread.setDaemon(true);
            return thread;
        });
        
        log.info("SessionAutosaveService initialized");
    }
    
    /**
     * Enables autosave for a session.
     * 
     * @param context session context with options
     * @param saveHandler handler to perform the actual save
     */
    public void enableAutosave(SessionContext context, Consumer<Session> saveHandler) {
        if (context == null || saveHandler == null) {
            throw new IllegalArgumentException("Context and saveHandler must not be null");
        }
        
        String sessionId = context.getSessionId();
        
        // Cancel existing autosave if any
        disableAutosave(sessionId);
        
        if (!context.getOptions().isAutosaveEnabled()) {
            log.info("Autosave is disabled for session {}", sessionId);
            return;
        }
        
        Duration interval = context.getOptions().getAutosaveInterval();
        
        log.info("Enabling autosave for session {} with interval {}", sessionId, interval);
        
        // Create autosave task
        Runnable autosaveTask = () -> performAutosave(sessionId, saveHandler);
        
        // Schedule periodic autosave
        ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(
                autosaveTask,
                interval.toMillis(),
                interval.toMillis(),
                TimeUnit.MILLISECONDS
        );
        
        autosaveTasks.put(sessionId, future);
        autosaveStatuses.put(sessionId, new AutosaveStatus(sessionId, interval));
        
        if (diagnosticMode.get()) {
            log.info("[DIAGNOSTIC] Autosave enabled - Session: {}, Interval: {}", 
                    sessionId, interval);
        }
    }
    
    /**
     * Disables autosave for a session.
     * 
     * @param sessionId the session ID
     */
    public void disableAutosave(String sessionId) {
        ScheduledFuture<?> future = autosaveTasks.remove(sessionId);
        
        if (future != null) {
            future.cancel(false);
            log.info("Autosave disabled for session {}", sessionId);
        }
        
        AutosaveStatus status = autosaveStatuses.get(sessionId);
        if (status != null) {
            status.setEnabled(false);
        }
        
        if (diagnosticMode.get()) {
            log.info("[DIAGNOSTIC] Autosave disabled - Session: {}", sessionId);
        }
    }
    
    /**
     * Gets the autosave status for a session.
     * 
     * @param sessionId the session ID
     * @return autosave status or null if not found
     */
    public AutosaveStatus getStatus(String sessionId) {
        return autosaveStatuses.get(sessionId);
    }
    
    /**
     * Manually triggers an autosave for a session.
     * 
     * @param sessionId the session ID
     * @param saveHandler the save handler
     * @return true if autosave was triggered
     */
    public boolean triggerAutosave(String sessionId, Consumer<Session> saveHandler) {
        if (!autosaveStatuses.containsKey(sessionId)) {
            log.warn("No autosave configured for session {}", sessionId);
            return false;
        }
        
        performAutosave(sessionId, saveHandler);
        return true;
    }
    
    /**
     * Gets all active autosave sessions.
     * 
     * @return map of session IDs to their autosave status
     */
    public Map<String, AutosaveStatus> getAllAutosaveStatuses() {
        return new ConcurrentHashMap<>(autosaveStatuses);
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> states = new ConcurrentHashMap<>();
        states.put("activeSessions", autosaveTasks.size());
        states.put("totalAutosaves", totalAutosaves.get());
        states.put("failedAutosaves", failedAutosaves.get());
        states.put("successRate", calculateSuccessRate());
        
        // Add per-session status
        autosaveStatuses.forEach((sessionId, status) -> {
            states.put("session." + sessionId + ".enabled", status.isEnabled());
            states.put("session." + sessionId + ".lastSave", status.getLastSaveTime());
            states.put("session." + sessionId + ".saveCount", status.getSaveCount());
        });
        
        return DiagnosticInfo.builder()
                .component("SessionAutosaveService")
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
        log.info("Diagnostic mode {}", enabled ? "enabled" : "disabled");
    }
    
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down SessionAutosaveService");
        
        // Cancel all autosave tasks
        autosaveTasks.values().forEach(future -> future.cancel(false));
        autosaveTasks.clear();
        
        // Shutdown scheduler
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        log.info("SessionAutosaveService shutdown complete");
    }
    
    /**
     * Performs the actual autosave operation.
     */
    private void performAutosave(String sessionId, Consumer<Session> saveHandler) {
        AutosaveStatus status = autosaveStatuses.get(sessionId);
        if (status == null || !status.isEnabled()) {
            return;
        }
        
        log.debug("Performing autosave for session {}", sessionId);
        
        try {
            // Create a minimal session object for the save handler
            // In a real implementation, this would get the actual session
            Session session = new Session();
            session.setId(sessionId);
            
            saveHandler.accept(session);
            
            status.recordSuccessfulSave();
            totalAutosaves.incrementAndGet();
            
            log.debug("Autosave completed for session {}", sessionId);
            
            if (diagnosticMode.get()) {
                log.info("[DIAGNOSTIC] Autosave completed - Session: {}, Total saves: {}",
                        sessionId, status.getSaveCount());
            }
            
        } catch (Exception e) {
            log.error("Autosave failed for session {}", sessionId, e);
            status.recordFailedSave();
            failedAutosaves.incrementAndGet();
        }
    }
    
    /**
     * Calculates the autosave success rate.
     */
    private double calculateSuccessRate() {
        int total = totalAutosaves.get();
        if (total == 0) {
            return 100.0;
        }
        int successful = total - failedAutosaves.get();
        return (successful * 100.0) / total;
    }
    
    /**
     * Status information for session autosave.
     */
    public static class AutosaveStatus {
        private final String sessionId;
        private final Duration interval;
        private volatile boolean enabled = true;
        private volatile LocalDateTime lastSaveTime;
        private final AtomicInteger saveCount = new AtomicInteger(0);
        private final AtomicInteger failureCount = new AtomicInteger(0);
        
        public AutosaveStatus(String sessionId, Duration interval) {
            this.sessionId = sessionId;
            this.interval = interval;
        }
        
        public void recordSuccessfulSave() {
            lastSaveTime = LocalDateTime.now();
            saveCount.incrementAndGet();
        }
        
        public void recordFailedSave() {
            failureCount.incrementAndGet();
        }
        
        // Getters
        public String getSessionId() { return sessionId; }
        public Duration getInterval() { return interval; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public LocalDateTime getLastSaveTime() { return lastSaveTime; }
        public int getSaveCount() { return saveCount.get(); }
        public int getFailureCount() { return failureCount.get(); }
    }
}