package io.github.jspinak.brobot.database.data;

import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateString;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface StateStringRepo extends JpaRepository<StateString, Long> {
    Optional<StateString> findByName(String name);
    List<StateString> findByNameContainingIgnoreCase(String name);

}
