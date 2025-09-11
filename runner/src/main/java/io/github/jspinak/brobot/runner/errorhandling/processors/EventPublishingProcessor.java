package io.github.jspinak.brobot.runner.errorhandling.processors;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.IErrorProcessor;
import io.github.jspinak.brobot.runner.events.ErrorEvent;
import io.github.jspinak.brobot.runner.events.EventBus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Error processor that publishes error events to the event bus. Enables reactive error handling
 * throughout the application.
 */
@Slf4j
@RequiredArgsConstructor
public class EventPublishingProcessor implements IErrorProcessor {

    private final EventBus eventBus;

    @Override
    public void process(Throwable error, ErrorContext context) {
        try {
            // Map ErrorContext.ErrorSeverity to ErrorEvent.ErrorSeverity
            ErrorEvent.ErrorSeverity eventSeverity = mapSeverity(context.getSeverity());

            // Create error event
            ErrorEvent event =
                    new ErrorEvent(
                            this,
                            error.getMessage(),
                            error instanceof Exception ? (Exception) error : new Exception(error),
                            eventSeverity,
                            context.getComponent() != null
                                    ? context.getComponent()
                                    : "ErrorHandler");

            // Publish to event bus
            eventBus.publish(event);

            log.trace(
                    "Published error event for {} with severity {}",
                    error.getClass().getSimpleName(),
                    eventSeverity);

        } catch (Exception e) {
            // Don't let event publishing failures cascade
            log.error("Failed to publish error event", e);
        }
    }

    private ErrorEvent.ErrorSeverity mapSeverity(ErrorContext.ErrorSeverity contextSeverity) {
        return switch (contextSeverity) {
            case LOW -> ErrorEvent.ErrorSeverity.LOW;
            case MEDIUM -> ErrorEvent.ErrorSeverity.MEDIUM;
            case HIGH -> ErrorEvent.ErrorSeverity.HIGH;
            case CRITICAL -> ErrorEvent.ErrorSeverity.FATAL;
        };
    }
}
