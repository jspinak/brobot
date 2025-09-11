package io.github.jspinak.brobot.runner.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.jspinak.brobot.runner.config.RecordingConfiguration;
import io.github.jspinak.brobot.runner.persistence.entity.RecordingSessionEntity;
import io.github.jspinak.brobot.runner.persistence.repository.ActionRecordRepository;
import io.github.jspinak.brobot.runner.persistence.repository.RecordingSessionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Service for cleaning up old recording sessions and maintaining database size. */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        prefix = "brobot.runner.recording.cleanup",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class RecordingCleanupService {

    private final RecordingConfiguration configuration;
    private final RecordingSessionRepository sessionRepository;
    private final ActionRecordRepository recordRepository;
    private final ActionHistoryExportService exportService;

    /** Scheduled cleanup task - runs based on configured schedule */
    @Scheduled(cron = "${brobot.runner.recording.cleanup.schedule:0 0 2 * * ?}")
    @Transactional
    public void performCleanup() {
        log.info("Starting scheduled recording cleanup");

        try {
            // Clean up old sessions
            if (configuration.getCleanup().getRetentionDays() > 0) {
                cleanupOldSessions();
            }

            // Enforce session limit
            if (configuration.getMaxSessions() > 0) {
                enforceSessionLimit();
            }

            // Archive if configured
            if (configuration.getCleanup().isAutoArchive()) {
                archiveOldSessions();
            }

            log.info("Recording cleanup completed successfully");

        } catch (Exception e) {
            log.error("Error during recording cleanup", e);
        }
    }

    /** Clean up sessions older than retention period */
    private void cleanupOldSessions() {
        LocalDateTime cutoffDate =
                LocalDateTime.now().minusDays(configuration.getCleanup().getRetentionDays());

        List<RecordingSessionEntity> oldSessions =
                sessionRepository.findSessionsForCleanup(cutoffDate);

        if (!oldSessions.isEmpty()) {
            log.info(
                    "Found {} sessions older than {} days for cleanup",
                    oldSessions.size(),
                    configuration.getCleanup().getRetentionDays());

            for (RecordingSessionEntity session : oldSessions) {
                // Archive if needed
                if (configuration.getCleanup().isAutoArchive() && !session.isExported()) {
                    archiveSession(session);
                }

                // Delete session and its records
                sessionRepository.delete(session);
                log.debug("Deleted session: {} ({})", session.getName(), session.getId());
            }

            log.info("Deleted {} old sessions", oldSessions.size());
        }
    }

    /** Enforce maximum session limit */
    private void enforceSessionLimit() {
        long sessionCount = sessionRepository.count();

        if (sessionCount > configuration.getMaxSessions()) {
            long toDelete = sessionCount - configuration.getMaxSessions();
            log.info(
                    "Session limit exceeded ({}/{}), deleting {} oldest sessions",
                    sessionCount,
                    configuration.getMaxSessions(),
                    toDelete);

            // Find oldest sessions to delete
            List<RecordingSessionEntity> oldestSessions =
                    sessionRepository.findAll().stream()
                            .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                            .limit(toDelete)
                            .toList();

            for (RecordingSessionEntity session : oldestSessions) {
                // Archive if needed
                if (configuration.getCleanup().isAutoArchive() && !session.isExported()) {
                    archiveSession(session);
                }

                sessionRepository.delete(session);
                log.debug(
                        "Deleted session due to limit: {} ({})",
                        session.getName(),
                        session.getId());
            }
        }
    }

    /** Archive old sessions to external storage */
    private void archiveOldSessions() {
        if (configuration.getCleanup().getArchivePath() == null) {
            log.warn("Archive path not configured, skipping archival");
            return;
        }

        LocalDateTime archiveCutoff =
                LocalDateTime.now()
                        .minusDays(
                                configuration.getCleanup().getRetentionDays()
                                        / 2); // Archive at half retention

        List<RecordingSessionEntity> toArchive =
                sessionRepository.findByStartTimeBetween(LocalDateTime.MIN, archiveCutoff).stream()
                        .filter(s -> !s.isExported())
                        .toList();

        if (!toArchive.isEmpty()) {
            log.info("Archiving {} sessions", toArchive.size());

            for (RecordingSessionEntity session : toArchive) {
                archiveSession(session);
            }
        }
    }

    /** Archive a single session */
    private void archiveSession(RecordingSessionEntity session) {
        try {
            Path archivePath = Paths.get(configuration.getCleanup().getArchivePath());
            Files.createDirectories(archivePath);

            // Create archive filename
            String filename =
                    String.format(
                            "%s_%s_%s.json",
                            session.getName().replaceAll("[^a-zA-Z0-9-_]", "_"),
                            session.getId(),
                            session.getStartTime()
                                    .format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE));

            File archiveFile = archivePath.resolve(filename).toFile();

            // Export session to archive
            exportService.exportSessionToFile(
                    session.getId(),
                    archiveFile,
                    ActionHistoryExportService.ExportFormat.JSON_COMPRESSED);

            log.info("Archived session {} to {}", session.getName(), archiveFile.getName());

        } catch (IOException e) {
            log.error("Failed to archive session {}: {}", session.getName(), e.getMessage());
        }
    }

    /** Manual cleanup trigger */
    @Transactional
    public CleanupStatistics performManualCleanup() {
        CleanupStatistics stats = new CleanupStatistics();
        stats.setStartTime(LocalDateTime.now());

        long initialSessions = sessionRepository.count();
        long initialRecords = recordRepository.count();

        performCleanup();

        stats.setEndTime(LocalDateTime.now());
        stats.setSessionsDeleted((int) (initialSessions - sessionRepository.count()));
        stats.setRecordsDeleted((int) (initialRecords - recordRepository.count()));

        return stats;
    }

    /** Get cleanup statistics */
    public CleanupStatistics getStatistics() {
        CleanupStatistics stats = new CleanupStatistics();

        stats.setTotalSessions((int) sessionRepository.count());
        stats.setTotalRecords((int) recordRepository.count());

        if (configuration.getCleanup().getRetentionDays() > 0) {
            LocalDateTime cutoffDate =
                    LocalDateTime.now().minusDays(configuration.getCleanup().getRetentionDays());
            stats.setSessionsPendingCleanup(
                    sessionRepository.findSessionsForCleanup(cutoffDate).size());
        }

        return stats;
    }

    /** Cleanup statistics */
    public static class CleanupStatistics {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private int sessionsDeleted;
        private int recordsDeleted;
        private int sessionsArchived;
        private int totalSessions;
        private int totalRecords;
        private int sessionsPendingCleanup;

        // Getters and setters
        public LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
        }

        public int getSessionsDeleted() {
            return sessionsDeleted;
        }

        public void setSessionsDeleted(int sessionsDeleted) {
            this.sessionsDeleted = sessionsDeleted;
        }

        public int getRecordsDeleted() {
            return recordsDeleted;
        }

        public void setRecordsDeleted(int recordsDeleted) {
            this.recordsDeleted = recordsDeleted;
        }

        public int getSessionsArchived() {
            return sessionsArchived;
        }

        public void setSessionsArchived(int sessionsArchived) {
            this.sessionsArchived = sessionsArchived;
        }

        public int getTotalSessions() {
            return totalSessions;
        }

        public void setTotalSessions(int totalSessions) {
            this.totalSessions = totalSessions;
        }

        public int getTotalRecords() {
            return totalRecords;
        }

        public void setTotalRecords(int totalRecords) {
            this.totalRecords = totalRecords;
        }

        public int getSessionsPendingCleanup() {
            return sessionsPendingCleanup;
        }

        public void setSessionsPendingCleanup(int sessionsPendingCleanup) {
            this.sessionsPendingCleanup = sessionsPendingCleanup;
        }
    }
}
