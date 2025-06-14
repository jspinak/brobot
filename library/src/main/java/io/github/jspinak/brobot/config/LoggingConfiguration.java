package io.github.jspinak.brobot.config;

import io.github.jspinak.brobot.report.log.spi.LogSink;
import io.github.jspinak.brobot.report.log.spi.NoOpLogSink;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggingConfiguration {

    @Bean
    @ConditionalOnMissingBean(LogSink.class)
    public LogSink defaultLogSink() {
        // This bean will only be created if no other LogSink bean
        // (like the runner's DatabaseLogSink) is present in the context.
        return new NoOpLogSink();
    }
}