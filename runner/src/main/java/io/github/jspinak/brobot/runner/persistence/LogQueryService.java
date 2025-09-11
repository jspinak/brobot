package io.github.jspinak.brobot.runner.persistence;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.jspinak.brobot.runner.persistence.entity.LogEntry;
import io.github.jspinak.brobot.runner.persistence.mapper.LogMapper;
import io.github.jspinak.brobot.runner.persistence.repo.LogEntryRepository;
import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;

import lombok.Data;

@Service
@Data
public class LogQueryService {

    private final LogEntryRepository logRepo;
    private final LogMapper logMapper;

    public LogQueryService(LogEntryRepository logRepo, LogMapper logMapper) {
        this.logRepo = logRepo;
        this.logMapper = logMapper;
    }

    @Transactional(readOnly = true)
    public List<LogData> getRecentLogs(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("timestamp").descending());
        Page<LogEntry> logPage = logRepo.findAll(pageable);

        return logPage.getContent().stream().map(logMapper::toLogData).collect(Collectors.toList());
    }

    /** Get paginated logs with optional filtering. This is the correct home for this method. */
    @Transactional(readOnly = true)
    public Page<LogData> getLogs(
            int page,
            int size,
            Long projectId,
            LogEventType type,
            String sessionId,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());

        // Use a Specification to build the dynamic query based on which filters are provided
        Specification<LogEntry> spec =
                (root, query, criteriaBuilder) -> {
                    List<Predicate> predicates = new ArrayList<>();
                    if (projectId != null) {
                        predicates.add(criteriaBuilder.equal(root.get("projectId"), projectId));
                    }
                    if (type != null) {
                        predicates.add(criteriaBuilder.equal(root.get("type"), type));
                    }
                    if (sessionId != null && !sessionId.isEmpty()) {
                        predicates.add(criteriaBuilder.equal(root.get("sessionId"), sessionId));
                    }
                    if (startDate != null) {
                        predicates.add(
                                criteriaBuilder.greaterThanOrEqualTo(
                                        root.get("timestamp"),
                                        startDate.atZone(ZoneId.systemDefault()).toInstant()));
                    }
                    if (endDate != null) {
                        predicates.add(
                                criteriaBuilder.lessThanOrEqualTo(
                                        root.get("timestamp"),
                                        endDate.atZone(ZoneId.systemDefault()).toInstant()));
                    }
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                };

        // Execute the query and map the results
        Page<LogEntry> entityPage = logRepo.findAll(spec, pageable);
        return entityPage.map(logMapper::toLogData);
    }
}
