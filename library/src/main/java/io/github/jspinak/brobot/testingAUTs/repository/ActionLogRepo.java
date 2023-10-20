package io.github.jspinak.brobot.testingAUTs.repository;

import io.github.jspinak.brobot.testingAUTs.model.ActionLog;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

//@Repository
public interface ActionLogRepo extends ElasticsearchRepository<ActionLog, String> {
}
