package io.github.jspinak.brobot.runner.persistence;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.persistence.entity.LogEntry;
import io.github.jspinak.brobot.runner.persistence.mapper.LogEntityMapper;
import io.github.jspinak.brobot.runner.persistence.repo.LogEntryRepository;
import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.logging.spi.LogSink;

@Component // This class is the Spring bean that gets injected into the library
public class DatabaseLogSink implements LogSink {

    private final LogEntryRepository logRepo;
    private final LogEntityMapper mapper;

    public DatabaseLogSink(LogEntryRepository logRepo, LogEntityMapper mapper) {
        this.logRepo = logRepo;
        this.mapper = mapper;
    }

    @Override
    public void save(LogData logData) {
        // 1. Map from the library's DTO to the runner's JPA Entity
        LogEntry logEntry = mapper.toEntity(logData);

        // 2. Use the runner's repository to save to the database
        if (logEntry != null) {
            logRepo.save(logEntry);
        }
    }
}
