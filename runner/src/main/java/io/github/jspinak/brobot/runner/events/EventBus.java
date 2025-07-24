package io.github.jspinak.brobot.runner.events;

import lombok.Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Central event bus for the Brobot Runner application.
 * 
 * <p>This component implements a publish-subscribe pattern for decoupled communication
 * between different parts of the application. It manages event publishing and subscription,
 * allowing components to communicate without direct dependencies.</p>
 * 
 * <p>Key features:
 * <ul>
 *   <li>Thread-safe event publishing and subscription</li>
 *   <li>Asynchronous event delivery using a cached thread pool</li>
 *   <li>Type-based event filtering</li>
 *   <li>Support for multiple subscribers per event type</li>
 *   <li>Automatic exception handling in event handlers</li>
 * </ul>
 * </p>
 * 
 * <p>Common event types include:
 * <ul>
 *   <li>CONFIG_LOADED - Configuration file loaded</li>
 *   <li>AUTOMATION_STARTED/STOPPED - Automation lifecycle events</li>
 *   <li>STATE_CHANGED - State transition events</li>
 *   <li>ERROR_OCCURRED - Error events</li>
 *   <li>LOG_ENTRY - Logging events</li>
 * </ul>
 * </p>
 * 
 * @see BrobotEvent
 * @see BrobotEvent.EventType
 */
@Component
@Data
public class EventBus {
    private static final Logger logger = LoggerFactory.getLogger(EventBus.class);

    // Configuration constants
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 20;
    private static final int QUEUE_CAPACITY = 1000;
    private static final long KEEP_ALIVE_TIME = 60L;
    private static final int BACKPRESSURE_THRESHOLD = 800; // 80% of queue capacity

    private final Map<BrobotEvent.EventType, Set<Consumer<BrobotEvent>>> subscribers = new ConcurrentHashMap<>();
    
    // Using ThreadPoolExecutor with bounded queue for backpressure
    private final ThreadPoolExecutor executorService;
    
    // Metrics for monitoring
    private final AtomicInteger droppedEvents = new AtomicInteger(0);
    private final AtomicInteger processedEvents = new AtomicInteger(0);
    private final AtomicInteger queuedEvents = new AtomicInteger(0);
    
    public EventBus() {
        // Create bounded queue for backpressure
        LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
        
        // Custom thread factory
        java.util.concurrent.ThreadFactory threadFactory = r -> {
            Thread thread = new Thread(r, "EventBus-Worker-" + System.currentTimeMillis());
            thread.setDaemon(true);
            return thread;
        };
        
        // Create executor with bounded queue and rejection handler
        this.executorService = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            workQueue,
            threadFactory,
            new BackpressureRejectionHandler()
        );
        
        // Allow core threads to timeout
        this.executorService.allowCoreThreadTimeOut(true);
    }

    /**
     * Publishes an event to all subscribers.
     * @param event The event to publish
     */
    public void publish(BrobotEvent event) {
        logger.debug("Publishing event: {}", event.getEventType());
        
        // Check queue size for backpressure warning
        int queueSize = executorService.getQueue().size();
        if (queueSize > BACKPRESSURE_THRESHOLD) {
            logger.warn("EventBus queue size {} exceeds threshold {}, backpressure may occur", 
                       queueSize, BACKPRESSURE_THRESHOLD);
        }

        Set<Consumer<BrobotEvent>> eventSubscribers = subscribers.get(event.getEventType());
        if (eventSubscribers != null) {
            for (Consumer<BrobotEvent> subscriber : eventSubscribers) {
                // Track queued events
                queuedEvents.incrementAndGet();
                
                // Execute event handling asynchronously to avoid blocking the publisher
                executorService.submit(() -> {
                    try {
                        queuedEvents.decrementAndGet();
                        subscriber.accept(event);
                        processedEvents.incrementAndGet();
                    } catch (Exception e) {
                        logger.error("Error handling event {} in subscriber", event.getEventType(), e);
                    }
                });
            }
        }
    }

    /**
     * Subscribes to events of a specific type.
     * @param eventType The event type to subscribe to
     * @param handler The handler for the event
     * @return A subscription ID that can be used to unsubscribe
     */
    public void subscribe(BrobotEvent.EventType eventType, Consumer<BrobotEvent> handler) {
        logger.debug("Subscribing to event type: {}", eventType);
        subscribers.computeIfAbsent(eventType, k -> ConcurrentHashMap.newKeySet()).add(handler);
    }

    /**
     * Subscribes to multiple event types with the same handler.
     * @param eventTypes The event types to subscribe to
     * @param handler The handler for the events
     */
    public void subscribeToTypes(List<BrobotEvent.EventType> eventTypes, Consumer<BrobotEvent> handler) {
        for (BrobotEvent.EventType eventType : eventTypes) {
            subscribe(eventType, handler);
        }
    }

    /**
     * Unsubscribes a handler from an event type.
     * @param eventType The event type to unsubscribe from
     * @param handler The handler to remove
     */
    public void unsubscribe(BrobotEvent.EventType eventType, Consumer<BrobotEvent> handler) {
        logger.debug("Unsubscribing from event type: {}", eventType);
        Set<Consumer<BrobotEvent>> eventSubscribers = subscribers.get(eventType);
        if (eventSubscribers != null) {
            eventSubscribers.remove(handler);
        }
    }

    /**
     * Unsubscribes a handler from multiple event types.
     * @param eventTypes The event types to unsubscribe from
     * @param handler The handler to remove
     */
    public void unsubscribeFromTypes(List<BrobotEvent.EventType> eventTypes, Consumer<BrobotEvent> handler) {
        for (BrobotEvent.EventType eventType : eventTypes) {
            unsubscribe(eventType, handler);
        }
    }

    /**
     * Initializes the event bus.
     */
    @PostConstruct
    public void init() {
        logger.info("EventBus initialized with cached thread pool");
    }

    /**
     * Shuts down the event bus executor service.
     */
    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down EventBus executor service");
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warn("EventBus executor did not terminate within timeout, forcing shutdown");
                executorService.shutdownNow();
                if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                    logger.error("EventBus executor did not terminate after forced shutdown");
                }
            }
        } catch (InterruptedException e) {
            logger.error("Interrupted while shutting down EventBus executor", e);
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("EventBus executor service shut down successfully");
    }
    
    /**
     * Gets current metrics for monitoring.
     * 
     * @return event bus metrics
     */
    public EventBusMetrics getMetrics() {
        return EventBusMetrics.builder()
            .queueSize(executorService.getQueue().size())
            .activeThreads(executorService.getActiveCount())
            .poolSize(executorService.getPoolSize())
            .processedEvents(processedEvents.get())
            .droppedEvents(droppedEvents.get())
            .queuedEvents(queuedEvents.get())
            .subscriberCount(subscribers.values().stream()
                .mapToInt(Set::size)
                .sum())
            .build();
    }
    
    /**
     * Checks if the event bus is experiencing backpressure.
     * 
     * @return true if under backpressure
     */
    public boolean isUnderBackpressure() {
        return executorService.getQueue().size() > BACKPRESSURE_THRESHOLD;
    }
    
    /**
     * Rejection handler for backpressure situations.
     */
    private class BackpressureRejectionHandler implements java.util.concurrent.RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            droppedEvents.incrementAndGet();
            queuedEvents.decrementAndGet(); // Compensate for increment in publish
            
            logger.error("Event dropped due to backpressure - queue full ({} events)", 
                        executor.getQueue().size());
            
            // Try to extract event info for better logging
            if (r instanceof java.util.concurrent.FutureTask) {
                logger.error("Dropped event task: {}", r.toString());
            }
        }
    }
    
    /**
     * Event bus metrics for monitoring.
     */
    @lombok.Builder
    @lombok.Data
    public static class EventBusMetrics {
        private final int queueSize;
        private final int activeThreads;
        private final int poolSize;
        private final int processedEvents;
        private final int droppedEvents;
        private final int queuedEvents;
        private final int subscriberCount;
        
        public double getDropRate() {
            int total = processedEvents + droppedEvents;
            return total > 0 ? (double) droppedEvents / total * 100 : 0.0;
        }
        
        public double getQueueUtilization() {
            return (double) queueSize / QUEUE_CAPACITY * 100;
        }
    }
}