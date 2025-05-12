package io.github.jspinak.brobot.json.schemaValidation.business;

import io.github.jspinak.brobot.json.schemaValidation.model.ValidationError;
import io.github.jspinak.brobot.json.schemaValidation.model.ValidationResult;
import io.github.jspinak.brobot.json.schemaValidation.model.ValidationSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Validates business rules related to state transitions.
 * This includes checking for transition loops, state reachability,
 * transition efficiency, and other transition-specific rules.
 */
@Component
public class TransitionRuleValidator {
    private static final Logger logger = LoggerFactory.getLogger(TransitionRuleValidator.class);

    /**
     * Validates transition-specific business rules.
     *
     * @param projectModel Parsed project model
     * @return Validation result
     */
    public ValidationResult validateTransitionRules(Object projectModel) {
        ValidationResult result = new ValidationResult();

        if (projectModel == null) {
            result.addError(new ValidationError(
                    "Invalid project model",
                    "Project model is null",
                    ValidationSeverity.CRITICAL
            ));
            return result;
        }

        try {
            Map<String, Object> project = (Map<String, Object>) projectModel;

            // Check for transition cycles
            validateTransitionCycles(project, result);

            // Check for unreachable states
            validateStateReachability(project, result);

            // Check for inefficient transitions
            validateTransitionEfficiency(project, result);

            // Check for potential race conditions in transitions
            validateTransitionConcurrency(project, result);

        } catch (ClassCastException e) {
            logger.error("Project model is not a valid type", e);
            result.addError(new ValidationError(
                    "Invalid project model type",
                    "Project model could not be processed: " + e.getMessage(),
                    ValidationSeverity.CRITICAL
            ));
        } catch (Exception e) {
            logger.error("Error during transition rule validation", e);
            result.addError(new ValidationError(
                    "Validation error",
                    "Error validating transition rules: " + e.getMessage(),
                    ValidationSeverity.ERROR
            ));
        }

        return result;
    }

    /**
     * Validates that there are no problematic cycles in the transition graph.
     * Some cycles are normal and expected, but certain patterns can lead to issues.
     */
    private void validateTransitionCycles(Map<String, Object> project, ValidationResult result) {
        if (!project.containsKey("states") || !project.containsKey("stateTransitions")) {
            return;
        }

        try {
            // Build a directed graph of state transitions
            Map<Integer, Set<Integer>> transitionGraph = buildTransitionGraph(project);

            // Find cycles in the graph
            Set<List<Integer>> cycles = findCycles(transitionGraph);

            // Analyze each cycle for potential issues
            for (List<Integer> cycle : cycles) {
                if (isPotentiallyProblematicCycle(cycle, project)) {
                    result.addError(new ValidationError(
                            "Potentially problematic transition cycle",
                            "Found a transition cycle that may cause issues: " + formatCycle(cycle, project),
                            ValidationSeverity.WARNING
                    ));
                }
            }
        } catch (Exception e) {
            logger.error("Error during transition cycle validation", e);
            result.addError(new ValidationError(
                    "Validation error",
                    "Error validating transition cycles: " + e.getMessage(),
                    ValidationSeverity.ERROR
            ));
        }
    }

    /**
     * Validates that all states are reachable from at least one other state.
     */
    private void validateStateReachability(Map<String, Object> project, ValidationResult result) {
        if (!project.containsKey("states") || !project.containsKey("stateTransitions")) {
            return;
        }

        try {
            // Get all state IDs
            List<Map<String, Object>> states = (List<Map<String, Object>>) project.get("states");
            Set<Integer> stateIds = new HashSet<>();

            for (Map<String, Object> state : states) {
                if (state.containsKey("id")) {
                    stateIds.add((Integer) state.get("id"));
                }
            }

            // Build graph of transitions
            Map<Integer, Set<Integer>> transitionGraph = buildTransitionGraph(project);

            // Check for unreachable states
            Set<Integer> reachableStates = new HashSet<>();

            // Assume first state is reachable (or find a designated "start" state)
            if (!stateIds.isEmpty()) {
                Integer startState = stateIds.iterator().next();
                findReachableStates(startState, transitionGraph, reachableStates);
            }

            // Report unreachable states
            for (Integer stateId : stateIds) {
                if (!reachableStates.contains(stateId)) {
                    result.addError(new ValidationError(
                            "Unreachable state",
                            "State with ID " + stateId + " appears to be unreachable from other states",
                            ValidationSeverity.WARNING
                    ));
                }
            }

        } catch (Exception e) {
            logger.error("Error during state reachability validation", e);
            result.addError(new ValidationError(
                    "Validation error",
                    "Error validating state reachability: " + e.getMessage(),
                    ValidationSeverity.ERROR
            ));
        }
    }

    /**
     * Validates transition efficiency.
     */
    private void validateTransitionEfficiency(Map<String, Object> project, ValidationResult result) {
        if (!project.containsKey("stateTransitions")) {
            return;
        }

        try {
            // Analyze transitions for efficiency issues
            List<Map<String, Object>> transitions = (List<Map<String, Object>>) project.get("stateTransitions");

            // Check for redundant transitions (multiple transitions between same states)
            Map<String, List<Integer>> stateToStateTransitions = new HashMap<>();

            for (int i = 0; i < transitions.size(); i++) {
                Map<String, Object> transition = transitions.get(i);

                if (transition.containsKey("sourceStateId") && transition.containsKey("statesToEnter") &&
                        ((List<Integer>)transition.get("statesToEnter")).size() > 0) {

                    Integer sourceId = (Integer) transition.get("sourceStateId");
                    List<Integer> targetIds = (List<Integer>) transition.get("statesToEnter");

                    for (Integer targetId : targetIds) {
                        String key = sourceId + "->" + targetId;
                        stateToStateTransitions.computeIfAbsent(key, k -> new ArrayList<>())
                                .add(transition.containsKey("id") ? (Integer) transition.get("id") : i);
                    }
                }
            }

            // Report redundant transitions
            for (Map.Entry<String, List<Integer>> entry : stateToStateTransitions.entrySet()) {
                if (entry.getValue().size() > 1) {
                    result.addError(new ValidationError(
                            "Redundant transitions",
                            "Multiple transitions exist between the same states: " +
                                    entry.getKey() + " (transition IDs: " + entry.getValue() + ")",
                            ValidationSeverity.WARNING
                    ));
                }
            }

        } catch (Exception e) {
            logger.error("Error during transition efficiency validation", e);
            result.addError(new ValidationError(
                    "Validation error",
                    "Error validating transition efficiency: " + e.getMessage(),
                    ValidationSeverity.ERROR
            ));
        }
    }

    /**
     * Validates potential concurrency issues in transitions.
     */
    private void validateTransitionConcurrency(Map<String, Object> project, ValidationResult result) {
        if (!project.containsKey("stateTransitions")) {
            return;
        }

        try {
            // Identify states that are activated by multiple transitions
            List<Map<String, Object>> transitions = (List<Map<String, Object>>) project.get("stateTransitions");

            Map<Integer, List<Integer>> stateActivators = new HashMap<>();

            for (int i = 0; i < transitions.size(); i++) {
                Map<String, Object> transition = transitions.get(i);

                if (transition.containsKey("statesToEnter")) {
                    List<Integer> targetIds = (List<Integer>) transition.get("statesToEnter");

                    for (Integer targetId : targetIds) {
                        stateActivators.computeIfAbsent(targetId, k -> new ArrayList<>())
                                .add(transition.containsKey("id") ? (Integer) transition.get("id") : i);
                    }
                }
            }

            // Check for states activated by multiple transitions
            for (Map.Entry<Integer, List<Integer>> entry : stateActivators.entrySet()) {
                if (entry.getValue().size() > 3) {  // More than 3 transitions to same state might be a concern
                    result.addError(new ValidationError(
                            "Potential concurrency issue",
                            "State " + entry.getKey() + " is activated by many transitions (" +
                                    entry.getValue().size() + "). Consider consolidating.",
                            ValidationSeverity.WARNING
                    ));
                }
            }

        } catch (Exception e) {
            logger.error("Error during transition concurrency validation", e);
            result.addError(new ValidationError(
                    "Validation error",
                    "Error validating transition concurrency: " + e.getMessage(),
                    ValidationSeverity.ERROR
            ));
        }
    }

    /**
     * Builds a directed graph of state transitions.
     */
    private Map<Integer, Set<Integer>> buildTransitionGraph(Map<String, Object> project) {
        Map<Integer, Set<Integer>> graph = new HashMap<>();

        List<Map<String, Object>> transitions = (List<Map<String, Object>>) project.get("stateTransitions");

        for (Map<String, Object> transition : transitions) {
            if (transition.containsKey("sourceStateId") && transition.containsKey("statesToEnter")) {
                Integer sourceId = (Integer) transition.get("sourceStateId");
                List<Integer> targetIds = (List<Integer>) transition.get("statesToEnter");

                Set<Integer> outgoingStates = graph.computeIfAbsent(sourceId, k -> new HashSet<>());
                outgoingStates.addAll(targetIds);
            }
        }

        return graph;
    }

    /**
     * Finds cycles in the transition graph using depth-first search.
     */
    private Set<List<Integer>> findCycles(Map<Integer, Set<Integer>> graph) {
        Set<List<Integer>> cycles = new HashSet<>();
        Set<Integer> visited = new HashSet<>();
        Set<Integer> recursionStack = new HashSet<>();

        for (Integer node : graph.keySet()) {
            findCyclesDFS(node, graph, visited, recursionStack, new ArrayList<>(), cycles);
        }

        return cycles;
    }

    /**
     * Recursive DFS helper to find cycles.
     */
    private void findCyclesDFS(Integer current, Map<Integer, Set<Integer>> graph,
                               Set<Integer> visited, Set<Integer> recursionStack,
                               List<Integer> path, Set<List<Integer>> cycles) {

        if (recursionStack.contains(current)) {
            // Found a cycle
            int startIndex = path.indexOf(current);
            if (startIndex >= 0) {
                List<Integer> cycle = new ArrayList<>(path.subList(startIndex, path.size()));
                cycle.add(current); // Complete the cycle
                cycles.add(cycle);
            }
            return;
        }

        if (visited.contains(current)) {
            return;
        }

        visited.add(current);
        recursionStack.add(current);
        path.add(current);

        if (graph.containsKey(current)) {
            for (Integer neighbor : graph.get(current)) {
                findCyclesDFS(neighbor, graph, visited, recursionStack, path, cycles);
            }
        }

        path.removeLast();
        recursionStack.remove(current);
    }

    /**
     * Finds all states reachable from a given start state.
     */
    private void findReachableStates(Integer startState, Map<Integer, Set<Integer>> graph,
                                     Set<Integer> reachableStates) {
        if (reachableStates.contains(startState)) {
            return;
        }

        reachableStates.add(startState);

        if (graph.containsKey(startState)) {
            for (Integer neighbor : graph.get(startState)) {
                findReachableStates(neighbor, graph, reachableStates);
            }
        }
    }

    /**
     * Determines if a cycle is potentially problematic.
     */
    private boolean isPotentiallyProblematicCycle(List<Integer> cycle, Map<String, Object> project) {
        // Example logic: Cycles of length 2 might indicate reciprocal transitions
        if (cycle.size() == 2) {
            return true;
        }

        // Short cycles that don't involve specific state types might be problematic
        if (cycle.size() <= 3) {
            // Example: Check if any state in the cycle is a "special" type that's expected in cycles
            for (Integer stateId : cycle) {
                if (isSpecialCycleState(stateId, project)) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    /**
     * Determines if a state is a special type that's expected to participate in cycles.
     */
    private boolean isSpecialCycleState(Integer stateId, Map<String, Object> project) {
        // This is just a placeholder - in a real implementation, we'd check state properties
        return false;
    }

    /**
     * Formats a cycle for display in error messages.
     */
    private String formatCycle(List<Integer> cycle, Map<String, Object> project) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < cycle.size(); i++) {
            Integer stateId = cycle.get(i);
            sb.append(getStateName(stateId, project));

            if (i < cycle.size() - 1) {
                sb.append(" -> ");
            }
        }

        return sb.toString();
    }

    /**
     * Gets a state name given its ID.
     */
    private String getStateName(Integer stateId, Map<String, Object> project) {
        if (project.containsKey("states")) {
            List<Map<String, Object>> states = (List<Map<String, Object>>) project.get("states");

            for (Map<String, Object> state : states) {
                if (state.containsKey("id") && state.get("id").equals(stateId)) {
                    return state.containsKey("name") ?
                            state.get("name") + " (#" + stateId + ")" :
                            "#" + stateId;
                }
            }
        }

        return "#" + stateId;
    }
}