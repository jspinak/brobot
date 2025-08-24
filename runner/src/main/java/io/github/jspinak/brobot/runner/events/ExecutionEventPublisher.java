package io.github.jspinak.brobot.runner.events;

import lombok.Data;

import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Publisher for execution-related events.
 * Monitors execution status and publishes events when status changes.
 */
@Component
@Data
public class ExecutionEventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(ExecutionEventPublisher.class);

    private final EventBus eventBus;
    private ExecutionState lastReportedState = null;
    private double lastReportedProgress = -1;

    public ExecutionEventPublisher(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Returns a consumer that can be registered with the ExecutionController
     * to monitor execution status.
     */
    public Consumer<ExecutionStatus> getStatusConsumer() {
        return this::onStatusUpdate;
    }

    /**
     * Processes a status update and publishes appropriate events.
     */
    public void onStatusUpdate(ExecutionStatus status) {
        ExecutionState currentState = status.getState();

        // Only publish state change events when the state actually changes
        if (lastReportedState != currentState) {
            logger.debug("Execution state changed from {} to {}", lastReportedState, currentState);
            publishStateChangeEvent(status);
            lastReportedState = currentState;
        }

        // Only publish progress events when progress changes significantly
        double currentProgress = status.getProgress();
        if (Math.abs(currentProgress - lastReportedProgress) >= 0.05) {  // 5% progress change
            eventBus.publish(ExecutionStatusEvent.progress(this, status,
                    "Execution progress: " + Math.round(currentProgress * 100) + "%"));
            lastReportedProgress = currentProgress;
        }
    }

    /**
     * Publishes an event based on the new execution state.
     */
    private void publishStateChangeEvent(ExecutionStatus status) {
        String message = "Execution state changed to: " + status.getState().getDescription();

        switch (status.getState()) {
            case STARTING:
            case RUNNING:
                eventBus.publish(ExecutionStatusEvent.started(this, status, message));
                break;
            case COMPLETED:
                eventBus.publish(ExecutionStatusEvent.completed(this, status, message));
                break;
            case ERROR:
                eventBus.publish(ExecutionStatusEvent.failed(this, status,
                        "Execution failed: " + (status.getError() != null ? status.getError().getMessage() : "Unknown error")));

                // Also publish an error event for more detailed error handling
                if (status.getError() != null) {
                    eventBus.publish(ErrorEvent.high(this, "Execution error", status.getError(), "ExecutionController"));
                }
                break;
            case PAUSED:
                eventBus.publish(ExecutionStatusEvent.paused(this, status, message));
                break;
            case STOPPING:
            case STOPPED:
                eventBus.publish(ExecutionStatusEvent.stopped(this, status, message));
                break;
            case TIMEOUT:
                eventBus.publish(ExecutionStatusEvent.failed(this, status, "Execution timed out"));
                break;
            case IDLE:
                // No specific event for idle state
                break;
        }
    }

    /**
     * Manually publishes an execution event.
     */
    public void publishExecutionEvent(ExecutionStatusEvent event) {
        eventBus.publish(event);
    }
}