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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for ActionPatternVerification.
 * Tests validation of action execution patterns and retry behavior.
 */
@DisplayName("ActionPatternVerification Tests")
class ActionPatternVerificationTest extends BrobotTestBase {

    private MockBehaviorVerifier mockVerifier;
    private MockTestContext mockContext;
    private ActionResult successResult;
    private ActionResult failureResult;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mockVerifier = mock(MockBehaviorVerifier.class);
        mockContext = mock(MockTestContext.class);
        
        // Create success result
        successResult = new ActionResult();
        successResult.setSuccess(true);
        successResult.setDuration(Duration.ofMillis(100));
        
        // Create failure result
        failureResult = new ActionResult();
        failureResult.setSuccess(false);
        failureResult.setDuration(Duration.ofMillis(50));
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create simple action pattern verification")
        void shouldCreateSimpleActionPatternVerification() {
            // Act
            ActionPatternVerification verification = 
                new ActionPatternVerification.Builder("test_pattern", mockVerifier)
                    .action(ActionType.FIND)
                    .verify();
            
            // Assert
            assertNotNull(verification);
            assertEquals("test_pattern", verification.getVerificationId());
            assertEquals(ActionType.FIND, verification.getTargetAction());
            assertEquals(Integer.MAX_VALUE, verification.getMaxAttempts());
            assertEquals(0.0, verification.getExpectedSuccessRate());
            assertNull(verification.getBackoffDuration());
            assertNull(verification.getVerificationWindow());
            
            verify(mockVerifier).addPatternVerification("test_pattern", verification);
        }

        @Test
        @DisplayName("Should configure all pattern parameters")
        void shouldConfigureAllPatternParameters() {
            // Act
            ActionPatternVerification verification = 
                new ActionPatternVerification.Builder("full_config", mockVerifier)
                    .action(ActionType.CLICK)
                    .maxAttempts(3)
                    .withBackoff(Duration.ofMillis(500))
                    .expectedSuccessRate(0.8)
                    .within(Duration.ofSeconds(10))
                    .verify();
            
            // Assert
            assertEquals(ActionType.CLICK, verification.getTargetAction());
            assertEquals(3, verification.getMaxAttempts());
            assertEquals(Duration.ofMillis(500), verification.getBackoffDuration());
            assertEquals(0.8, verification.getExpectedSuccessRate());
            assertEquals(Duration.ofSeconds(10), verification.getVerificationWindow());
        }

        @Test
        @DisplayName("Should throw exception when action not specified")
        void shouldThrowExceptionWhenActionNotSpecified() {
            // Arrange
            ActionPatternVerification.Builder builder = 
                new ActionPatternVerification.Builder("no_action", mockVerifier)
                    .maxAttempts(3);
            
            // Act & Assert
            assertThrows(IllegalStateException.class, 
                builder::verify,
                "Target action must be specified");
        }

        @Test
        @DisplayName("Should allow chaining of builder methods")
        void shouldAllowChainingOfBuilderMethods() {
            // Act & Assert - just verify it compiles and runs
            ActionPatternVerification verification = 
                new ActionPatternVerification.Builder("chain_test", mockVerifier)
                    .action(ActionType.TYPE)
                    .maxAttempts(5)
                    .withBackoff(Duration.ofSeconds(1))
                    .expectedSuccessRate(0.95)
                    .within(Duration.ofMinutes(1))
                    .verify();
            
            assertNotNull(verification);
        }
    }

    @Nested
    @DisplayName("Event Processing Tests")
    class EventProcessingTests {

        private ActionPatternVerification verification;
        
        @BeforeEach
        void setup() {
            verification = new ActionPatternVerification.Builder("test", mockVerifier)
                .action(ActionType.FIND)
                .maxAttempts(3)
                .verify();
        }

        @Test
        @DisplayName("Should record successful action attempts")
        void shouldRecordSuccessfulActionAttempts() {
            // Arrange
            // Set expected success rate so it completes on success
            verification = new ActionPatternVerification.Builder("test", mockVerifier)
                .action(ActionType.FIND)
                .maxAttempts(3)
                .expectedSuccessRate(0.5)
                .verify();
            
            MockBehaviorVerifier.ExecutionEvent event = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .action(ActionType.FIND)
                .result(successResult)
                .context(mockContext)
                .build();
            
            // Act
            verification.checkEvent(event);
            
            // Assert
            assertEquals(1, verification.getAttemptCount());
            assertEquals(VerificationResult.PASSED, verification.getResult());
            assertTrue(verification.getErrors().isEmpty());
        }

        @Test
        @DisplayName("Should record failed action attempts")
        void shouldRecordFailedActionAttempts() {
            // Arrange
            MockBehaviorVerifier.ExecutionEvent event = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .action(ActionType.FIND)
                .result(failureResult)
                .context(mockContext)
                .build();
            
            // Act
            verification.checkEvent(event);
            
            // Assert
            assertEquals(1, verification.getAttemptCount());
            // Verification remains IN_PROGRESS after first failure (waiting for retry)
            assertEquals(VerificationResult.IN_PROGRESS, verification.getResult());
        }

        @Test
        @DisplayName("Should ignore non-matching action types")
        void shouldIgnoreNonMatchingActionTypes() {
            // Arrange
            MockBehaviorVerifier.ExecutionEvent event = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .action(ActionType.CLICK)  // Different action
                .result(successResult)
                .context(mockContext)
                .build();
            
            // Act
            verification.checkEvent(event);
            
            // Assert
            assertEquals(0, verification.getAttemptCount());
            assertEquals(VerificationResult.IN_PROGRESS, verification.getResult());
        }

        @Test
        @DisplayName("Should ignore non-action events")
        void shouldIgnoreNonActionEvents() {
            // Arrange
            MockBehaviorVerifier.ExecutionEvent stateEvent = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .fromState("A")
                .toState("B")
                .context(mockContext)
                .build();
            
            // Act
            verification.checkEvent(stateEvent);
            
            // Assert
            assertEquals(0, verification.getAttemptCount());
            assertEquals(VerificationResult.IN_PROGRESS, verification.getResult());
        }

        @Test
        @DisplayName("Should ignore events after verification completes")
        void shouldIgnoreEventsAfterVerificationCompletes() {
            // Arrange
            verification = new ActionPatternVerification.Builder("test", mockVerifier)
                .action(ActionType.FIND)
                .maxAttempts(1)
                .expectedSuccessRate(0.5)  // Complete on success
                .verify();
                
            MockBehaviorVerifier.ExecutionEvent event1 = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .action(ActionType.FIND)
                .result(successResult)
                .build();
                
            MockBehaviorVerifier.ExecutionEvent event2 = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .action(ActionType.FIND)
                .result(successResult)
                .build();
            
            // Act
            verification.checkEvent(event1);  // This completes verification
            assertEquals(VerificationResult.PASSED, verification.getResult());
            int countAfterFirst = verification.getAttemptCount();
            verification.checkEvent(event2);  // This should be ignored
            
            // Assert
            assertEquals(countAfterFirst, verification.getAttemptCount());
        }
    }

    @Nested
    @DisplayName("Retry Pattern Tests")
    class RetryPatternTests {

        @Test
        @DisplayName("Should fail when max attempts exceeded")
        void shouldFailWhenMaxAttemptsExceeded() {
            // Arrange
            ActionPatternVerification verification = 
                new ActionPatternVerification.Builder("retry_test", mockVerifier)
                    .action(ActionType.CLICK)
                    .maxAttempts(2)
                    .verify();
            
            LocalDateTime now = LocalDateTime.now();
            
            // Act - send 3 attempts (exceeds max of 2)
            for (int i = 0; i < 3; i++) {
                MockBehaviorVerifier.ExecutionEvent event = MockBehaviorVerifier.ExecutionEvent.builder()
                    .timestamp(now.plusSeconds(i))
                    .action(ActionType.CLICK)
                    .result(failureResult)
                    .build();
                verification.checkEvent(event);
            }
            
            // Assert
            assertEquals(VerificationResult.FAILED, verification.getResult());
            assertFalse(verification.getErrors().isEmpty());
            assertTrue(verification.getErrors().get(0).contains("exceeded maximum attempts"));
        }

        @Test
        @DisplayName("Should validate backoff timing between retries")
        void shouldValidateBackoffTimingBetweenRetries() throws InterruptedException {
            // Arrange
            Duration backoff = Duration.ofMillis(100);
            ActionPatternVerification verification = 
                new ActionPatternVerification.Builder("backoff_test", mockVerifier)
                    .action(ActionType.FIND)
                    .withBackoff(backoff)
                    .maxAttempts(3)
                    .verify();
            
            LocalDateTime firstAttempt = LocalDateTime.now();
            
            // First attempt
            MockBehaviorVerifier.ExecutionEvent event1 = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(firstAttempt)
                .action(ActionType.FIND)
                .result(failureResult)
                .build();
            verification.checkEvent(event1);
            
            // Second attempt with correct backoff
            MockBehaviorVerifier.ExecutionEvent event2 = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(firstAttempt.plus(backoff))
                .action(ActionType.FIND)
                .result(successResult)
                .build();
            verification.checkEvent(event2);
            
            // Assert
            assertTrue(verification.getErrors().isEmpty());
        }

        @Test
        @DisplayName("Should detect too-short retry delay")
        void shouldDetectTooShortRetryDelay() {
            // Arrange
            Duration backoff = Duration.ofSeconds(1);
            ActionPatternVerification verification = 
                new ActionPatternVerification.Builder("short_delay", mockVerifier)
                    .action(ActionType.CLICK)
                    .withBackoff(backoff)
                    .verify();
            
            LocalDateTime firstAttempt = LocalDateTime.now();
            
            // First attempt
            verification.checkEvent(MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(firstAttempt)
                .action(ActionType.CLICK)
                .result(failureResult)
                .build());
            
            // Second attempt too soon (only 100ms instead of 1s)
            verification.checkEvent(MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(firstAttempt.plusNanos(100_000_000))  // 100ms
                .action(ActionType.CLICK)
                .result(failureResult)
                .build());
            
            // Assert
            assertFalse(verification.getErrors().isEmpty());
            assertTrue(verification.getErrors().get(0).contains("Retry delay too short"));
        }

        @Test
        @DisplayName("Should detect too-long retry delay")
        void shouldDetectTooLongRetryDelay() {
            // Arrange
            Duration backoff = Duration.ofMillis(100);
            ActionPatternVerification verification = 
                new ActionPatternVerification.Builder("long_delay", mockVerifier)
                    .action(ActionType.TYPE)
                    .withBackoff(backoff)
                    .verify();
            
            LocalDateTime firstAttempt = LocalDateTime.now();
            
            // First attempt
            verification.checkEvent(MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(firstAttempt)
                .action(ActionType.TYPE)
                .result(failureResult)
                .build());
            
            // Second attempt too late (200ms instead of 100ms, exceeds 10% tolerance)
            verification.checkEvent(MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(firstAttempt.plusNanos(200_000_000))  // 200ms
                .action(ActionType.TYPE)
                .result(failureResult)
                .build());
            
            // Assert
            assertFalse(verification.getErrors().isEmpty());
            assertTrue(verification.getErrors().get(0).contains("Retry delay too long"));
        }
    }

    @Nested
    @DisplayName("Success Rate Tests")
    class SuccessRateTests {

        @Test
        @DisplayName("Should verify expected success rate")
        void shouldVerifyExpectedSuccessRate() {
            // Arrange
            ActionPatternVerification verification = 
                new ActionPatternVerification.Builder("success_rate", mockVerifier)
                    .action(ActionType.FIND)
                    .expectedSuccessRate(0.75)  // 75% success rate
                    .within(Duration.ofSeconds(1))
                    .verify();
            
            LocalDateTime now = LocalDateTime.now();
            
            // Send 4 attempts: 3 successes, 1 failure = 75% success rate
            verification.checkEvent(MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(now)
                .action(ActionType.FIND)
                .result(successResult)
                .build());
                
            verification.checkEvent(MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(now.plusNanos(100_000_000))
                .action(ActionType.FIND)
                .result(failureResult)
                .build());
                
            verification.checkEvent(MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(now.plusNanos(200_000_000))
                .action(ActionType.FIND)
                .result(successResult)
                .build());
                
            // This last success should trigger completion since we meet the success rate
            verification.checkEvent(MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(now.plusNanos(300_000_000))
                .action(ActionType.FIND)
                .result(successResult)
                .build());
            
            // Assert
            assertEquals(VerificationResult.PASSED, verification.getResult());
            assertTrue(verification.getErrors().isEmpty());
        }

        @Test
        @DisplayName("Should fail when success rate too low")
        void shouldFailWhenSuccessRateTooLow() throws InterruptedException {
            // Arrange
            ActionPatternVerification verification = 
                new ActionPatternVerification.Builder("low_success", mockVerifier)
                    .action(ActionType.CLICK)
                    .expectedSuccessRate(0.8)  // 80% required
                    .within(Duration.ofMillis(100))
                    .verify();
            
            // Send one success to trigger completion check
            verification.checkEvent(MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .action(ActionType.CLICK)
                .result(successResult)  // This will trigger completion since expectedSuccessRate > 0
                .build());
            
            // Assert - 1 success out of 1 = 100% which passes the 80% requirement
            // We need to send failures first to lower the rate
            
            // Reset and try again
            verification = new ActionPatternVerification.Builder("low_success2", mockVerifier)
                .action(ActionType.CLICK)
                .expectedSuccessRate(0.8)  // 80% required
                .within(Duration.ofMillis(100))
                .verify();
            
            // Send mostly failures first
            for (int i = 0; i < 4; i++) {
                verification.checkEvent(MockBehaviorVerifier.ExecutionEvent.builder()
                    .timestamp(LocalDateTime.now())
                    .action(ActionType.CLICK)
                    .result(failureResult)
                    .build());
            }
            
            // Now send a success to trigger completion (1 out of 5 = 20% success)
            verification.checkEvent(MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .action(ActionType.CLICK)
                .result(successResult)
                .build());
            
            // Assert
            assertEquals(VerificationResult.FAILED, verification.getResult());
            assertFalse(verification.getErrors().isEmpty());
            assertTrue(verification.getErrors().get(0).contains("Success rate too low"));
        }
    }

    @Nested
    @DisplayName("Verification Window Tests")
    class VerificationWindowTests {

        @Test
        @DisplayName("Should complete when verification window expires")
        void shouldCompleteWhenVerificationWindowExpires() throws InterruptedException {
            // Arrange
            ActionPatternVerification verification = 
                new ActionPatternVerification.Builder("window_test", mockVerifier)
                    .action(ActionType.MOVE)
                    .within(Duration.ofMillis(50))
                    .verify();
            
            // Wait for window to expire
            Thread.sleep(100);
            
            // Send event after window expired
            MockBehaviorVerifier.ExecutionEvent event = MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(LocalDateTime.now())
                .action(ActionType.MOVE)
                .result(successResult)
                .build();
            
            // Act
            verification.checkEvent(event);
            
            // Assert - should have completed due to timeout
            assertNotEquals(VerificationResult.IN_PROGRESS, verification.getResult());
        }

        @Test
        @DisplayName("Should track attempts within verification window")
        void shouldTrackAttemptsWithinVerificationWindow() {
            // Arrange
            ActionPatternVerification verification = 
                new ActionPatternVerification.Builder("within_window", mockVerifier)
                    .action(ActionType.HIGHLIGHT)
                    .within(Duration.ofSeconds(10))
                    .verify();
            
            LocalDateTime now = LocalDateTime.now();
            
            // Send multiple events within window
            for (int i = 0; i < 3; i++) {
                verification.checkEvent(MockBehaviorVerifier.ExecutionEvent.builder()
                    .timestamp(now.plusNanos(i * 100_000_000L))
                    .action(ActionType.HIGHLIGHT)
                    .result(successResult)
                    .build());
            }
            
            // Assert
            assertEquals(3, verification.getAttemptCount());
        }
    }

    @Nested
    @DisplayName("Error Reporting Tests")
    class ErrorReportingTests {

        @Test
        @DisplayName("Should collect multiple errors")
        void shouldCollectMultipleErrors() throws InterruptedException {
            // Arrange
            ActionPatternVerification verification = 
                new ActionPatternVerification.Builder("multi_error", mockVerifier)
                    .action(ActionType.FIND)
                    .maxAttempts(2)
                    .withBackoff(Duration.ofMillis(100))
                    .expectedSuccessRate(0.9)
                    .within(Duration.ofMillis(50))
                    .verify();
            
            LocalDateTime now = LocalDateTime.now();
            
            // First attempt
            verification.checkEvent(MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(now)
                .action(ActionType.FIND)
                .result(failureResult)
                .build());
            
            // Second attempt with wrong timing
            verification.checkEvent(MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(now.plusNanos(10_000_000))  // Too fast
                .action(ActionType.FIND)
                .result(failureResult)
                .build());
            
            // Third attempt exceeds max
            verification.checkEvent(MockBehaviorVerifier.ExecutionEvent.builder()
                .timestamp(now.plusNanos(110_000_000))
                .action(ActionType.FIND)
                .result(failureResult)
                .build());
            
            // Assert
            List<String> errors = verification.getErrors();
            assertTrue(errors.size() >= 1);  // At least one error (timing or max attempts)
        }

        @Test
        @DisplayName("Should return defensive copy of errors")
        void shouldReturnDefensiveCopyOfErrors() {
            // Arrange
            ActionPatternVerification verification = 
                new ActionPatternVerification.Builder("defensive", mockVerifier)
                    .action(ActionType.CLICK)
                    .maxAttempts(1)
                    .verify();
            
            // Exceed max attempts to generate error
            for (int i = 0; i < 2; i++) {
                verification.checkEvent(MockBehaviorVerifier.ExecutionEvent.builder()
                    .timestamp(LocalDateTime.now())
                    .action(ActionType.CLICK)
                    .result(failureResult)
                    .build());
            }
            
            // Act
            List<String> errors1 = verification.getErrors();
            List<String> errors2 = verification.getErrors();
            
            // Assert
            assertNotSame(errors1, errors2);
            assertEquals(errors1, errors2);
            
            // Modifying returned list shouldn't affect internal state
            errors1.clear();
            assertFalse(verification.getErrors().isEmpty());
        }
    }
}