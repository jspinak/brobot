package io.github.jspinak.brobot.runner.persistence.repository;

import io.github.jspinak.brobot.runner.persistence.entity.ActionRecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ActionRecord entities with custom queries for
 * filtering and analysis.
 */
@Repository
public interface ActionRecordRepository extends JpaRepository<ActionRecordEntity, Long> {
    
    /**
     * Find all records for a specific session
     */
    List<ActionRecordEntity> findBySessionId(Long sessionId);
    
    /**
     * Find all records for a specific session with pagination
     */
    Page<ActionRecordEntity> findBySessionId(Long sessionId, Pageable pageable);
    
    /**
     * Find records by state name
     */
    List<ActionRecordEntity> findByStateName(String stateName);
    
    /**
     * Find records by success status
     */
    List<ActionRecordEntity> findByActionSuccess(boolean success);
    
    /**
     * Find records by action type
     */
    List<ActionRecordEntity> findByActionConfigType(String actionConfigType);
    
    /**
     * Find records within a time range
     */
    List<ActionRecordEntity> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * Find records for a specific state in a session
     */
    @Query("SELECT ar FROM ActionRecordEntity ar WHERE ar.session.id = :sessionId AND ar.stateName = :stateName")
    List<ActionRecordEntity> findBySessionIdAndStateName(@Param("sessionId") Long sessionId, 
                                                         @Param("stateName") String stateName);
    
    /**
     * Find successful records in a session
     */
    @Query("SELECT ar FROM ActionRecordEntity ar WHERE ar.session.id = :sessionId AND ar.actionSuccess = true")
    List<ActionRecordEntity> findSuccessfulBySessionId(@Param("sessionId") Long sessionId);
    
    /**
     * Find failed records in a session
     */
    @Query("SELECT ar FROM ActionRecordEntity ar WHERE ar.session.id = :sessionId AND ar.actionSuccess = false")
    List<ActionRecordEntity> findFailedBySessionId(@Param("sessionId") Long sessionId);
    
    /**
     * Count records by action type in a session
     */
    @Query("SELECT ar.actionConfigType, COUNT(ar) FROM ActionRecordEntity ar " +
           "WHERE ar.session.id = :sessionId GROUP BY ar.actionConfigType")
    List<Object[]> countByActionTypeInSession(@Param("sessionId") Long sessionId);
    
    /**
     * Find records with text results
     */
    @Query("SELECT ar FROM ActionRecordEntity ar WHERE ar.text IS NOT NULL AND ar.text != ''")
    List<ActionRecordEntity> findRecordsWithText();
    
    /**
     * Find slowest actions in a session
     */
    @Query("SELECT ar FROM ActionRecordEntity ar WHERE ar.session.id = :sessionId " +
           "ORDER BY ar.duration DESC")
    Page<ActionRecordEntity> findSlowestInSession(@Param("sessionId") Long sessionId, Pageable pageable);
    
    /**
     * Calculate average duration by action type
     */
    @Query("SELECT ar.actionConfigType, AVG(ar.duration) FROM ActionRecordEntity ar " +
           "WHERE ar.session.id = :sessionId GROUP BY ar.actionConfigType")
    List<Object[]> averageDurationByType(@Param("sessionId") Long sessionId);
    
    /**
     * Find records with matches
     */
    @Query("SELECT DISTINCT ar FROM ActionRecordEntity ar LEFT JOIN FETCH ar.matches " +
           "WHERE ar.session.id = :sessionId AND SIZE(ar.matches) > 0")
    List<ActionRecordEntity> findRecordsWithMatches(@Param("sessionId") Long sessionId);
    
    /**
     * Delete old records
     */
    @Query("DELETE FROM ActionRecordEntity ar WHERE ar.timestamp < :cutoffDate")
    void deleteOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Find most recent record for an object
     */
    @Query("SELECT ar FROM ActionRecordEntity ar WHERE ar.objectName = :objectName " +
           "ORDER BY ar.timestamp DESC")
    Optional<ActionRecordEntity> findMostRecentByObjectName(@Param("objectName") String objectName, 
                                                            Pageable pageable);
    
    /**
     * Count success rate for an object
     */
    @Query("SELECT COUNT(ar) as total, " +
           "SUM(CASE WHEN ar.actionSuccess = true THEN 1 ELSE 0 END) as successful " +
           "FROM ActionRecordEntity ar WHERE ar.objectName = :objectName")
    Object[] getSuccessRateForObject(@Param("objectName") String objectName);
}