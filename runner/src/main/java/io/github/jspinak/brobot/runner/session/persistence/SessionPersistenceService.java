package io.github.jspinak.brobot.runner.session.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.session.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handles persistence operations for sessions including saving, loading, and deleting.
 * 
 * This service is responsible for all file I/O operations related to sessions,
 * including serialization/deserialization and file management.
 * 
 * Thread Safety: This class is thread-safe.
 * 
 * @since 1.0.0
 */
@Slf4j
@Service("legacySessionPersistenceService")
public class SessionPersistenceService implements DiagnosticCapable {
    
    private static final String SESSION_FILE_EXTENSION = ".json";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    @Value("${brobot.sessions.storage-path:sessions}")
    private String sessionStoragePathConfig;
    
    private Path sessionStoragePath;
    private final ObjectMapper objectMapper;
    
    // Statistics
    private final AtomicInteger sessionsSaved = new AtomicInteger(0);
    private final AtomicInteger sessionsLoaded = new AtomicInteger(0);
    private final AtomicInteger sessionsDeleted = new AtomicInteger(0);
    
    // Diagnostic mode
    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);
    
    public SessionPersistenceService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules(); // This will find and register JavaTimeModule
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    @PostConstruct
    public void initialize() {
        sessionStoragePath = Paths.get(sessionStoragePathConfig);
        
        try {
            Files.createDirectories(sessionStoragePath);
            log.info("Session storage initialized at: {}", sessionStoragePath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to create session storage directory", e);
            throw new RuntimeException("Failed to initialize session storage", e);
        }
    }
    
    /**
     * Saves a session to disk.
     * 
     * @param session the session to save
     * @return path where session was saved
     * @throws IOException if save fails
     */
    public Path saveSession(Session session) throws IOException {
        if (session == null || session.getId() == null) {
            throw new IllegalArgumentException("Session and session ID must not be null");
        }
        
        Path sessionFile = getSessionPath(session.getId());
        
        log.info("Saving session {} to {}", session.getId(), sessionFile);
        
        try {
            // Ensure parent directory exists
            Files.createDirectories(sessionFile.getParent());
            
            // Write session to file
            objectMapper.writeValue(sessionFile.toFile(), session);
            
            sessionsSaved.incrementAndGet();
            
            if (diagnosticMode.get()) {
                long fileSize = Files.size(sessionFile);
                log.info("[DIAGNOSTIC] Session saved - ID: {}, Size: {} bytes, Path: {}",
                        session.getId(), fileSize, sessionFile);
            }
            
            return sessionFile;
            
        } catch (IOException e) {
            log.error("Failed to save session {}", session.getId(), e);
            throw new IOException("Failed to save session: " + e.getMessage(), e);
        }
    }
    
    /**
     * Loads a session from disk.
     * 
     * @param sessionId the session ID to load
     * @return loaded session or empty if not found
     */
    public Optional<Session> loadSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return Optional.empty();
        }
        
        Path sessionFile = getSessionPath(sessionId);
        
        if (!Files.exists(sessionFile)) {
            log.warn("Session file not found: {}", sessionFile);
            return Optional.empty();
        }
        
        log.info("Loading session {} from {}", sessionId, sessionFile);
        
        try {
            Session session = objectMapper.readValue(sessionFile.toFile(), Session.class);
            
            sessionsLoaded.incrementAndGet();
            
            if (diagnosticMode.get()) {
                long fileSize = Files.size(sessionFile);
                log.info("[DIAGNOSTIC] Session loaded - ID: {}, Size: {} bytes, Active: {}",
                        session.getId(), fileSize, session.isActive());
            }
            
            return Optional.of(session);
            
        } catch (IOException e) {
            log.error("Failed to load session {}", sessionId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Deletes a session from disk.
     * 
     * @param sessionId the session ID to delete
     * @return true if deleted successfully
     */
    public boolean deleteSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return false;
        }
        
        Path sessionFile = getSessionPath(sessionId);
        
        if (!Files.exists(sessionFile)) {
            log.warn("Session file not found for deletion: {}", sessionFile);
            return false;
        }
        
        log.info("Deleting session {} at {}", sessionId, sessionFile);
        
        try {
            Files.delete(sessionFile);
            sessionsDeleted.incrementAndGet();
            
            if (diagnosticMode.get()) {
                log.info("[DIAGNOSTIC] Session deleted - ID: {}, Path: {}", sessionId, sessionFile);
            }
            
            return true;
            
        } catch (IOException e) {
            log.error("Failed to delete session {}", sessionId, e);
            return false;
        }
    }
    
    /**
     * Gets the file path for a session.
     * 
     * @param sessionId the session ID
     * @return path to session file
     */
    public Path getSessionPath(String sessionId) {
        return sessionStoragePath.resolve(sessionId + SESSION_FILE_EXTENSION);
    }
    
    /**
     * Creates a backup of a session.
     * 
     * @param sessionId the session ID to backup
     * @return path to backup file or empty if failed
     */
    public Optional<Path> backupSession(String sessionId) {
        Optional<Session> session = loadSession(sessionId);
        if (session.isEmpty()) {
            return Optional.empty();
        }
        
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String backupId = sessionId + "_backup_" + timestamp;
        Path backupPath = sessionStoragePath.resolve("backups").resolve(backupId + SESSION_FILE_EXTENSION);
        
        try {
            Files.createDirectories(backupPath.getParent());
            Session backupSession = session.get();
            backupSession.setId(backupId);
            objectMapper.writeValue(backupPath.toFile(), backupSession);
            
            log.info("Created backup of session {} at {}", sessionId, backupPath);
            return Optional.of(backupPath);
            
        } catch (IOException e) {
            log.error("Failed to create backup of session {}", sessionId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Checks if a session exists on disk.
     * 
     * @param sessionId the session ID to check
     * @return true if session file exists
     */
    public boolean sessionExists(String sessionId) {
        return Files.exists(getSessionPath(sessionId));
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> states = new ConcurrentHashMap<>();
        states.put("storagePath", sessionStoragePath.toAbsolutePath().toString());
        states.put("sessionsSaved", sessionsSaved.get());
        states.put("sessionsLoaded", sessionsLoaded.get());
        states.put("sessionsDeleted", sessionsDeleted.get());
        
        try {
            long storageSize = Files.walk(sessionStoragePath)
                    .filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .sum();
            states.put("totalStorageSizeMB", storageSize / (1024 * 1024));
            
            long sessionCount = Files.walk(sessionStoragePath)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(SESSION_FILE_EXTENSION))
                    .count();
            states.put("totalSessionFiles", sessionCount);
            
        } catch (IOException e) {
            states.put("storageError", e.getMessage());
        }
        
        return DiagnosticInfo.builder()
                .component("SessionPersistenceService")
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
}