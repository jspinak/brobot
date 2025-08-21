package io.github.jspinak.brobot.action.internal.execution;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test suite for ActionExecution class.
 * Tests the execution flow of actions including pre/post conditions and lifecycle.
 */
@DisplayName("ActionExecution Tests")
public class ActionExecutionTest extends BrobotTestBase {

    @Mock
    private BiFunction<ActionConfig, ObjectCollection, ActionResult> actionFunction;
    
    @Mock
    private ActionConfig actionConfig;
    
    @Mock
    private ObjectCollection objectCollection;
    
    private ActionExecution actionExecution;
    private AutoCloseable mockCloseable;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mockCloseable = MockitoAnnotations.openMocks(this);
        actionExecution = new ActionExecution();
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (mockCloseable != null) {
            mockCloseable.close();
        }
    }
    
    @Nested
    @DisplayName("Basic Execution")
    class BasicExecution {
        
        @Test
        @DisplayName("Should execute action successfully")
        void shouldExecuteActionSuccessfully() {
            // Arrange
            ActionResult expectedResult = new ActionResult();
            expectedResult.setSuccess(true);
            when(actionFunction.apply(any(), any())).thenReturn(expectedResult);
            
            // Act
            ActionResult result = actionExecution.execute(actionFunction, actionConfig, objectCollection);
            
            // Assert
            assertNotNull(result);
            assertTrue(result.isSuccess());
            verify(actionFunction).apply(actionConfig, objectCollection);
        }
        
        @Test
        @DisplayName("Should handle action failure")
        void shouldHandleActionFailure() {
            // Arrange
            ActionResult expectedResult = new ActionResult();
            expectedResult.setSuccess(false);
            when(actionFunction.apply(any(), any())).thenReturn(expectedResult);
            
            // Act
            ActionResult result = actionExecution.execute(actionFunction, actionConfig, objectCollection);
            
            // Assert
            assertNotNull(result);
            assertFalse(result.isSuccess());
        }
        
        @Test
        @DisplayName("Should capture execution duration")
        void shouldCaptureExecutionDuration() {
            // Arrange
            ActionResult expectedResult = new ActionResult();
            when(actionFunction.apply(any(), any())).thenAnswer(invocation -> {
                Thread.sleep(50); // Simulate some processing time
                return expectedResult;
            });
            
            // Act
            ActionResult result = actionExecution.execute(actionFunction, actionConfig, objectCollection);
            
            // Assert
            assertNotNull(result);
            assertNotNull(result.getDuration());
            assertTrue(result.getDuration().toMillis() >= 50);
        }
    }
    
    @Nested
    @DisplayName("Execution with Matches")
    class ExecutionWithMatches {
        
        @Test
        @DisplayName("Should preserve matches in result")
        void shouldPreserveMatchesInResult() {
            // Arrange
            Match match1 = mock(Match.class);
            Match match2 = mock(Match.class);
            List<Match> matches = Arrays.asList(match1, match2);
            
            ActionResult expectedResult = new ActionResult();
            expectedResult.setMatchList(matches);
            when(actionFunction.apply(any(), any())).thenReturn(expectedResult);
            
            // Act
            ActionResult result = actionExecution.execute(actionFunction, actionConfig, objectCollection);
            
            // Assert
            assertNotNull(result);
            assertEquals(2, result.getMatchList().size());
            assertTrue(result.getMatchList().contains(match1));
            assertTrue(result.getMatchList().contains(match2));
        }
        
        @Test
        @DisplayName("Should handle empty matches")
        void shouldHandleEmptyMatches() {
            // Arrange
            ActionResult expectedResult = new ActionResult();
            expectedResult.setMatchList(Collections.emptyList());
            when(actionFunction.apply(any(), any())).thenReturn(expectedResult);
            
            // Act
            ActionResult result = actionExecution.execute(actionFunction, actionConfig, objectCollection);
            
            // Assert
            assertNotNull(result);
            assertTrue(result.getMatchList().isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Should handle runtime exception in action")
        void shouldHandleRuntimeException() {
            // Arrange
            when(actionFunction.apply(any(), any())).thenThrow(new RuntimeException("Test error"));
            
            // Act
            ActionResult result = actionExecution.execute(actionFunction, actionConfig, objectCollection);
            
            // Assert
            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(result.getOutputText().contains("Test error") || result.isEmpty());
        }
        
        @Test
        @DisplayName("Should handle null action function")
        void shouldHandleNullActionFunction() {
            // Act
            ActionResult result = actionExecution.execute(null, actionConfig, objectCollection);
            
            // Assert
            assertNotNull(result);
            assertFalse(result.isSuccess());
        }
        
        @Test
        @DisplayName("Should handle null parameters")
        void shouldHandleNullParameters() {
            // Arrange
            ActionResult expectedResult = new ActionResult();
            when(actionFunction.apply(null, null)).thenReturn(expectedResult);
            
            // Act
            ActionResult result = actionExecution.execute(actionFunction, null, null);
            
            // Assert
            assertNotNull(result);
            verify(actionFunction).apply(null, null);
        }
    }
    
    @Nested
    @DisplayName("Lifecycle Management")
    class LifecycleManagement {
        
        @Test
        @DisplayName("Should execute pre-action hooks")
        void shouldExecutePreActionHooks() {
            // Arrange
            ActionResult expectedResult = new ActionResult();
            when(actionFunction.apply(any(), any())).thenReturn(expectedResult);
            
            boolean[] hookCalled = {false};
            actionExecution = new ActionExecution() {
                @Override
                protected void preExecution(ActionConfig config, ObjectCollection objects) {
                    hookCalled[0] = true;
                    super.preExecution(config, objects);
                }
            };
            
            // Act
            actionExecution.execute(actionFunction, actionConfig, objectCollection);
            
            // Assert
            assertTrue(hookCalled[0]);
        }
        
        @Test
        @DisplayName("Should execute post-action hooks")
        void shouldExecutePostActionHooks() {
            // Arrange
            ActionResult expectedResult = new ActionResult();
            when(actionFunction.apply(any(), any())).thenReturn(expectedResult);
            
            boolean[] hookCalled = {false};
            actionExecution = new ActionExecution() {
                @Override
                protected void postExecution(ActionResult result) {
                    hookCalled[0] = true;
                    super.postExecution(result);
                }
            };
            
            // Act
            actionExecution.execute(actionFunction, actionConfig, objectCollection);
            
            // Assert
            assertTrue(hookCalled[0]);
        }
        
        @Test
        @DisplayName("Should execute post-action hooks even on failure")
        void shouldExecutePostActionHooksOnFailure() {
            // Arrange
            when(actionFunction.apply(any(), any())).thenThrow(new RuntimeException("Test error"));
            
            boolean[] hookCalled = {false};
            actionExecution = new ActionExecution() {
                @Override
                protected void postExecution(ActionResult result) {
                    hookCalled[0] = true;
                    super.postExecution(result);
                }
            };
            
            // Act
            actionExecution.execute(actionFunction, actionConfig, objectCollection);
            
            // Assert
            assertTrue(hookCalled[0]);
        }
    }
    
    @Nested
    @DisplayName("Execution Context")
    class ExecutionContext {
        
        @Test
        @DisplayName("Should maintain execution context")
        void shouldMaintainExecutionContext() {
            // Arrange
            ActionResult expectedResult = new ActionResult();
            expectedResult.setActionDescription("Test action");
            when(actionFunction.apply(any(), any())).thenReturn(expectedResult);
            when(actionConfig.toString()).thenReturn("TestConfig");
            
            // Act
            ActionResult result = actionExecution.execute(actionFunction, actionConfig, objectCollection);
            
            // Assert
            assertNotNull(result);
            assertEquals("Test action", result.getActionDescription());
        }
        
        @Test
        @DisplayName("Should track action metadata")
        void shouldTrackActionMetadata() {
            // Arrange
            ActionResult expectedResult = new ActionResult();
            when(actionFunction.apply(any(), any())).thenReturn(expectedResult);
            
            PatternFindOptions options = new PatternFindOptions.Builder().build();
            when(actionConfig.getClass()).thenReturn((Class) options.getClass());
            
            // Act
            ActionResult result = actionExecution.execute(actionFunction, actionConfig, objectCollection);
            
            // Assert
            assertNotNull(result);
            assertNotNull(result.getDuration());
        }
    }
    
    @Nested
    @DisplayName("Performance")
    class Performance {
        
        @ParameterizedTest
        @ValueSource(ints = {10, 50, 100, 200})
        @DisplayName("Should handle various execution durations")
        void shouldHandleVariousExecutionDurations(int delayMs) {
            // Arrange
            ActionResult expectedResult = new ActionResult();
            when(actionFunction.apply(any(), any())).thenAnswer(invocation -> {
                Thread.sleep(delayMs);
                return expectedResult;
            });
            
            // Act
            long startTime = System.currentTimeMillis();
            ActionResult result = actionExecution.execute(actionFunction, actionConfig, objectCollection);
            long endTime = System.currentTimeMillis();
            
            // Assert
            assertNotNull(result);
            long actualDuration = endTime - startTime;
            assertTrue(actualDuration >= delayMs, 
                "Execution took " + actualDuration + "ms, expected at least " + delayMs + "ms");
        }
        
        @Test
        @DisplayName("Should execute quickly for no-op action")
        void shouldExecuteQuicklyForNoOp() {
            // Arrange
            ActionResult expectedResult = new ActionResult();
            when(actionFunction.apply(any(), any())).thenReturn(expectedResult);
            
            // Act
            long startTime = System.currentTimeMillis();
            ActionResult result = actionExecution.execute(actionFunction, actionConfig, objectCollection);
            long endTime = System.currentTimeMillis();
            
            // Assert
            assertNotNull(result);
            assertTrue(endTime - startTime < 100, "No-op action should execute in less than 100ms");
        }
    }
}