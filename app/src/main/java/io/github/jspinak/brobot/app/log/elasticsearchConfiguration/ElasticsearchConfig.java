package io.github.jspinak.brobot.app.log.elasticsearchConfiguration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "io.github.jspinak.brobot.log.repository")
@EntityScan(basePackages = "io.github.jspinak.brobot.log.entity")
public class ElasticsearchConfig {
    // Any additional Elasticsearch configuration can go here
}
