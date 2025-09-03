package io.github.jspinak.brobot.startup.orchestration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.ResolvableType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.Executor;

/**
 * Configuration to ensure robust event handling across module boundaries.
 * 
 * This configuration:
 * 1. Explicitly configures the ApplicationEventMulticaster
 * 2. Ensures all ApplicationListener beans are properly registered
 * 3. Provides synchronous event handling for critical initialization events
 * 4. Adds logging for event publishing diagnostics
 */
@Configuration
@Slf4j
public class EventListenerConfiguration {
    
    // REMOVED @Autowired ApplicationEventMulticaster to fix circular dependency
    // The multicaster is created by this configuration, so we can't inject it
    
    @PostConstruct
    public void logEventConfiguration() {
        log.debug("Event listener configuration initialized");
    }
    
    /**
     * Configure a custom ApplicationEventMulticaster with enhanced logging.
     * We use synchronous event publishing for initialization events to ensure
     * proper ordering and completion.
     */
    @Bean(name = "applicationEventMulticaster")
    public ApplicationEventMulticaster applicationEventMulticaster() {
        SimpleApplicationEventMulticaster multicaster = new LoggingApplicationEventMulticaster();
        // Use synchronous event publishing for initialization
        // This ensures that critical initialization events like StatesRegisteredEvent
        // are processed in order and complete before continuing
        log.debug("Created synchronous ApplicationEventMulticaster");
        return multicaster;
    }
    
    /**
     * Custom event multicaster with enhanced logging for debugging
     */
    public static class LoggingApplicationEventMulticaster extends SimpleApplicationEventMulticaster {
        
        @Override
        public void multicastEvent(org.springframework.context.ApplicationEvent event) {
            ResolvableType eventType = ResolvableType.forInstance(event);
            // Only log at trace level to reduce noise
            if (log.isTraceEnabled()) {
                String eventName = event.getClass().getSimpleName();
                log.trace("Broadcasting {} to {} listeners", 
                    eventName, 
                    getApplicationListeners(event, eventType).size());
            }
            
            super.multicastEvent(event);
        }
        
        @Override
        protected void invokeListener(ApplicationListener<?> listener, 
                                    org.springframework.context.ApplicationEvent event) {
            String listenerName = listener.getClass().getSimpleName();
            String eventName = event.getClass().getSimpleName();
            
            long startTime = System.currentTimeMillis();
            
            try {
                super.invokeListener(listener, event);
                long duration = System.currentTimeMillis() - startTime;
                
                // Only warn about slow listeners
                if (duration > 500) {
                    log.warn("Slow event listener: {} took {}ms for {}", 
                        listenerName, duration, eventName);
                }
            } catch (Exception e) {
                log.error("Error in listener {} handling {}", 
                    listenerName, eventName, e);
                throw e; // Re-throw to maintain Spring's error handling
            }
        }
    }
    
    /**
     * Optional: Configure an async executor for performance-critical runtime events.
     * This is not used for initialization events but can be enabled for runtime events.
     */
    @Bean(name = "asyncEventExecutor")
    public Executor asyncEventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AsyncEvent-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        log.info("[EVENT CONFIG] Async event executor configured (not used for initialization events)");
        return executor;
    }
}