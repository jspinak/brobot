package io.github.jspinak.brobot.runner.config;

import io.github.jspinak.brobot.logging.ActionLogger;
import io.github.jspinak.brobot.logging.TestSessionLogger;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.EventPublishingActionLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration class for setting up the event system integration with logging.
 */
@Configuration
public class EventSystemConfig {

    /**
     * Creates an EventPublishingActionLogger that wraps the actual implementations
     * of ActionLogger and TestSessionLogger, adding event publishing behavior.
     *
     * This is registered as the primary bean for both interfaces, so it will be
     * injected in place of the original implementations when either interface
     * is requested.
     */
    @Bean
    @Primary
    public EventPublishingActionLogger eventPublishingLogger(
            ActionLogger actionLoggerDelegate,
            TestSessionLogger sessionLoggerDelegate,
            EventBus eventBus) {

        return new EventPublishingActionLogger(
                actionLoggerDelegate,
                sessionLoggerDelegate,
                eventBus);
    }
}