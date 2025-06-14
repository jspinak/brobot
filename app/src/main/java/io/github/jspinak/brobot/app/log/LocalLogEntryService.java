package io.github.jspinak.brobot.app.log;

import io.github.jspinak.brobot.report.log.model.LogData;
import io.github.jspinak.brobot.report.log.model.LogType;
import io.github.jspinak.brobot.report.log.service.LogEntryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class LocalLogEntryService implements LogEntryService {
    private final LogEntryRepository logEntryRepository;

    public LocalLogEntryService(LogEntryRepository logEntryRepository) {
        this.logEntryRepository = logEntryRepository;
    }

    @Override
    public LogData saveLog(LogData logData) {
        LogData savedLog = logEntryRepository.save(logData);
        return savedLog;
    }

    @Override
    public List<LogData> getAllLogs() {
        return logEntryRepository.findAll();
    }

    @Override
    public LogData getLogById(String id) {
        return logEntryRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new RuntimeException("Log not found"));
    }

    @Override
    public List<LogData> getRecentLogs(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        return logEntryRepository.findAll(pageRequest).getContent();
    }

    @Override
    public List<LogData> getLogEntriesBySessionId(String sessionId) {
        return logEntryRepository.findBySessionId(sessionId);
    }

    @Override
    public List<LogData> getFailedLogEntries() {
        return logEntryRepository.findBySuccessFalse();
    }

    @Override
    public List<LogData> getLogEntriesByState(String state) {
        return logEntryRepository.findByCurrentStateName(state);
    }

    @Override
    public List<LogData> getLogEntriesByType(String type) {
        return logEntryRepository.findByType(LogType.valueOf(type));
    }

    @Override
    public List<LogData> getLogEntriesBetween(Instant startTime, Instant endTime) {
        return logEntryRepository.findByTimestampBetween(startTime, endTime);
    }

}
