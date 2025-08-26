package io.github.jspinak.brobot.tools.testing.mock.verification;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.testing.mock.scenario.MockTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for StateTransitionVerification.
 * Tests validation of state transition sequences with timing constraints.
 */
@DisplayName("StateTransitionVerification Tests")
class StateTransitionVerificationTest extends BrobotTestBase {

    private MockBehaviorVerifier mockVerifier;
    private MockTestContext mockContext;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mockVerifier = mock(MockBehaviorVerifier.class);
        mockContext = mock(MockTestContext.class);
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create simple transition verification")
        void shouldCreateSimpleTransitionVerification() {
            // Arrange
            StateTransitionVerification.Builder builder = 
                new StateTransitionVerification.Builder("test_id", mockVerifier);
            
            // Act
            StateTransitionVerification verification = builder
                .fromState("LOGIN")
                .toState("DASHBOARD")
                .verify();
            
            // Assert
            assertNotNull(verification);
            assertEquals("test_id", verification.getVerificationId());
            assertEquals(1, verification.getExpectedSteps().size());
            
            StateTransitionVerification.TransitionStep step = verification.getExpectedSteps().get(0);
            assertEquals("LOGIN", step.getFromState());
            assertEquals("DASHBOARD", step.getToState());
            assertFalse(step.isOptional());
            
            verify(mockVerifier).addTransitionVerification("test_id", verification);
        }

        @Test
        @DisplayName("Should create multi-step transition sequence")
        void shouldCreateMultiStepTransitionSequence() {
            // Arrange
            StateTransitionVerification.Builder builder = 
                new StateTransitionVerification.Builder("multi_step", mockVerifier);
            
            // Act
            StateTransitionVerification verification = builder
                .transition("START", "LOADING", false)
                .transition("LOADING", "MIDDLE", false)
                .transition("MIDDLE", "END", false)
                .verify();
            
            // Assert
            assertEquals(3, verification.getExpectedSteps().size());
            
            List<StateTransitionVerification.TransitionStep> steps = verification.getExpectedSteps();
            assertEquals("START", steps.get(0).getFromState());
            assertEquals("LOADING", steps.get(0).getToState());
            assertEquals("LOADING", steps.get(1).getFromState());
            assertEquals("MIDDLE", steps.get(1).getToState());
            assertEquals("MIDDLE", steps.get(2).getFromState());
            assertEquals("END", steps.get(2).getToState());
        }

        @Test
        @DisplayName("Should set timing constraints")
        void shouldSetTimingConstraints() {
            // Arrange
            StateTransitionVerification.Builder builder = 
                new StateTransitionVerification.Builder("timed_test", mockVerifier);
            
            // Act
            StateTransitionVerification verification = builder
                .fromState("A")
                .toState("B")
                .minDuration(Duration.ofMillis(100))
                .maxDuration(Duration.ofSeconds(2))
                .withinTime(Duration.ofSeconds(5))
                .verify();
            
            // Assert
            StateTransitionVerification.TransitionStep step = verification.getExpectedSteps().get(0);
            assertEquals(Duration.ofMillis(100), step.getMinDuration());
            assertEquals(Duration.ofSeconds(2), step.getMaxDuration());
            assertEquals(Duration.ofSeconds(5), verification.getMaxTotalTime());
        }

        @Test
        @DisplayName("Should handle optional transitions")
        void shouldHandleOptionalTransitions() {
            // Arrange & Act
            StateTransitionVerification verification = 
                new StateTransitionVerification.Builder("optional_test", mockVerifier)
                    .transition("A", "B", false)  // Required
                    .transition("B", "C", true)   // Optional
                    .transition("C", "D", false)  // Required
                    .verify();
            
            // Assert
            List<StateTransitionVerification.TransitionStep> steps = verification.getExpectedSteps();
            assertFalse(steps.get(0).isOptional());
            assertTrue(steps.get(1).isOptional());
            assertFalse(steps.get(2).isOptional());
        }

        @Test
        @DisplayName("Should throw exception when toState called before fromState")
        void shouldThrowExceptionWhenToStateCalledBeforeFromState() {
            // Arrange
            StateTransitionVerification.Builder builder = 
                new StateTransitionVerification.Builder("error_test", mockVerifier);
            
            // Act & Assert
            assertThrows(IllegalStateException.class, 
                () -> builder.toState("DASHBOARD"),
                "Must call fromState() first");
        }

        @Test
        @DisplayName("Should throw exception when duration set without transition")
        void shouldThrowExceptionWhenDurationSetWithoutTransition() {
            // Arrange
            StateTransitionVerification.Builder builder = 
                new StateTransitionVerification.Builder("error_test", mockVerifier);
            
            // Act & Assert
            assertThrows(IllegalStateException.class, 
                () -> builder.minDuration(Duration.ofSeconds(1)),
                "Must add a transition step first");
                
            assertThrows(IllegalStateException.class, 
                () -> builder.maxDuration(Duration.ofSeconds(1)),
                "Must add a transition step first");
        }
    }

    @Nested
    @DisplayName("Event Checking Tests")
    class EventCheckingTests {

        private StateTransitionVerification verification;
        
        @BeforeEach
        void setup() {
            verification = new StateTransitionVerification.Builder("test", mockVerifier)
                .fromState("A")
                .toState("B")
                .verify();
        }

        @Test
        @DisplayName("Should pass when matching transition occurs")
        void shouldPassWhenMatchingTransitionOccurs() {
            // Arrange
            MockBehaviorVerifier.ExecutionEvent event = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .fromState("A")
                .toState("B")
                .context(mockContext)
                .build();
            
            // Act
            verification.checkEvent(event);
            
            // Assert
            assertEquals(VerificationResult.PASSED, verification.getResult());
            assertTrue(verification.getErrors().isEmpty());
        }

        @Test
        @DisplayName("Should fail when wrong transition occurs")
        void shouldFailWhenWrongTransitionOccurs() {
            // Arrange
            MockBehaviorVerifier.ExecutionEvent wrongEvent = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .fromState("A")
                .toState("C")  // Wrong destination
                .context(mockContext)
                .build();
            
            // Act
            verification.checkEvent(wrongEvent);
            
            // Assert
            assertEquals(VerificationResult.FAILED, verification.getResult());
            assertFalse(verification.getErrors().isEmpty());
            assertTrue(verification.getErrors().get(0).contains("Expected transition A -> B but got A -> C"));
        }

        @Test
        @DisplayName("Should ignore non-transition events")
        void shouldIgnoreNonTransitionEvents() {
            // Arrange
            MockBehaviorVerifier.ExecutionEvent actionEvent = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .action(io.github.jspinak.brobot.action.ActionType.CLICK)
                .context(mockContext)
                .build();
            
            // Act
            verification.checkEvent(actionEvent);
            
            // Assert
            assertEquals(VerificationResult.IN_PROGRESS, verification.getResult());
            assertTrue(verification.getErrors().isEmpty());
        }

        @Test
        @DisplayName("Should ignore events after verification completes")
        void shouldIgnoreEventsAfterVerificationCompletes() {
            // Arrange
            MockBehaviorVerifier.ExecutionEvent event1 = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .fromState("A")
                .toState("B")
                .build();
                
            MockBehaviorVerifier.ExecutionEvent event2 = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .fromState("B")
                .toState("C")
                .build();
            
            // Act
            verification.checkEvent(event1);  // This completes the verification
            verification.checkEvent(event2);  // This should be ignored
            
            // Assert
            assertEquals(VerificationResult.PASSED, verification.getResult());
            // Result should not change after completion
        }
    }

    @Nested
    @DisplayName("Timing Constraint Tests")
    class TimingConstraintTests {

        @Test
        @DisplayName("Should fail when step takes too long")
        void shouldFailWhenStepTakesTooLong() throws InterruptedException {
            // Arrange
            StateTransitionVerification verification = 
                new StateTransitionVerification.Builder("timing_test", mockVerifier)
                    .fromState("A")
                    .toState("B")
                    .maxDuration(Duration.ofMillis(50))
                    .verify();
            
            // Wait to simulate delay
            Thread.sleep(100);
            
            MockBehaviorVerifier.ExecutionEvent event = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .fromState("A")
                .toState("B")
                .build();
            
            // Act
            verification.checkEvent(event);
            
            // Assert
            assertEquals(VerificationResult.FAILED, verification.getResult());
            assertFalse(verification.getErrors().isEmpty());
            assertTrue(verification.getErrors().get(0).contains("took too long"));
        }

        @Test
        @DisplayName("Should fail when step completes too quickly")
        void shouldFailWhenStepCompletesTooQuickly() {
            // Arrange
            StateTransitionVerification verification = 
                new StateTransitionVerification.Builder("timing_test", mockVerifier)
                    .fromState("A")
                    .toState("B")
                    .minDuration(Duration.ofSeconds(1))
                    .verify();
            
            // Immediate event
            MockBehaviorVerifier.ExecutionEvent event = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .fromState("A")
                .toState("B")
                .build();
            
            // Act
            verification.checkEvent(event);
            
            // Assert
            assertEquals(VerificationResult.FAILED, verification.getResult());
            assertFalse(verification.getErrors().isEmpty());
            assertTrue(verification.getErrors().get(0).contains("completed too quickly"));
        }

        @Test
        @DisplayName("Should fail when total time exceeds limit")
        void shouldFailWhenTotalTimeExceedsLimit() throws InterruptedException {
            // Arrange
            StateTransitionVerification verification = 
                new StateTransitionVerification.Builder("timeout_test", mockVerifier)
                    .fromState("A")
                    .toState("B")
                    .withinTime(Duration.ofMillis(50))
                    .verify();
            
            // Wait to exceed timeout
            Thread.sleep(100);
            
            MockBehaviorVerifier.ExecutionEvent event = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .fromState("A")
                .toState("B")
                .build();
            
            // Act
            verification.checkEvent(event);
            
            // Assert
            assertEquals(VerificationResult.FAILED, verification.getResult());
            assertFalse(verification.getErrors().isEmpty());
            assertTrue(verification.getErrors().get(0).contains("timed out"));
        }

        @Test
        @DisplayName("Should pass when timing constraints are met")
        void shouldPassWhenTimingConstraintsMet() throws InterruptedException {
            // Arrange
            StateTransitionVerification verification = 
                new StateTransitionVerification.Builder("timing_test", mockVerifier)
                    .fromState("A")
                    .toState("B")
                    .minDuration(Duration.ofMillis(50))
                    .maxDuration(Duration.ofMillis(200))
                    .withinTime(Duration.ofSeconds(1))
                    .verify();
            
            // Wait appropriate time
            Thread.sleep(100);
            
            MockBehaviorVerifier.ExecutionEvent event = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .fromState("A")
                .toState("B")
                .build();
            
            // Act
            verification.checkEvent(event);
            
            // Assert
            assertEquals(VerificationResult.PASSED, verification.getResult());
            assertTrue(verification.getErrors().isEmpty());
        }
    }

    @Nested
    @DisplayName("Multi-Step Sequence Tests")
    class MultiStepSequenceTests {

        @Test
        @DisplayName("Should verify complete multi-step sequence")
        void shouldVerifyCompleteMultiStepSequence() {
            // Arrange
            StateTransitionVerification verification = 
                new StateTransitionVerification.Builder("multi", mockVerifier)
                    .transition("START", "LOADING", false)
                    .transition("LOADING", "READY", false)
                    .transition("READY", "COMPLETE", false)
                    .verify();
            
            LocalDateTime now = LocalDateTime.now();
            
            MockBehaviorVerifier.ExecutionEvent event1 = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(now.plusSeconds(1))
                .fromState("START")
                .toState("LOADING")
                .build();
                
            MockBehaviorVerifier.ExecutionEvent event2 = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(now.plusSeconds(2))
                .fromState("LOADING")
                .toState("READY")
                .build();
                
            MockBehaviorVerifier.ExecutionEvent event3 = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(now.plusSeconds(3))
                .fromState("READY")
                .toState("COMPLETE")
                .build();
            
            // Act
            verification.checkEvent(event1);
            assertEquals(VerificationResult.IN_PROGRESS, verification.getResult());
            
            verification.checkEvent(event2);
            assertEquals(VerificationResult.IN_PROGRESS, verification.getResult());
            
            verification.checkEvent(event3);
            
            // Assert
            assertEquals(VerificationResult.PASSED, verification.getResult());
            assertTrue(verification.getErrors().isEmpty());
        }

        @Test
        @DisplayName("Should handle optional steps correctly")
        void shouldHandleOptionalStepsCorrectly() {
            // Arrange
            StateTransitionVerification verification = 
                new StateTransitionVerification.Builder("optional", mockVerifier)
                    .transition("A", "B", false)    // Required
                    .transition("B", "C", true)     // Optional - can be skipped
                    .transition("C", "D", false)    // Required after C (if C is reached)
                    .verify();
            
            // Complete required step A->B
            MockBehaviorVerifier.ExecutionEvent event1 = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .fromState("A")
                .toState("B")
                .build();
            
            // Act
            verification.checkEvent(event1);
            
            // After completing A->B, we're at step index 1 (B->C which is optional)
            // The verification should remain IN_PROGRESS since B->C is optional
            // and we're waiting to see if it happens or if we move to the next step
            assertEquals(VerificationResult.IN_PROGRESS, verification.getResult());
            
            // Now test including the optional step
            MockBehaviorVerifier.ExecutionEvent event2 = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .fromState("B")
                .toState("C")
                .build();
                
            MockBehaviorVerifier.ExecutionEvent event3 = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .fromState("C")
                .toState("D")
                .build();
            
            verification.checkEvent(event2);
            assertEquals(VerificationResult.IN_PROGRESS, verification.getResult());
            
            verification.checkEvent(event3);
            assertEquals(VerificationResult.PASSED, verification.getResult());
        }

        @Test
        @DisplayName("Should fail on wrong order in sequence")
        void shouldFailOnWrongOrderInSequence() {
            // Arrange
            StateTransitionVerification verification = 
                new StateTransitionVerification.Builder("order", mockVerifier)
                    .transition("A", "B", false)
                    .transition("B", "C", false)
                    .verify();
            
            // Wrong order - go directly to second transition
            MockBehaviorVerifier.ExecutionEvent wrongEvent = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .fromState("B")
                .toState("C")
                .build();
            
            // Act
            verification.checkEvent(wrongEvent);
            
            // Assert
            assertEquals(VerificationResult.FAILED, verification.getResult());
            assertFalse(verification.getErrors().isEmpty());
        }

        @Test
        @DisplayName("Should track progress through sequence")
        void shouldTrackProgressThroughSequence() {
            // Arrange
            StateTransitionVerification verification = 
                new StateTransitionVerification.Builder("progress", mockVerifier)
                    .transition("A", "B", false)
                    .transition("B", "C", false)
                    .transition("C", "D", false)
                    .verify();
            
            // Act & Assert - check progress at each step
            assertEquals(0, verification.getCurrentStepIndex());
            
            verification.checkEvent(MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .fromState("A")
                .toState("B")
                .build());
            assertEquals(1, verification.getCurrentStepIndex());
            assertEquals(VerificationResult.IN_PROGRESS, verification.getResult());
            
            verification.checkEvent(MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .fromState("B")
                .toState("C")
                .build());
            assertEquals(2, verification.getCurrentStepIndex());
            assertEquals(VerificationResult.IN_PROGRESS, verification.getResult());
            
            verification.checkEvent(MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .fromState("C")
                .toState("D")
                .build());
            assertEquals(3, verification.getCurrentStepIndex());
            assertEquals(VerificationResult.PASSED, verification.getResult());
        }
    }

    @Nested
    @DisplayName("Error Reporting Tests")
    class ErrorReportingTests {

        @Test
        @DisplayName("Should collect multiple timing errors")
        void shouldCollectMultipleTimingErrors() throws InterruptedException {
            // Arrange
            StateTransitionVerification verification = 
                new StateTransitionVerification.Builder("errors", mockVerifier)
                    .transition("A", "B", false)
                    .minDuration(Duration.ofMillis(100))
                    .maxDuration(Duration.ofMillis(200))
                    .transition("B", "C", false)
                    .minDuration(Duration.ofMillis(50))
                    .verify();
            
            // First transition too fast
            MockBehaviorVerifier.ExecutionEvent event1 = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .fromState("A")
                .toState("B")
                .build();
            verification.checkEvent(event1);
            
            // Second transition also too fast
            MockBehaviorVerifier.ExecutionEvent event2 = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .fromState("B")
                .toState("C")
                .build();
            verification.checkEvent(event2);
            
            // Assert
            assertEquals(VerificationResult.FAILED, verification.getResult());
            List<String> errors = verification.getErrors();
            assertEquals(2, errors.size());
            assertTrue(errors.get(0).contains("completed too quickly"));
            assertTrue(errors.get(1).contains("completed too quickly"));
        }

        @Test
        @DisplayName("Should provide detailed error messages")
        void shouldProvideDetailedErrorMessages() {
            // Arrange
            StateTransitionVerification verification = 
                new StateTransitionVerification.Builder("details", mockVerifier)
                    .fromState("LOGIN")
                    .toState("DASHBOARD")
                    .verify();
            
            MockBehaviorVerifier.ExecutionEvent wrongEvent = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .fromState("LOGIN")
                .toState("ERROR_PAGE")
                .build();
            
            // Act
            verification.checkEvent(wrongEvent);
            
            // Assert
            List<String> errors = verification.getErrors();
            assertEquals(1, errors.size());
            String error = errors.get(0);
            assertTrue(error.contains("LOGIN"));
            assertTrue(error.contains("DASHBOARD"));
            assertTrue(error.contains("ERROR_PAGE"));
        }

        @Test
        @DisplayName("Should return defensive copy of errors")
        void shouldReturnDefensiveCopyOfErrors() {
            // Arrange
            StateTransitionVerification verification = 
                new StateTransitionVerification.Builder("defensive", mockVerifier)
                    .fromState("A")
                    .toState("B")
                    .verify();
            
            MockBehaviorVerifier.ExecutionEvent wrongEvent = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .fromState("A")
                .toState("C")
                .build();
            
            verification.checkEvent(wrongEvent);
            
            // Act
            List<String> errors1 = verification.getErrors();
            List<String> errors2 = verification.getErrors();
            
            // Assert
            assertNotSame(errors1, errors2);
            assertEquals(errors1, errors2);
            
            // Modifying returned list shouldn't affect internal state
            errors1.clear();
            assertEquals(1, verification.getErrors().size());
        }
    }
}