package io.github.jspinak.brobot.app.log;

import io.github.jspinak.brobot.log.entities.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface LogEntryRepository extends JpaRepository<LogEntry, String> {
    List<LogEntry> findBySessionId(String sessionId);
    List<LogEntry> findByPassedFalse();
    List<LogEntry> findByCurrentStateName(String state);
    List<LogEntry> findByType(String type);
    List<LogEntry> findByTimestampBetween(Instant start, Instant end);
    List<LogEntry> findBySessionIdAndPassedFalse(String sessionId);
    List<LogEntry> findByTypeAndPassedFalse(String type);
}

