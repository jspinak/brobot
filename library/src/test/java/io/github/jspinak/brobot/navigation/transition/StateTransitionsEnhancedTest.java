package io.github.jspinak.brobot.navigation.transition;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.runner.dsl.model.TaskSequence;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Enhanced test suite for StateTransitions to improve coverage. Tests builder patterns, transition
 * management, and utility methods.
 */
@DisplayName("StateTransitions Enhanced Tests")
class StateTransitionsEnhancedTest extends BrobotTestBase {

    @Mock private State mockState;

    @Mock private TaskSequence mockTaskSequence;

    @Mock private BooleanSupplier mockSupplier;

    private StateTransitions stateTransitions;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);

        stateTransitions = new StateTransitions();
        stateTransitions.setStateName("TestState");
        stateTransitions.setStateId(1L);
    }

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderPatternTests {

        @Test
        @DisplayName("Should build StateTransitions with all configurations")
        void testCompleteBuilder() {
            // Arrange
            BooleanSupplier finishSupplier = () -> true;
            BooleanSupplier transition1 = () -> true;
            BooleanSupplier transition2 = () -> false;

            // Act
            StateTransitions built =
                    new StateTransitions.Builder("MainState")
                            .addTransitionFinish(finishSupplier)
                            .addTransition(transition1, "State2", "State3")
                            .addTransition(transition2, "State4")
                            .setStaysVisibleAfterTransition(true)
                            .build();

            // Assert
            assertNotNull(built);
            assertEquals("MainState", built.getStateName());
            assertNotNull(built.getTransitionFinish());
            assertEquals(2, built.getTransitions().size());
            assertTrue(built.isStaysVisibleAfterTransition());
        }

        @Test
        @DisplayName("Should build with JavaStateTransition")
        void testBuilderWithJavaStateTransition() {
            // Arrange
            JavaStateTransition javaTransition = new JavaStateTransition();
            javaTransition.setTransitionFunction(() -> true);
            javaTransition.setActivateNames(Set.of("Target1", "Target2"));

            // Act
            StateTransitions built =
                    new StateTransitions.Builder("TestState").addTransition(javaTransition).build();

            // Assert
            assertEquals(1, built.getTransitions().size());
            assertEquals(javaTransition, built.getTransitions().get(0));
        }

        @Test
        @DisplayName("Should build with custom transition finish")
        void testBuilderWithCustomTransitionFinish() {
            // Arrange
            JavaStateTransition customFinish = new JavaStateTransition();
            customFinish.setTransitionFunction(() -> true);

            // Act
            StateTransitions built =
                    new StateTransitions.Builder("TestState")
                            .addTransitionFinish(customFinish)
                            .build();

            // Assert
            assertEquals(customFinish, built.getTransitionFinish());
        }

        @Test
        @DisplayName("Should build with default values when not specified")
        void testBuilderDefaults() {
            // Act
            StateTransitions built = new StateTransitions.Builder("MinimalState").build();

            // Assert
            assertEquals("MinimalState", built.getStateName());
            assertNotNull(built.getTransitionFinish());
            assertTrue(built.getTransitions().isEmpty());
            assertFalse(built.isStaysVisibleAfterTransition());
        }
    }

    @Nested
    @DisplayName("Transition Addition Methods")
    class TransitionAdditionMethods {

        @Test
        @DisplayName("Should add JavaStateTransition")
        void testAddJavaStateTransition() {
            // Arrange
            JavaStateTransition transition = new JavaStateTransition();
            transition.setActivate(Set.of(2L, 3L));
            transition.setTransitionFunction(mockSupplier);

            // Act
            stateTransitions.addTransition(transition);

            // Assert
            assertEquals(1, stateTransitions.getTransitions().size());
            assertTrue(stateTransitions.getTransitions().contains(transition));
        }

        @Test
        @DisplayName("Should add TaskSequenceStateTransition with mapping")
        void testAddTaskSequenceStateTransition() {
            // Arrange
            TaskSequenceStateTransition transition = new TaskSequenceStateTransition();
            transition.setActivate(Set.of(4L, 5L, 6L));
            transition.setActionDefinition(mockTaskSequence);

            // Act
            stateTransitions.addTransition(transition);

            // Assert
            assertEquals(
                    3, stateTransitions.getTransitions().size()); // One for each activated state
            assertEquals(3, stateTransitions.getActionDefinitionTransitions().size());
            assertTrue(stateTransitions.getActionDefinitionTransitions().containsKey(4L));
            assertTrue(stateTransitions.getActionDefinitionTransitions().containsKey(5L));
            assertTrue(stateTransitions.getActionDefinitionTransitions().containsKey(6L));
        }

        @Test
        @DisplayName("Should add simple BooleanSupplier transition")
        void testAddBooleanSupplierTransition() {
            // Arrange
            BooleanSupplier supplier = () -> true;

            // Act
            stateTransitions.addTransition(supplier, "Target1", "Target2");

            // Assert
            assertEquals(1, stateTransitions.getTransitions().size());
            StateTransition added = stateTransitions.getTransitions().get(0);
            assertTrue(added instanceof JavaStateTransition);
            JavaStateTransition javaTransition = (JavaStateTransition) added;
            assertEquals(supplier, javaTransition.getTransitionFunction());
        }

        @Test
        @DisplayName("Should handle multiple transitions to same target")
        void testMultipleTransitionsToSameTarget() {
            // Arrange
            TaskSequenceStateTransition transition1 = new TaskSequenceStateTransition();
            transition1.setActivate(Set.of(2L));

            TaskSequenceStateTransition transition2 = new TaskSequenceStateTransition();
            transition2.setActivate(Set.of(2L)); // Same target

            // Act
            stateTransitions.addTransition(transition1);
            stateTransitions.addTransition(transition2);

            // Assert
            assertEquals(2, stateTransitions.getTransitions().size());
            // Last one wins in the map
            assertEquals(transition2, stateTransitions.getActionDefinitionTransitions().get(2L));
        }
    }

    @Nested
    @DisplayName("ActionDefinition Retrieval")
    class ActionDefinitionRetrieval {

        @Test
        @DisplayName("Should get ActionDefinition for target state")
        void testGetActionDefinition() {
            // Arrange
            TaskSequenceStateTransition transition = new TaskSequenceStateTransition();
            transition.setActivate(Set.of(3L));
            transition.setActionDefinition(mockTaskSequence);
            stateTransitions.addTransition(transition);

            // Act
            Optional<TaskSequence> result = stateTransitions.getActionDefinition(3L);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(mockTaskSequence, result.get());
        }

        @Test
        @DisplayName("Should return empty for non-TaskSequence transition")
        void testGetActionDefinitionForNonTaskSequence() {
            // Arrange
            JavaStateTransition transition = new JavaStateTransition();
            transition.setActivate(Set.of(3L));
            transition.setTransitionFunction(() -> true);
            stateTransitions.addTransition(transition);

            // Act
            Optional<TaskSequence> result = stateTransitions.getActionDefinition(3L);

            // Assert
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should return empty for non-existent target")
        void testGetActionDefinitionForNonExistentTarget() {
            // Act
            Optional<TaskSequence> result = stateTransitions.getActionDefinition(99L);

            // Assert
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should handle transition finish request")
        void testGetActionDefinitionForTransitionFinish() {
            // Arrange
            TaskSequenceStateTransition finishTransition = new TaskSequenceStateTransition();
            finishTransition.setActionDefinition(mockTaskSequence);
            stateTransitions.setTransitionFinish(finishTransition);

            // Act
            Optional<TaskSequence> result =
                    stateTransitions.getActionDefinition(1L); // Same as state ID

            // Assert
            assertTrue(result.isPresent());
            assertEquals(mockTaskSequence, result.get());
        }
    }

    @Nested
    @DisplayName("ToString Method")
    class ToStringMethod {

        @Test
        @DisplayName("Should format transitions with TaskSequence")
        void testToStringWithTaskSequenceTransitions() {
            // Arrange
            TaskSequenceStateTransition transition = new TaskSequenceStateTransition();
            transition.setActivate(Set.of(2L, 3L));
            stateTransitions.addTransition(transition);

            // Act
            String result = stateTransitions.toString();

            // Assert
            assertNotNull(result);
            assertTrue(result.contains("id=1"));
            assertTrue(result.contains("from=TestState"));
            assertTrue(result.contains("to="));
            assertTrue(result.contains("[2, 3]") || result.contains("[3, 2]"));
        }

        @Test
        @DisplayName("Should format transitions with JavaStateTransition")
        void testToStringWithJavaTransitions() {
            // Arrange
            JavaStateTransition transition = new JavaStateTransition();
            transition.setActivateNames(Set.of("State2", "State3"));
            transition.setActivate(Set.of(2L, 3L));
            stateTransitions.addTransition(transition);

            // Act
            String result = stateTransitions.toString();

            // Assert
            assertNotNull(result);
            assertTrue(result.contains("from=TestState"));
            assertTrue(result.contains("State2") || result.contains("State3"));
        }

        @Test
        @DisplayName("Should handle empty transitions")
        void testToStringWithEmptyTransitions() {
            // Act
            String result = stateTransitions.toString();

            // Assert
            assertNotNull(result);
            assertTrue(result.contains("id=1"));
            assertTrue(result.contains("from=TestState"));
            assertTrue(result.contains("to="));
        }

        @Test
        @DisplayName("Should handle null state name and ID")
        void testToStringWithNulls() {
            // Arrange
            StateTransitions nullTransitions = new StateTransitions();

            // Act
            String result = nullTransitions.toString();

            // Assert
            assertNotNull(result);
            assertTrue(result.contains("to="));
        }
    }

    @Nested
    @DisplayName("JavaStateTransition Builder Tests")
    class JavaStateTransitionBuilderTests {

        @Test
        @DisplayName("Should build JavaStateTransition with all options")
        void testJavaTransitionCompleteBuilder() {
            // Act
            JavaStateTransition transition =
                    new JavaStateTransition.Builder()
                            .setFunction(() -> true)
                            .addToActivate("State1", "State2", "State3")
                            .addToExit("State4", "State5")
                            .setScore(100)
                            .setStaysVisibleAfterTransition(StateTransition.StaysVisible.TRUE)
                            .build();
            transition.setTimesSuccessful(5);

            // Assert
            assertNotNull(transition);
            assertNotNull(transition.getTransitionFunction());
            assertEquals(3, transition.getActivateNames().size());
            assertEquals(2, transition.getExitNames().size());
            assertEquals(100, transition.getScore());
            assertEquals(5, transition.getTimesSuccessful());
            assertEquals(
                    StateTransition.StaysVisible.TRUE, transition.getStaysVisibleAfterTransition());
        }

        @Test
        @DisplayName("Should build with minimal configuration")
        void testJavaTransitionMinimalBuilder() {
            // Act
            JavaStateTransition transition =
                    new JavaStateTransition.Builder().setFunction(() -> false).build();

            // Assert
            assertNotNull(transition);
            assertNotNull(transition.getTransitionFunction());
            assertTrue(transition.getActivateNames().isEmpty());
            assertTrue(transition.getExitNames().isEmpty());
            assertEquals(0, transition.getScore());
            assertEquals(0, transition.getTimesSuccessful());
            assertEquals(
                    StateTransition.StaysVisible.NONE, transition.getStaysVisibleAfterTransition());
        }

        @Test
        @DisplayName("Should add individual states to activate")
        void testJavaTransitionAddIndividualActivate() {
            // Act
            JavaStateTransition transition =
                    new JavaStateTransition.Builder()
                            .setFunction(() -> true)
                            .addToActivate("State1")
                            .addToActivate("State2")
                            .build();

            // Assert
            assertEquals(2, transition.getActivateNames().size());
            assertTrue(transition.getActivateNames().contains("State1"));
            assertTrue(transition.getActivateNames().contains("State2"));
        }
    }

    @Nested
    @DisplayName("TaskSequenceStateTransition Tests")
    class TaskSequenceStateTransitionTests {

        @Test
        @DisplayName("Should get TaskSequence from transition")
        void testGetTaskSequence() {
            // Arrange
            TaskSequenceStateTransition transition = new TaskSequenceStateTransition();
            transition.setActionDefinition(mockTaskSequence);

            // Act
            Optional<TaskSequence> result = transition.getTaskSequenceOptional();

            // Assert
            assertTrue(result.isPresent());
            assertEquals(mockTaskSequence, result.get());
        }

        @Test
        @DisplayName("Should return empty when TaskSequence is null")
        void testGetTaskSequenceWhenNull() {
            // Arrange
            TaskSequenceStateTransition transition = new TaskSequenceStateTransition();

            // Act
            Optional<TaskSequence> result = transition.getTaskSequenceOptional();

            // Assert
            assertFalse(result.isPresent());
        }
    }
}
