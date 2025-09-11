package io.github.jspinak.brobot.persistence.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.github.jspinak.brobot.persistence.PersistenceProvider;
import io.github.jspinak.brobot.persistence.PersistenceProviderFactory;
import io.github.jspinak.brobot.persistence.config.PersistenceConfiguration;
import io.github.jspinak.brobot.persistence.database.repository.ActionRecordRepository;
import io.github.jspinak.brobot.persistence.database.repository.RecordingSessionRepository;
import io.github.jspinak.brobot.persistence.provider.DatabasePersistenceProvider;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring Boot auto-configuration for Brobot persistence. Automatically configures persistence based
 * on application properties.
 */
@Configuration
@ConditionalOnClass(PersistenceProvider.class)
@EnableJpaRepositories(basePackages = "io.github.jspinak.brobot.persistence.database.repository")
@EntityScan(basePackages = "io.github.jspinak.brobot.persistence.database.entity")
@Slf4j
public class PersistenceAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "brobot.persistence")
    @ConditionalOnMissingBean
    public PersistenceConfiguration persistenceConfiguration() {
        return new PersistenceConfiguration();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "brobot.persistence",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    public PersistenceProvider persistenceProvider(
            PersistenceConfiguration configuration,
            @Autowired(required = false) RecordingSessionRepository sessionRepository,
            @Autowired(required = false) ActionRecordRepository recordRepository) {

        log.info("Auto-configuring Brobot persistence provider: {}", configuration.getType());

        if (configuration.getType() == PersistenceConfiguration.PersistenceType.DATABASE) {
            if (sessionRepository == null || recordRepository == null) {
                log.error(
                        "Database persistence requires JPA repositories but they are not"
                                + " available");
                throw new IllegalStateException("Database persistence requires Spring Data JPA");
            }
            return new DatabasePersistenceProvider(
                    configuration, sessionRepository, recordRepository);
        }

        return PersistenceProviderFactory.create(configuration);
    }

    @Bean
    @ConditionalOnMissingBean
    public PersistenceEventListener persistenceEventListener(PersistenceProvider provider) {
        return new PersistenceEventListener(provider);
    }
}
