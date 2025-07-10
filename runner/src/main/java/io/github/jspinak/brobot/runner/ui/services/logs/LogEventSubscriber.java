package io.github.jspinak.brobot.runner.ui.services.logs;

import io.github.jspinak.brobot.runner.events.*;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * Handles event subscriptions and processing for log events.
 */
@Slf4j
public class LogEventSubscriber {
    
    private final EventBus eventBus;
    private final Consumer<LogEntryViewModel> onLogReceived;
    private final Consumer<BrobotEvent> eventHandler;
    
    public LogEventSubscriber(EventBus eventBus, Consumer<LogEntryViewModel> onLogReceived) {
        this.eventBus = eventBus;
        this.onLogReceived = onLogReceived;
        this.eventHandler = this::handleEvent;
    }
    
    /**
     * Subscribes to log events.
     */
    public void subscribe() {
        eventBus.subscribe(BrobotEvent.EventType.LOG_MESSAGE, eventHandler);
        eventBus.subscribe(BrobotEvent.EventType.LOG_WARNING, eventHandler);
        eventBus.subscribe(BrobotEvent.EventType.LOG_ERROR, eventHandler);
        
        log.info("Subscribed to log events");
    }
    
    /**
     * Unsubscribes from log events.
     */
    public void unsubscribe() {
        eventBus.unsubscribe(BrobotEvent.EventType.LOG_MESSAGE, eventHandler);
        eventBus.unsubscribe(BrobotEvent.EventType.LOG_WARNING, eventHandler);
        eventBus.unsubscribe(BrobotEvent.EventType.LOG_ERROR, eventHandler);
        
        log.info("Unsubscribed from log events");
    }
    
    /**
     * Handles incoming log events.
     */
    private void handleEvent(BrobotEvent event) {
        try {
            LogEntryViewModel viewModel = null;
            
            if (event instanceof LogEntryEvent logEntryEvent) {
                if (logEntryEvent.getLogEntry() != null) {
                    viewModel = new LogEntryViewModel(logEntryEvent.getLogEntry());
                }
            } else if (event instanceof LogEvent logEvent) {
                viewModel = new LogEntryViewModel(logEvent);
            }
            
            if (viewModel != null) {
                // Use Platform.runLater to ensure UI updates happen on JavaFX thread
                final LogEntryViewModel finalViewModel = viewModel;
                Platform.runLater(() -> onLogReceived.accept(finalViewModel));
            }
        } catch (Exception e) {
            log.error("Error processing log event", e);
        }
    }
}