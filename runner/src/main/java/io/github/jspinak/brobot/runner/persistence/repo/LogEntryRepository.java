package io.github.jspinak.brobot.runner.persistence.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import io.github.jspinak.brobot.runner.persistence.entity.LogEntry;

@Repository
public interface LogEntryRepository
        extends JpaRepository<LogEntry, Long>, JpaSpecificationExecutor<LogEntry> {}
