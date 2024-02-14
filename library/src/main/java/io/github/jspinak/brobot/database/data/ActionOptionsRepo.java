package io.github.jspinak.brobot.database.data;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface ActionOptionsRepo extends JpaRepository<ActionOptions, Long> {

}
