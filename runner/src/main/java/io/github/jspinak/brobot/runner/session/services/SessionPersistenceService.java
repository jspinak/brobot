package io.github.jspinak.brobot.runner.session.services;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.session.Session;
import io.github.jspinak.brobot.runner.session.SessionSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for persisting and loading sessions from disk.
 * Handles all file I/O operations related to session storage.
 */
@Slf4j
@Service("sessionPersistenceService")
public class SessionPersistenceService {
    
    private static final String SESSION_PREFIX = "session_";
    private static final String SESSION_EXTENSION = ".json";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    private final EventBus eventBus;
    private final BrobotRunnerProperties properties;
    private final ConfigurationParser jsonParser;
    
    private Path sessionStoragePath;
    
    @Autowired
    public SessionPersistenceService(
            EventBus eventBus,
            BrobotRunnerProperties properties,
            ConfigurationParser jsonParser) {
        
        this.eventBus = eventBus;
        this.properties = properties;
        this.jsonParser = jsonParser;
        
        initializeStoragePath();
    }
    
    /**
     * Initializes the session storage directory.
     */
    private void initializeStoragePath() {
        sessionStoragePath = Paths.get(properties.getLogPath(), "sessions");
        
        try {
            Files.createDirectories(sessionStoragePath);
            log.info("Session storage directory initialized: {}", sessionStoragePath);
            eventBus.publish(LogEvent.info(this,
                    "Session directory created: " + sessionStoragePath, "Session"));
        } catch (IOException e) {
            log.error("Failed to create session directory", e);
            eventBus.publish(LogEvent.error(this,
                    "Failed to create session directory: " + e.getMessage(), "Session", e));
        }
    }
    
    /**
     * Saves a session to disk.
     */
    public void saveSession(Session session) {
        if (session == null) {
            log.warn("Cannot save null session");
            return;
        }
        
        String filename = generateSessionFilename(session.getId());
        Path filePath = sessionStoragePath.resolve(filename);
        
        try {
            // Use the safe serialization approach for complex objects
            String json = jsonParser.toPrettyJsonSafe(session);
            Files.writeString(filePath, json);
            
            log.debug("Session saved: {} to {}", session.getId(), filePath);
            eventBus.publish(LogEvent.debug(this,
                    "Session saved: " + session.getId(), "Session"));
        } catch (Exception e) {
            log.error("Failed to save session: {}", session.getId(), e);
            eventBus.publish(LogEvent.error(this,
                    "Failed to save session: " + e.getMessage(), "Session", e));
        }
    }
    
    /**
     * Loads a session by ID.
     */
    public Optional<Session> loadSession(String sessionId) {
        File[] files = findSessionFiles(sessionId);
        
        if (files == null || files.length == 0) {
            log.warn("No session found with ID: {}", sessionId);
            eventBus.publish(LogEvent.warning(this,
                    "No session found with ID: " + sessionId, "Session"));
            return Optional.empty();
        }
        
        // Find the latest session file
        File latestFile = findLatestFile(files);
        
        try {
            String json = Files.readString(latestFile.toPath());
            Session loadedSession = jsonParser.convertJson(json, Session.class);
            
            log.info("Session loaded: {} from {}", sessionId, latestFile.getName());
            eventBus.publish(LogEvent.info(this,
                    "Session loaded: " + sessionId, "Session"));
            
            return Optional.of(loadedSession);
        } catch (Exception e) {
            log.error("Failed to load session: {}", sessionId, e);
            eventBus.publish(LogEvent.error(this,
                    "Failed to load session: " + e.getMessage(), "Session", e));
            return Optional.empty();
        }
    }
    
    /**
     * Gets a list of all available session summaries.
     */
    public List<SessionSummary> getAllSessionSummaries() {
        List<SessionSummary> summaries = new ArrayList<>();
        File[] files = sessionStoragePath.toFile().listFiles(
                (dir, name) -> name.startsWith(SESSION_PREFIX) && name.endsWith(SESSION_EXTENSION));
        
        if (files == null || files.length == 0) {
            return summaries;
        }
        
        // Group files by session ID
        Map<String, List<File>> sessionFiles = groupFilesBySessionId(files);
        
        // Create summaries from the latest file for each session
        for (Map.Entry<String, List<File>> entry : sessionFiles.entrySet()) {
            createSessionSummary(entry.getKey(), entry.getValue())
                .ifPresent(summaries::add);
        }
        
        // Sort by start time (most recent first)
        summaries.sort(Comparator.comparing(SessionSummary::getStartTime).reversed());
        
        return summaries;
    }
    
    /**
     * Deletes all files for a session.
     */
    public boolean deleteSession(String sessionId) {
        File[] files = findSessionFiles(sessionId);
        
        if (files == null || files.length == 0) {
            log.warn("No session found to delete with ID: {}", sessionId);
            eventBus.publish(LogEvent.warning(this,
                    "No session found to delete with ID: " + sessionId, "Session"));
            return false;
        }
        
        boolean allDeleted = Arrays.stream(files)
            .map(this::deleteFile)
            .reduce(true, (a, b) -> a && b);
        
        if (allDeleted) {
            log.info("Session deleted: {}", sessionId);
            eventBus.publish(LogEvent.info(this,
                    "Session deleted: " + sessionId, "Session"));
        }
        
        return allDeleted;
    }
    
    /**
     * Generates a filename for a session.
     */
    private String generateSessionFilename(String sessionId) {
        String timestamp = TIMESTAMP_FORMAT.format(LocalDateTime.now());
        return SESSION_PREFIX + sessionId + "_" + timestamp + SESSION_EXTENSION;
    }
    
    /**
     * Finds all files for a given session ID.
     */
    private File[] findSessionFiles(String sessionId) {
        return sessionStoragePath.toFile().listFiles(
                (dir, name) -> name.contains(SESSION_PREFIX + sessionId));
    }
    
    /**
     * Finds the latest file from an array of files.
     */
    private File findLatestFile(File[] files) {
        return Arrays.stream(files)
            .max(Comparator.comparingLong(File::lastModified))
            .orElse(files[0]);
    }
    
    /**
     * Groups session files by session ID.
     */
    private Map<String, List<File>> groupFilesBySessionId(File[] files) {
        Map<String, List<File>> sessionFiles = new HashMap<>();
        
        for (File file : files) {
            extractSessionId(file.getName())
                .ifPresent(sessionId -> 
                    sessionFiles.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(file));
        }
        
        return sessionFiles;
    }
    
    /**
     * Extracts session ID from filename.
     */
    private Optional<String> extractSessionId(String filename) {
        if (!filename.startsWith(SESSION_PREFIX) || !filename.endsWith(SESSION_EXTENSION)) {
            return Optional.empty();
        }
        
        int startIndex = SESSION_PREFIX.length();
        int endIndex = filename.indexOf('_', startIndex);
        
        if (endIndex < 0) {
            return Optional.empty();
        }
        
        return Optional.of(filename.substring(startIndex, endIndex));
    }
    
    /**
     * Creates a session summary from session files.
     */
    private Optional<SessionSummary> createSessionSummary(String sessionId, List<File> sessionFiles) {
        // Find the latest file
        File latestFile = sessionFiles.stream()
            .max(Comparator.comparing(File::lastModified))
            .orElse(null);
        
        if (latestFile == null) {
            return Optional.empty();
        }
        
        try {
            String json = Files.readString(latestFile.toPath());
            JsonNode rootNode = jsonParser.parseJson(json);
            
            SessionSummary summary = new SessionSummary();
            summary.setId(sessionId);
            summary.setProjectName(getStringValue(rootNode, "projectName"));
            summary.setStartTime(getDateTimeValue(rootNode, "startTime"));
            summary.setEndTime(getDateTimeValue(rootNode, "endTime"));
            summary.setLastSaved(LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(latestFile.lastModified()),
                    java.time.ZoneId.systemDefault()));
            summary.setActive(getBooleanValue(rootNode, "active"));
            
            return Optional.of(summary);
        } catch (Exception e) {
            log.error("Error reading session file: {}", latestFile.getName(), e);
            return Optional.empty();
        }
    }
    
    /**
     * Deletes a single file.
     */
    private boolean deleteFile(File file) {
        try {
            boolean deleted = file.delete();
            if (!deleted) {
                log.warn("Failed to delete session file: {}", file.getName());
                eventBus.publish(LogEvent.warning(this,
                        "Failed to delete session file: " + file.getName(), "Session"));
            }
            return deleted;
        } catch (Exception e) {
            log.error("Error deleting session file: {}", file.getName(), e);
            eventBus.publish(LogEvent.error(this,
                    "Error deleting session file: " + e.getMessage(), "Session", e));
            return false;
        }
    }
    
    /**
     * Gets the session storage path.
     */
    public Path getSessionStoragePath() {
        return sessionStoragePath;
    }
    
    // JSON parsing helper methods
    
    private String getStringValue(JsonNode node, String fieldName) {
        return Optional.ofNullable(node)
            .filter(n -> n.has(fieldName))
            .map(n -> n.get(fieldName))
            .filter(n -> !n.isNull())
            .map(JsonNode::asText)
            .orElse(null);
    }
    
    private LocalDateTime getDateTimeValue(JsonNode node, String fieldName) {
        return Optional.ofNullable(node)
            .filter(n -> n.has(fieldName))
            .map(n -> n.get(fieldName))
            .filter(n -> !n.isNull())
            .map(JsonNode::asText)
            .map(text -> {
                try {
                    return LocalDateTime.parse(text);
                } catch (Exception e) {
                    return null;
                }
            })
            .orElse(null);
    }
    
    private Boolean getBooleanValue(JsonNode node, String fieldName) {
        return Optional.ofNullable(node)
            .filter(n -> n.has(fieldName))
            .map(n -> n.get(fieldName))
            .filter(n -> !n.isNull())
            .map(JsonNode::asBoolean)
            .orElse(null);
    }
}