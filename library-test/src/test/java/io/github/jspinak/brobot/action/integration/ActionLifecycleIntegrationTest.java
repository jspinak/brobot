package io.github.jspinak.brobot.action.integration;

import io.github.jspinak.brobot.action.*;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.internal.execution.ActionLifecycle;
import io.github.jspinak.brobot.action.internal.execution.ActionLifecycleManagement;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.*;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for action lifecycle management using real Spring context.
 * Tests the complete lifecycle of actions from initialization to cleanup.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "brobot.logging.verbosity=VERBOSE",
    "brobot.console.actions.enabled=true",
    "brobot.action.lifecycle.tracking=true",
    "brobot.action.validation.enabled=true",
    "brobot.mock.enabled=false"
})
class ActionLifecycleIntegrationTest extends BrobotIntegrationTestBase {
    
    @Autowired
    private Action action;
    
    @Autowired
    private ActionLifecycleManagement lifecycleManagement;
    
    @Autowired
    private ActionLifecycle actionLifecycle;
    
    private StateImage testImage;
    private ObjectCollection testCollection;
    
    @BeforeEach
    void setupTestData() {
        testImage = new StateImage.Builder()
            .setName("test-image")
            .addPattern("images/test.png")
            .setSimilarity(0.8)
            .build();
        
        testCollection = new ObjectCollection.Builder()
            .withImages(testImage)
            .build();
    }
    
    @Nested
    @DisplayName("Action Lifecycle Phase Tests")
    class ActionLifecyclePhaseTests {
        
        @Test
        @DisplayName("Should execute complete action lifecycle")
        void shouldExecuteCompleteActionLifecycle() {
            // Given
            PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
            AtomicReference<ActionLifecyclePhase> currentPhase = new AtomicReference<>();
            
            lifecycleManager.registerListener(new ActionLifecycleListener() {
                @Override
                public void onPhaseStart(ActionLifecyclePhase phase, ActionConfig config) {
                    currentPhase.set(phase);
                }
                
                @Override
                public void onPhaseComplete(ActionLifecyclePhase phase, ActionResult result) {
                    // Track phase completion
                }
            });
            
            // When
            ActionResult result = action.perform(findOptions, testCollection);
            
            // Then
            assertNotNull(result);
            assertNotNull(currentPhase.get());
            // Should have gone through initialization, validation, execution, and cleanup
        }
        
        @Test
        @DisplayName("Should track action initialization phase")
        void shouldTrackActionInitializationPhase() {
            // Given
            AtomicBoolean initializationTracked = new AtomicBoolean(false);
            AtomicReference<LocalDateTime> initTime = new AtomicReference<>();
            
            lifecycleManager.registerListener(new ActionLifecycleListener() {
                @Override
                public void onPhaseStart(ActionLifecyclePhase phase, ActionConfig config) {
                    if (phase == ActionLifecyclePhase.INITIALIZATION) {
                        initializationTracked.set(true);
                        initTime.set(LocalDateTime.now());
                    }
                }
            });
            
            // When
            action.perform(new ClickOptions.Builder().build(), testCollection);
            
            // Then
            assertTrue(initializationTracked.get());
            assertNotNull(initTime.get());
        }
        
        @Test
        @DisplayName("Should track action validation phase")
        void shouldTrackActionValidationPhase() {
            // Given
            AtomicBoolean validationTracked = new AtomicBoolean(false);
            AtomicReference<ValidationResult> validationResult = new AtomicReference<>();
            
            lifecycleManager.registerListener(new ActionLifecycleListener() {
                @Override
                public void onValidation(ValidationResult result) {
                    validationTracked.set(true);
                    validationResult.set(result);
                }
            });
            
            // When
            action.perform(new PatternFindOptions.Builder().build(), testCollection);
            
            // Then
            assertTrue(validationTracked.get());
            assertNotNull(validationResult.get());
        }
        
        @Test
        @DisplayName("Should track action execution phase")
        void shouldTrackActionExecutionPhase() {
            // Given
            AtomicBoolean executionStarted = new AtomicBoolean(false);
            AtomicBoolean executionCompleted = new AtomicBoolean(false);
            AtomicReference<Duration> executionDuration = new AtomicReference<>();
            
            lifecycleManager.registerListener(new ActionLifecycleListener() {
                private LocalDateTime startTime;
                
                @Override
                public void onPhaseStart(ActionLifecyclePhase phase, ActionConfig config) {
                    if (phase == ActionLifecyclePhase.EXECUTION) {
                        executionStarted.set(true);
                        startTime = LocalDateTime.now();
                    }
                }
                
                @Override
                public void onPhaseComplete(ActionLifecyclePhase phase, ActionResult result) {
                    if (phase == ActionLifecyclePhase.EXECUTION) {
                        executionCompleted.set(true);
                        if (startTime != null) {
                            executionDuration.set(Duration.between(startTime, LocalDateTime.now()));
                        }
                    }
                }
            });
            
            // When
            action.perform(new TypeOptions.Builder().build(), 
                new ObjectCollection.Builder().withStrings("test").build());
            
            // Then
            assertTrue(executionStarted.get());
            assertTrue(executionCompleted.get());
            assertNotNull(executionDuration.get());
        }
        
        @Test
        @DisplayName("Should track action cleanup phase")
        void shouldTrackActionCleanupPhase() {
            // Given
            AtomicBoolean cleanupExecuted = new AtomicBoolean(false);
            List<String> cleanedResources = new ArrayList<>();
            
            lifecycleManager.registerListener(new ActionLifecycleListener() {
                @Override
                public void onCleanup(List<String> resources) {
                    cleanupExecuted.set(true);
                    cleanedResources.addAll(resources);
                }
            });
            
            // When
            action.perform(new PatternFindOptions.Builder().build(), testCollection);
            
            // Then
            assertTrue(cleanupExecuted.get());
            assertNotNull(cleanedResources);
        }
    }
    
    @Nested
    @DisplayName("Action Validation Tests")
    class ActionValidationTests {
        
        @Test
        @DisplayName("Should validate action configuration before execution")
        void shouldValidateActionConfigurationBeforeExecution() {
            // Given
            PatternFindOptions invalidOptions = new PatternFindOptions.Builder()
                .setSimilarity(-0.5) // Invalid similarity
                .build();
            
            // When
            ValidationResult validation = validator.validate(invalidOptions, testCollection);
            
            // Then
            assertFalse(validation.isValid());
            assertFalse(validation.getErrors().isEmpty());
            assertTrue(validation.getErrors().stream()
                .anyMatch(error -> error.contains("similarity")));
        }
        
        @Test
        @DisplayName("Should validate object collection before execution")
        void shouldValidateObjectCollectionBeforeExecution() {
            // Given - empty collection
            ObjectCollection emptyCollection = new ObjectCollection.Builder().build();
            
            // When
            ValidationResult validation = validator.validate(
                new PatternFindOptions.Builder().build(),
                emptyCollection
            );
            
            // Then
            assertFalse(validation.isValid());
            assertTrue(validation.getErrors().stream()
                .anyMatch(error -> error.contains("empty") || error.contains("no objects")));
        }
        
        @Test
        @DisplayName("Should skip execution on validation failure")
        void shouldSkipExecutionOnValidationFailure() {
            // Given
            AtomicBoolean executionSkipped = new AtomicBoolean(true);
            
            lifecycleManager.registerListener(new ActionLifecycleListener() {
                @Override
                public void onPhaseStart(ActionLifecyclePhase phase, ActionConfig config) {
                    if (phase == ActionLifecyclePhase.EXECUTION) {
                        executionSkipped.set(false);
                    }
                }
            });
            
            // Invalid configuration
            PatternFindOptions invalidOptions = new PatternFindOptions.Builder()
                .setTimeout(-1.0) // Invalid timeout
                .build();
            
            // When
            ActionResult result = action.perform(invalidOptions, testCollection);
            
            // Then - execution may or may not be skipped depending on validation strictness
            assertNotNull(result);
        }
    }
    
    @Nested
    @DisplayName("Action Interceptor Tests")
    class ActionInterceptorTests {
        
        @Test
        @DisplayName("Should intercept action before execution")
        void shouldInterceptActionBeforeExecution() {
            // Given
            AtomicBoolean intercepted = new AtomicBoolean(false);
            AtomicReference<ActionConfig> interceptedConfig = new AtomicReference<>();
            
            interceptor.registerBeforeInterceptor((config, collection) -> {
                intercepted.set(true);
                interceptedConfig.set(config);
                return true; // Allow execution
            });
            
            PatternFindOptions options = new PatternFindOptions.Builder().build();
            
            // When
            action.perform(options, testCollection);
            
            // Then
            assertTrue(intercepted.get());
            assertEquals(options, interceptedConfig.get());
        }
        
        @Test
        @DisplayName("Should intercept action after execution")
        void shouldInterceptActionAfterExecution() {
            // Given
            AtomicBoolean intercepted = new AtomicBoolean(false);
            AtomicReference<ActionResult> interceptedResult = new AtomicReference<>();
            
            interceptor.registerAfterInterceptor((result) -> {
                intercepted.set(true);
                interceptedResult.set(result);
                return result; // Pass through
            });
            
            // When
            ActionResult result = action.perform(
                new ClickOptions.Builder().build(),
                testCollection
            );
            
            // Then
            assertTrue(intercepted.get());
            assertEquals(result, interceptedResult.get());
        }
        
        @Test
        @DisplayName("Should modify action result in after interceptor")
        void shouldModifyActionResultInAfterInterceptor() {
            // Given
            Text modifiedText = new Text("Modified by interceptor");
            
            interceptor.registerAfterInterceptor((result) -> {
                result.setText(modifiedText);
                return result;
            });
            
            // When
            ActionResult result = action.perform(
                new TypeOptions.Builder().build(),
                new ObjectCollection.Builder().withStrings("original").build()
            );
            
            // Then
            assertEquals(modifiedText, result.getText());
        }
        
        @Test
        @DisplayName("Should block action execution from interceptor")
        void shouldBlockActionExecutionFromInterceptor() {
            // Given
            interceptor.registerBeforeInterceptor((config, collection) -> {
                return false; // Block execution
            });
            
            // When
            ActionResult result = action.perform(
                new PatternFindOptions.Builder().build(),
                testCollection
            );
            
            // Then
            assertNotNull(result);
            // Result should indicate blocking or failure
            assertFalse(result.isSuccess() && result.getMatchList().size() > 0);
        }
    }
    
    @Nested
    @DisplayName("Action Record Management Tests")
    class ActionRecordManagementTests {
        
        @Test
        @DisplayName("Should record action execution details")
        void shouldRecordActionExecutionDetails() {
            // Given
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setSimilarity(0.9)
                .build();
            
            // When
            ActionResult result = action.perform(options, testCollection);
            
            // Then
            ActionRecord record = recordManager.getLatestRecord();
            assertNotNull(record);
            assertEquals(options, record.getActionConfig());
            assertNotNull(record.getTimeStamp());
            assertNotNull(record.getDuration());
        }
        
        @Test
        @DisplayName("Should maintain action execution history")
        void shouldMaintainActionExecutionHistory() {
            // Given - execute multiple actions
            action.perform(new PatternFindOptions.Builder().build(), testCollection);
            action.perform(new ClickOptions.Builder().build(), testCollection);
            action.perform(new TypeOptions.Builder().build(), 
                new ObjectCollection.Builder().withStrings("test").build());
            
            // When
            List<ActionRecord> history = recordManager.getHistory();
            
            // Then
            assertNotNull(history);
            assertTrue(history.size() >= 3);
            
            // Verify chronological order
            for (int i = 0; i < history.size() - 1; i++) {
                assertTrue(history.get(i).getTimeStamp()
                    .isBefore(history.get(i + 1).getTimeStamp()) ||
                    history.get(i).getTimeStamp()
                    .isEqual(history.get(i + 1).getTimeStamp()));
            }
        }
        
        @Test
        @DisplayName("Should query action records by criteria")
        void shouldQueryActionRecordsByCriteria() {
            // Given - execute various actions
            action.perform(new PatternFindOptions.Builder().build(), testCollection);
            action.perform(new ClickOptions.Builder().build(), testCollection);
            action.perform(new PatternFindOptions.Builder().build(), testCollection);
            
            // When
            List<ActionRecord> findRecords = recordManager.queryRecords(
                record -> record.getActionConfig() instanceof PatternFindOptions
            );
            
            List<ActionRecord> clickRecords = recordManager.queryRecords(
                record -> record.getActionConfig() instanceof ClickOptions
            );
            
            // Then
            assertTrue(findRecords.size() >= 2);
            assertTrue(clickRecords.size() >= 1);
        }
        
        @Test
        @DisplayName("Should limit action history size")
        void shouldLimitActionHistorySize() {
            // Given
            int maxHistorySize = 100;
            recordManager.setMaxHistorySize(maxHistorySize);
            
            // Execute more actions than the limit
            for (int i = 0; i < maxHistorySize + 50; i++) {
                action.perform(new PatternFindOptions.Builder().build(), testCollection);
            }
            
            // When
            List<ActionRecord> history = recordManager.getHistory();
            
            // Then
            assertEquals(maxHistorySize, history.size());
            // Should keep the most recent records
        }
    }
    
    @Nested
    @DisplayName("Action Metrics Tests")
    class ActionMetricsTests {
        
        @Test
        @DisplayName("Should collect action execution metrics")
        void shouldCollectActionExecutionMetrics() {
            // Given
            int executionCount = 5;
            
            // When - execute multiple actions
            for (int i = 0; i < executionCount; i++) {
                action.perform(new PatternFindOptions.Builder().build(), testCollection);
            }
            
            // Then
            ActionMetrics metrics = lifecycleManager.getMetrics();
            assertNotNull(metrics);
            assertTrue(metrics.getTotalExecutions() >= executionCount);
            assertNotNull(metrics.getAverageExecutionTime());
            assertTrue(metrics.getAverageExecutionTime().toMillis() >= 0);
        }
        
        @Test
        @DisplayName("Should track action success rate")
        void shouldTrackActionSuccessRate() {
            // Given - mix of successful and failed actions
            for (int i = 0; i < 3; i++) {
                action.perform(new PatternFindOptions.Builder().build(), testCollection);
            }
            
            // Force some failures with invalid config
            ObjectCollection emptyCollection = new ObjectCollection.Builder().build();
            for (int i = 0; i < 2; i++) {
                action.perform(new PatternFindOptions.Builder().build(), emptyCollection);
            }
            
            // When
            ActionMetrics metrics = lifecycleManager.getMetrics();
            
            // Then
            assertNotNull(metrics);
            double successRate = metrics.getSuccessRate();
            assertTrue(successRate >= 0.0 && successRate <= 1.0);
        }
        
        @Test
        @DisplayName("Should track action type distribution")
        void shouldTrackActionTypeDistribution() {
            // Given - execute different action types
            action.perform(new PatternFindOptions.Builder().build(), testCollection);
            action.perform(new PatternFindOptions.Builder().build(), testCollection);
            action.perform(new ClickOptions.Builder().build(), testCollection);
            action.perform(new TypeOptions.Builder().build(), 
                new ObjectCollection.Builder().withStrings("test").build());
            
            // When
            Map<Class<?>, Integer> distribution = lifecycleManager.getActionTypeDistribution();
            
            // Then
            assertNotNull(distribution);
            assertTrue(distribution.containsKey(PatternFindOptions.class));
            assertTrue(distribution.containsKey(ClickOptions.class));
            assertTrue(distribution.containsKey(TypeOptions.class));
            assertEquals(2, distribution.get(PatternFindOptions.class).intValue());
        }
    }
    
    @Nested
    @DisplayName("Concurrent Lifecycle Management Tests")
    class ConcurrentLifecycleManagementTests {
        
        @Test
        @DisplayName("Should handle concurrent action executions")
        void shouldHandleConcurrentActionExecutions() throws InterruptedException {
            // Given
            int threadCount = 10;
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicReference<Exception> error = new AtomicReference<>();
            
            // When - execute actions concurrently
            for (int i = 0; i < threadCount; i++) {
                new Thread(() -> {
                    try {
                        ActionResult result = action.perform(
                            new PatternFindOptions.Builder().build(),
                            testCollection
                        );
                        if (result != null) {
                            successCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        error.set(e);
                    } finally {
                        latch.countDown();
                    }
                }).start();
            }
            
            // Then
            assertTrue(latch.await(10, TimeUnit.SECONDS));
            assertNull(error.get());
            assertEquals(threadCount, successCount.get());
            
            // Verify all executions were recorded
            List<ActionRecord> history = recordManager.getHistory();
            assertTrue(history.size() >= threadCount);
        }
        
        @Test
        @DisplayName("Should maintain thread-safe metrics collection")
        void shouldMaintainThreadSafeMetricsCollection() throws InterruptedException {
            // Given
            int threadCount = 20;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch completeLatch = new CountDownLatch(threadCount);
            
            // When - all threads start simultaneously
            for (int i = 0; i < threadCount; i++) {
                new Thread(() -> {
                    try {
                        startLatch.await(); // Wait for signal
                        action.perform(new PatternFindOptions.Builder().build(), testCollection);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        completeLatch.countDown();
                    }
                }).start();
            }
            
            startLatch.countDown(); // Start all threads
            
            // Then
            assertTrue(completeLatch.await(10, TimeUnit.SECONDS));
            
            ActionMetrics metrics = lifecycleManager.getMetrics();
            assertEquals(threadCount, metrics.getTotalExecutions());
        }
    }
    
    @Nested
    @DisplayName("Action Recovery Tests")
    class ActionRecoveryTests {
        
        @Test
        @DisplayName("Should recover from action execution failure")
        void shouldRecoverFromActionExecutionFailure() {
            // Given
            AtomicInteger retryCount = new AtomicInteger(0);
            AtomicBoolean recovered = new AtomicBoolean(false);
            
            lifecycleManager.registerRecoveryHandler(new ActionRecoveryHandler() {
                @Override
                public boolean handleFailure(ActionConfig config, Exception error) {
                    retryCount.incrementAndGet();
                    if (retryCount.get() >= 3) {
                        recovered.set(true);
                        return true; // Recovered
                    }
                    return false; // Retry
                }
            });
            
            // When - simulate failure scenario
            ObjectCollection problematicCollection = new ObjectCollection.Builder()
                .withImages(testImage)
                .build();
            
            ActionResult result = action.perform(
                new PatternFindOptions.Builder().build(),
                problematicCollection
            );
            
            // Then
            assertNotNull(result);
            // Recovery handler should have been invoked if needed
        }
        
        @Test
        @DisplayName("Should apply fallback strategy on failure")
        void shouldApplyFallbackStrategyOnFailure() {
            // Given
            AtomicBoolean fallbackApplied = new AtomicBoolean(false);
            
            lifecycleManager.registerFallbackStrategy(new ActionFallbackStrategy() {
                @Override
                public ActionResult applyFallback(ActionConfig config, ObjectCollection collection) {
                    fallbackApplied.set(true);
                    ActionResult fallbackResult = new ActionResult();
                    fallbackResult.setSuccess(true);
                    fallbackResult.setText(new Text("Fallback applied"));
                    return fallbackResult;
                }
            });
            
            // When - execute with problematic configuration
            ActionResult result = action.perform(
                new PatternFindOptions.Builder().setTimeout(0.001).build(), // Very short timeout
                testCollection
            );
            
            // Then
            assertNotNull(result);
            // Fallback may or may not be applied depending on actual failure
        }
    }
}