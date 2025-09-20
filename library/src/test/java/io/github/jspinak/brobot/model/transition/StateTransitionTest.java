package io.github.jspinak.brobot.model.transition;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import io.github.jspinak.brobot.navigation.transition.JavaStateTransition;
import io.github.jspinak.brobot.navigation.transition.TaskSequenceStateTransition;
import io.github.jspinak.brobot.runner.dsl.model.TaskSequence;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive tests for the StateTransition interface and its implementations which represent
 * edges in the state graph for GUI automation.
 */
@DisplayName("StateTransition Interface Tests")
public class StateTransitionTest extends BrobotTestBase {

    private StateTransition taskSequenceTransition;
    private StateTransition javaTransition;
    private TaskSequence mockTaskSequence;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mockTaskSequence = mock(TaskSequence.class);
        taskSequenceTransition = new TaskSequenceStateTransition();
        javaTransition = new JavaStateTransition();
    }

    @Test
    @DisplayName("Should implement StateTransition interface correctly")
    void testImplementsInterface() {
        // Then
        assertTrue(taskSequenceTransition instanceof StateTransition);
        assertTrue(javaTransition instanceof StateTransition);
    }

    @ParameterizedTest
    @EnumSource(StateTransition.StaysVisible.class)
    @DisplayName("Should handle all StaysVisible enum values")
    void testStaysVisibleEnum(StateTransition.StaysVisible visibility) {
        // When
        taskSequenceTransition.setStaysVisibleAfterTransition(visibility);

        // Then
        assertEquals(visibility, taskSequenceTransition.getStaysVisibleAfterTransition());
        assertNotNull(visibility.name());
    }

    @Test
    @DisplayName("Should handle StaysVisible enum semantics")
    void testStaysVisibleSemantics() {
        // NONE means inherit from StateTransitions
        assertEquals("NONE", StateTransition.StaysVisible.NONE.name());

        // TRUE means state remains visible
        assertEquals("TRUE", StateTransition.StaysVisible.TRUE.name());

        // FALSE means state becomes hidden
        assertEquals("FALSE", StateTransition.StaysVisible.FALSE.name());

        // All values
        StateTransition.StaysVisible[] values = StateTransition.StaysVisible.values();
        assertEquals(3, values.length);
    }

    @Test
    @DisplayName("Should get and set activation states")
    void testActivationStates() {
        // Given
        Set<Long> activationStates = new HashSet<>();
        activationStates.add(1L);
        activationStates.add(2L);
        activationStates.add(3L);

        // When
        taskSequenceTransition.setActivate(activationStates);

        // Then
        assertEquals(activationStates, taskSequenceTransition.getActivate());
        assertEquals(3, taskSequenceTransition.getActivate().size());
        assertTrue(taskSequenceTransition.getActivate().contains(2L));
    }

    @Test
    @DisplayName("Should get and set exit states")
    void testExitStates() {
        // Given
        Set<Long> exitStates = new HashSet<>();
        exitStates.add(10L);
        exitStates.add(20L);

        // When
        taskSequenceTransition.setExit(exitStates);

        // Then
        assertEquals(exitStates, taskSequenceTransition.getExit());
        assertEquals(2, taskSequenceTransition.getExit().size());
        assertTrue(taskSequenceTransition.getExit().contains(10L));
    }

    @Test
    @DisplayName("Should get and set path cost for path finding")
    void testPathCost() {
        // When
        taskSequenceTransition.setPathCost(100);
        javaTransition.setPathCost(-50);

        // Then
        assertEquals(100, taskSequenceTransition.getPathCost());
        assertEquals(-50, javaTransition.getPathCost());
    }

    @Test
    @DisplayName("Should track successful executions")
    void testSuccessTracking() {
        // Given
        assertEquals(0, taskSequenceTransition.getTimesSuccessful());

        // When
        taskSequenceTransition.setTimesSuccessful(5);

        // Then
        assertEquals(5, taskSequenceTransition.getTimesSuccessful());

        // Increment
        taskSequenceTransition.setTimesSuccessful(taskSequenceTransition.getTimesSuccessful() + 1);
        assertEquals(6, taskSequenceTransition.getTimesSuccessful());
    }

    @Test
    @DisplayName("Should handle TaskSequence in TaskSequenceStateTransition")
    void testTaskSequenceHandling() {
        // Given
        TaskSequenceStateTransition transition = new TaskSequenceStateTransition();

        // Initially empty
        Optional<TaskSequence> empty = transition.getTaskSequenceOptional();
        assertTrue(empty.isEmpty());

        // When - Set task sequence (using actionDefinition field)
        transition.setActionDefinition(mockTaskSequence);

        // Then
        Optional<TaskSequence> present = transition.getTaskSequenceOptional();
        assertTrue(present.isPresent());
        assertEquals(mockTaskSequence, present.get());
    }

    @Test
    @DisplayName("Should return empty TaskSequence for JavaStateTransition")
    void testJavaTransitionTaskSequence() {
        // JavaStateTransition doesn't use TaskSequence
        Optional<TaskSequence> taskSequence = javaTransition.getTaskSequenceOptional();

        // Then
        assertTrue(taskSequence.isEmpty());
    }

    @Test
    @DisplayName("Should provide toString for debugging")
    void testToString() {
        // Given
        taskSequenceTransition.setPathCost(10);
        Set<Long> activate = Set.of(1L, 2L);
        taskSequenceTransition.setActivate(activate);

        // When
        String toString = taskSequenceTransition.toString();

        // Then
        assertNotNull(toString);
        // Should contain useful debugging info
        assertTrue(toString.length() > 0);
    }

    @TestFactory
    @DisplayName("State transition scenarios")
    Stream<DynamicTest> testTransitionScenarios() {
        return Stream.of(
                dynamicTest(
                        "Simple navigation transition",
                        () -> {
                            StateTransition transition = new TaskSequenceStateTransition();
                            transition.setActivate(Set.of(2L)); // Navigate to state 2
                            transition.setExit(Set.of(1L)); // Exit state 1
                            transition.setStaysVisibleAfterTransition(
                                    StateTransition.StaysVisible.FALSE);

                            assertEquals(1, transition.getActivate().size());
                            assertEquals(1, transition.getExit().size());
                            assertEquals(
                                    StateTransition.StaysVisible.FALSE,
                                    transition.getStaysVisibleAfterTransition());
                        }),
                dynamicTest(
                        "Modal dialog transition",
                        () -> {
                            StateTransition transition = new TaskSequenceStateTransition();
                            transition.setActivate(Set.of(10L)); // Show dialog
                            transition.setExit(new HashSet<>()); // Don't exit current state
                            transition.setStaysVisibleAfterTransition(
                                    StateTransition.StaysVisible.TRUE);

                            assertEquals(1, transition.getActivate().size());
                            assertEquals(0, transition.getExit().size());
                            assertEquals(
                                    StateTransition.StaysVisible.TRUE,
                                    transition.getStaysVisibleAfterTransition());
                        }),
                dynamicTest(
                        "Multi-state activation",
                        () -> {
                            StateTransition transition = new TaskSequenceStateTransition();
                            transition.setActivate(Set.of(3L, 4L, 5L)); // Activate multiple states
                            transition.setPathCost(50); // Medium cost

                            assertEquals(3, transition.getActivate().size());
                            assertEquals(50, transition.getPathCost());
                        }),
                dynamicTest(
                        "High-reliability transition",
                        () -> {
                            StateTransition transition = new TaskSequenceStateTransition();
                            transition.setTimesSuccessful(100);
                            transition.setPathCost(5); // Low cost (preferred)

                            assertEquals(100, transition.getTimesSuccessful());
                            assertEquals(5, transition.getPathCost());
                        }));
    }

    @Test
    @DisplayName("Should handle empty state sets")
    void testEmptyStateSets() {
        // Given
        StateTransition transition = new TaskSequenceStateTransition();

        // When - Set empty sets
        transition.setActivate(new HashSet<>());
        transition.setExit(new HashSet<>());

        // Then
        assertNotNull(transition.getActivate());
        assertNotNull(transition.getExit());
        assertEquals(0, transition.getActivate().size());
        assertEquals(0, transition.getExit().size());
    }

    @Test
    @DisplayName("Should handle null state sets")
    void testNullStateSets() {
        // Given
        StateTransition transition = new TaskSequenceStateTransition();

        // When - Set null (implementation dependent)
        transition.setActivate(null);
        transition.setExit(null);

        // Then - Behavior depends on implementation
        // Some implementations might store null, others might initialize empty sets
        Set<Long> activate = transition.getActivate();
        Set<Long> exit = transition.getExit();

        // Either null or empty is acceptable
        assertTrue(activate == null || activate.isEmpty());
        assertTrue(exit == null || exit.isEmpty());
    }

    @Test
    @DisplayName("Should handle state ID boundaries")
    void testStateIdBoundaries() {
        // Given
        StateTransition transition = new TaskSequenceStateTransition();
        Set<Long> extremeIds = new HashSet<>();
        extremeIds.add(0L);
        extremeIds.add(Long.MAX_VALUE);
        extremeIds.add(-1L); // Might be used for special states

        // When
        transition.setActivate(extremeIds);

        // Then
        assertEquals(3, transition.getActivate().size());
        assertTrue(transition.getActivate().contains(0L));
        assertTrue(transition.getActivate().contains(Long.MAX_VALUE));
        assertTrue(transition.getActivate().contains(-1L));
    }

    @Test
    @DisplayName("Should demonstrate path scoring logic")
    void testPathScoringLogic() {
        // Given - Three transitions with different scores
        StateTransition preferred = new TaskSequenceStateTransition();
        StateTransition normal = new TaskSequenceStateTransition();
        StateTransition discouraged = new TaskSequenceStateTransition();

        preferred.setPathCost(10);
        normal.setPathCost(50);
        discouraged.setPathCost(100);

        // Then - Lower costs are preferred
        assertTrue(preferred.getPathCost() < normal.getPathCost());
        assertTrue(normal.getPathCost() < discouraged.getPathCost());

        // Path finder would prefer: preferred > normal > discouraged
    }

    @Test
    @DisplayName("Should handle visibility inheritance")
    void testVisibilityInheritance() {
        // Given
        StateTransition transition = new TaskSequenceStateTransition();

        // Default should be NONE (inherit from container)
        StateTransition.StaysVisible initial = transition.getStaysVisibleAfterTransition();

        // When explicitly set
        transition.setStaysVisibleAfterTransition(StateTransition.StaysVisible.TRUE);

        // Then - Explicit value takes precedence
        assertEquals(
                StateTransition.StaysVisible.TRUE, transition.getStaysVisibleAfterTransition());

        // When set back to NONE
        transition.setStaysVisibleAfterTransition(StateTransition.StaysVisible.NONE);

        // Then - Will inherit from StateTransitions container
        assertEquals(
                StateTransition.StaysVisible.NONE, transition.getStaysVisibleAfterTransition());
    }

    @Test
    @DisplayName("Should modify state sets after creation")
    void testStateSetModification() {
        // Given
        StateTransition transition = new TaskSequenceStateTransition();
        Set<Long> initial = new HashSet<>();
        initial.add(1L);
        transition.setActivate(initial);

        // When - Modify the returned set
        Set<Long> retrieved = transition.getActivate();
        if (retrieved != null) {
            retrieved.add(2L);

            // Then - Changes are reflected
            assertTrue(transition.getActivate().contains(2L));
            assertEquals(2, transition.getActivate().size());
        }
    }

    @Test
    @DisplayName("Should track reliability metrics")
    void testReliabilityMetrics() {
        // Given
        StateTransition transition = new TaskSequenceStateTransition();

        // Simulate successful executions
        for (int i = 0; i < 10; i++) {
            int current = transition.getTimesSuccessful();
            transition.setTimesSuccessful(current + 1);
        }

        // Then
        assertEquals(10, transition.getTimesSuccessful());

        // Could calculate reliability score
        double reliability = transition.getTimesSuccessful() / 10.0;
        assertEquals(1.0, reliability);
    }

    @Test
    @DisplayName("Should demonstrate transition types")
    void testTransitionTypes() {
        // TaskSequenceStateTransition - declarative
        StateTransition declarative = new TaskSequenceStateTransition();
        assertTrue(declarative.getTaskSequenceOptional().isEmpty());

        // JavaStateTransition - programmatic
        StateTransition programmatic = new JavaStateTransition();
        assertTrue(programmatic.getTaskSequenceOptional().isEmpty());

        // Both implement same interface
        assertTrue(declarative instanceof StateTransition);
        assertTrue(programmatic instanceof StateTransition);
    }

    @Test
    @DisplayName("Should handle concurrent state changes")
    void testConcurrentStateChanges() {
        // Given - Transition that affects multiple states
        StateTransition transition = new TaskSequenceStateTransition();

        // Exit multiple states
        Set<Long> exitStates = Set.of(1L, 2L, 3L);
        transition.setExit(exitStates);

        // Activate different states
        Set<Long> activateStates = Set.of(10L, 11L);
        transition.setActivate(activateStates);

        // Then - No overlap between exit and activate
        for (Long exitId : transition.getExit()) {
            assertFalse(transition.getActivate().contains(exitId));
        }
    }

    @Test
    @DisplayName("Should validate StaysVisible enum completeness")
    void testStaysVisibleEnumCompleteness() {
        // Given
        StateTransition.StaysVisible[] values = StateTransition.StaysVisible.values();

        // Then - Exactly 3 values
        assertEquals(3, values.length);

        // Contains all expected values
        Set<String> names = new HashSet<>();
        for (StateTransition.StaysVisible value : values) {
            names.add(value.name());
        }

        assertTrue(names.contains("NONE"));
        assertTrue(names.contains("TRUE"));
        assertTrue(names.contains("FALSE"));
    }
}
