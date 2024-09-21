package io.github.jspinak.brobot.log.service;

import io.github.jspinak.brobot.log.entities.LogEntry;

import java.util.Collections;
import java.util.List;

public interface LogEntryService {
    default LogEntry saveLog(LogEntry logEntry) {
        // No-op implementation, returning null
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
}
