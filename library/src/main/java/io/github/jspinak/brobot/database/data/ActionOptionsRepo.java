package io.github.jspinak.brobot.database.data;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
@Transactional
public interface ActionOptionsRepo extends CrudRepository<ActionOptions, Long> {

}
