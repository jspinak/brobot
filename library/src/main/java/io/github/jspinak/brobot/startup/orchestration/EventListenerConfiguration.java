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
        log.info("[EVENT CONFIG] Event listener configuration initialized");
        log.info("[EVENT CONFIG] Will create custom ApplicationEventMulticaster bean");
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
        log.info("[EVENT CONFIG] Created synchronous ApplicationEventMulticaster for reliable initialization");
        return multicaster;
    }
    
    /**
     * Custom event multicaster with enhanced logging for debugging
     */
    public static class LoggingApplicationEventMulticaster extends SimpleApplicationEventMulticaster {
        
        @Override
        public void multicastEvent(org.springframework.context.ApplicationEvent event) {
            ResolvableType eventType = ResolvableType.forInstance(event);
            log.debug("[EVENT DISPATCH] Broadcasting event: {} to {} listeners", 
                eventType.getType().getTypeName(), 
                getApplicationListeners(event, eventType).size());
            
            // Log specific details for StatesRegisteredEvent
            if (event.getClass().getName().contains("StatesRegisteredEvent")) {
                log.info("[EVENT DISPATCH] StatesRegisteredEvent being dispatched to {} listeners", 
                    getApplicationListeners(event, eventType).size());
                for (ApplicationListener<?> listener : getApplicationListeners(event, eventType)) {
                    log.info("[EVENT DISPATCH]   -> Listener: {}", listener.getClass().getName());
                }
            }
            
            super.multicastEvent(event);
            
            if (event.getClass().getName().contains("StatesRegisteredEvent")) {
                log.info("[EVENT DISPATCH] StatesRegisteredEvent dispatch completed");
            }
        }
        
        @Override
        protected void invokeListener(ApplicationListener<?> listener, 
                                    org.springframework.context.ApplicationEvent event) {
            String listenerName = listener.getClass().getSimpleName();
            String eventName = event.getClass().getSimpleName();
            
            log.debug("[EVENT INVOKE] Invoking {} with {}", listenerName, eventName);
            long startTime = System.currentTimeMillis();
            
            try {
                super.invokeListener(listener, event);
                long duration = System.currentTimeMillis() - startTime;
                
                if (duration > 100) {
                    log.warn("[EVENT INVOKE] Slow listener: {} took {}ms for {}", 
                        listenerName, duration, eventName);
                } else {
                    log.debug("[EVENT INVOKE] {} completed in {}ms", listenerName, duration);
                }
            } catch (Exception e) {
                log.error("[EVENT INVOKE] Error in listener {} handling {}", 
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