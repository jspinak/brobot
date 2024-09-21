package io.github.jspinak.brobot.app.database.repositories;

import io.github.jspinak.brobot.app.database.entities.TransitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface TransitionRepo extends JpaRepository<TransitionEntity, Long> {
    List<TransitionEntity> findAllByProjectId(Long projectId);

    @Modifying
    @Query(value = "DELETE FROM transitions WHERE source_state_id = ?1", nativeQuery = true)
    void deleteBySourceStateId(Long sourceStateId);

    @Modifying
    @Query(value = "DELETE FROM transition_enter_states WHERE state_id = :sourceStateId", nativeQuery = true)
    void removeStateFromEnterStates(@Param("sourceStateId") Long sourceStateId);

    @Modifying
    @Query(value = "DELETE FROM transition_exit_states WHERE state_id = :sourceStateId", nativeQuery = true)
    void removeStateFromExitStates(@Param("sourceStateId") Long sourceStateId);

    @Modifying
    @Query(value = "DELETE FROM transition_enter_states WHERE state_id = :sourceStateId; " +
            "DELETE FROM transition_exit_states WHERE state_id = :sourceStateId", nativeQuery = true)
    void removeStateFromEnterAndExitStates(@Param("sourceStateId") Long sourceStateId);
}