package io.github.jspinak.brobot.runner.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Spring configuration for action recording beans.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class RecordingBeanConfiguration {
    
    /**
     * Configure ObjectMapper for ActionRecord serialization
     */
    @Bean
    public ObjectMapper recordingObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
    
    /**
     * Configure async executor for recording operations
     */
    @Bean(name = "recordingExecutor")
    public Executor recordingExecutor(RecordingConfiguration config) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(config.getPerformance().getThreadPoolSize());
        executor.setMaxPoolSize(config.getPerformance().getThreadPoolSize() * 2);
        executor.setQueueCapacity(config.getPerformance().getQueueCapacity());
        executor.setThreadNamePrefix("recording-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}