package io.github.jspinak.brobot.runner.session.discovery;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.session.SessionSummary;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for discovering and searching stored sessions.
 *
 * <p>This service provides functionality to list, search, and filter saved sessions based on
 * various criteria.
 *
 * <p>Thread Safety: This class is thread-safe.
 *
 * @since 1.0.0
 */
@Slf4j
@Service
public class SessionDiscoveryService implements DiagnosticCapable {

    private static final String SESSION_FILE_EXTENSION = ".json";
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Value("${brobot.sessions.storage-path:sessions}")
    private String sessionStoragePathConfig;

    private Path sessionStoragePath;
    private final ObjectMapper objectMapper;

    // Cache for session summaries
    private final Map<String, SessionSummary> summaryCache = new ConcurrentHashMap<>();
    private volatile LocalDateTime cacheLastUpdated;
    private static final long CACHE_VALIDITY_MINUTES = 5;

    // Diagnostic mode
    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);

    public SessionDiscoveryService() {
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void initialize() {
        sessionStoragePath = Paths.get(sessionStoragePathConfig);

        if (!Files.exists(sessionStoragePath)) {
            try {
                Files.createDirectories(sessionStoragePath);
                log.info("Created session storage directory: {}", sessionStoragePath);
            } catch (IOException e) {
                log.error("Failed to create session storage directory", e);
            }
        }
    }

    /**
     * Lists all available session summaries.
     *
     * @return list of session summaries
     */
    public List<SessionSummary> listAvailableSessions() {
        log.debug("Listing all available sessions");

        // Check cache validity
        if (isCacheValid()) {
            return new ArrayList<>(summaryCache.values());
        }

        // Refresh cache
        refreshCache();

        List<SessionSummary> summaries = new ArrayList<>(summaryCache.values());

        // Sort by start time, most recent first
        summaries.sort(
                (s1, s2) -> {
                    LocalDateTime t1 = s1.getStartTime();
                    LocalDateTime t2 = s2.getStartTime();
                    if (t1 == null || t2 == null) return 0;
                    return t2.compareTo(t1);
                });

        if (diagnosticMode.get()) {
            log.info("[DIAGNOSTIC] Listed {} sessions", summaries.size());
        }

        return summaries;
    }

    /**
     * Finds a specific session by ID.
     *
     * @param sessionId the session ID to find
     * @return session summary or empty if not found
     */
    public Optional<SessionSummary> findSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return Optional.empty();
        }

        // Check cache first
        SessionSummary cached = summaryCache.get(sessionId);
        if (cached != null && isCacheValid()) {
            return Optional.of(cached);
        }

        // Load from disk
        Path sessionFile = sessionStoragePath.resolve(sessionId + SESSION_FILE_EXTENSION);
        if (!Files.exists(sessionFile)) {
            return Optional.empty();
        }

        try {
            SessionSummary summary = loadSessionSummary(sessionFile);
            if (summary != null) {
                summaryCache.put(sessionId, summary);
                return Optional.of(summary);
            }
        } catch (IOException e) {
            log.error("Failed to load session summary: {}", sessionId, e);
        }

        return Optional.empty();
    }

    /**
     * Finds sessions by date.
     *
     * @param date the date to search for
     * @return list of sessions from that date
     */
    public List<SessionSummary> findSessionsByDate(LocalDate date) {
        if (date == null) {
            return Collections.emptyList();
        }

        log.debug("Finding sessions for date: {}", date);

        return listAvailableSessions().stream()
                .filter(
                        summary -> {
                            LocalDateTime startTime = summary.getStartTime();
                            return startTime != null && startTime.toLocalDate().equals(date);
                        })
                .collect(Collectors.toList());
    }

    /**
     * Finds sessions by project name.
     *
     * @param projectName the project name to search for
     * @return list of sessions for that project
     */
    public List<SessionSummary> findSessionsByProject(String projectName) {
        if (projectName == null || projectName.isEmpty()) {
            return Collections.emptyList();
        }

        log.debug("Finding sessions for project: {}", projectName);

        return listAvailableSessions().stream()
                .filter(summary -> projectName.equals(summary.getProjectName()))
                .collect(Collectors.toList());
    }

    /**
     * Searches sessions by keyword in session name or project name.
     *
     * @param keyword the keyword to search for
     * @return list of matching sessions
     */
    public List<SessionSummary> searchSessions(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return listAvailableSessions();
        }

        String lowerKeyword = keyword.toLowerCase();

        return listAvailableSessions().stream()
                .filter(
                        summary -> {
                            String sessionName = summary.getSessionName();
                            String projectName = summary.getProjectName();

                            return (sessionName != null
                                            && sessionName.toLowerCase().contains(lowerKeyword))
                                    || (projectName != null
                                            && projectName.toLowerCase().contains(lowerKeyword));
                        })
                .collect(Collectors.toList());
    }

    /**
     * Gets recent sessions up to a specified limit.
     *
     * @param limit maximum number of sessions to return
     * @return list of recent sessions
     */
    public List<SessionSummary> getRecentSessions(int limit) {
        return listAvailableSessions().stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> states = new ConcurrentHashMap<>();
        states.put("storagePath", sessionStoragePath.toAbsolutePath().toString());
        states.put("cachedSessions", summaryCache.size());
        states.put("cacheLastUpdated", cacheLastUpdated);
        states.put("cacheValid", isCacheValid());

        try {
            long fileCount =
                    Files.walk(sessionStoragePath, 1)
                            .filter(Files::isRegularFile)
                            .filter(p -> p.toString().endsWith(SESSION_FILE_EXTENSION))
                            .count();
            states.put("totalSessionFiles", fileCount);
        } catch (IOException e) {
            states.put("fileCountError", e.getMessage());
        }

        return DiagnosticInfo.builder().component("SessionDiscoveryService").states(states).build();
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

    /** Checks if the cache is still valid. */
    private boolean isCacheValid() {
        if (cacheLastUpdated == null) {
            return false;
        }

        return cacheLastUpdated.plusMinutes(CACHE_VALIDITY_MINUTES).isAfter(LocalDateTime.now());
    }

    /** Refreshes the session summary cache. */
    private void refreshCache() {
        log.debug("Refreshing session summary cache");

        summaryCache.clear();

        try (Stream<Path> paths = Files.walk(sessionStoragePath, 1)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(SESSION_FILE_EXTENSION))
                    .forEach(
                            path -> {
                                try {
                                    SessionSummary summary = loadSessionSummary(path);
                                    if (summary != null && summary.getId() != null) {
                                        summaryCache.put(summary.getId(), summary);
                                    }
                                } catch (IOException e) {
                                    log.warn("Failed to load session summary from: {}", path, e);
                                }
                            });
        } catch (IOException e) {
            log.error("Failed to scan session directory", e);
        }

        cacheLastUpdated = LocalDateTime.now();

        if (diagnosticMode.get()) {
            log.info("[DIAGNOSTIC] Cache refreshed - Found {} sessions", summaryCache.size());
        }
    }

    /** Loads a session summary from a file. */
    private SessionSummary loadSessionSummary(Path sessionFile) throws IOException {
        JsonNode root = objectMapper.readTree(sessionFile.toFile());

        SessionSummary summary = new SessionSummary();
        summary.setId(getStringValue(root, "id"));
        summary.setProjectName(getStringValue(root, "projectName"));
        summary.setSessionName(getStringValue(root, "sessionName"));
        summary.setStartTime(getDateTimeValue(root, "startTime"));
        summary.setEndTime(getDateTimeValue(root, "endTime"));
        summary.setActive(getBooleanValue(root, "active"));
        summary.setConfigPath(getStringValue(root, "configPath"));

        // Calculate file size
        try {
            summary.setFileSize(Files.size(sessionFile));
        } catch (IOException e) {
            summary.setFileSize(0L);
        }

        // Generate session name if not present
        if (summary.getSessionName() == null || summary.getSessionName().isEmpty()) {
            summary.setSessionName(generateSessionName(summary));
        }

        return summary;
    }

    /** Gets a string value from JSON node. */
    private String getStringValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return field != null && !field.isNull() ? field.asText() : null;
    }

    /** Gets a date-time value from JSON node. */
    private LocalDateTime getDateTimeValue(JsonNode node, String fieldName) {
        String value = getStringValue(node, fieldName);
        if (value == null) {
            return null;
        }

        try {
            return LocalDateTime.parse(value, DATE_TIME_FORMAT);
        } catch (Exception e) {
            log.warn("Failed to parse date-time: {}", value);
            return null;
        }
    }

    /** Gets a boolean value from JSON node. */
    private Boolean getBooleanValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return field != null && !field.isNull() ? field.asBoolean() : null;
    }

    /** Generates a session name if not present. */
    private String generateSessionName(SessionSummary summary) {
        if (summary.getStartTime() != null) {
            return "Session "
                    + summary.getStartTime()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
        return "Session " + summary.getId().substring(0, 8);
    }
}
