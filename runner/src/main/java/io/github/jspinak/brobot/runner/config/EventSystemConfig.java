package io.github.jspinak.brobot.runner.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.EventPublishingActionLogger;
import io.github.jspinak.brobot.tools.logging.ActionLogger;
import io.github.jspinak.brobot.tools.logging.SessionLifecycleLogger;

/** Configuration class for setting up the event system integration with logging. */
@Configuration
public class EventSystemConfig {

    /**
     * Creates an EventPublishingActionLogger that wraps the actual implementations of ActionLogger
     * and TestSessionLogger, adding event publishing behavior.
     *
     * <p>This is registered as the primary bean for both interfaces, so it will be injected in
     * place of the original implementations when either interface is requested.
     */
    @Bean
    @Primary
    public EventPublishingActionLogger eventPublishingLogger(
            @Qualifier("actionLoggerImpl") ActionLogger actionLoggerDelegate,
            @Qualifier("sessionLoggerImpl") SessionLifecycleLogger sessionLoggerDelegate,
            EventBus eventBus) {

        return new EventPublishingActionLogger(
                actionLoggerDelegate, sessionLoggerDelegate, eventBus);
    }
}
