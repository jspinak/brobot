package io.github.jspinak.brobot.navigation.transition;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.runner.dsl.model.ActionStep;
import io.github.jspinak.brobot.runner.dsl.model.TaskSequence;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite for TransitionConditionPackager. Tests conversion of various transition
 * types into executable BooleanSupplier functions.
 */
@DisplayName("TransitionConditionPackager Tests")
class TransitionConditionPackagerTest extends BrobotTestBase {

    private TransitionConditionPackager packager;

    @Mock private Action mockAction;

    @Mock private StateTransition mockStateTransition;

    @Mock private JavaStateTransition mockJavaTransition;

    @Mock private TaskSequenceStateTransition mockTaskSequenceTransition;

    @Mock private TaskSequence mockTaskSequence;

    @Mock private ActionResult mockActionResult;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        packager = new TransitionConditionPackager(mockAction);
    }

    @Nested
    @DisplayName("Java State Transition Handling")
    class JavaStateTransitionHandling {

        @Test
        @DisplayName("Should extract BooleanSupplier from JavaStateTransition")
        void testJavaTransitionExtraction() {
            // Arrange
            BooleanSupplier expectedSupplier = () -> true;
            when(mockJavaTransition.getTransitionFunction()).thenReturn(expectedSupplier);

            // Act
            BooleanSupplier result = packager.toBooleanSupplier(mockJavaTransition);

            // Assert
            assertSame(expectedSupplier, result);
            assertTrue(result.getAsBoolean());
        }

        @Test
        @DisplayName("Should handle JavaStateTransition with false result")
        void testJavaTransitionFalseResult() {
            // Arrange
            BooleanSupplier falseSupplier = () -> false;
            when(mockJavaTransition.getTransitionFunction()).thenReturn(falseSupplier);

            // Act
            BooleanSupplier result = packager.toBooleanSupplier(mockJavaTransition);

            // Assert
            assertFalse(result.getAsBoolean());
        }

        @Test
        @DisplayName("Should handle JavaStateTransition with complex logic")
        void testJavaTransitionComplexLogic() {
            // Arrange
            final int[] counter = {0};
            BooleanSupplier complexSupplier =
                    () -> {
                        counter[0]++;
                        return counter[0] > 2;
                    };
            when(mockJavaTransition.getTransitionFunction()).thenReturn(complexSupplier);

            // Act
            BooleanSupplier result = packager.toBooleanSupplier(mockJavaTransition);

            // Assert
            assertFalse(result.getAsBoolean()); // First call, counter = 1
            assertFalse(result.getAsBoolean()); // Second call, counter = 2
            assertTrue(result.getAsBoolean()); // Third call, counter = 3
        }
    }

    @Nested
    @DisplayName("Task Sequence Transition Handling")
    class TaskSequenceTransitionHandling {

        @Test
        @DisplayName("Should convert TaskSequence with single step")
        void testSingleStepTaskSequence() {
            // Arrange
            ActionStep step = createActionStep();
            List<ActionStep> steps = List.of(step);

            TaskSequence taskSequence = new TaskSequence();
            taskSequence.getSteps().addAll(steps);

            when(mockStateTransition.getTaskSequenceOptional())
                    .thenReturn(Optional.of(taskSequence));
            when(mockActionResult.isSuccess()).thenReturn(true);
            when(mockAction.perform(any(ActionConfig.class), any(ObjectCollection.class)))
                    .thenReturn(mockActionResult);

            // Act
            BooleanSupplier result = packager.toBooleanSupplier(mockStateTransition);
            boolean success = result.getAsBoolean();

            // Assert
            assertTrue(success);
            verify(mockAction, times(1))
                    .perform(any(ActionConfig.class), any(ObjectCollection.class));
        }

        @Test
        @DisplayName("Should execute all steps but only evaluate last")
        void testMultiStepTaskSequence() {
            // Arrange
            ActionStep step1 = createActionStep();
            ActionStep step2 = createActionStep();
            ActionStep step3 = createActionStep();
            List<ActionStep> steps = List.of(step1, step2, step3);

            TaskSequence taskSequence = new TaskSequence();
            taskSequence.getSteps().addAll(steps);

            when(mockStateTransition.getTaskSequenceOptional())
                    .thenReturn(Optional.of(taskSequence));

            // First two steps fail, last succeeds
            ActionResult failResult = mock(ActionResult.class);
            when(failResult.isSuccess()).thenReturn(false);
            ActionResult successResult = mock(ActionResult.class);
            when(successResult.isSuccess()).thenReturn(true);

            when(mockAction.perform(any(ActionConfig.class), any(ObjectCollection.class)))
                    .thenReturn(failResult) // Step 1 fails
                    .thenReturn(failResult) // Step 2 fails
                    .thenReturn(successResult); // Step 3 succeeds

            // Act
            BooleanSupplier result = packager.toBooleanSupplier(mockStateTransition);
            boolean success = result.getAsBoolean();

            // Assert
            assertTrue(success); // Only last step matters
            verify(mockAction, times(3))
                    .perform(any(ActionConfig.class), any(ObjectCollection.class));
        }

        @Test
        @DisplayName("Should return false for empty TaskSequence")
        void testEmptyTaskSequence() {
            // Arrange
            TaskSequence emptySequence = new TaskSequence();

            when(mockStateTransition.getTaskSequenceOptional())
                    .thenReturn(Optional.of(emptySequence));

            // Act
            BooleanSupplier result = packager.toBooleanSupplier(mockStateTransition);
            boolean success = result.getAsBoolean();

            // Assert
            assertFalse(success);
            verify(mockAction, never())
                    .perform(any(ActionConfig.class), any(ObjectCollection.class));
        }

        @Test
        @DisplayName("Should handle TaskSequence with failing last step")
        void testTaskSequenceFailingLastStep() {
            // Arrange
            ActionStep step1 = createActionStep();
            ActionStep step2 = createActionStep();
            List<ActionStep> steps = List.of(step1, step2);

            TaskSequence taskSequence = new TaskSequence();
            taskSequence.getSteps().addAll(steps);

            when(mockStateTransition.getTaskSequenceOptional())
                    .thenReturn(Optional.of(taskSequence));

            ActionResult successResult = mock(ActionResult.class);
            when(successResult.isSuccess()).thenReturn(true);
            ActionResult failResult = mock(ActionResult.class);
            when(failResult.isSuccess()).thenReturn(false);

            when(mockAction.perform(any(ActionConfig.class), any(ObjectCollection.class)))
                    .thenReturn(successResult) // Step 1 succeeds
                    .thenReturn(failResult); // Step 2 (last) fails

            // Act
            BooleanSupplier result = packager.toBooleanSupplier(mockStateTransition);
            boolean success = result.getAsBoolean();

            // Assert
            assertFalse(success);
            verify(mockAction, times(2))
                    .perform(any(ActionConfig.class), any(ObjectCollection.class));
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should throw exception for unsupported transition type")
        void testUnsupportedTransitionType() {
            // Arrange
            when(mockStateTransition.getTaskSequenceOptional()).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> packager.toBooleanSupplier(mockStateTransition),
                    "Should throw for unsupported transition type");
        }

        @Test
        @DisplayName("Should handle null transition")
        void testNullTransition() {
            // Act & Assert
            assertThrows(
                    NullPointerException.class,
                    () -> packager.toBooleanSupplier((StateTransition) null));
        }

        @Test
        @DisplayName("Should handle TaskSequence with null steps")
        void testTaskSequenceNullSteps() {
            // Arrange
            TaskSequence taskSequence = mock(TaskSequence.class);
            when(taskSequence.getSteps()).thenReturn(null);

            when(mockStateTransition.getTaskSequenceOptional())
                    .thenReturn(Optional.of(taskSequence));

            // Act
            BooleanSupplier result = packager.toBooleanSupplier(mockStateTransition);

            // Assert
            assertThrows(NullPointerException.class, result::getAsBoolean);
        }
    }

    @Nested
    @DisplayName("Convenience Methods")
    class ConvenienceMethods {

        @Test
        @DisplayName("Should execute transition directly with getAsBoolean")
        void testGetAsBooleanSuccess() {
            // Arrange
            BooleanSupplier supplier = () -> true;
            when(mockJavaTransition.getTransitionFunction()).thenReturn(supplier);

            // Act
            boolean result = packager.getAsBoolean(mockJavaTransition);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("Should handle failure in getAsBoolean")
        void testGetAsBooleanFailure() {
            // Arrange
            BooleanSupplier supplier = () -> false;
            when(mockJavaTransition.getTransitionFunction()).thenReturn(supplier);

            // Act
            boolean result = packager.getAsBoolean(mockJavaTransition);

            // Assert
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Complex Scenarios")
    class ComplexScenarios {

        @Test
        @DisplayName("Should handle TaskSequence with many steps")
        void testManyStepsTaskSequence() {
            // Arrange
            TaskSequence taskSequence = new TaskSequence();
            int stepCount = 10;

            for (int i = 0; i < stepCount; i++) {
                taskSequence.getSteps().add(createActionStep());
            }

            when(mockStateTransition.getTaskSequenceOptional())
                    .thenReturn(Optional.of(taskSequence));
            when(mockActionResult.isSuccess()).thenReturn(true);
            when(mockAction.perform(any(ActionConfig.class), any(ObjectCollection.class)))
                    .thenReturn(mockActionResult);

            // Act
            BooleanSupplier result = packager.toBooleanSupplier(mockStateTransition);
            boolean success = result.getAsBoolean();

            // Assert
            assertTrue(success);
            verify(mockAction, times(stepCount))
                    .perform(any(ActionConfig.class), any(ObjectCollection.class));
        }

        @Test
        @DisplayName("Should handle repeated execution of same supplier")
        void testRepeatedExecution() {
            // Arrange
            ActionStep step = createActionStep();
            TaskSequence taskSequence = new TaskSequence();
            taskSequence.getSteps().add(step);

            when(mockStateTransition.getTaskSequenceOptional())
                    .thenReturn(Optional.of(taskSequence));

            ActionResult result1 = mock(ActionResult.class);
            ActionResult result2 = mock(ActionResult.class);
            ActionResult result3 = mock(ActionResult.class);

            when(result1.isSuccess()).thenReturn(true);
            when(result2.isSuccess()).thenReturn(false);
            when(result3.isSuccess()).thenReturn(true);

            when(mockAction.perform(any(ActionConfig.class), any(ObjectCollection.class)))
                    .thenReturn(result1, result2, result3);

            // Act
            BooleanSupplier supplier = packager.toBooleanSupplier(mockStateTransition);

            // Assert
            assertTrue(supplier.getAsBoolean()); // First execution
            assertFalse(supplier.getAsBoolean()); // Second execution
            assertTrue(supplier.getAsBoolean()); // Third execution

            verify(mockAction, times(3))
                    .perform(any(ActionConfig.class), any(ObjectCollection.class));
        }
    }

    private ActionStep createActionStep() {
        ActionStep step = new ActionStep();
        step.setActionConfig(mock(ActionConfig.class));
        step.setObjectCollection(mock(ObjectCollection.class));
        return step;
    }
}
