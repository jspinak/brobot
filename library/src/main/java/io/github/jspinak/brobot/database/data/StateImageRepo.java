package io.github.jspinak.brobot.database.data;

import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface StateImageRepo extends JpaRepository<StateImage, Long> {
    Optional<StateImage> findByName(String name);
    List<StateImage> findByNameContainingIgnoreCase(String name);
    List<StateImage> findByProjectId(Long projectId);

}
