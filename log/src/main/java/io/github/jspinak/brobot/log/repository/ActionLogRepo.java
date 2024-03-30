package io.github.jspinak.brobot.log.repository;

import io.github.jspinak.brobot.log.model.ActionLogDTO;
import org.springframework.stereotype.Repository;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

@Repository
public interface ActionLogRepo extends ElasticsearchRepository<ActionLogDTO, String> {
}
