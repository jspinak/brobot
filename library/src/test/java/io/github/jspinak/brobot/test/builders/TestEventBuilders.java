package io.github.jspinak.brobot.test.builders;

import java.time.Duration;
import java.util.UUID;

import io.github.jspinak.brobot.action.basic.find.FindStrategy;
import io.github.jspinak.brobot.logging.events.ActionEvent;
import io.github.jspinak.brobot.logging.events.MatchEvent;
import io.github.jspinak.brobot.logging.events.PerformanceEvent;
import io.github.jspinak.brobot.logging.events.TransitionEvent;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;

/**
 * Factory class for creating test event objects. Provides pre-configured builders for common test
 * scenarios.
 */
public class TestEventBuilders {

    /**
     * Creates a default ActionEvent builder for testing.
     *
     * @return An ActionEvent.Builder with default test values
     */
    public static ActionEvent.ActionEventBuilder actionEvent() {
        return ActionEvent.builder()
                .actionType("TEST_ACTION")
                .target("test-target")
                .success(true)
                .duration(Duration.ofMillis(100))
                .correlationId(UUID.randomUUID().toString());
    }

    /**
     * Creates a failed ActionEvent builder for testing.
     *
     * @return An ActionEvent.Builder configured for a failed action
     */
    public static ActionEvent.ActionEventBuilder failedActionEvent() {
        return ActionEvent.builder()
                .actionType("FAILED_ACTION")
                .target("failed-target")
                .success(false)
                .duration(Duration.ofMillis(500))
                .errorMessage("Action failed: Target not found")
                .correlationId(UUID.randomUUID().toString());
    }

    /**
     * Creates a click ActionEvent builder.
     *
     * @param target The click target
     * @return An ActionEvent.Builder configured for a click action
     */
    public static ActionEvent.ActionEventBuilder clickEvent(String target) {
        return ActionEvent.builder()
                .actionType("CLICK")
                .target(target)
                .success(true)
                .duration(Duration.ofMillis(50))
                .location(new Location(100, 200))
                .correlationId(UUID.randomUUID().toString());
    }

    /**
     * Creates a type ActionEvent builder.
     *
     * @param text The text to type
     * @return An ActionEvent.Builder configured for a type action
     */
    public static ActionEvent.ActionEventBuilder typeEvent(String text) {
        return ActionEvent.builder()
                .actionType("TYPE")
                .target("text-field")
                .success(true)
                .duration(Duration.ofMillis(200))
                .correlationId(UUID.randomUUID().toString());
    }

    /**
     * Creates a default TransitionEvent builder for testing.
     *
     * @return A TransitionEvent.Builder with default test values
     */
    public static TransitionEvent.TransitionEventBuilder transitionEvent() {
        return TransitionEvent.builder()
                .fromState("STATE_A")
                .toState("STATE_B")
                .success(true)
                .duration(Duration.ofMillis(200))
                .correlationId(UUID.randomUUID().toString());
    }

    /**
     * Creates a failed TransitionEvent builder.
     *
     * @return A TransitionEvent.Builder configured for a failed transition
     */
    public static TransitionEvent.TransitionEventBuilder failedTransitionEvent() {
        return TransitionEvent.builder()
                .fromState("STATE_A")
                .toState("STATE_B")
                .success(false)
                .duration(Duration.ofMillis(1000))
                .errorMessage("Transition timeout")
                .correlationId(UUID.randomUUID().toString());
    }

    /**
     * Creates a transition with specific states.
     *
     * @param from The source state
     * @param to The target state
     * @return A TransitionEvent.Builder configured with the specified states
     */
    public static TransitionEvent.TransitionEventBuilder transitionBetween(String from, String to) {
        return TransitionEvent.builder()
                .fromState(from)
                .toState(to)
                .success(true)
                .duration(Duration.ofMillis(150))
                .correlationId(UUID.randomUUID().toString());
    }

    /**
     * Creates a default MatchEvent builder for testing.
     *
     * @return A MatchEvent.Builder with default test values
     */
    public static MatchEvent.MatchEventBuilder matchEvent() {
        Match match =
                new Match.Builder()
                        .setRegion(new Region(100, 200, 50, 50))
                        .setSimScore(0.95)
                        .build();

        return MatchEvent.builder()
                .pattern("test-pattern")
                .matches(java.util.Collections.singletonList(match))
                .searchTime(Duration.ofMillis(30))
                .strategy(FindStrategy.FIRST)
                .searchRegion(new Region(0, 0, 800, 600))
                .correlationId(UUID.randomUUID().toString());
    }

    /**
     * Creates a MatchEvent builder for no match found.
     *
     * @return A MatchEvent.Builder configured for no match
     */
    public static MatchEvent.MatchEventBuilder noMatchEvent() {
        return MatchEvent.builder()
                .pattern("missing-pattern")
                .matches(java.util.Collections.emptyList())
                .searchTime(Duration.ofMillis(100))
                .strategy(FindStrategy.FIRST)
                .searchRegion(new Region(0, 0, 800, 600))
                .correlationId(UUID.randomUUID().toString());
    }

    /**
     * Creates a MatchEvent with specific similarity.
     *
     * @param pattern The pattern name
     * @param similarity The match similarity score
     * @return A MatchEvent.Builder configured with the specified similarity
     */
    public static MatchEvent.MatchEventBuilder matchWithSimilarity(
            String pattern, double similarity) {
        Match match =
                similarity > 0.7
                        ? new Match.Builder()
                                .setRegion(new Region(150, 250, 60, 40))
                                .setSimScore(similarity)
                                .build()
                        : null;

        return MatchEvent.builder()
                .pattern(pattern)
                .matches(
                        match != null
                                ? java.util.Collections.singletonList(match)
                                : java.util.Collections.emptyList())
                .searchTime(Duration.ofMillis(35))
                .strategy(FindStrategy.FIRST)
                .searchRegion(new Region(0, 0, 800, 600))
                .correlationId(UUID.randomUUID().toString());
    }

    /**
     * Creates a default PerformanceEvent builder for testing.
     *
     * @return A PerformanceEvent.Builder with default test values
     */
    public static PerformanceEvent.PerformanceEventBuilder performanceEvent() {
        return PerformanceEvent.builder()
                .operation("test-operation")
                .duration(Duration.ofMillis(500))
                .memoryUsed(1024 * 1024) // 1MB
                .correlationId(UUID.randomUUID().toString());
    }

    /**
     * Creates a slow PerformanceEvent builder.
     *
     * @return A PerformanceEvent.Builder configured for a slow operation
     */
    public static PerformanceEvent.PerformanceEventBuilder slowOperationEvent() {
        return PerformanceEvent.builder()
                .operation("slow-operation")
                .duration(Duration.ofSeconds(5))
                .memoryUsed(5 * 1024 * 1024) // 5MB
                .breakdown(
                        java.util.Map.of(
                                "init", Duration.ofSeconds(1),
                                "process", Duration.ofSeconds(3),
                                "cleanup", Duration.ofSeconds(1)))
                .correlationId(UUID.randomUUID().toString());
    }

    /**
     * Creates a PerformanceEvent for a specific operation.
     *
     * @param operation The operation name
     * @param durationMs The duration in milliseconds
     * @return A PerformanceEvent.Builder configured for the operation
     */
    public static PerformanceEvent.PerformanceEventBuilder operationPerformance(
            String operation, long durationMs) {
        return PerformanceEvent.builder()
                .operation(operation)
                .duration(Duration.ofMillis(durationMs))
                .memoryUsed(1024 * 512) // 512KB
                .correlationId(UUID.randomUUID().toString());
    }

    /**
     * Creates a batch of related events with the same correlation ID. Useful for testing correlated
     * event flows.
     */
    public static class CorrelatedEventSet {
        private final String correlationId = UUID.randomUUID().toString();

        public ActionEvent.ActionEventBuilder action() {
            return actionEvent().correlationId(correlationId);
        }

        public TransitionEvent.TransitionEventBuilder transition() {
            return transitionEvent().correlationId(correlationId);
        }

        public MatchEvent.MatchEventBuilder match() {
            return matchEvent().correlationId(correlationId);
        }

        public PerformanceEvent.PerformanceEventBuilder performance() {
            return performanceEvent().correlationId(correlationId);
        }

        public String getCorrelationId() {
            return correlationId;
        }
    }

    /**
     * Creates a set of correlated events for testing event flows.
     *
     * @return A CorrelatedEventSet with the same correlation ID
     */
    public static CorrelatedEventSet correlatedEvents() {
        return new CorrelatedEventSet();
    }
}
