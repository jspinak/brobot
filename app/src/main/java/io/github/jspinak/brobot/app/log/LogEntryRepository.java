package io.github.jspinak.brobot.app.log;

import io.github.jspinak.brobot.log.entities.LogEntry;
import io.github.jspinak.brobot.log.entities.LogType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {
    List<LogEntry> findBySessionId(String sessionId);
    List<LogEntry> findBySuccessFalse();
    List<LogEntry> findByCurrentStateName(String state);
    List<LogEntry> findByType(LogType type);
    List<LogEntry> findByTimestampBetween(Instant start, Instant end);
    List<LogEntry> findByProjectIdOrderByTimestampDesc(Long projectId, Pageable pageable);
    List<LogEntry> findBySessionIdAndSuccessFalse(String sessionId);
    List<LogEntry> findByTypeAndSuccessFalse(String type);
}

