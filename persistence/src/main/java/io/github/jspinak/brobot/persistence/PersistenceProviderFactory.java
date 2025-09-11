package io.github.jspinak.brobot.persistence;

import io.github.jspinak.brobot.persistence.config.PersistenceConfiguration;
import io.github.jspinak.brobot.persistence.provider.FileBasedPersistenceProvider;
import io.github.jspinak.brobot.persistence.provider.InMemoryPersistenceProvider;

import lombok.extern.slf4j.Slf4j;

/** Factory for creating persistence providers based on configuration. */
@Slf4j
public class PersistenceProviderFactory {

    /**
     * Create a persistence provider based on the configuration.
     *
     * @param configuration the persistence configuration
     * @return the appropriate persistence provider
     */
    public static PersistenceProvider create(PersistenceConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }

        switch (configuration.getType()) {
            case FILE:
                log.info(
                        "Creating file-based persistence provider at: {}",
                        configuration.getFile().getBasePath());
                return new FileBasedPersistenceProvider(configuration);

            case MEMORY:
                log.info("Creating in-memory persistence provider");
                return new InMemoryPersistenceProvider(configuration);

            case DATABASE:
                log.warn(
                        "Database persistence requires Spring Data JPA repositories. Use Spring"
                                + " auto-configuration or provide repositories manually.");
                throw new UnsupportedOperationException(
                        "Database persistence requires Spring context with repositories. Use"
                                + " @Import(PersistenceAutoConfiguration.class) in your Spring"
                                + " application.");

            case CUSTOM:
                throw new UnsupportedOperationException(
                        "Custom persistence requires implementing PersistenceProvider interface");

            default:
                throw new IllegalArgumentException(
                        "Unknown persistence type: " + configuration.getType());
        }
    }

    /** Create a default file-based persistence provider. */
    public static PersistenceProvider createDefault() {
        return create(PersistenceConfiguration.fileDefault());
    }

    /** Create a file-based persistence provider with custom path. */
    public static PersistenceProvider createFile(String basePath) {
        PersistenceConfiguration config = PersistenceConfiguration.fileDefault();
        config.getFile().setBasePath(basePath);
        return create(config);
    }

    /** Create an in-memory persistence provider for testing. */
    public static PersistenceProvider createInMemory() {
        return create(PersistenceConfiguration.memoryDefault());
    }

    /** Create an in-memory persistence provider with shutdown export. */
    public static PersistenceProvider createInMemoryWithExport(String exportPath) {
        PersistenceConfiguration config = PersistenceConfiguration.memoryDefault();
        config.getMemory().setPersistOnShutdown(true);
        config.getMemory().setShutdownExportPath(exportPath);
        return create(config);
    }
}
