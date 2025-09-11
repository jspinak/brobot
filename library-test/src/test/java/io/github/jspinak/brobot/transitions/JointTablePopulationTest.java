package io.github.jspinak.brobot.transitions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
 * Test that verifies the fix for joint table population with state IDs. This test directly tests
 * the joint table behavior without requiring the full AnnotationProcessor dependencies.
 */
public class JointTablePopulationTest extends BrobotTestBase {

    @Mock private StateService stateService;

    @Mock private StateTransitionService transitionService;

    private StateTransitionsJointTable jointTable;
    private PathFinder pathFinder;

    private State promptState;
    private State workingState;
    private Long promptId = 100L;
    private Long workingId = 200L;

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
    public void testJointTableRequiresStateIdsNotNames() {
        System.out.println("=== TEST: Joint Table Requires State IDs ===");

        // Create a transition with only names (the bug scenario)
        JavaStateTransition transitionWithOnlyNames =
                new JavaStateTransition.Builder()
                        .addToActivate("Working") // This sets activateNames
                        .setFunction(() -> true)
                        .build();
        // Note: activate (IDs) field is still empty!

        StateTransitions promptTransitions = new StateTransitions();
        promptTransitions.setStateId(promptId);
        promptTransitions.setStateName("Prompt");
        promptTransitions.setTransitions(List.of(transitionWithOnlyNames));

        // Add to joint table
        jointTable.addToJointTable(promptTransitions);

        // Check if Working has any parents
        Set<Long> parentsOfWorking = jointTable.getStatesWithTransitionsTo(workingId);
        System.out.println("Parents of Working (with only names): " + parentsOfWorking);

        // This demonstrates the bug - no parents found because IDs are missing
        assertTrue(
                parentsOfWorking.isEmpty(),
                "Joint table should NOT find parents when transition only has names");

        // Path finding will also fail
        Set<Long> activeStates = Set.of(promptId);
        Paths paths = pathFinder.getPathsToState(activeStates, workingId);
        assertTrue(paths.isEmpty(), "Path finding should fail when joint table has no IDs");
    }

    @Test
    public void testJointTableWorksWithStateIds() {
        System.out.println("=== TEST: Joint Table Works With State IDs ===");

        // Create a transition with IDs (the fix)
        JavaStateTransition transitionWithIds =
                new JavaStateTransition.Builder()
                        .addToActivate("Working") // Sets activateNames
                        .setFunction(() -> true)
                        .build();

        // THE FIX: Manually set the IDs
        transitionWithIds.setActivate(Set.of(workingId));

        StateTransitions promptTransitions = new StateTransitions();
        promptTransitions.setStateId(promptId);
        promptTransitions.setStateName("Prompt");
        promptTransitions.setTransitions(List.of(transitionWithIds));

        // Add to joint table
        jointTable.addToJointTable(promptTransitions);

        // Check if Working has parents now
        Set<Long> parentsOfWorking = jointTable.getStatesWithTransitionsTo(workingId);
        System.out.println("Parents of Working (with IDs): " + parentsOfWorking);

        // This shows the fix works
        assertFalse(
                parentsOfWorking.isEmpty(),
                "Joint table SHOULD find parents when transition has IDs");
        assertTrue(parentsOfWorking.contains(promptId), "Prompt should be a parent of Working");

        // Setup transition service for path finding
        when(transitionService.getTransition(promptId, workingId))
                .thenReturn(Optional.of(transitionWithIds));

        // Path finding should work now
        Set<Long> activeStates = Set.of(promptId);
        Paths paths = pathFinder.getPathsToState(activeStates, workingId);
        assertFalse(paths.isEmpty(), "Path finding should succeed when joint table has IDs");

        System.out.println("✓ Fix verified: Joint table works with state IDs");
    }

    @Test
    public void testAnnotationProcessorFixPattern() {
        System.out.println("=== TEST: Annotation Processor Fix Pattern ===");

        // This simulates what the fixed AnnotationProcessor does

        // Step 1: Build transition with name
        JavaStateTransition transition =
                new JavaStateTransition.Builder()
                        .addToActivate("Working")
                        .setFunction(() -> true)
                        .build();

        // Step 2: THE FIX - Set the ID after building
        transition.setActivate(Set.of(workingId));

        // Now both name and ID are set
        assertEquals(Set.of("Working"), transition.getActivateNames(), "Should have state name");
        assertEquals(Set.of(workingId), transition.getActivate(), "Should have state ID");

        // Step 3: Add to transitions container
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.setStateId(promptId);
        stateTransitions.setStateName("Prompt");
        stateTransitions.setTransitions(List.of(transition));

        // Step 4: Add to joint table
        jointTable.addToJointTable(stateTransitions);

        // Verify it works
        Set<Long> parents = jointTable.getStatesWithTransitionsTo(workingId);
        assertTrue(parents.contains(promptId), "Joint table should have the transition");

        System.out.println("✓ Annotation processor fix pattern verified");
    }
}
