package io.github.jspinak.brobot.app.log;

import io.github.jspinak.brobot.log.entities.LogEntry;
import io.github.jspinak.brobot.log.service.LogEntryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class LocalLogEntryService implements LogEntryService {

    private final LogEntryRepository logEntryRepository;
    private final LogSender logSender;

    public LocalLogEntryService(LogEntryRepository logEntryRepository, LogSender logSender) {
        this.logEntryRepository = logEntryRepository;
        this.logSender = logSender;
    }

    @Override
    public LogEntry saveLog(LogEntry logEntry) {
        System.out.println("Saving LogEntry: " + logEntry);
        System.out.println("StateImages: " + logEntry.getStateImages());

        LogEntry savedLogEntry = logEntryRepository.save(logEntry);
        logSender.sendLog(logEntry);  // Send to client app
        return savedLogEntry;
    }

    @Override
    public List<LogEntry> getAllLogs() {
        return logEntryRepository.findAll();
    }

    @Override
    public LogEntry getLogById(String id) {
        return logEntryRepository.findById(id).orElseThrow(() -> new RuntimeException("Log not found"));
    }

    @Override
    public List<LogEntry> getRecentLogs(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        return logEntryRepository.findAll(pageRequest).getContent();
    }

    @Override
    public List<LogEntry> getLogEntriesBySessionId(String sessionId) {
        return logEntryRepository.findBySessionId(sessionId);
    }

    @Override
    public List<LogEntry> getFailedLogEntries() {
        return logEntryRepository.findByPassedFalse();
    }

    @Override
    public List<LogEntry> getLogEntriesByState(String state) {
        return logEntryRepository.findByCurrentStateName(state);
    }

    @Override
    public List<LogEntry> getLogEntriesByType(String type) {
        return logEntryRepository.findByType(type);
    }

    public List<LogEntry> getLogEntriesBetween(Instant startTime, Instant endTime) {
        return logEntryRepository.findByTimestampBetween(startTime, endTime);
    }

}
