package io.github.jspinak.brobot.action.logging;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogBuilder;
import io.github.jspinak.brobot.logging.unified.LogEvent;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for ActionChainLogger following Brobot testing guidelines.
 * Tests the logging of action chains with proper API usage.
 */
@DisplayName("ActionChainLogger Tests")
public class ActionChainLoggerTest extends BrobotTestBase {
    
    @Mock
    private BrobotLogger mockLogger;
    
    @Mock
    private LogBuilder mockLogBuilder;
    
    private ActionChainLogger chainLogger;
    private AutoCloseable mockCloseable;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mockCloseable = MockitoAnnotations.openMocks(this);
        
        chainLogger = new ActionChainLogger();
        ReflectionTestUtils.setField(chainLogger, "logger", mockLogger);
        
        // Setup mock logger chain
        when(mockLogger.log()).thenReturn(mockLogBuilder);
        when(mockLogBuilder.level(any())).thenReturn(mockLogBuilder);
        when(mockLogBuilder.message(anyString())).thenReturn(mockLogBuilder);
        when(mockLogBuilder.metadata(anyString(), any())).thenReturn(mockLogBuilder);
    }
    
    @Nested
    @DisplayName("Chain Lifecycle")
    class ChainLifecycleTests {
        
        @Test
        @DisplayName("Should start action chain with unique ID")
        void shouldStartChainWithUniqueId() {
            // Act
            String chainId = chainLogger.logChainStart("TestChain", "Test chain description");
            
            // Assert
            assertNotNull(chainId);
            assertFalse(chainId.isEmpty());
            assertTrue(chainLogger.isChainActive(chainId));
            
            // Verify logging
            verify(mockLogger).log();
            verify(mockLogBuilder).message(contains("Starting Action Chain: TestChain"));
            verify(mockLogBuilder).metadata("chainId", chainId);
            verify(mockLogBuilder).metadata("description", "Test chain description");
            verify(mockLogBuilder).log();
        }
        
        @Test
        @DisplayName("Should end chain successfully")
        void shouldEndChainSuccessfully() {
            // Arrange
            String chainId = chainLogger.logChainStart("TestChain", "Description");
            
            // Act
            chainLogger.logChainEnd(chainId, true, "Chain completed successfully");
            
            // Assert
            assertFalse(chainLogger.isChainActive(chainId));
            verify(mockLogBuilder, atLeast(2)).log();
            verify(mockLogBuilder).message(contains("Action Chain Completed"));
            verify(mockLogBuilder).metadata("success", true);
        }
        
        @Test
        @DisplayName("Should handle chain failure")
        void shouldHandleChainFailure() {
            // Arrange
            String chainId = chainLogger.logChainStart("TestChain", "Description");
            
            // Add a failed step
            ActionResult failedResult = new ActionResult();
            failedResult.setSuccess(false);
            PatternFindOptions config = new PatternFindOptions.Builder().build();
            chainLogger.logStepTransition(chainId, "Step1", "Step2", config, failedResult);
            
            // Act
            chainLogger.logChainEnd(chainId, false, "Chain failed with error");
            
            // Assert
            assertFalse(chainLogger.isChainActive(chainId));
            verify(mockLogBuilder).metadata("success", false);
            // Verify failed step is logged
            verify(mockLogBuilder, atLeastOnce()).level(LogEvent.Level.WARNING);
        }
        
        @Test
        @DisplayName("Should track chain duration")
        void shouldTrackChainDuration() throws InterruptedException {
            // Arrange
            String chainId = chainLogger.logChainStart("TestChain", "Description");
            Thread.sleep(10); // Small delay to ensure duration > 0
            
            // Act
            chainLogger.logChainEnd(chainId, true, "Complete");
            
            // Assert
            ArgumentCaptor<Long> durationCaptor = ArgumentCaptor.forClass(Long.class);
            verify(mockLogBuilder).metadata(eq("durationMs"), durationCaptor.capture());
            assertTrue(durationCaptor.getValue() >= 0);
        }
    }
    
    @Nested
    @DisplayName("Step Management")
    class StepManagementTests {
        
        @Test
        @DisplayName("Should log step transition")
        void shouldLogStepTransition() {
            // Arrange
            String chainId = chainLogger.logChainStart("TestChain", "Description");
            PatternFindOptions config = new PatternFindOptions.Builder()
                .setSimilarity(0.85)
                .build();
            ActionResult result = new ActionResult();
            result.setSuccess(true);
            
            Match match = new Match.Builder()
                .setRegion(new Region(10, 10, 100, 100))
                .setSimScore(0.95)
                .build();
            result.add(match);
            
            // Act
            chainLogger.logStepTransition(chainId, "FindButton", "ClickButton", config, result);
            
            // Assert
            verify(mockLogBuilder).message(contains("FindButton -> ClickButton"));
            verify(mockLogBuilder).metadata("success", true);
            verify(mockLogBuilder).metadata("matches", 1);
            verify(mockLogBuilder).metadata("configType", "PatternFindOptions");
        }
        
        @Test
        @DisplayName("Should log simple step")
        void shouldLogSimpleStep() {
            // Arrange
            String chainId = chainLogger.logChainStart("TestChain", "Description");
            
            // Act
            chainLogger.logStep(chainId, "Performing validation");
            
            // Assert
            verify(mockLogBuilder).message(contains("Step 1: Performing validation"));
        }
        
        @Test
        @DisplayName("Should increment step numbers")
        void shouldIncrementStepNumbers() {
            // Arrange
            String chainId = chainLogger.logChainStart("TestChain", "Description");
            
            // Act - Log multiple steps
            chainLogger.logStep(chainId, "Step One");
            chainLogger.logStep(chainId, "Step Two");
            chainLogger.logStep(chainId, "Step Three");
            
            // Assert
            verify(mockLogBuilder).message(contains("Step 1: Step One"));
            verify(mockLogBuilder).message(contains("Step 2: Step Two"));
            verify(mockLogBuilder).message(contains("Step 3: Step Three"));
        }
        
        @Test
        @DisplayName("Should track steps in context")
        void shouldTrackStepsInContext() {
            // Arrange
            String chainId = chainLogger.logChainStart("TestChain", "Description");
            
            // Act
            chainLogger.logStep(chainId, "First step");
            chainLogger.logStep(chainId, "Second step");
            
            ActionChainLogger.ChainContext context = chainLogger.getChainContext(chainId);
            
            // Assert
            assertNotNull(context);
            assertEquals(2, context.getSteps().size());
            assertEquals("First step", context.getSteps().get(0).getDescription());
            assertEquals("Second step", context.getSteps().get(1).getDescription());
        }
    }
    
    @Nested
    @DisplayName("Step Logging Control")
    class StepLoggingControlTests {
        
        @Test
        @DisplayName("Should enable step logging")
        void shouldEnableStepLogging() {
            // Arrange
            String chainId = chainLogger.logChainStart("TestChain", "Description");
            
            // Act
            chainLogger.withStepLogging(chainId, true);
            chainLogger.logStep(chainId, "Visible step");
            
            // Assert
            verify(mockLogBuilder).message(contains("Visible step"));
        }
        
        @Test
        @DisplayName("Should disable step logging")
        void shouldDisableStepLogging() {
            // Arrange
            String chainId = chainLogger.logChainStart("TestChain", "Description");
            
            // Act
            chainLogger.withStepLogging(chainId, false);
            chainLogger.logStep(chainId, "Hidden step");
            
            // Assert
            // The start chain message should be logged
            verify(mockLogBuilder).message(contains("Starting Action Chain"));
            // But the step should not be logged
            verify(mockLogBuilder, never()).message(contains("Hidden step"));
        }
        
        @Test
        @DisplayName("Should support method chaining")
        void shouldSupportMethodChaining() {
            // Arrange
            String chainId = chainLogger.logChainStart("TestChain", "Description");
            
            // Act
            ActionChainLogger returnedLogger = chainLogger.withStepLogging(chainId, true);
            
            // Assert
            assertSame(chainLogger, returnedLogger);
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Should handle invalid chain ID for step transition")
        void shouldHandleInvalidChainIdForStepTransition() {
            // Arrange
            String invalidId = UUID.randomUUID().toString();
            ActionResult result = new ActionResult();
            PatternFindOptions config = new PatternFindOptions.Builder().build();
            
            // Act
            chainLogger.logStepTransition(invalidId, "Step1", "Step2", config, result);
            
            // Assert
            verify(mockLogBuilder).level(LogEvent.Level.WARNING);
            verify(mockLogBuilder).message(contains("No active chain found"));
        }
        
        @Test
        @DisplayName("Should handle invalid chain ID for end")
        void shouldHandleInvalidChainIdForEnd() {
            // Arrange
            String invalidId = UUID.randomUUID().toString();
            
            // Act
            chainLogger.logChainEnd(invalidId, true, "Summary");
            
            // Assert
            verify(mockLogBuilder).level(LogEvent.Level.WARNING);
            verify(mockLogBuilder).message(contains("No active chain found"));
        }
        
        @Test
        @DisplayName("Should handle null logger gracefully")
        void shouldHandleNullLogger() {
            // Arrange
            ActionChainLogger loggerWithoutBrobot = new ActionChainLogger();
            ReflectionTestUtils.setField(loggerWithoutBrobot, "logger", null);
            
            // Act - Should not throw exceptions
            assertDoesNotThrow(() -> {
                String chainId = loggerWithoutBrobot.logChainStart("Test", "Description");
                loggerWithoutBrobot.logStep(chainId, "Step");
                loggerWithoutBrobot.logChainEnd(chainId, true, "Done");
            });
        }
        
        @Test
        @DisplayName("Should handle null config and result")
        void shouldHandleNullConfigAndResult() {
            // Arrange
            String chainId = chainLogger.logChainStart("TestChain", "Description");
            
            // Act
            chainLogger.logStepTransition(chainId, "Step1", "Step2", null, null);
            
            // Assert
            verify(mockLogBuilder).metadata("configType", "null");
            verify(mockLogBuilder).metadata("success", false);
            verify(mockLogBuilder).metadata("matches", 0);
        }
    }
    
    @Nested
    @DisplayName("Multiple Chains")
    class MultipleChainsTests {
        
        @Test
        @DisplayName("Should handle multiple concurrent chains")
        void shouldHandleMultipleConcurrentChains() {
            // Act
            String chain1 = chainLogger.logChainStart("Chain1", "First chain");
            String chain2 = chainLogger.logChainStart("Chain2", "Second chain");
            String chain3 = chainLogger.logChainStart("Chain3", "Third chain");
            
            // Assert
            assertNotEquals(chain1, chain2);
            assertNotEquals(chain2, chain3);
            assertNotEquals(chain1, chain3);
            
            assertTrue(chainLogger.isChainActive(chain1));
            assertTrue(chainLogger.isChainActive(chain2));
            assertTrue(chainLogger.isChainActive(chain3));
            
            // Clean up one chain
            chainLogger.logChainEnd(chain2, true, "Done");
            
            assertTrue(chainLogger.isChainActive(chain1));
            assertFalse(chainLogger.isChainActive(chain2));
            assertTrue(chainLogger.isChainActive(chain3));
        }
        
        @Test
        @DisplayName("Should isolate steps between chains")
        void shouldIsolateStepsBetweenChains() {
            // Arrange
            String chain1 = chainLogger.logChainStart("Chain1", "First");
            String chain2 = chainLogger.logChainStart("Chain2", "Second");
            
            // Act
            chainLogger.logStep(chain1, "Chain1 Step");
            chainLogger.logStep(chain2, "Chain2 Step");
            
            // Assert
            ActionChainLogger.ChainContext context1 = chainLogger.getChainContext(chain1);
            ActionChainLogger.ChainContext context2 = chainLogger.getChainContext(chain2);
            
            assertEquals(1, context1.getSteps().size());
            assertEquals(1, context2.getSteps().size());
            assertEquals("Chain1 Step", context1.getSteps().get(0).getDescription());
            assertEquals("Chain2 Step", context2.getSteps().get(0).getDescription());
        }
    }
    
    @Nested
    @DisplayName("Cleanup Operations")
    class CleanupOperationsTests {
        
        @Test
        @DisplayName("Should clear all active chains")
        void shouldClearAllActiveChains() {
            // Arrange
            String chain1 = chainLogger.logChainStart("Chain1", "First");
            String chain2 = chainLogger.logChainStart("Chain2", "Second");
            
            // Act
            chainLogger.clearAllChains();
            
            // Assert
            assertFalse(chainLogger.isChainActive(chain1));
            assertFalse(chainLogger.isChainActive(chain2));
        }
        
        @Test
        @DisplayName("Should remove chain after end")
        void shouldRemoveChainAfterEnd() {
            // Arrange
            String chainId = chainLogger.logChainStart("TestChain", "Description");
            assertTrue(chainLogger.isChainActive(chainId));
            
            // Act
            chainLogger.logChainEnd(chainId, true, "Done");
            
            // Assert
            assertFalse(chainLogger.isChainActive(chainId));
            assertNull(chainLogger.getChainContext(chainId));
        }
    }
}