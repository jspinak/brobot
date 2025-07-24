package io.github.jspinak.brobot.runner.persistence;

import io.github.jspinak.brobot.runner.persistence.entities.IllustrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA repository for illustration entities.
 * <p>
 * Provides database access for illustration metadata with
 * various query methods for gallery functionality.
 *
 * @see IllustrationEntity
 */
@Repository
public interface IllustrationRepository extends JpaRepository<IllustrationEntity, Long> {
    
    /**
     * Finds all illustrations for a session ordered by timestamp.
     *
     * @param sessionId the session ID
     * @return list of illustrations ordered by newest first
     */
    List<IllustrationEntity> findBySessionIdOrderByTimestampDesc(String sessionId);
    
    /**
     * Finds illustrations containing a specific tag.
     *
     * @param tag the tag to search for
     * @return list of illustrations with the tag
     */
    @Query("SELECT i FROM IllustrationEntity i JOIN i.tags t WHERE t = :tag")
    List<IllustrationEntity> findByTagsContaining(String tag);
    
    /**
     * Finds the most recent illustrations.
     *
     * @param limit maximum number to return
     * @return list of recent illustrations
     */
    @Query("SELECT i FROM IllustrationEntity i ORDER BY i.timestamp DESC LIMIT :limit")
    List<IllustrationEntity> findTopNByOrderByTimestampDesc(int limit);
    
    /**
     * Finds the oldest illustrations for cleanup.
     *
     * @param limit maximum number to return
     * @return list of oldest illustrations
     */
    @Query("SELECT i FROM IllustrationEntity i ORDER BY i.timestamp ASC LIMIT :limit")
    List<IllustrationEntity> findTopNByOrderByTimestampAsc(int limit);
    
    /**
     * Counts illustrations by success status.
     *
     * @param success the success status
     * @return count of illustrations
     */
    long countBySuccess(boolean success);
    
    /**
     * Finds illustrations within a date range.
     *
     * @param start start date (inclusive)
     * @param end end date (inclusive)
     * @return list of illustrations in range
     */
    List<IllustrationEntity> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * Finds illustrations by action type and success status.
     *
     * @param actionType the action type
     * @param success the success status
     * @return list of matching illustrations
     */
    List<IllustrationEntity> findByActionTypeAndSuccess(String actionType, boolean success);
    
    /**
     * Deletes illustrations older than a specific date.
     *
     * @param cutoffDate the cutoff date
     * @return number of deleted illustrations
     */
    long deleteByTimestampBefore(LocalDateTime cutoffDate);
}