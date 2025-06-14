package io.github.jspinak.brobot.app.log;

import io.github.jspinak.brobot.report.log.model.LogData;
import io.github.jspinak.brobot.report.log.model.LogType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface LogEntryRepository extends JpaRepository<LogData, Long> {
    List<LogData> findBySessionId(String sessionId);
    List<LogData> findBySuccessFalse();
    List<LogData> findByCurrentStateName(String state);
    List<LogData> findByType(LogType type);
    List<LogData> findByTimestampBetween(Instant start, Instant end);
    List<LogData> findByProjectIdOrderByTimestampDesc(Long projectId, Pageable pageable);
    List<LogData> findBySessionIdAndSuccessFalse(String sessionId);
    List<LogData> findByTypeAndSuccessFalse(String type);
}

