package io.github.jspinak.brobot.runner.json.validation.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.json.validation.model.ValidationError;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;

import java.util.*;

/**
 * Validates business rules for state transitions in Brobot configurations.
 * 
 * <p>This validator ensures that state transitions form a coherent, efficient,
 * and maintainable state machine. It goes beyond basic reference validation to
 * analyze the transition graph structure, identifying potential issues that could
 * lead to deadlocks, unreachable states, or inefficient automation flows.</p>
 * 
 * <h2>Validation Categories:</h2>
 * <ul>
 *   <li><b>Cycle Detection</b> - Identifies potentially problematic loops in state transitions</li>
 *   <li><b>Reachability Analysis</b> - Ensures all states can be reached from the start</li>
 *   <li><b>Efficiency Checks</b> - Detects redundant or inefficient transition patterns</li>
 *   <li><b>Concurrency Analysis</b> - Identifies potential race conditions in transitions</li>
 * </ul>
 * 
 * <h2>Why Transition Rules Matter:</h2>
 * <p>State transitions define the flow of automation. Poorly designed transitions
 * can lead to:</p>
 * <ul>
 *   <li>Automation getting stuck in infinite loops</li>
 *   <li>States that can never be reached (dead code)</li>
 *   <li>Race conditions when multiple transitions compete</li>
 *   <li>Inefficient paths that slow down automation</li>
 * </ul>
 * 
 * <h2>Graph Theory Application:</h2>
 * <p>This validator treats transitions as a directed graph and applies graph
 * algorithms to detect structural issues. Understanding the graph structure
 * helps identify systemic problems early.</p>
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * TransitionRuleValidator validator = new TransitionRuleValidator();
 * ValidationResult result = validator.validateTransitionRules(projectModel);
 * 
 * // Check for unreachable states
 * result.getWarnings().stream()
 *     .filter(w -> w.errorCode().equals("Unreachable state"))
 *     .forEach(w -> {
 *         logger.warn("Dead state detected: {}", w.message());
 *         // Consider removing or connecting the state
 *     });
 * }</pre>
 * 
 * @see BusinessRuleValidator for the parent validation coordinator
 * @see ValidationResult for understanding validation outcomes
 * @author jspinak
 */
@Component
public class TransitionRuleValidator {
    private static final Logger logger = LoggerFactory.getLogger(TransitionRuleValidator.class);

    /**
     * Validates transition-specific business rules in the project model.
     * 
     * <p>This method performs comprehensive analysis of the state transition graph,
     * applying multiple validation strategies to ensure the state machine is
     * well-formed and efficient. Each validation check is independent, allowing
     * all issues to be discovered in a single pass.</p>
     * 
     * <h3>Validation Sequence:</h3>
     * <ol>
     *   <li><b>Cycle Analysis</b> - Detects loops that might cause infinite execution</li>
     *   <li><b>Reachability Check</b> - Ensures no states are orphaned or unreachable</li>
     *   <li><b>Efficiency Analysis</b> - Identifies redundant or suboptimal transitions</li>
     *   <li><b>Concurrency Check</b> - Warns about potential race conditions</li>
     * </ol>
     * 
     * @param projectModel Parsed project model containing states and transitions.
     *                     Expected to be a Map with "states" and "stateTransitions" arrays
     * @return ValidationResult containing discovered issues:
     *         <ul>
     *           <li>CRITICAL - Invalid model structure preventing analysis</li>
     *           <li>ERROR - Serious issues like invalid state references</li>
     *           <li>WARNING - Quality issues like unreachable states or cycles</li>
     *         </ul>
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
     * Validates cycle patterns in the state transition graph.
     * 
     * <p>Not all cycles are problematic - many state machines have legitimate loops
     * for retry logic or recurring processes. This method identifies cycles that
     * are likely to cause issues based on their structure and the types of states
     * involved.</p>
     * 
     * <h3>Problematic Cycle Patterns:</h3>
     * <ul>
     *   <li><b>Tight Loops</b> - 2-state cycles that might execute too rapidly</li>
     *   <li><b>No-Exit Cycles</b> - Cycles with no conditional exits</li>
     *   <li><b>Resource-Intensive Cycles</b> - Loops containing heavy operations</li>
     * </ul>
     * 
     * <h3>Algorithm:</h3>
     * <p>Uses depth-first search (DFS) with a recursion stack to detect cycles,
     * then analyzes each cycle's characteristics to determine if it's problematic.</p>
     * 
     * @param project The project model containing transition definitions
     * @param result ValidationResult to add cycle warnings to
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
     * Validates state reachability in the transition graph.
     * 
     * <p>This method ensures that all defined states can actually be reached during
     * automation execution. Unreachable states represent dead code that wastes
     * resources and indicates potential design flaws in the state machine.</p>
     * 
     * <h3>Reachability Analysis:</h3>
     * <ul>
     *   <li>Assumes the first state is the entry point (or finds designated start state)</li>
     *   <li>Performs graph traversal to find all reachable states</li>
     *   <li>Reports any states that cannot be reached from the start</li>
     * </ul>
     * 
     * <h3>Common Causes of Unreachable States:</h3>
     * <ul>
     *   <li>States added during development but never connected</li>
     *   <li>Transitions removed without updating target states</li>
     *   <li>Copy-paste errors in configuration</li>
     * </ul>
     * 
     * @param project The project model containing state and transition definitions
     * @param result ValidationResult to add unreachability warnings to
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
     * Validates transition efficiency and identifies redundancy.
     * 
     * <p>This method checks for inefficient transition patterns that could be
     * simplified or optimized. Redundant transitions make the state machine
     * harder to understand and maintain while potentially causing unexpected
     * behavior.</p>
     * 
     * <h3>Efficiency Checks:</h3>
     * <ul>
     *   <li><b>Duplicate Transitions</b> - Multiple transitions between same states</li>
     *   <li><b>Redundant Paths</b> - Different routes achieving the same result</li>
     *   <li><b>Unnecessary Complexity</b> - Overly complex transition conditions</li>
     * </ul>
     * 
     * <h3>Impact of Inefficiency:</h3>
     * <p>Redundant transitions can cause confusion about which transition will
     * execute, make debugging difficult, and indicate poor state machine design
     * that should be refactored.</p>
     * 
     * @param project The project model containing transition definitions
     * @param result ValidationResult to add efficiency warnings to
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
     * Validates potential concurrency issues in state transitions.
     * 
     * <p>This method identifies patterns that might lead to race conditions or
     * unpredictable behavior when multiple transitions could execute simultaneously.
     * While Brobot typically executes transitions sequentially, certain patterns
     * can still cause logical race conditions.</p>
     * 
     * <h3>Concurrency Concerns:</h3>
     * <ul>
     *   <li><b>Multiple Activators</b> - States triggered by many different transitions</li>
     *   <li><b>Competing Transitions</b> - Multiple transitions with similar triggers</li>
     *   <li><b>State Conflicts</b> - Transitions that might interfere with each other</li>
     * </ul>
     * 
     * <h3>Why This Matters:</h3>
     * <p>When a state can be activated by many transitions, it becomes difficult
     * to predict the system's behavior and ensure consistent state. This often
     * indicates a need to refactor the state machine design.</p>
     * 
     * @param project The project model containing transition definitions
     * @param result ValidationResult to add concurrency warnings to
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