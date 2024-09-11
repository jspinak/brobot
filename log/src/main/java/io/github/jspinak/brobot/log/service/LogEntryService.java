package io.github.jspinak.brobot.log.service;

import io.github.jspinak.brobot.log.entities.LogEntry;

import java.util.List;

public interface LogEntryService {
    LogEntry saveLog(LogEntry logEntry);
    List<LogEntry> getAllLogs();
    LogEntry getLogById(String id);
    List<LogEntry> getRecentLogs(int limit);
    List<LogEntry> getLogEntriesBySessionId(String sessionId);
    List<LogEntry> getFailedLogEntries();
    List<LogEntry> getLogEntriesByState(String stateName);
    List<LogEntry> getLogEntriesByType(String type);
}
