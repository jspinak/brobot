package io.github.jspinak.brobot.persistence.database.repository;

import io.github.jspinak.brobot.persistence.database.entity.ActionRecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA repository for action records.
 */
@Repository
public interface ActionRecordRepository extends JpaRepository<ActionRecordEntity, Long> {
    
    List<ActionRecordEntity> findBySessionId(Long sessionId);
    
    @Query("SELECT a FROM ActionRecordEntity a WHERE a.session.id = :sessionId ORDER BY a.timestamp")
    List<ActionRecordEntity> findBySessionIdOrderByTimestamp(@Param("sessionId") Long sessionId);
    
    Page<ActionRecordEntity> findBySessionId(Long sessionId, Pageable pageable);
    
    List<ActionRecordEntity> findByActionConfigType(String actionConfigType);
    
    List<ActionRecordEntity> findByActionSuccess(boolean success);
    
    @Query("SELECT a FROM ActionRecordEntity a WHERE a.session.id = :sessionId AND a.actionSuccess = :success")
    List<ActionRecordEntity> findBySessionIdAndSuccess(@Param("sessionId") Long sessionId, 
                                                       @Param("success") boolean success);
    
    @Query("SELECT a FROM ActionRecordEntity a WHERE a.timestamp >= :startTime AND a.timestamp <= :endTime")
    List<ActionRecordEntity> findByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                            @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT COUNT(a) FROM ActionRecordEntity a WHERE a.session.id = :sessionId")
    long countBySessionId(@Param("sessionId") Long sessionId);
    
    @Query("SELECT COUNT(a) FROM ActionRecordEntity a WHERE a.session.id = :sessionId AND a.actionSuccess = true")
    long countSuccessfulBySessionId(@Param("sessionId") Long sessionId);
    
    @Query("SELECT AVG(a.duration) FROM ActionRecordEntity a WHERE a.session.id = :sessionId")
    Double averageDurationBySessionId(@Param("sessionId") Long sessionId);
    
    void deleteBySessionId(Long sessionId);
}