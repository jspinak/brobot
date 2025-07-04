package io.github.jspinak.brobot.runner.session;

import lombok.Data;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jspinak.brobot.runner.config.ApplicationConfig;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Manages session state and recovery after application crashes.
 * Automatically saves session state periodically and on significant events.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Data
public class SessionRecoveryManager {

    private final ApplicationConfig applicationConfig;
    private final EventBus eventBus;
    private final ObjectMapper objectMapper;
    
    private static final String SESSION_DIR = "sessions";
    private static final String CURRENT_SESSION_FILE = "current_session.json";
    private static final String BACKUP_SESSION_FILE = "backup_session.json";
    private static final String RECOVERY_DIR = "recovery";
    
    private Path sessionDirectory;
    private Path currentSessionPath;
    private Path backupSessionPath;
    private Path recoveryDirectory;
    
    private SessionState currentSession;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
        r -> {
            Thread t = new Thread(r, "session-recovery");
            t.setDaemon(true);
            return t;
        }
    );
    
    private volatile boolean autoSaveEnabled = true;
    private volatile int autoSaveIntervalSeconds = 30;
    
    @PostConstruct
    public void initialize() {
        try {
            setupDirectories();
            
            // Try to recover previous session
            Optional<SessionState> recoveredSession = recoverSession();
            if (recoveredSession.isPresent()) {
                currentSession = recoveredSession.get();
                log.info("Recovered previous session: {}", currentSession.getSessionId());
                
                // Publish recovery event
                eventBus.publish(LogEvent.info(this, 
                    "Session recovered from " + currentSession.getStartTime(), 
                    "SessionRecovery"));
            } else {
                // Create new session
                currentSession = createNewSession();
                log.info("Started new session: {}", currentSession.getSessionId());
            }
            
            // Start auto-save
            startAutoSave();
            
        } catch (Exception e) {
            log.error("Failed to initialize session recovery", e);
            currentSession = createNewSession();
        }
    }
    
    @PreDestroy
    public void shutdown() {
        try {
            // Final save before shutdown
            saveSession();
            
            // Mark session as ended normally
            currentSession.setEndTime(LocalDateTime.now());
            currentSession.setEndedNormally(true);
            saveSession();
            
            scheduler.shutdown();
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (Exception e) {
            log.error("Error during session recovery shutdown", e);
        }
    }
    
    private void setupDirectories() throws IOException {
        Path baseDir = applicationConfig.getConfigDirectory().toPath();
        sessionDirectory = baseDir.resolve(SESSION_DIR);
        recoveryDirectory = sessionDirectory.resolve(RECOVERY_DIR);
        
        Files.createDirectories(sessionDirectory);
        Files.createDirectories(recoveryDirectory);
        
        currentSessionPath = sessionDirectory.resolve(CURRENT_SESSION_FILE);
        backupSessionPath = sessionDirectory.resolve(BACKUP_SESSION_FILE);
    }
    
    private SessionState createNewSession() {
        return SessionState.builder()
            .sessionId(UUID.randomUUID().toString())
            .startTime(LocalDateTime.now())
            .lastSaveTime(LocalDateTime.now())
            .applicationVersion(getApplicationVersion())
            .openConfigurations(new ArrayList<>())
            .windowStates(new HashMap<>())
            .userPreferences(new HashMap<>())
            .executionHistory(new ArrayList<>())
            .build();
    }
    
    /**
     * Save current session state.
     */
    public void saveSession() {
        if (currentSession == null) {
            return;
        }
        
        try {
            currentSession.setLastSaveTime(LocalDateTime.now());
            
            // Save to backup first
            if (Files.exists(currentSessionPath)) {
                Files.copy(currentSessionPath, backupSessionPath, 
                    StandardCopyOption.REPLACE_EXISTING);
            }
            
            // Save current session
            String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(currentSession);
            Files.writeString(currentSessionPath, json);
            
            log.debug("Session saved: {}", currentSession.getSessionId());
            
        } catch (Exception e) {
            log.error("Failed to save session", e);
        }
    }
    
    /**
     * Recover session from disk.
     */
    public Optional<SessionState> recoverSession() {
        // Try current session file first
        Optional<SessionState> session = loadSessionFromFile(currentSessionPath);
        if (session.isPresent() && isRecoverable(session.get())) {
            archiveSession(session.get());
            return session;
        }
        
        // Try backup if current failed
        session = loadSessionFromFile(backupSessionPath);
        if (session.isPresent() && isRecoverable(session.get())) {
            archiveSession(session.get());
            return session;
        }
        
        return Optional.empty();
    }
    
    private Optional<SessionState> loadSessionFromFile(Path path) {
        if (!Files.exists(path)) {
            return Optional.empty();
        }
        
        try {
            String json = Files.readString(path);
            SessionState session = objectMapper.readValue(json, SessionState.class);
            return Optional.of(session);
        } catch (Exception e) {
            log.error("Failed to load session from {}", path, e);
            return Optional.empty();
        }
    }
    
    private boolean isRecoverable(SessionState session) {
        // Don't recover if session ended normally
        if (session.isEndedNormally()) {
            return false;
        }
        
        // Don't recover very old sessions (> 24 hours)
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        return session.getLastSaveTime().isAfter(cutoff);
    }
    
    private void archiveSession(SessionState session) {
        try {
            String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("session_%s_%s.json", 
                session.getSessionId().substring(0, 8), timestamp);
            
            Path archivePath = recoveryDirectory.resolve(filename);
            String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(session);
            Files.writeString(archivePath, json);
            
            // Clean old archives (keep last 10)
            cleanOldArchives();
            
        } catch (Exception e) {
            log.error("Failed to archive session", e);
        }
    }
    
    private void cleanOldArchives() {
        try {
            List<Path> archives = Files.list(recoveryDirectory)
                .filter(p -> p.toString().endsWith(".json"))
                .sorted((a, b) -> {
                    try {
                        return Files.getLastModifiedTime(b)
                            .compareTo(Files.getLastModifiedTime(a));
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .collect(Collectors.toList());
                
            // Keep only last 10 archives
            if (archives.size() > 10) {
                for (int i = 10; i < archives.size(); i++) {
                    try {
                        Files.delete(archives.get(i));
                    } catch (IOException e) {
                        log.debug("Failed to delete old archive", e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to clean old archives", e);
        }
    }
    
    private void startAutoSave() {
        if (autoSaveEnabled) {
            scheduler.scheduleAtFixedRate(
                this::saveSession,
                autoSaveIntervalSeconds,
                autoSaveIntervalSeconds,
                TimeUnit.SECONDS
            );
        }
    }
    
    /**
     * Update configuration state in session.
     */
    public void updateConfiguration(String configPath, boolean opened) {
        if (opened) {
            if (!currentSession.getOpenConfigurations().contains(configPath)) {
                currentSession.getOpenConfigurations().add(configPath);
            }
        } else {
            currentSession.getOpenConfigurations().remove(configPath);
        }
        
        // Trigger save on next cycle
    }
    
    /**
     * Update window state in session.
     */
    public void updateWindowState(String windowId, WindowState state) {
        currentSession.getWindowStates().put(windowId, state);
    }
    
    /**
     * Update user preference in session.
     */
    public void updatePreference(String key, String value) {
        currentSession.getUserPreferences().put(key, value);
    }
    
    /**
     * Add execution to history.
     */
    public void addExecutionHistory(ExecutionRecord record) {
        currentSession.getExecutionHistory().add(record);
        
        // Keep only last 100 executions
        if (currentSession.getExecutionHistory().size() > 100) {
            currentSession.setExecutionHistory(
                currentSession.getExecutionHistory()
                    .subList(currentSession.getExecutionHistory().size() - 100,
                            currentSession.getExecutionHistory().size())
            );
        }
    }
    
    /**
     * Get current session state.
     */
    public SessionState getCurrentSession() {
        return currentSession;
    }
    
    /**
     * Get recovery suggestions based on crashed session.
     */
    public RecoverySuggestions getRecoverySuggestions(SessionState crashedSession) {
        List<String> suggestions = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Check for open configurations
        if (!crashedSession.getOpenConfigurations().isEmpty()) {
            suggestions.add("Reopen configurations: " + 
                String.join(", ", crashedSession.getOpenConfigurations()));
        }
        
        // Check for recent executions
        if (!crashedSession.getExecutionHistory().isEmpty()) {
            ExecutionRecord lastExecution = crashedSession.getExecutionHistory()
                .get(crashedSession.getExecutionHistory().size() - 1);
            
            if (!lastExecution.isCompleted()) {
                warnings.add("Last execution was not completed: " + 
                    lastExecution.getConfigurationName());
                suggestions.add("Check execution logs for errors");
            }
        }
        
        // Check session duration
        long sessionDurationMinutes = java.time.Duration.between(
            crashedSession.getStartTime(), 
            crashedSession.getLastSaveTime()
        ).toMinutes();
        
        if (sessionDurationMinutes < 1) {
            warnings.add("Session crashed shortly after startup");
            suggestions.add("Check application logs for startup errors");
        }
        
        return new RecoverySuggestions(suggestions, warnings);
    }
    
    private String getApplicationVersion() {
        return getClass().getPackage().getImplementationVersion() != null ?
            getClass().getPackage().getImplementationVersion() : "unknown";
    }
    
    /**
     * Clear current session and start fresh.
     */
    public void clearSession() {
        currentSession = createNewSession();
        saveSession();
        log.info("Session cleared, new session: {}", currentSession.getSessionId());
    }
    
    /**
     * Export session for debugging.
     */
    public void exportSession(Path outputPath) throws IOException {
        String json = objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(currentSession);
        Files.writeString(outputPath, json);
    }
    
    public void setAutoSaveEnabled(boolean enabled) {
        this.autoSaveEnabled = enabled;
    }
    
    public void setAutoSaveInterval(int seconds) {
        this.autoSaveIntervalSeconds = seconds;
    }
}