package io.github.jspinak.brobot.navigation.path;

import io.github.jspinak.brobot.model.state.special.SpecialStateType;
import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.navigation.transition.TransitionExecutor;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.navigation.transition.TransitionConditionPackager;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for PathTraverser - executes navigation paths.
 * Tests path execution, failure detection, and transition completion.
 */
@DisplayName("PathTraverser Tests")
class PathTraverserTest extends BrobotTestBase {
    
    private PathTraverser pathTraverser;
    
    @Mock
    private TransitionExecutor mockTransitionExecutor;
    
    @Mock
    private StateTransitionService mockStateTransitionService;
    
    @Mock
    private TransitionConditionPackager mockConditionPackager;
    
    @Mock
    private StateTransitions mockStateTransitions;
    
    @Mock
    private StateTransition mockTransition;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        pathTraverser = new PathTraverser(
            mockTransitionExecutor,
            mockStateTransitionService,
            mockConditionPackager
        );
    }
    
    @Nested
    @DisplayName("Path Traversal")
    class PathTraversal {
        
        @Test
        @DisplayName("Should traverse simple path successfully")
        void testTraverseSimplePath() {
            // Arrange
            Path path = new Path();
            path.getStates().add(1L);
            path.getStates().add(2L);
            path.getStates().add(3L);
            
            when(mockTransitionExecutor.go(1L, 2L)).thenReturn(true);
            when(mockTransitionExecutor.go(2L, 3L)).thenReturn(true);
            
            // Act
            boolean result = pathTraverser.traverse(path);
            
            // Assert
            assertTrue(result);
            verify(mockTransitionExecutor).go(1L, 2L);
            verify(mockTransitionExecutor).go(2L, 3L);
            assertEquals(SpecialStateType.NULL.getId(), pathTraverser.getFailedTransitionStartState());
        }
        
        @Test
        @DisplayName("Should handle single state path")
        void testSingleStatePath() {
            // Arrange
            Path path = new Path();
            path.getStates().add(1L);
            
            // Act
            boolean result = pathTraverser.traverse(path);
            
            // Assert
            assertTrue(result); // No transitions to execute
            verify(mockTransitionExecutor, never()).go(anyLong(), anyLong());
        }
        
        @Test
        @DisplayName("Should handle empty path")
        void testEmptyPath() {
            // Arrange
            Path path = new Path();
            
            // Act
            boolean result = pathTraverser.traverse(path);
            
            // Assert
            assertTrue(result); // No transitions to execute
            verify(mockTransitionExecutor, never()).go(anyLong(), anyLong());
        }
        
        @Test
        @DisplayName("Should stop on first failure")
        void testStopOnFirstFailure() {
            // Arrange
            Path path = new Path();
            path.getStates().add(1L);
            path.getStates().add(2L);
            path.getStates().add(3L);
            path.getStates().add(4L);
            
            when(mockTransitionExecutor.go(1L, 2L)).thenReturn(true);
            when(mockTransitionExecutor.go(2L, 3L)).thenReturn(false); // Fails here
            
            // Act
            boolean result = pathTraverser.traverse(path);
            
            // Assert
            assertFalse(result);
            verify(mockTransitionExecutor).go(1L, 2L);
            verify(mockTransitionExecutor).go(2L, 3L);
            verify(mockTransitionExecutor, never()).go(3L, 4L); // Should not continue
            assertEquals(2L, pathTraverser.getFailedTransitionStartState());
        }
        
        @Test
        @DisplayName("Should record failure at first transition")
        void testFailureAtFirstTransition() {
            // Arrange
            Path path = new Path();
            path.getStates().add(1L);
            path.getStates().add(2L);
            path.getStates().add(3L);
            
            when(mockTransitionExecutor.go(1L, 2L)).thenReturn(false); // Fails immediately
            
            // Act
            boolean result = pathTraverser.traverse(path);
            
            // Assert
            assertFalse(result);
            verify(mockTransitionExecutor).go(1L, 2L);
            verify(mockTransitionExecutor, never()).go(2L, 3L);
            assertEquals(1L, pathTraverser.getFailedTransitionStartState());
        }
        
        @Test
        @DisplayName("Should record failure at last transition")
        void testFailureAtLastTransition() {
            // Arrange
            Path path = new Path();
            path.getStates().add(1L);
            path.getStates().add(2L);
            path.getStates().add(3L);
            
            when(mockTransitionExecutor.go(1L, 2L)).thenReturn(true);
            when(mockTransitionExecutor.go(2L, 3L)).thenReturn(false); // Fails at last
            
            // Act
            boolean result = pathTraverser.traverse(path);
            
            // Assert
            assertFalse(result);
            assertEquals(2L, pathTraverser.getFailedTransitionStartState());
        }
    }
    
    @Nested
    @DisplayName("Finish Transition")
    class FinishTransition {
        
        @Test
        @DisplayName("Should complete finish transition successfully")
        void testFinishTransitionSuccess() {
            // Arrange
            Long targetStateId = 5L;
            
            when(mockStateTransitionService.getTransitions(targetStateId))
                .thenReturn(Optional.of(mockStateTransitions));
            when(mockStateTransitions.getTransitionFinish()).thenReturn(mockTransition);
            when(mockConditionPackager.getAsBoolean(mockTransition)).thenReturn(true);
            
            // Act
            boolean result = pathTraverser.finishTransition(targetStateId);
            
            // Assert
            assertTrue(result);
            verify(mockStateTransitionService).getTransitions(targetStateId);
            verify(mockConditionPackager).getAsBoolean(mockTransition);
        }
        
        @Test
        @DisplayName("Should handle missing state transitions")
        void testFinishTransitionMissingState() {
            // Arrange
            Long targetStateId = 5L;
            
            when(mockStateTransitionService.getTransitions(targetStateId))
                .thenReturn(Optional.empty());
            
            // Act
            boolean result = pathTraverser.finishTransition(targetStateId);
            
            // Assert
            assertFalse(result);
            verify(mockStateTransitionService).getTransitions(targetStateId);
            verify(mockConditionPackager, never()).getAsBoolean(any());
        }
        
        @Test
        @DisplayName("Should handle failed finish transition")
        void testFinishTransitionFailure() {
            // Arrange
            Long targetStateId = 5L;
            
            when(mockStateTransitionService.getTransitions(targetStateId))
                .thenReturn(Optional.of(mockStateTransitions));
            when(mockStateTransitions.getTransitionFinish()).thenReturn(mockTransition);
            when(mockConditionPackager.getAsBoolean(mockTransition)).thenReturn(false);
            
            // Act
            boolean result = pathTraverser.finishTransition(targetStateId);
            
            // Assert
            assertFalse(result);
            verify(mockConditionPackager).getAsBoolean(mockTransition);
        }
        
        @Test
        @DisplayName("Should handle null finish transition")
        void testNullFinishTransition() {
            // Arrange
            Long targetStateId = 5L;
            
            when(mockStateTransitionService.getTransitions(targetStateId))
                .thenReturn(Optional.of(mockStateTransitions));
            when(mockStateTransitions.getTransitionFinish()).thenReturn(null);
            when(mockConditionPackager.getAsBoolean(null))
                .thenThrow(new NullPointerException("Null transition"));
            
            // Act & Assert
            assertThrows(NullPointerException.class, 
                () -> pathTraverser.finishTransition(targetStateId));
        }
    }
    
    @Nested
    @DisplayName("Complex Scenarios")
    class ComplexScenarios {
        
        @Test
        @DisplayName("Should handle long path with multiple transitions")
        void testLongPath() {
            // Arrange
            Path path = new Path();
            for (long i = 1; i <= 10; i++) {
                path.getStates().add(i);
            }
            
            // All transitions succeed
            when(mockTransitionExecutor.go(anyLong(), anyLong())).thenReturn(true);
            
            // Act
            boolean result = pathTraverser.traverse(path);
            
            // Assert
            assertTrue(result);
            verify(mockTransitionExecutor, times(9)).go(anyLong(), anyLong());
            
            // Verify sequential calls
            for (long i = 1; i < 10; i++) {
                verify(mockTransitionExecutor).go(i, i + 1);
            }
        }
        
        @Test
        @DisplayName("Should handle cyclic path")
        void testCyclicPath() {
            // Arrange
            Path path = new Path();
            path.getStates().add(1L);
            path.getStates().add(2L);
            path.getStates().add(3L);
            path.getStates().add(1L); // Cycle back
            
            when(mockTransitionExecutor.go(1L, 2L)).thenReturn(true);
            when(mockTransitionExecutor.go(2L, 3L)).thenReturn(true);
            when(mockTransitionExecutor.go(3L, 1L)).thenReturn(true);
            
            // Act
            boolean result = pathTraverser.traverse(path);
            
            // Assert
            assertTrue(result);
            verify(mockTransitionExecutor).go(3L, 1L); // Verify cycle transition
        }
        
        @Test
        @DisplayName("Should handle repeated traversal")
        void testRepeatedTraversal() {
            // Arrange
            Path path = new Path();
            path.getStates().add(1L);
            path.getStates().add(2L);
            
            // First traversal succeeds
            when(mockTransitionExecutor.go(1L, 2L))
                .thenReturn(true)
                .thenReturn(false); // Second traversal fails
            
            // Act
            boolean result1 = pathTraverser.traverse(path);
            boolean result2 = pathTraverser.traverse(path);
            
            // Assert
            assertTrue(result1);
            assertFalse(result2);
            assertEquals(1L, pathTraverser.getFailedTransitionStartState());
            verify(mockTransitionExecutor, times(2)).go(1L, 2L);
        }
        
        @Test
        @DisplayName("Should update failure state on subsequent failures")
        void testUpdateFailureState() {
            // Arrange
            Path path1 = new Path();
            path1.getStates().add(1L);
            path1.getStates().add(2L);
            
            Path path2 = new Path();
            path2.getStates().add(3L);
            path2.getStates().add(4L);
            
            when(mockTransitionExecutor.go(1L, 2L)).thenReturn(false);
            when(mockTransitionExecutor.go(3L, 4L)).thenReturn(false);
            
            // Act
            pathTraverser.traverse(path1);
            Long firstFailure = pathTraverser.getFailedTransitionStartState();
            
            pathTraverser.traverse(path2);
            Long secondFailure = pathTraverser.getFailedTransitionStartState();
            
            // Assert
            assertEquals(1L, firstFailure);
            assertEquals(3L, secondFailure); // Updated to new failure
        }
    }
    
    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {
        
        @Test
        @DisplayName("Should combine traverse and finish transition")
        void testTraverseAndFinish() {
            // Arrange
            Path path = new Path();
            path.getStates().add(1L);
            path.getStates().add(2L);
            path.getStates().add(3L);
            
            when(mockTransitionExecutor.go(1L, 2L)).thenReturn(true);
            when(mockTransitionExecutor.go(2L, 3L)).thenReturn(true);
            
            when(mockStateTransitionService.getTransitions(3L))
                .thenReturn(Optional.of(mockStateTransitions));
            when(mockStateTransitions.getTransitionFinish()).thenReturn(mockTransition);
            when(mockConditionPackager.getAsBoolean(mockTransition)).thenReturn(true);
            
            // Act
            boolean traverseResult = pathTraverser.traverse(path);
            boolean finishResult = pathTraverser.finishTransition(3L);
            
            // Assert
            assertTrue(traverseResult);
            assertTrue(finishResult);
        }
        
        @Test
        @DisplayName("Should handle traverse success but finish failure")
        void testTraverseSuccessFinishFailure() {
            // Arrange
            Path path = new Path();
            path.getStates().add(1L);
            path.getStates().add(2L);
            
            when(mockTransitionExecutor.go(1L, 2L)).thenReturn(true);
            
            when(mockStateTransitionService.getTransitions(2L))
                .thenReturn(Optional.of(mockStateTransitions));
            when(mockStateTransitions.getTransitionFinish()).thenReturn(mockTransition);
            when(mockConditionPackager.getAsBoolean(mockTransition)).thenReturn(false);
            
            // Act
            boolean traverseResult = pathTraverser.traverse(path);
            boolean finishResult = pathTraverser.finishTransition(2L);
            
            // Assert
            assertTrue(traverseResult);
            assertFalse(finishResult);
            // Failed transition state should remain from traversal, not finish
            assertEquals(SpecialStateType.NULL.getId(), 
                pathTraverser.getFailedTransitionStartState());
        }
    }
}