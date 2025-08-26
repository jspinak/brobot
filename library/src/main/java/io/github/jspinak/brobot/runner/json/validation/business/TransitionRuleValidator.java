package io.github.jspinak.brobot.runner.json.validation.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.json.validation.model.ValidationError;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;

import java.util.*;
import java.util.stream.Collectors;

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

            // First check for malformed transitions
            validateTransitionStructure(project, result);

            // Check for transition cycles
            validateTransitionCycles(project, result);

            // Check for unreachable states
            validateStateReachability(project, result);

            // Check for inefficient transitions
            validateTransitionEfficiency(project, result);
            
            // Check for duplicate transitions
            detectDuplicateTransitions(project, result);
            
            // Check for redundant conditions
            detectRedundantConditions(project, result);
            
            // Validate probability-based transitions
            validateProbabilities(project, result);

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
     * Validates transition structure for malformed data.
     */
    private void validateTransitionStructure(Map<String, Object> project, ValidationResult result) {
        if (!project.containsKey("stateTransitions")) {
            return;
        }
        
        List<Map<String, Object>> transitions = (List<Map<String, Object>>) project.get("stateTransitions");
        
        for (Map<String, Object> transition : transitions) {
            if (transition == null) continue;
            
            // Check sourceStateId/fromState fields
            if (transition.containsKey("sourceStateId")) {
                Object value = transition.get("sourceStateId");
                if (value != null && !(value instanceof Number)) {
                    result.addError(new ValidationError(
                        "Malformed transition",
                        "Error validating transition: sourceStateId is not a valid number type",
                        ValidationSeverity.ERROR
                    ));
                }
            }
            
            if (transition.containsKey("fromState")) {
                Object value = transition.get("fromState");
                if (value != null && !(value instanceof Number)) {
                    result.addError(new ValidationError(
                        "Malformed transition",
                        "Error validating transition: fromState is not a valid number type",
                        ValidationSeverity.ERROR
                    ));
                }
            }
            
            // Check statesToEnter/toState fields  
            if (transition.containsKey("statesToEnter")) {
                Object value = transition.get("statesToEnter");
                if (value != null && !(value instanceof List)) {
                    result.addError(new ValidationError(
                        "Malformed transition",
                        "Error validating transition: statesToEnter is not a valid list",
                        ValidationSeverity.ERROR
                    ));
                }
            }
            
            if (transition.containsKey("toState")) {
                Object value = transition.get("toState");
                if (value != null && !(value instanceof Number || value instanceof List)) {
                    result.addError(new ValidationError(
                        "Malformed transition",
                        "Error validating transition: toState is not a valid type",
                        ValidationSeverity.ERROR
                    ));
                }
            }
        }
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

            Map<Integer, List<Map<String, Object>>> stateActivators = new HashMap<>();
            Map<Integer, List<Map<String, Object>>> asyncActivators = new HashMap<>();

            for (Map<String, Object> transition : transitions) {
                Set<Integer> targetIds = getTargetStateIds(transition);
                boolean isAsync = transition.containsKey("async") && 
                                 Boolean.TRUE.equals(transition.get("async"));

                for (Integer targetId : targetIds) {
                    stateActivators.computeIfAbsent(targetId, k -> new ArrayList<>())
                            .add(transition);
                    
                    if (isAsync) {
                        asyncActivators.computeIfAbsent(targetId, k -> new ArrayList<>())
                                .add(transition);
                    }
                }
            }

            // Check for potential race conditions with async transitions
            for (Map.Entry<Integer, List<Map<String, Object>>> entry : asyncActivators.entrySet()) {
                if (entry.getValue().size() > 1) {
                    result.addError(new ValidationError(
                            "Potential race condition",
                            "State " + entry.getKey() + " is targeted by multiple async transitions. " +
                            "This may cause race conditions.",
                            ValidationSeverity.WARNING
                    ));
                }
            }

            // Check for states activated by too many transitions
            for (Map.Entry<Integer, List<Map<String, Object>>> entry : stateActivators.entrySet()) {
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
            try {
                Integer sourceId = getSourceStateId(transition);
                Set<Integer> targetIds = getTargetStateIds(transition);
                
                if (sourceId != null && targetIds != null && !targetIds.isEmpty()) {
                    Set<Integer> outgoingStates = graph.computeIfAbsent(sourceId, k -> new HashSet<>());
                    outgoingStates.addAll(targetIds);
                }
            } catch (ClassCastException e) {
                // Malformed transition - skip it but it will be reported elsewhere
                logger.warn("Skipping malformed transition in graph building", e);
            }
        }

        return graph;
    }

    /**
     * Finds ALL cycles in the transition graph, including multiple independent cycles.
     * Uses Tarjan's algorithm for strongly connected components.
     */
    private Set<List<Integer>> findCycles(Map<Integer, Set<Integer>> graph) {
        Set<List<Integer>> allCycles = new HashSet<>();
        
        // Find strongly connected components first
        List<Set<Integer>> sccs = findStronglyConnectedComponents(graph);
        
        // For each SCC with more than one node, find cycles
        for (Set<Integer> scc : sccs) {
            if (scc.size() > 1) {
                // This SCC contains at least one cycle
                findCyclesInSCC(scc, graph, allCycles);
            } else {
                // Check for self-loops
                Integer node = scc.iterator().next();
                if (graph.containsKey(node) && graph.get(node).contains(node)) {
                    allCycles.add(Arrays.asList(node, node));
                }
            }
        }
        
        return allCycles;
    }

    /**
     * Finds strongly connected components using Tarjan's algorithm.
     */
    private List<Set<Integer>> findStronglyConnectedComponents(Map<Integer, Set<Integer>> graph) {
        TarjanSCC tarjan = new TarjanSCC(graph);
        return tarjan.findSCCs();
    }
    
    /**
     * Find cycles within a strongly connected component.
     */
    private void findCyclesInSCC(Set<Integer> scc, Map<Integer, Set<Integer>> graph, 
                                  Set<List<Integer>> cycles) {
        // An SCC with more than one node contains at least one cycle
        // Find the simplest cycle by doing BFS from each node
        Set<List<Integer>> foundCycles = new HashSet<>();
        
        for (Integer start : scc) {
            List<Integer> cycle = findSimpleCycleFromNode(start, scc, graph);
            if (cycle != null && !cycle.isEmpty()) {
                List<Integer> normalizedCycle = normalizeCycle(cycle);
                if (!foundCycles.contains(normalizedCycle)) {
                    cycles.add(normalizedCycle);
                    foundCycles.add(normalizedCycle);
                }
            }
        }
    }
    
    /**
     * Find a simple cycle starting from a given node using BFS.
     */
    private List<Integer> findSimpleCycleFromNode(Integer start, Set<Integer> scc, 
                                                  Map<Integer, Set<Integer>> graph) {
        Queue<List<Integer>> queue = new LinkedList<>();
        queue.offer(Arrays.asList(start));
        Set<Integer> visited = new HashSet<>();
        
        while (!queue.isEmpty()) {
            List<Integer> path = queue.poll();
            Integer current = path.get(path.size() - 1);
            
            if (graph.containsKey(current)) {
                for (Integer next : graph.get(current)) {
                    if (!scc.contains(next)) continue;
                    
                    if (next.equals(start) && path.size() > 1) {
                        // Found a cycle back to start
                        List<Integer> cycle = new ArrayList<>(path);
                        cycle.add(start);
                        return cycle;
                    }
                    
                    if (!path.contains(next)) {
                        List<Integer> newPath = new ArrayList<>(path);
                        newPath.add(next);
                        queue.offer(newPath);
                    }
                }
            }
        }
        
        return null;
    }
    
    
    /**
     * Normalize cycle to start with smallest node for comparison.
     */
    private List<Integer> normalizeCycle(List<Integer> cycle) {
        if (cycle.isEmpty()) return cycle;
        
        // Remove duplicate last element if it's the same as first
        List<Integer> normalized = new ArrayList<>(cycle);
        if (normalized.size() > 1 && 
            normalized.get(0).equals(normalized.get(normalized.size() - 1))) {
            normalized.remove(normalized.size() - 1);
        }
        
        // Find minimum element
        int minIndex = 0;
        Integer minValue = normalized.get(0);
        for (int i = 1; i < normalized.size(); i++) {
            if (normalized.get(i) < minValue) {
                minValue = normalized.get(i);
                minIndex = i;
            }
        }
        
        // Rotate to start with minimum
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < normalized.size(); i++) {
            result.add(normalized.get((minIndex + i) % normalized.size()));
        }
        
        return result;
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
        // All cycles are potentially problematic unless they involve special states
        if (cycle.size() >= 2) {
            // Check if any state in the cycle is a "special" type that's expected in cycles
            boolean hasSpecialState = false;
            for (Integer stateId : cycle) {
                if (isSpecialCycleState(stateId, project)) {
                    hasSpecialState = true;
                    break;
                }
            }
            
            // If no special states, the cycle is problematic
            if (!hasSpecialState) {
                return true;
            }
            
            // Even with special states, very short cycles might be problematic
            if (cycle.size() == 2) {
                return true;
            }
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
    
    /**
     * Detects duplicate transitions between states.
     */
    private void detectDuplicateTransitions(Map<String, Object> project, ValidationResult result) {
        if (!project.containsKey("stateTransitions")) {
            return;
        }
        
        try {
            List<Map<String, Object>> transitions = (List<Map<String, Object>>) project.get("stateTransitions");
            Map<String, List<Map<String, Object>>> transitionMap = new HashMap<>();
            
            // Group transitions by source->target
            for (Map<String, Object> transition : transitions) {
                Integer sourceId = getSourceStateId(transition);
                Set<Integer> targets = getTargetStateIds(transition);
                
                if (sourceId != null) {
                    for (Integer targetId : targets) {
                        String key = sourceId + "->" + targetId;
                        transitionMap.computeIfAbsent(key, k -> new ArrayList<>()).add(transition);
                    }
                }
            }
            
            // Report duplicates - multiple transitions between same states
            for (Map.Entry<String, List<Map<String, Object>>> entry : transitionMap.entrySet()) {
                if (entry.getValue().size() > 1) {
                    // Check if they have different conditions
                    Set<String> conditions = new HashSet<>();
                    boolean hasDifferentConditions = false;
                    
                    for (Map<String, Object> trans : entry.getValue()) {
                        if (trans.containsKey("condition")) {
                            String condition = trans.get("condition").toString();
                            if (!conditions.add(condition)) {
                                // Exact duplicate with same condition
                                result.addError(new ValidationError(
                                        "Duplicate transition",
                                        "Duplicate transitions found for " + entry.getKey() + " with same condition",
                                        ValidationSeverity.ERROR
                                ));
                            } else {
                                hasDifferentConditions = true;
                            }
                        }
                    }
                    
                    if (hasDifferentConditions) {
                        // Multiple transitions with different conditions
                        result.addError(new ValidationError(
                                "Redundant transitions",
                                "Multiple redundant transitions between same states: " + entry.getKey() + " with different conditions",
                                ValidationSeverity.WARNING
                        ));
                    } else if (entry.getValue().size() > 1 && conditions.isEmpty()) {
                        // Multiple unconditional transitions
                        result.addError(new ValidationError(
                                "Duplicate transition",
                                "Multiple unconditional transitions found for " + entry.getKey(),
                                ValidationSeverity.ERROR
                        ));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error detecting duplicate transitions", e);
        }
    }
    
    /**
     * Detects transitions with redundant conditions.
     */
    private void detectRedundantConditions(Map<String, Object> project, ValidationResult result) {
        if (!project.containsKey("stateTransitions")) {
            return;
        }
        
        try {
            List<Map<String, Object>> transitions = (List<Map<String, Object>>) project.get("stateTransitions");
            Map<String, List<String>> stateToConditions = new HashMap<>();
            
            for (Map<String, Object> transition : transitions) {
                Integer sourceId = getSourceStateId(transition);
                if (sourceId != null && transition.containsKey("condition")) {
                    String condition = transition.get("condition").toString();
                    String key = sourceId.toString();
                    
                    if (!stateToConditions.containsKey(key)) {
                        stateToConditions.put(key, new ArrayList<>());
                    }
                    
                    List<String> existingConditions = stateToConditions.get(key);
                    if (existingConditions.contains(condition)) {
                        result.addError(new ValidationError(
                                "Redundant condition",
                                "State " + sourceId + " has duplicate condition: " + condition,
                                ValidationSeverity.WARNING
                        ));
                    }
                    existingConditions.add(condition);
                }
            }
        } catch (Exception e) {
            logger.error("Error detecting redundant conditions", e);
        }
    }
    
    /**
     * Validates probability-based transitions.
     */
    private void validateProbabilities(Map<String, Object> project, ValidationResult result) {
        if (!project.containsKey("stateTransitions")) {
            return;
        }
        
        try {
            List<Map<String, Object>> transitions = (List<Map<String, Object>>) project.get("stateTransitions");
            Map<Integer, Double> stateProbabilities = new HashMap<>();
            
            for (Map<String, Object> transition : transitions) {
                Integer sourceId = getSourceStateId(transition);
                if (sourceId != null && transition.containsKey("probability")) {
                    double prob = ((Number) transition.get("probability")).doubleValue();
                    stateProbabilities.put(sourceId, 
                        stateProbabilities.getOrDefault(sourceId, 0.0) + prob);
                }
            }
            
            // Check if any state has probabilities that don't sum correctly
            for (Map.Entry<Integer, Double> entry : stateProbabilities.entrySet()) {
                double sum = entry.getValue();
                if (Math.abs(sum - 100.0) > 0.01 && Math.abs(sum - 1.0) > 0.01) {
                    result.addError(new ValidationError(
                            "Invalid probability sum",
                            "State " + entry.getKey() + " has probabilities summing to " + sum,
                            ValidationSeverity.WARNING
                    ));
                }
            }
        } catch (Exception e) {
            logger.error("Error validating probabilities", e);
        }
    }
    
    /**
     * Helper to extract source state ID from transition.
     */
    private Integer getSourceStateId(Map<String, Object> transition) {
        try {
            if (transition.containsKey("sourceStateId")) {
                Object value = transition.get("sourceStateId");
                if (value instanceof Integer) {
                    return (Integer) value;
                } else if (value instanceof Number) {
                    return ((Number) value).intValue();
                }
            }
            if (transition.containsKey("stateId")) {
                Object value = transition.get("stateId");
                if (value instanceof Integer) {
                    return (Integer) value;
                } else if (value instanceof Number) {
                    return ((Number) value).intValue();
                }
            }
            // Also check for legacy field names
            if (transition.containsKey("fromState")) {
                Object value = transition.get("fromState");
                if (value instanceof Integer) {
                    return (Integer) value;
                } else if (value instanceof Number) {
                    return ((Number) value).intValue();
                }
            }
        } catch (Exception e) {
            // Malformed data - return null
        }
        return null;
    }
    
    /**
     * Helper to extract target state IDs from transition.
     */
    private Set<Integer> getTargetStateIds(Map<String, Object> transition) {
        Set<Integer> targets = new HashSet<>();
        
        try {
            if (transition.containsKey("statesToEnter")) {
                Object value = transition.get("statesToEnter");
                if (value instanceof List) {
                    List<?> enterStates = (List<?>) value;
                    for (Object state : enterStates) {
                        if (state instanceof Integer) {
                            targets.add((Integer) state);
                        } else if (state instanceof Number) {
                            targets.add(((Number) state).intValue());
                        }
                    }
                }
            }
            
            if (transition.containsKey("activate")) {
                Object value = transition.get("activate");
                if (value instanceof List) {
                    List<?> activateStates = (List<?>) value;
                    for (Object state : activateStates) {
                        if (state instanceof Integer) {
                            targets.add((Integer) state);
                        } else if (state instanceof Number) {
                            targets.add(((Number) state).intValue());
                        }
                    }
                }
            }
            
            // Also check for legacy field names
            if (transition.containsKey("toState")) {
                Object value = transition.get("toState");
                if (value instanceof Integer) {
                    targets.add((Integer) value);
                } else if (value instanceof Number) {
                    targets.add(((Number) value).intValue());
                }
            }
        } catch (Exception e) {
            // Malformed data - return empty set
        }
        
        return targets;
    }
    
    /**
     * Tarjan's algorithm implementation for finding SCCs.
     */
    static class TarjanSCC {
        private Map<Integer, Set<Integer>> graph;
        private int index = 0;
        private Stack<Integer> stack = new Stack<>();
        private Map<Integer, Integer> indices = new HashMap<>();
        private Map<Integer, Integer> lowLinks = new HashMap<>();
        private Set<Integer> onStack = new HashSet<>();
        private List<Set<Integer>> sccs = new ArrayList<>();
        
        TarjanSCC(Map<Integer, Set<Integer>> graph) {
            this.graph = graph;
        }
        
        List<Set<Integer>> findSCCs() {
            for (Integer node : graph.keySet()) {
                if (!indices.containsKey(node)) {
                    strongConnect(node);
                }
            }
            return sccs;
        }
        
        private void strongConnect(Integer v) {
            indices.put(v, index);
            lowLinks.put(v, index);
            index++;
            stack.push(v);
            onStack.add(v);
            
            if (graph.containsKey(v)) {
                for (Integer w : graph.get(v)) {
                    if (!indices.containsKey(w)) {
                        strongConnect(w);
                        lowLinks.put(v, Math.min(lowLinks.get(v), lowLinks.get(w)));
                    } else if (onStack.contains(w)) {
                        lowLinks.put(v, Math.min(lowLinks.get(v), indices.get(w)));
                    }
                }
            }
            
            if (lowLinks.get(v).equals(indices.get(v))) {
                Set<Integer> scc = new HashSet<>();
                Integer w;
                do {
                    w = stack.pop();
                    onStack.remove(w);
                    scc.add(w);
                } while (!w.equals(v));
                sccs.add(scc);
            }
        }
    }
}