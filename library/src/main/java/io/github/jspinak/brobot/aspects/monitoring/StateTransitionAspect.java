package io.github.jspinak.brobot.aspects.monitoring;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.logging.BrobotLogger;
import io.github.jspinak.brobot.logging.events.ActionEvent;
import io.github.jspinak.brobot.model.state.State;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import io.github.jspinak.brobot.logging.LogCategory;
import io.github.jspinak.brobot.logging.LogLevel;


/**
 * Aspect that tracks and analyzes state transitions in the Brobot framework.
 *
 * <p>This aspect provides comprehensive monitoring of state machine behavior: - Tracks all state
 * transitions with timing and success rates - Builds a real-time state transition graph - Detects
 * unreachable states and transition bottlenecks - Generates visualizations of the state machine -
 * Provides analytics on navigation patterns
 *
 * <p>The collected data enables: - Debugging navigation issues - Optimizing state machine design -
 * Understanding application flow - Identifying common failure points
 */
@Aspect
@Component
@Slf4j
@ConditionalOnProperty(
        prefix = "brobot.aspects.state-transition",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class StateTransitionAspect {

    private final BrobotLogger brobotLogger;

    @Autowired
    public StateTransitionAspect(BrobotLogger brobotLogger) {
        this.brobotLogger = brobotLogger;
    }

    @Value("${brobot.aspects.state-transition.generate-visualizations:true}")
    private boolean generateVisualizations;

    @Value("${brobot.aspects.state-transition.visualization-dir:./state-visualizations}")
    private String visualizationDir;

    @Value("${brobot.aspects.state-transition.track-success-rates:true}")
    private boolean trackSuccessRates;

    // State transition tracking
    private final ConcurrentHashMap<String, StateNode> stateGraph = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TransitionStats> transitionStats =
            new ConcurrentHashMap<>();
    private final List<TransitionEvent> recentTransitions =
            Collections.synchronizedList(new ArrayList<>());

    // Global statistics
    private final AtomicLong totalTransitions = new AtomicLong();
    private final AtomicLong successfulTransitions = new AtomicLong();
    private final AtomicLong failedTransitions = new AtomicLong();

    @PostConstruct
    public void init() {
        log.info("State Transition Aspect initialized");
        if (generateVisualizations) {
            File dir = new File(visualizationDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
    }

    /** Pointcut for state navigation methods */
    @Pointcut(
            "execution(*"
                + " io.github.jspinak.brobot.navigation.transition.StateNavigator.navigate(..))")
    public void stateNavigation() {}

    /** Pointcut for state transition methods */
    @Pointcut(
            "execution(*"
                + " io.github.jspinak.brobot.navigation.transition.TransitionExecutor.execute*(..))")
    public void stateTransition() {}

    /** Combined pointcut for all transition-related methods */
    @Pointcut("stateNavigation() || stateTransition()")
    public void transitionMethods() {}

    /** Monitor state transitions */
    @Around("transitionMethods()")
    public Object monitorTransition(ProceedingJoinPoint joinPoint) throws Throwable {
        // Extract transition information from method arguments
        TransitionInfo transitionInfo = extractTransitionInfo(joinPoint);
        if (transitionInfo == null) {
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();
        boolean success = false;
        Throwable error = null;

        try {
            // Execute the transition
            Object result = joinPoint.proceed();

            // Determine success based on result
            success = isSuccessfulTransition(result);

            return result;

        } catch (Throwable e) {
            error = e;
            throw e;

        } finally {
            // Record the transition
            long duration = System.currentTimeMillis() - startTime;
            recordTransition(transitionInfo, success, duration, error);
        }
    }

    /** Extract transition information from method arguments */
    private TransitionInfo extractTransitionInfo(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String methodName = joinPoint.getSignature().getName();

        // Try to extract from and to states from arguments
        Set<String> fromStates = new HashSet<>();
        Set<String> toStates = new HashSet<>();

        for (Object arg : args) {
            if (arg instanceof State) {
                toStates.add(((State) arg).getName());
            } else if (arg instanceof Set) {
                Set<?> set = (Set<?>) arg;
                if (!set.isEmpty() && set.iterator().next() instanceof State) {
                    for (Object obj : set) {
                        State state = (State) obj;
                        if (methodName.contains("from")) {
                            fromStates.add(state.getName());
                        } else {
                            toStates.add(state.getName());
                        }
                    }
                }
            }
        }

        // If we couldn't extract states, return null
        if (fromStates.isEmpty() && toStates.isEmpty()) {
            return null;
        }

        TransitionInfo info = new TransitionInfo();
        info.setFromStates(fromStates);
        info.setToStates(toStates);
        info.setMethodName(methodName);
        info.setTimestamp(Instant.now());

        return info;
    }

    /** Determine if transition was successful */
    private boolean isSuccessfulTransition(Object result) {
        if (result == null) {
            return false;
        }

        // Check for boolean result
        if (result instanceof Boolean) {
            return (Boolean) result;
        }

        // Check for collection result (non-empty means success)
        if (result instanceof Collection) {
            return !((Collection<?>) result).isEmpty();
        }

        // Default to success if we got a result
        return true;
    }

    /** Record transition data */
    private void recordTransition(
            TransitionInfo info, boolean success, long duration, Throwable error) {
        // Update global counters
        totalTransitions.incrementAndGet();
        if (success) {
            successfulTransitions.incrementAndGet();
        } else {
            failedTransitions.incrementAndGet();
        }

        // Create transition event
        TransitionEvent event = new TransitionEvent();
        event.setFromStates(info.getFromStates());
        event.setToStates(info.getToStates());
        event.setSuccess(success);
        event.setDuration(duration);
        event.setTimestamp(info.getTimestamp());
        event.setError(error != null ? error.getMessage() : null);

        // Add to recent transitions
        recentTransitions.add(event);
        if (recentTransitions.size() > 1000) {
            recentTransitions.remove(0);
        }

        // Update state graph
        updateStateGraph(event);

        // Update transition statistics
        if (trackSuccessRates) {
            updateTransitionStats(event);
        }

        // Log the transition
        logTransition(event);
    }

    /** Update the state graph with transition information */
    private void updateStateGraph(TransitionEvent event) {
        // Add nodes for all states
        event.getFromStates().forEach(state -> stateGraph.computeIfAbsent(state, StateNode::new));
        event.getToStates().forEach(state -> stateGraph.computeIfAbsent(state, StateNode::new));

        // Add edges for successful transitions
        if (event.isSuccess()) {
            for (String from : event.getFromStates()) {
                for (String to : event.getToStates()) {
                    StateNode fromNode = stateGraph.get(from);
                    fromNode.addTransition(to);
                }
            }
        }
    }

    /** Update transition statistics */
    private void updateTransitionStats(TransitionEvent event) {
        for (String from : event.getFromStates()) {
            for (String to : event.getToStates()) {
                String key = from + " -> " + to;
                transitionStats.compute(
                        key,
                        (k, stats) -> {
                            if (stats == null) {
                                stats = new TransitionStats(from, to);
                            }
                            stats.recordTransition(event.isSuccess(), event.getDuration());
                            return stats;
                        });
            }
        }
    }

    /** Log transition event */
    private void logTransition(TransitionEvent event) {
        LogLevel level = event.isSuccess() ? LogLevel.DEBUG : LogLevel.WARN;

        brobotLogger.builder(LogCategory.TRANSITIONS)
                .level(level)
                .action("STATE_TRANSITION", "transition")
                .context("success", event.isSuccess())
                .duration(Duration.ofMillis(event.getDuration()))
                .context("fromStates", String.join(",", event.getFromStates()))
                .context("toStates", String.join(",", event.getToStates()))
                .context("error", event.getError())
                .log();
    }

    /** Generate state machine visualization */
    @Scheduled(
            fixedDelayString = "${brobot.aspects.state-transition.visualization-interval:300}000")
    public void generateVisualization() {
        if (!generateVisualizations || stateGraph.isEmpty()) {
            return;
        }

        try {
            String timestamp =
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File dotFile = new File(visualizationDir, "state_graph_" + timestamp + ".dot");

            try (FileWriter writer = new FileWriter(dotFile)) {
                writer.write("digraph StateTransitions {\n");
                writer.write("  rankdir=LR;\n");
                writer.write("  node [shape=box, style=rounded];\n");

                // Write nodes
                for (StateNode node : stateGraph.values()) {
                    writer.write(String.format("  \"%s\";\n", node.getStateName()));
                }

                // Write edges with statistics
                for (TransitionStats stats : transitionStats.values()) {
                    double successRate = stats.getSuccessRate();
                    String color = successRate > 80 ? "green" : successRate > 50 ? "yellow" : "red";
                    String label =
                            String.format("%.0f%% (%d)", successRate, stats.getTotalAttempts());

                    writer.write(
                            String.format(
                                    "  \"%s\" -> \"%s\" [label=\"%s\", color=%s];\n",
                                    stats.getFromState(), stats.getToState(), label, color));
                }

                writer.write("}\n");
            }

            log.info("Generated state visualization: {}", dotFile.getAbsolutePath());

        } catch (IOException e) {
            log.error("Failed to generate state visualization", e);
        }
    }

    /** Generate analytics report */
    @Scheduled(fixedDelayString = "${brobot.aspects.state-transition.report-interval:600}000")
    public void generateAnalyticsReport() {
        if (stateGraph.isEmpty()) {
            return;
        }

        log.info("=== State Transition Analytics ===");
        log.info("Total transitions: {}", totalTransitions.get());
        log.info(
                "Successful: {} ({:.1f}%)",
                successfulTransitions.get(),
                (double) successfulTransitions.get() / totalTransitions.get() * 100);
        log.info("Failed: {}", failedTransitions.get());

        // Find most common transitions
        List<Map.Entry<String, TransitionStats>> commonTransitions =
                transitionStats.entrySet().stream()
                        .sorted(
                                (a, b) ->
                                        Integer.compare(
                                                b.getValue().getTotalAttempts(),
                                                a.getValue().getTotalAttempts()))
                        .limit(10)
                        .collect(Collectors.toList());

        log.info("Most common transitions:");
        commonTransitions.forEach(
                entry -> {
                    TransitionStats stats = entry.getValue();
                    log.info(
                            "  {} ({}x, {:.1f}% success, avg {}ms)",
                            entry.getKey(),
                            stats.getTotalAttempts(),
                            stats.getSuccessRate(),
                            stats.getAverageDuration());
                });

        // Find unreachable states
        Set<String> allStates = new HashSet<>(stateGraph.keySet());
        Set<String> reachableStates = new HashSet<>();

        // Assume first state or states with incoming transitions are reachable
        stateGraph
                .values()
                .forEach(
                        node -> {
                            reachableStates.addAll(node.getOutgoingTransitions());
                        });

        Set<String> unreachableStates = new HashSet<>(allStates);
        unreachableStates.removeAll(reachableStates);

        if (!unreachableStates.isEmpty()) {
            log.warn("Potentially unreachable states: {}", unreachableStates);
        }
    }

    /** Get state graph for external analysis */
    public Map<String, StateNode> getStateGraph() {
        return new HashMap<>(stateGraph);
    }

    /** Get transition statistics */
    public Map<String, TransitionStats> getTransitionStats() {
        return new HashMap<>(transitionStats);
    }

    /** Reset all statistics */
    public void resetStatistics() {
        stateGraph.clear();
        transitionStats.clear();
        recentTransitions.clear();
        totalTransitions.set(0);
        successfulTransitions.set(0);
        failedTransitions.set(0);
        log.info("State transition statistics reset");
    }

    /** Inner class representing transition information */
    @Data
    private static class TransitionInfo {
        private Set<String> fromStates;
        private Set<String> toStates;
        private String methodName;
        private Instant timestamp;
    }

    /** Inner class representing a transition event */
    @Data
    private static class TransitionEvent {
        private Set<String> fromStates;
        private Set<String> toStates;
        private boolean success;
        private long duration;
        private Instant timestamp;
        private String error;
    }

    /** Inner class representing a state node in the graph */
    @Data
    public static class StateNode {
        private final String stateName;
        private final Set<String> outgoingTransitions =
                Collections.synchronizedSet(new HashSet<>());
        private final AtomicInteger visitCount = new AtomicInteger();

        public StateNode(String stateName) {
            this.stateName = stateName;
        }

        public void addTransition(String toState) {
            outgoingTransitions.add(toState);
            visitCount.incrementAndGet();
        }
    }

    /** Inner class for tracking transition statistics */
    @Data
    public static class TransitionStats {
        private final String fromState;
        private final String toState;
        private final AtomicInteger totalAttempts = new AtomicInteger();
        private final AtomicInteger successfulAttempts = new AtomicInteger();
        private final AtomicLong totalDuration = new AtomicLong();

        public TransitionStats(String fromState, String toState) {
            this.fromState = fromState;
            this.toState = toState;
        }

        public void recordTransition(boolean success, long duration) {
            totalAttempts.incrementAndGet();
            if (success) {
                successfulAttempts.incrementAndGet();
            }
            totalDuration.addAndGet(duration);
        }

        public double getSuccessRate() {
            int total = totalAttempts.get();
            return total > 0 ? (double) successfulAttempts.get() / total * 100 : 0;
        }

        public long getAverageDuration() {
            int total = totalAttempts.get();
            return total > 0 ? totalDuration.get() / total : 0;
        }

        public int getTotalAttempts() {
            return totalAttempts.get();
        }
    }
}
