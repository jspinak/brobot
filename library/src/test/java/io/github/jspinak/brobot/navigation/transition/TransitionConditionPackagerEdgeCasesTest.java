package io.github.jspinak.brobot.navigation.transition;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;
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
 * Edge case and boundary tests for TransitionConditionPackager. Supplements the main test suite
 * with additional scenarios.
 */
@DisplayName("TransitionConditionPackager Edge Cases")
class TransitionConditionPackagerEdgeCasesTest extends BrobotTestBase {

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
    @DisplayName("Exception Throwing Scenarios")
    class ExceptionScenarios {

        @Test
        @DisplayName("Should handle JavaStateTransition with exception-throwing function")
        void testJavaTransitionWithException() {
            BooleanSupplier throwingSupplier =
                    () -> {
                        throw new RuntimeException("Test exception");
                    };
            when(mockJavaTransition.getTransitionFunction()).thenReturn(throwingSupplier);

            BooleanSupplier result = packager.toBooleanSupplier(mockJavaTransition);

            assertThrows(RuntimeException.class, result::getAsBoolean);
        }

        @Test
        @DisplayName("Should propagate exception from Action.perform")
        void testActionPerformException() {
            ActionStep step = createActionStep();
            TaskSequence taskSequence = new TaskSequence();
            taskSequence.getSteps().add(step);

            when(mockStateTransition.getTaskSequenceOptional())
                    .thenReturn(Optional.of(taskSequence));
            when(mockAction.perform(any(ActionConfig.class), any(ObjectCollection.class)))
                    .thenThrow(new RuntimeException("Action failed"));

            BooleanSupplier result = packager.toBooleanSupplier(mockStateTransition);

            assertThrows(RuntimeException.class, result::getAsBoolean);
        }

        @Test
        @DisplayName("Should handle null BooleanSupplier from JavaStateTransition")
        void testNullBooleanSupplier() {
            when(mockJavaTransition.getTransitionFunction()).thenReturn(null);

            BooleanSupplier result = packager.toBooleanSupplier(mockJavaTransition);

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("TaskSequence Boundary Conditions")
    class TaskSequenceBoundaries {

        @Test
        @DisplayName("Should handle TaskSequence with single failing step")
        void testSingleFailingStep() {
            ActionStep step = createActionStep();
            TaskSequence taskSequence = new TaskSequence();
            taskSequence.getSteps().add(step);

            when(mockStateTransition.getTaskSequenceOptional())
                    .thenReturn(Optional.of(taskSequence));
            when(mockActionResult.isSuccess()).thenReturn(false);
            when(mockAction.perform(any(ActionConfig.class), any(ObjectCollection.class)))
                    .thenReturn(mockActionResult);

            BooleanSupplier result = packager.toBooleanSupplier(mockStateTransition);

            assertFalse(result.getAsBoolean());
            verify(mockAction, times(1))
                    .perform(any(ActionConfig.class), any(ObjectCollection.class));
        }

        @Test
        @DisplayName("Should handle TaskSequence with 100 steps")
        void testLargeTaskSequence() {
            TaskSequence taskSequence = new TaskSequence();
            for (int i = 0; i < 100; i++) {
                taskSequence.getSteps().add(createActionStep());
            }

            when(mockStateTransition.getTaskSequenceOptional())
                    .thenReturn(Optional.of(taskSequence));
            when(mockActionResult.isSuccess()).thenReturn(true);
            when(mockAction.perform(any(ActionConfig.class), any(ObjectCollection.class)))
                    .thenReturn(mockActionResult);

            BooleanSupplier result = packager.toBooleanSupplier(mockStateTransition);
            boolean success = result.getAsBoolean();

            assertTrue(success);
            verify(mockAction, times(100))
                    .perform(any(ActionConfig.class), any(ObjectCollection.class));
        }

        @Test
        @DisplayName("Should handle TaskSequence with null ActionConfig in step")
        void testNullActionConfig() {
            ActionStep step = new ActionStep();
            step.setActionConfig(null);
            step.setObjectCollection(mock(ObjectCollection.class));

            TaskSequence taskSequence = new TaskSequence();
            taskSequence.getSteps().add(step);

            when(mockStateTransition.getTaskSequenceOptional())
                    .thenReturn(Optional.of(taskSequence));
            when(mockActionResult.isSuccess()).thenReturn(true);
            when(mockAction.perform((ActionConfig) isNull(), any(ObjectCollection.class)))
                    .thenReturn(mockActionResult);

            BooleanSupplier result = packager.toBooleanSupplier(mockStateTransition);
            boolean success = result.getAsBoolean();

            assertTrue(success);
            verify(mockAction).perform((ActionConfig) isNull(), any(ObjectCollection.class));
        }

        @Test
        @DisplayName("Should handle TaskSequence with null ObjectCollection in step")
        void testNullObjectCollection() {
            ActionStep step = new ActionStep();
            step.setActionConfig(mock(ActionConfig.class));
            step.setObjectCollection(null);

            TaskSequence taskSequence = new TaskSequence();
            taskSequence.getSteps().add(step);

            when(mockStateTransition.getTaskSequenceOptional())
                    .thenReturn(Optional.of(taskSequence));
            when(mockActionResult.isSuccess()).thenReturn(true);
            when(mockAction.perform(any(ActionConfig.class), (ObjectCollection) isNull()))
                    .thenReturn(mockActionResult);

            BooleanSupplier result = packager.toBooleanSupplier(mockStateTransition);
            boolean success = result.getAsBoolean();

            assertTrue(success);
            verify(mockAction).perform(any(ActionConfig.class), (ObjectCollection) isNull());
        }
    }

    @Nested
    @DisplayName("Mixed Success Patterns")
    class MixedSuccessPatterns {

        @Test
        @DisplayName("Should handle alternating success/failure in steps")
        void testAlternatingResults() {
            TaskSequence taskSequence = new TaskSequence();
            for (int i = 0; i < 5; i++) {
                taskSequence.getSteps().add(createActionStep());
            }

            when(mockStateTransition.getTaskSequenceOptional())
                    .thenReturn(Optional.of(taskSequence));

            ActionResult successResult = mock(ActionResult.class);
            when(successResult.isSuccess()).thenReturn(true);
            ActionResult failResult = mock(ActionResult.class);
            when(failResult.isSuccess()).thenReturn(false);

            // Alternate success and failure, ending with success
            when(mockAction.perform(any(ActionConfig.class), any(ObjectCollection.class)))
                    .thenReturn(successResult) // Step 1
                    .thenReturn(failResult) // Step 2
                    .thenReturn(successResult) // Step 3
                    .thenReturn(failResult) // Step 4
                    .thenReturn(successResult); // Step 5 (last)

            BooleanSupplier result = packager.toBooleanSupplier(mockStateTransition);

            assertTrue(result.getAsBoolean()); // Only last matters
            verify(mockAction, times(5))
                    .perform(any(ActionConfig.class), any(ObjectCollection.class));
        }

        @Test
        @DisplayName("Should handle all steps failing except last")
        void testAllFailExceptLast() {
            TaskSequence taskSequence = new TaskSequence();
            for (int i = 0; i < 10; i++) {
                taskSequence.getSteps().add(createActionStep());
            }

            when(mockStateTransition.getTaskSequenceOptional())
                    .thenReturn(Optional.of(taskSequence));

            ActionResult failResult = mock(ActionResult.class);
            when(failResult.isSuccess()).thenReturn(false);
            ActionResult successResult = mock(ActionResult.class);
            when(successResult.isSuccess()).thenReturn(true);

            // Mock action to return fail for first 9, success for last
            when(mockAction.perform(any(ActionConfig.class), any(ObjectCollection.class)))
                    .thenReturn(
                            failResult,
                            failResult,
                            failResult,
                            failResult,
                            failResult,
                            failResult,
                            failResult,
                            failResult,
                            failResult,
                            successResult);

            BooleanSupplier result = packager.toBooleanSupplier(mockStateTransition);
            assertTrue(result.getAsBoolean());
        }

        @Test
        @DisplayName("Should handle all steps succeeding except last")
        void testAllSucceedExceptLast() {
            TaskSequence taskSequence = new TaskSequence();
            for (int i = 0; i < 5; i++) {
                taskSequence.getSteps().add(createActionStep());
            }

            when(mockStateTransition.getTaskSequenceOptional())
                    .thenReturn(Optional.of(taskSequence));

            ActionResult successResult = mock(ActionResult.class);
            when(successResult.isSuccess()).thenReturn(true);
            ActionResult failResult = mock(ActionResult.class);
            when(failResult.isSuccess()).thenReturn(false);

            when(mockAction.perform(any(ActionConfig.class), any(ObjectCollection.class)))
                    .thenReturn(
                            successResult, successResult, successResult, successResult, failResult);

            BooleanSupplier result = packager.toBooleanSupplier(mockStateTransition);

            assertFalse(result.getAsBoolean()); // Last step fails
            verify(mockAction, times(5))
                    .perform(any(ActionConfig.class), any(ObjectCollection.class));
        }
    }

    @Nested
    @DisplayName("StateTransition Type Handling")
    class StateTransitionTypeHandling {

        @Test
        @DisplayName("Should handle custom StateTransition subclass")
        void testCustomStateTransition() {
            // Create a custom StateTransition that is neither Java nor has TaskSequence
            StateTransition customTransition = mock(StateTransition.class);
            when(customTransition.getTaskSequenceOptional()).thenReturn(Optional.empty());

            assertThrows(
                    IllegalArgumentException.class,
                    () -> packager.toBooleanSupplier(customTransition));
        }

        @Test
        @DisplayName("Should prioritize instanceof check over TaskSequence")
        void testInstanceOfPriority() {
            // Create a JavaStateTransition that also has a TaskSequence (hypothetically)
            JavaStateTransition hybridTransition = mock(JavaStateTransition.class);
            BooleanSupplier expectedSupplier = () -> true;
            when(hybridTransition.getTransitionFunction()).thenReturn(expectedSupplier);
            when(hybridTransition.getTaskSequenceOptional())
                    .thenReturn(Optional.of(mockTaskSequence));

            BooleanSupplier result = packager.toBooleanSupplier(hybridTransition);

            // Should use the Java function, not the TaskSequence
            assertSame(expectedSupplier, result);

            // Action should not be called
            verify(mockAction, never())
                    .perform(any(ActionConfig.class), any(ObjectCollection.class));
        }
    }

    @Nested
    @DisplayName("Supplier Reusability")
    class SupplierReusability {

        @Test
        @DisplayName("Should create reusable supplier for multiple evaluations")
        void testSupplierReusability() {
            ActionStep step = createActionStep();
            TaskSequence taskSequence = new TaskSequence();
            taskSequence.getSteps().add(step);

            when(mockStateTransition.getTaskSequenceOptional())
                    .thenReturn(Optional.of(taskSequence));

            // Different results for each call
            ActionResult result1 = mock(ActionResult.class);
            when(result1.isSuccess()).thenReturn(true);
            ActionResult result2 = mock(ActionResult.class);
            when(result2.isSuccess()).thenReturn(false);
            ActionResult result3 = mock(ActionResult.class);
            when(result3.isSuccess()).thenReturn(true);

            when(mockAction.perform(any(ActionConfig.class), any(ObjectCollection.class)))
                    .thenReturn(result1, result2, result3);

            BooleanSupplier supplier = packager.toBooleanSupplier(mockStateTransition);

            // Call the same supplier multiple times
            assertTrue(supplier.getAsBoolean()); // First call
            assertFalse(supplier.getAsBoolean()); // Second call
            assertTrue(supplier.getAsBoolean()); // Third call

            verify(mockAction, times(3))
                    .perform(any(ActionConfig.class), any(ObjectCollection.class));
        }

        @Test
        @DisplayName("Should maintain state consistency in JavaStateTransition supplier")
        void testJavaSupplierStateConsistency() {
            // Create a stateful supplier
            final int[] counter = {0};
            BooleanSupplier statefulSupplier =
                    () -> {
                        counter[0]++;
                        return counter[0] % 2 == 1; // Odd calls return true
                    };

            when(mockJavaTransition.getTransitionFunction()).thenReturn(statefulSupplier);

            BooleanSupplier result = packager.toBooleanSupplier(mockJavaTransition);

            // The supplier should maintain its state across calls
            assertTrue(result.getAsBoolean()); // counter = 1 (odd)
            assertFalse(result.getAsBoolean()); // counter = 2 (even)
            assertTrue(result.getAsBoolean()); // counter = 3 (odd)
            assertFalse(result.getAsBoolean()); // counter = 4 (even)

            assertEquals(4, counter[0]);
        }
    }

    @Nested
    @DisplayName("Thread Safety Considerations")
    class ThreadSafety {

        @Test
        @DisplayName("Should handle concurrent calls to same supplier")
        void testConcurrentSupplierCalls() throws InterruptedException {
            ActionStep step = createActionStep();
            TaskSequence taskSequence = new TaskSequence();
            taskSequence.getSteps().add(step);

            when(mockStateTransition.getTaskSequenceOptional())
                    .thenReturn(Optional.of(taskSequence));
            when(mockActionResult.isSuccess()).thenReturn(true);

            // Simulate slow action
            when(mockAction.perform(any(ActionConfig.class), any(ObjectCollection.class)))
                    .thenAnswer(
                            invocation -> {
                                Thread.sleep(10); // Small delay to increase chance of overlap
                                return mockActionResult;
                            });

            BooleanSupplier supplier = packager.toBooleanSupplier(mockStateTransition);

            // Run multiple threads calling the supplier
            List<Thread> threads = new ArrayList<>();
            List<Boolean> results = Collections.synchronizedList(new ArrayList<>());

            for (int i = 0; i < 5; i++) {
                Thread thread =
                        new Thread(
                                () -> {
                                    results.add(supplier.getAsBoolean());
                                });
                threads.add(thread);
                thread.start();
            }

            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }

            // All should succeed
            assertEquals(5, results.size());
            assertTrue(results.stream().allMatch(r -> r));

            // Action should be called 5 times
            verify(mockAction, times(5))
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
