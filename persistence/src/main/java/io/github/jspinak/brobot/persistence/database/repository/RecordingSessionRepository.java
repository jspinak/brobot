package io.github.jspinak.brobot.persistence.database.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.github.jspinak.brobot.persistence.database.entity.RecordingSessionEntity;

/** JPA repository for recording sessions. */
@Repository
public interface RecordingSessionRepository extends JpaRepository<RecordingSessionEntity, Long> {

    Optional<RecordingSessionEntity> findBySessionId(String sessionId);

    List<RecordingSessionEntity> findByStatus(RecordingSessionEntity.SessionStatus status);

    List<RecordingSessionEntity> findByApplicationOrderByStartTimeDesc(String application);

    List<RecordingSessionEntity> findByImportedOrderByStartTimeDesc(boolean imported);

    @Query(
            "SELECT s FROM RecordingSessionEntity s WHERE s.startTime >= :startTime AND s.endTime"
                    + " <= :endTime ORDER BY s.startTime DESC")
    List<RecordingSessionEntity> findByDateRange(LocalDateTime startTime, LocalDateTime endTime);

    @Query(
            "SELECT s FROM RecordingSessionEntity s WHERE s.status = 'ACTIVE' OR s.status ="
                    + " 'PAUSED'")
    List<RecordingSessionEntity> findActiveSessions();

    @Query("SELECT COUNT(s) FROM RecordingSessionEntity s WHERE s.status = :status")
    long countByStatus(RecordingSessionEntity.SessionStatus status);

    void deleteBySessionId(String sessionId);
}
