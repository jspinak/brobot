package io.github.jspinak.brobot.runner.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.github.jspinak.brobot.runner.persistence.entity.RecordingSessionEntity;
import io.github.jspinak.brobot.runner.persistence.entity.RecordingSessionEntity.SessionStatus;

/**
 * Repository for RecordingSession entities with custom queries for session management and analysis.
 */
@Repository
public interface RecordingSessionRepository extends JpaRepository<RecordingSessionEntity, Long> {

    /** Find session by name */
    Optional<RecordingSessionEntity> findByName(String name);

    /** Find all sessions for an application */
    List<RecordingSessionEntity> findByApplication(String application);

    /** Find sessions by status */
    List<RecordingSessionEntity> findByStatus(SessionStatus status);

    /** Find active recording sessions */
    @Query("SELECT rs FROM RecordingSessionEntity rs WHERE rs.status = 'RECORDING'")
    List<RecordingSessionEntity> findActiveSessions();

    /** Find sessions within a date range */
    List<RecordingSessionEntity> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    /** Find exported sessions */
    List<RecordingSessionEntity> findByExported(boolean exported);

    /** Find imported sessions */
    List<RecordingSessionEntity> findByImported(boolean imported);

    /** Find sessions with high success rate */
    @Query(
            "SELECT rs FROM RecordingSessionEntity rs WHERE "
                    + "(rs.successfulActions * 1.0 / NULLIF(rs.totalActions, 0)) > :threshold")
    List<RecordingSessionEntity> findHighSuccessRateSessions(@Param("threshold") double threshold);

    /** Find sessions with pagination */
    Page<RecordingSessionEntity> findByApplicationContaining(String application, Pageable pageable);

    /** Find sessions by tag */
    @Query("SELECT rs FROM RecordingSessionEntity rs WHERE rs.tags LIKE %:tag%")
    List<RecordingSessionEntity> findByTag(@Param("tag") String tag);

    /** Find most recent sessions */
    @Query("SELECT rs FROM RecordingSessionEntity rs ORDER BY rs.startTime DESC")
    Page<RecordingSessionEntity> findMostRecent(Pageable pageable);

    /** Count sessions by status */
    @Query("SELECT rs.status, COUNT(rs) FROM RecordingSessionEntity rs GROUP BY rs.status")
    List<Object[]> countByStatus();

    /** Find sessions needing export */
    @Query(
            "SELECT rs FROM RecordingSessionEntity rs WHERE "
                    + "rs.status = 'COMPLETED' AND rs.exported = false")
    List<RecordingSessionEntity> findSessionsNeedingExport();

    /** Find sessions for cleanup (older than specified date) */
    @Query(
            "SELECT rs FROM RecordingSessionEntity rs WHERE rs.endTime < :cutoffDate "
                    + "AND rs.status != 'ARCHIVED'")
    List<RecordingSessionEntity> findSessionsForCleanup(
            @Param("cutoffDate") LocalDateTime cutoffDate);

    /** Get session statistics */
    @Query(
            "SELECT COUNT(rs) as totalSessions, SUM(rs.totalActions) as totalActions,"
                + " SUM(rs.successfulActions) as successfulActions, AVG(rs.successfulActions * 1.0"
                + " / NULLIF(rs.totalActions, 0)) as avgSuccessRate FROM RecordingSessionEntity rs"
                + " WHERE rs.application = :application")
    Object[] getStatisticsForApplication(@Param("application") String application);

    /** Find longest sessions */
    @Query(
            "SELECT rs FROM RecordingSessionEntity rs WHERE rs.endTime IS NOT NULL "
                    + "ORDER BY (rs.endTime - rs.startTime) DESC")
    Page<RecordingSessionEntity> findLongestSessions(Pageable pageable);

    /** Find sessions with most actions */
    @Query("SELECT rs FROM RecordingSessionEntity rs ORDER BY rs.totalActions DESC")
    Page<RecordingSessionEntity> findSessionsWithMostActions(Pageable pageable);

    /** Archive old sessions */
    @Query(
            "UPDATE RecordingSessionEntity rs SET rs.status = 'ARCHIVED' "
                    + "WHERE rs.endTime < :cutoffDate AND rs.status = 'COMPLETED'")
    int archiveOldSessions(@Param("cutoffDate") LocalDateTime cutoffDate);
}
