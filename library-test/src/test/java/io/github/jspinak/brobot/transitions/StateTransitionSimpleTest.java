package io.github.jspinak.brobot.transitions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.path.PathFinder;
import io.github.jspinak.brobot.navigation.path.Paths;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.navigation.transition.JavaStateTransition;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.navigation.transition.StateTransitionsJointTable;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Simple unit test to verify the joint table population issue. This test replicates the problem
 * seen in claude-automator where the path from Prompt to Working cannot be found.
 */
public class StateTransitionSimpleTest extends BrobotTestBase {

    @Mock private StateService stateService;

    @Mock private StateTransitionService transitionService;

    private StateTransitionsJointTable jointTable;
    private PathFinder pathFinder;

    private State promptState;
    private State workingState;
    private Long promptId = 1L;
    private Long workingId = 2L;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);

        // Create states
        promptState = new State();
        promptState.setName("Prompt");
        promptState.setId(promptId);

        workingState = new State();
        workingState.setName("Working");
        workingState.setId(workingId);

        // Setup state service mock
        when(stateService.getStateName(promptId)).thenReturn("Prompt");
        when(stateService.getStateName(workingId)).thenReturn("Working");
        when(stateService.getState(promptId)).thenReturn(Optional.of(promptState));
        when(stateService.getState(workingId)).thenReturn(Optional.of(workingState));

        // Create joint table and path finder
        jointTable = new StateTransitionsJointTable(stateService);
        pathFinder = new PathFinder(jointTable, stateService, transitionService);
    }

    @Test
    public void testJointTableNotPopulated_PathFindingFails() {
        System.out.println("=== TEST: Joint Table Not Populated - Path Finding Should Fail ===");

        // This simulates the actual problem - joint table is empty
        // No transitions have been added to the joint table

        Set<Long> activeStates = new HashSet<>();
        activeStates.add(promptId);

        // Try to find path from Prompt to Working
        Paths paths = pathFinder.getPathsToState(activeStates, workingId);

        System.out.println("Path finding result: " + (!paths.isEmpty() ? "FOUND" : "NOT FOUND"));

        // Check joint table state
        Set<Long> parentsOfWorking = jointTable.getStatesWithTransitionsTo(workingId);
        System.out.println("Parents of Working in joint table: " + parentsOfWorking);

        // THIS TEST SHOULD PASS - demonstrating the problem
        assertTrue(paths.isEmpty(), "Path should NOT be found when joint table is not populated");
        assertTrue(
                parentsOfWorking.isEmpty(),
                "Working should have NO parents when joint table is not populated");
    }

    @Test
    public void testJointTablePopulated_PathFindingSucceeds() {
        System.out.println("=== TEST: Joint Table Populated - Path Finding Should Succeed ===");

        // Create a transition from Prompt to Working
        JavaStateTransition transition = new JavaStateTransition();
        transition.setActivate(Set.of(workingId));
        transition.setPathCost(100);
        transition.setTransitionFunction(() -> true);

        // Create StateTransitions container for Prompt
        StateTransitions promptTransitions = new StateTransitions();
        promptTransitions.setStateId(promptId);
        promptTransitions.setStateName("Prompt");
        promptTransitions.setTransitions(List.of(transition));

        // CRITICAL: Add the transition to the joint table
        jointTable.addToJointTable(promptTransitions);

        // Setup transition service mock to return the transition
        when(transitionService.getTransition(promptId, workingId))
                .thenReturn(Optional.of(transition));

        Set<Long> activeStates = new HashSet<>();
        activeStates.add(promptId);

        // Try to find path from Prompt to Working
        Paths paths = pathFinder.getPathsToState(activeStates, workingId);

        System.out.println("Path finding result: " + (!paths.isEmpty() ? "FOUND" : "NOT FOUND"));

        // Check joint table state
        Set<Long> parentsOfWorking = jointTable.getStatesWithTransitionsTo(workingId);
        System.out.println("Parents of Working in joint table: " + parentsOfWorking);

        // THIS TEST SHOULD PASS - showing the fix
        assertFalse(paths.isEmpty(), "Path SHOULD be found when joint table is properly populated");
        assertTrue(
                parentsOfWorking.contains(promptId),
                "Working should have Prompt as parent when joint table is populated");
    }

    @Test
    public void testMissingJointTablePopulation_ReplicatesClaudeAutomatorIssue() {
        System.out.println("=== TEST: Replicating Claude-Automator Issue ===");
        System.out.println("This test demonstrates the exact issue seen in the application");

        // Create transition but DO NOT add it to joint table
        // This is what's happening in the actual application
        JavaStateTransition transition = new JavaStateTransition();
        transition.setActivate(Set.of(workingId));
        transition.setTransitionFunction(() -> true);

        // The transition exists in the service...
        StateTransitions promptTransitions = new StateTransitions();
        promptTransitions.setStateId(promptId);
        promptTransitions.setStateName("Prompt");
        promptTransitions.setTransitions(List.of(transition));

        when(transitionService.getTransitions(promptId)).thenReturn(Optional.of(promptTransitions));

        // BUT the joint table was never populated!
        // jointTable.addToJointTable(promptTransitions); // <-- MISSING!

        Set<Long> activeStates = new HashSet<>();
        activeStates.add(promptId);

        // Simulate the path finding that happens in the application
        System.out.println("Find path: , Prompt -> Working");
        System.out.println("Recursing for state: Working");
        System.out.println("Current path:");
        System.out.println("Start states: , Prompt");

        Set<Long> parentsOfWorking = jointTable.getStatesWithTransitionsTo(workingId);
        System.out.println(
                "Parent states for Working: "
                        + (parentsOfWorking.isEmpty() ? "" : parentsOfWorking));

        Paths paths = pathFinder.getPathsToState(activeStates, workingId);

        if (paths.isEmpty()) {
            System.out.println("Finished recursing for state: Working");
            System.out.println("?Path to state not found.");
            System.out.println("? All paths tried, open failed.");
        }

        // This demonstrates the bug
        assertTrue(paths.isEmpty(), "Path is not found because joint table wasn't populated");
        assertTrue(
                parentsOfWorking.isEmpty(),
                "Parent states for Working is empty - this is the bug!");
    }
}
