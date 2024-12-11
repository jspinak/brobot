package io.github.jspinak.brobot.log.service;

import io.github.jspinak.brobot.log.entities.LogEntry;
import io.github.jspinak.brobot.log.entities.LogType;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface LogEntryService {
    default LogEntry saveLog(LogEntry logEntry) {
        // No-op implementation
        return null;
    }

    default List<LogEntry> getAllLogs() {
        // No-op implementation, returning an empty list
        return Collections.emptyList();
    }

    default LogEntry getLogById(String id) {
        // No-op implementation, returning null
        return null;
    }

    default List<LogEntry> getRecentLogs(int limit) {
        // No-op implementation, returning an empty list
        return Collections.emptyList();
    }

    default List<LogEntry> getRecentLogsByProjectId(Long projectId, int limit) {
        // No-op implementation, returning an empty list
        return Collections.emptyList();
    }

    default List<LogEntry> getLogEntriesBySessionId(String sessionId) {
        // No-op implementation, returning an empty list
        return Collections.emptyList();
    }

    default List<LogEntry> getFailedLogEntries() {
        // No-op implementation, returning an empty list
        return Collections.emptyList();
    }

    default List<LogEntry> getLogEntriesByState(String stateName) {
        // No-op implementation, returning an empty list
        return Collections.emptyList();
    }

    default List<LogEntry> getLogEntriesByType(String type) {
        // No-op implementation, returning an empty list
        return Collections.emptyList();
    }

    default List<LogEntry> getLogEntriesBetween(Instant startTime, Instant endTime) {
        // No-op implementation, returning an empty list
        return Collections.emptyList();
    }

    /**
     * Find a log entry by its ID
     * @param id the ID of the log entry
     * @return Optional containing the log entry if found
     */
    default Optional<LogEntry> findById(Long id) {
        // No-op implementation
        return Optional.empty();
    }

    /**
     * Get paginated logs with optional filtering
     * @param page page number (0-based)
     * @param size number of items per page
     * @param projectId optional project ID filter
     * @param type optional log type filter
     * @param sessionId optional session ID filter
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @return Page of LogEntry objects
     */
    default Page<LogEntry> getLogs(int page,
                                   int size,
                                   Long projectId,
                                   LogType type,
                                   String sessionId,
                                   LocalDateTime startDate,
                                   LocalDateTime endDate) {
        // No-op implementation
        return Page.empty();
    }
}
