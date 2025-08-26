package io.github.jspinak.brobot.tools.testing.mock.verification;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.testing.mock.scenario.MockTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for MockBehaviorVerifier.
 * Tests the core orchestration component responsible for managing verification workflows.
 */
@DisplayName("MockBehaviorVerifier Tests")
class MockBehaviorVerifierTest extends BrobotTestBase {

    private MockBehaviorVerifier verifier;
    private MockTestContext mockContext;
    private ActionResult successResult;
    private ActionResult failureResult;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        verifier = new MockBehaviorVerifier();
        mockContext = mock(MockTestContext.class);
        
        // Create success result
        successResult = new ActionResult();
        successResult.setSuccess(true);
        successResult.setDuration(Duration.ofMillis(100));
        successResult.setActionDescription("Test success action");
            
        // Create failure result
        failureResult = new ActionResult();
        failureResult.setSuccess(false);
        failureResult.setDuration(Duration.ofMillis(50));
        failureResult.setActionDescription("Test failure action");
    }

    @Nested
    @DisplayName("Action Recording Tests")
    class ActionRecordingTests {

        @Test
        @DisplayName("Should record action execution successfully")
        void shouldRecordActionExecution() {
            // Act
            verifier.recordAction(ActionType.CLICK, successResult, mockContext);
            
            // Assert
            List<MockBehaviorVerifier.ExecutionEvent> history = verifier.getExecutionHistory();
            assertEquals(1, history.size());
            
            MockBehaviorVerifier.ExecutionEvent event = history.get(0);
            assertTrue(event.isActionEvent());
            assertFalse(event.isStateTransitionEvent());
            assertEquals(ActionType.CLICK, event.getAction());
            assertEquals(successResult, event.getResult());
            assertEquals(mockContext, event.getContext());
            assertNotNull(event.getTimestamp());
        }

        @Test
        @DisplayName("Should record multiple actions in order")
        void shouldRecordMultipleActionsInOrder() {
            // Arrange
            LocalDateTime beforeRecording = LocalDateTime.now();
            
            // Act
            verifier.recordAction(ActionType.FIND, successResult, mockContext);
            sleepMillis(10); // Small delay to ensure different timestamps
            verifier.recordAction(ActionType.CLICK, failureResult, mockContext);
            sleepMillis(10);
            verifier.recordAction(ActionType.TYPE, successResult, mockContext);
            
            // Assert
            List<MockBehaviorVerifier.ExecutionEvent> history = verifier.getExecutionHistory();
            assertEquals(3, history.size());
            
            // Verify chronological order
            LocalDateTime previousTimestamp = beforeRecording;
            for (MockBehaviorVerifier.ExecutionEvent event : history) {
                assertTrue(event.getTimestamp().isAfter(previousTimestamp) || 
                          event.getTimestamp().isEqual(previousTimestamp));
                previousTimestamp = event.getTimestamp();
            }
            
            // Verify action types
            assertEquals(ActionType.FIND, history.get(0).getAction());
            assertEquals(ActionType.CLICK, history.get(1).getAction());
            assertEquals(ActionType.TYPE, history.get(2).getAction());
        }

        @Test
        @DisplayName("Should handle concurrent action recording")
        void shouldHandleConcurrentActionRecording() throws InterruptedException {
            // Arrange
            int threadCount = 10;
            int actionsPerThread = 50;
            Thread[] threads = new Thread[threadCount];
            
            // Act
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < actionsPerThread; j++) {
                        verifier.recordAction(
                            ActionType.values()[j % ActionType.values().length],
                            successResult,
                            mockContext
                        );
                    }
                });
                threads[i].start();
            }
            
            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }
            
            // Assert
            List<MockBehaviorVerifier.ExecutionEvent> history = verifier.getExecutionHistory();
            assertEquals(threadCount * actionsPerThread, history.size());
            
            // Verify all events are action events
            assertTrue(history.stream().allMatch(MockBehaviorVerifier.ExecutionEvent::isActionEvent));
        }
    }

    @Nested
    @DisplayName("State Transition Recording Tests")
    class StateTransitionRecordingTests {

        @Test
        @DisplayName("Should record state transition successfully")
        void shouldRecordStateTransition() {
            // Act
            verifier.recordStateTransition("LOGIN_PAGE", "DASHBOARD", mockContext);
            
            // Assert
            List<MockBehaviorVerifier.ExecutionEvent> history = verifier.getExecutionHistory();
            assertEquals(1, history.size());
            
            MockBehaviorVerifier.ExecutionEvent event = history.get(0);
            assertTrue(event.isStateTransitionEvent());
            assertFalse(event.isActionEvent());
            assertEquals("LOGIN_PAGE", event.getFromState());
            assertEquals("DASHBOARD", event.getToState());
            assertEquals(mockContext, event.getContext());
            assertNotNull(event.getTimestamp());
        }

        @Test
        @DisplayName("Should record multiple state transitions")
        void shouldRecordMultipleStateTransitions() {
            // Act
            verifier.recordStateTransition("START", "MIDDLE", mockContext);
            verifier.recordStateTransition("MIDDLE", "END", mockContext);
            verifier.recordStateTransition("END", "START", mockContext);
            
            // Assert
            List<MockBehaviorVerifier.ExecutionEvent> history = verifier.getExecutionHistory();
            assertEquals(3, history.size());
            
            // Verify transition sequence
            assertEquals("START", history.get(0).getFromState());
            assertEquals("MIDDLE", history.get(0).getToState());
            assertEquals("MIDDLE", history.get(1).getFromState());
            assertEquals("END", history.get(1).getToState());
            assertEquals("END", history.get(2).getFromState());
            assertEquals("START", history.get(2).getToState());
        }

        @Test
        @DisplayName("Should handle mixed actions and transitions")
        void shouldHandleMixedActionsAndTransitions() {
            // Act
            verifier.recordStateTransition("LOGIN", "LOADING", mockContext);
            verifier.recordAction(ActionType.VANISH, successResult, mockContext);
            verifier.recordStateTransition("LOADING", "DASHBOARD", mockContext);
            verifier.recordAction(ActionType.CLICK, successResult, mockContext);
            
            // Assert
            List<MockBehaviorVerifier.ExecutionEvent> history = verifier.getExecutionHistory();
            assertEquals(4, history.size());
            
            assertTrue(history.get(0).isStateTransitionEvent());
            assertTrue(history.get(1).isActionEvent());
            assertTrue(history.get(2).isStateTransitionEvent());
            assertTrue(history.get(3).isActionEvent());
        }
    }

    @Nested
    @DisplayName("Event Filtering Tests")
    class EventFilteringTests {

        @BeforeEach
        void setupEvents() {
            // Add various events for filtering
            verifier.recordAction(ActionType.FIND, successResult, mockContext);
            verifier.recordAction(ActionType.CLICK, failureResult, mockContext);
            verifier.recordStateTransition("A", "B", mockContext);
            verifier.recordAction(ActionType.TYPE, successResult, mockContext);
            verifier.recordStateTransition("B", "C", mockContext);
        }

        @Test
        @DisplayName("Should filter action events")
        void shouldFilterActionEvents() {
            // Act
            List<MockBehaviorVerifier.ExecutionEvent> actionEvents = 
                verifier.getEvents(MockBehaviorVerifier.ExecutionEvent::isActionEvent);
            
            // Assert
            assertEquals(3, actionEvents.size());
            assertTrue(actionEvents.stream().allMatch(MockBehaviorVerifier.ExecutionEvent::isActionEvent));
        }

        @Test
        @DisplayName("Should filter state transition events")
        void shouldFilterStateTransitionEvents() {
            // Act
            List<MockBehaviorVerifier.ExecutionEvent> transitionEvents = 
                verifier.getEvents(MockBehaviorVerifier.ExecutionEvent::isStateTransitionEvent);
            
            // Assert
            assertEquals(2, transitionEvents.size());
            assertTrue(transitionEvents.stream().allMatch(MockBehaviorVerifier.ExecutionEvent::isStateTransitionEvent));
        }

        @Test
        @DisplayName("Should filter by action type")
        void shouldFilterByActionType() {
            // Act
            Predicate<MockBehaviorVerifier.ExecutionEvent> findActions = 
                event -> event.isActionEvent() && event.getAction() == ActionType.FIND;
            List<MockBehaviorVerifier.ExecutionEvent> findEvents = verifier.getEvents(findActions);
            
            // Assert
            assertEquals(1, findEvents.size());
            assertEquals(ActionType.FIND, findEvents.get(0).getAction());
        }

        @Test
        @DisplayName("Should filter successful actions")
        void shouldFilterSuccessfulActions() {
            // Act
            Predicate<MockBehaviorVerifier.ExecutionEvent> successfulActions = 
                event -> event.isActionEvent() && 
                        event.getResult() != null && 
                        event.getResult().isSuccess();
            List<MockBehaviorVerifier.ExecutionEvent> successEvents = verifier.getEvents(successfulActions);
            
            // Assert
            assertEquals(2, successEvents.size()); // FIND and TYPE were successful
            assertTrue(successEvents.stream()
                .allMatch(e -> e.getResult().isSuccess()));
        }

        @Test
        @DisplayName("Should filter by state transition path")
        void shouldFilterByTransitionPath() {
            // Act
            Predicate<MockBehaviorVerifier.ExecutionEvent> fromStateB = 
                event -> event.isStateTransitionEvent() && "B".equals(event.getFromState());
            List<MockBehaviorVerifier.ExecutionEvent> fromBTransitions = verifier.getEvents(fromStateB);
            
            // Assert
            assertEquals(1, fromBTransitions.size());
            assertEquals("B", fromBTransitions.get(0).getFromState());
            assertEquals("C", fromBTransitions.get(0).getToState());
        }
    }

    @Nested
    @DisplayName("Verification Builder Tests")
    class VerificationBuilderTests {

        @Test
        @DisplayName("Should create transition verification builder")
        void shouldCreateTransitionVerificationBuilder() {
            // Act
            StateTransitionVerification.Builder builder = 
                verifier.expectTransitionSequence("test_transition");
            
            // Assert
            assertNotNull(builder);
            // The builder should be properly configured
            // Actual verification would happen when build() is called
        }

        @Test
        @DisplayName("Should create action pattern verification builder")
        void shouldCreateActionPatternVerificationBuilder() {
            // Act
            ActionPatternVerification.Builder builder = 
                verifier.expectActionPattern("test_pattern");
            
            // Assert
            assertNotNull(builder);
            // The builder should be properly configured
            // Actual verification would happen when build() is called
        }

        @Test
        @DisplayName("Should add transition verification to active set")
        void shouldAddTransitionVerificationToActiveSet() {
            // Arrange
            StateTransitionVerification mockVerification = mock(StateTransitionVerification.class);
            when(mockVerification.getResult()).thenReturn(VerificationResult.IN_PROGRESS);
            
            // Act
            verifier.addTransitionVerification("test_id", mockVerification);
            Map<String, VerificationResult> results = verifier.getVerificationResults();
            
            // Assert
            assertTrue(results.containsKey("test_id"));
            assertEquals(VerificationResult.IN_PROGRESS, results.get("test_id"));
        }

        @Test
        @DisplayName("Should add pattern verification to active set")
        void shouldAddPatternVerificationToActiveSet() {
            // Arrange
            ActionPatternVerification mockVerification = mock(ActionPatternVerification.class);
            when(mockVerification.getResult()).thenReturn(VerificationResult.PASSED);
            
            // Act
            verifier.addPatternVerification("pattern_id", mockVerification);
            Map<String, VerificationResult> results = verifier.getVerificationResults();
            
            // Assert
            assertTrue(results.containsKey("pattern_id"));
            assertEquals(VerificationResult.PASSED, results.get("pattern_id"));
        }
    }

    @Nested
    @DisplayName("Reset and Cleanup Tests")
    class ResetAndCleanupTests {

        @Test
        @DisplayName("Should reset all verifications and history")
        void shouldResetAllVerificationsAndHistory() {
            // Arrange
            verifier.recordAction(ActionType.CLICK, successResult, mockContext);
            verifier.recordStateTransition("A", "B", mockContext);
            
            StateTransitionVerification mockTransition = mock(StateTransitionVerification.class);
            ActionPatternVerification mockPattern = mock(ActionPatternVerification.class);
            verifier.addTransitionVerification("trans_id", mockTransition);
            verifier.addPatternVerification("pattern_id", mockPattern);
            
            // Act
            verifier.reset();
            
            // Assert
            assertTrue(verifier.getExecutionHistory().isEmpty());
            assertTrue(verifier.getVerificationResults().isEmpty());
        }

        @Test
        @DisplayName("Should maintain thread safety during reset")
        void shouldMaintainThreadSafetyDuringReset() throws InterruptedException {
            // Arrange
            Thread recordingThread = new Thread(() -> {
                for (int i = 0; i < 100; i++) {
                    verifier.recordAction(ActionType.CLICK, successResult, mockContext);
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            
            Thread resetThread = new Thread(() -> {
                try {
                    Thread.sleep(50);
                    verifier.reset();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            // Act
            recordingThread.start();
            resetThread.start();
            
            recordingThread.join();
            resetThread.join();
            
            // Assert - should not throw any exceptions
            // After reset, history should be cleared or have only events after reset
            List<MockBehaviorVerifier.ExecutionEvent> history = verifier.getExecutionHistory();
            assertTrue(history.size() < 100); // Some events should have been cleared
        }
    }

    @Nested
    @DisplayName("ExecutionEvent Tests")
    class ExecutionEventTests {

        @Test
        @DisplayName("Should calculate event age correctly")
        void shouldCalculateEventAge() throws InterruptedException {
            // Arrange
            LocalDateTime timestamp = LocalDateTime.now().minusSeconds(5);
            MockBehaviorVerifier.ExecutionEvent event = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(timestamp)
                .action(ActionType.CLICK)
                .build();
            
            // Act
            Duration age = event.getAge();
            
            // Assert
            assertTrue(age.getSeconds() >= 5);
            assertTrue(age.getSeconds() < 10); // Reasonable upper bound
        }

        @Test
        @DisplayName("Should identify action events correctly")
        void shouldIdentifyActionEventsCorrectly() {
            // Arrange
            MockBehaviorVerifier.ExecutionEvent actionEvent = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .action(ActionType.CLICK)
                .result(successResult)
                .build();
                
            MockBehaviorVerifier.ExecutionEvent transitionEvent = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .fromState("A")
                .toState("B")
                .build();
            
            // Assert
            assertTrue(actionEvent.isActionEvent());
            assertFalse(actionEvent.isStateTransitionEvent());
            assertFalse(transitionEvent.isActionEvent());
            assertTrue(transitionEvent.isStateTransitionEvent());
        }

        @Test
        @DisplayName("Should handle metadata correctly")
        void shouldHandleMetadataCorrectly() {
            // Arrange
            Map<String, Object> metadata = Map.of(
                "key1", "value1",
                "key2", 42,
                "key3", true
            );
            
            MockBehaviorVerifier.ExecutionEvent event = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .action(ActionType.FIND)
                .metadata(metadata)
                .build();
            
            // Assert
            assertNotNull(event.getMetadata());
            assertEquals(3, event.getMetadata().size());
            assertEquals("value1", event.getMetadata().get("key1"));
            assertEquals(42, event.getMetadata().get("key2"));
            assertEquals(true, event.getMetadata().get("key3"));
        }
    }

    @Nested
    @DisplayName("Integration with Active Verifications")
    class ActiveVerificationIntegrationTests {

        @Test
        @DisplayName("Should trigger active verifications on new events")
        void shouldTriggerActiveVerificationsOnNewEvents() {
            // Arrange
            StateTransitionVerification mockTransition = mock(StateTransitionVerification.class);
            ActionPatternVerification mockPattern = mock(ActionPatternVerification.class);
            
            verifier.addTransitionVerification("trans_id", mockTransition);
            verifier.addPatternVerification("pattern_id", mockPattern);
            
            // Act
            verifier.recordAction(ActionType.CLICK, successResult, mockContext);
            
            // Assert
            verify(mockTransition, times(1)).checkEvent(any(MockBehaviorVerifier.ExecutionEvent.class));
            verify(mockPattern, times(1)).checkEvent(any(MockBehaviorVerifier.ExecutionEvent.class));
        }

        @Test
        @DisplayName("Should collect results from multiple verifications")
        void shouldCollectResultsFromMultipleVerifications() {
            // Arrange
            StateTransitionVerification mockTransition1 = mock(StateTransitionVerification.class);
            StateTransitionVerification mockTransition2 = mock(StateTransitionVerification.class);
            ActionPatternVerification mockPattern = mock(ActionPatternVerification.class);
            
            when(mockTransition1.getResult()).thenReturn(VerificationResult.PASSED);
            when(mockTransition2.getResult()).thenReturn(VerificationResult.FAILED);
            when(mockPattern.getResult()).thenReturn(VerificationResult.IN_PROGRESS);
            
            verifier.addTransitionVerification("trans1", mockTransition1);
            verifier.addTransitionVerification("trans2", mockTransition2);
            verifier.addPatternVerification("pattern1", mockPattern);
            
            // Act
            Map<String, VerificationResult> results = verifier.getVerificationResults();
            
            // Assert
            assertEquals(3, results.size());
            assertEquals(VerificationResult.PASSED, results.get("trans1"));
            assertEquals(VerificationResult.FAILED, results.get("trans2"));
            assertEquals(VerificationResult.IN_PROGRESS, results.get("pattern1"));
        }
    }

    private void sleepMillis(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}