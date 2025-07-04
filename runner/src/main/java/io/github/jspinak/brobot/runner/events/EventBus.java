package io.github.jspinak.brobot.runner.events;

import lombok.Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    private final Map<BrobotEvent.EventType, Set<Consumer<BrobotEvent>>> subscribers = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r, "EventBus-Worker");
        thread.setDaemon(true);
        return thread;
    });

    /**
     * Publishes an event to all subscribers.
     * @param event The event to publish
     */
    public void publish(BrobotEvent event) {
        logger.debug("Publishing event: {}", event.getEventType());

        Set<Consumer<BrobotEvent>> eventSubscribers = subscribers.get(event.getEventType());
        if (eventSubscribers != null) {
            for (Consumer<BrobotEvent> subscriber : eventSubscribers) {
                // Execute event handling asynchronously to avoid blocking the publisher
                executorService.submit(() -> {
                    try {
                        subscriber.accept(event);
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
     * Shuts down the event bus executor service.
     */
    public void shutdown() {
        executorService.shutdown();
    }
}