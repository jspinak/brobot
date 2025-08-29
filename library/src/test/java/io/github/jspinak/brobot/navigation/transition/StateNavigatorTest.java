package io.github.jspinak.brobot.navigation.transition;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateEnum;
import io.github.jspinak.brobot.navigation.path.*;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.logging.ActionLogger;
import io.github.jspinak.brobot.tools.logging.ExecutionSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for StateNavigator.
 * Tests high-level state navigation orchestration and path finding.
 */
@DisplayName("StateNavigator Tests")
class StateNavigatorTest extends BrobotTestBase {
    
    @Mock
    private PathFinder pathFinder;
    
    @Mock
    private StateService stateService;
    
    @Mock
    private StateMemory stateMemory;
    
    @Mock
    private PathTraverser pathTraverser;
    
    @Mock
    private PathManager pathManager;
    
    @Mock
    private ActionLogger actionLogger;
    
    @Mock
    private ExecutionSession executionSession;
    
    @Mock
    private State mockState;
    
    @Mock
    private Paths mockPaths;
    
    @Mock
    private Path mockPath;
    
    private StateNavigator stateNavigator;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        
        stateNavigator = new StateNavigator(
            pathFinder, stateService, stateMemory, 
            pathTraverser, pathManager, actionLogger, executionSession
        );
        
        // Setup default behavior
        when(executionSession.getCurrentSessionId()).thenReturn("test-session-123");
        when(stateMemory.getActiveStates()).thenReturn(new HashSet<>(Arrays.asList(1L)));
        when(stateMemory.getActiveStateNamesAsString()).thenReturn("State1");
    }
    
    @Nested
    @DisplayName("Open State by Name")
    class OpenStateByName {
        
        @Test
        @DisplayName("Should navigate to state by name successfully")
        void testOpenStateByNameSuccess() {
            // Arrange
            String stateName = "TargetState";
            Long stateId = 2L;
            
            when(stateService.getStateId(stateName)).thenReturn(stateId);
            when(stateService.getStateName(stateId)).thenReturn(stateName);
            when(stateService.getState(stateId)).thenReturn(Optional.of(mockState));
            when(mockState.getName()).thenReturn(stateName);
            
            when(pathFinder.getPathsToState(any(Set.class), eq(stateId))).thenReturn(mockPaths);
            when(mockPaths.isEmpty()).thenReturn(false);
            when(mockPaths.getPaths()).thenReturn(List.of(mockPath));
            when(pathTraverser.traverse(mockPath)).thenReturn(true);
            
            when(stateService.findSetById(any(Set.class))).thenReturn(new HashSet<>(Arrays.asList(mockState)));
            
            // Act
            boolean result = stateNavigator.openState(stateName);
            
            // Assert
            assertTrue(result);
            verify(stateService).getStateId(stateName);
            verify(pathTraverser).traverse(mockPath);
            verify(actionLogger).logStateTransition(anyString(), any(), any(), any(), eq(true), anyLong());
        }
        
        @Test
        @DisplayName("Should fail when state name not found")
        void testOpenStateByNameNotFound() {
            // Arrange
            String stateName = "NonExistentState";
            when(stateService.getStateId(stateName)).thenReturn(null);
            
            // Act
            boolean result = stateNavigator.openState(stateName);
            
            // Assert
            assertFalse(result);
            verify(stateService).getStateId(stateName);
            verify(pathFinder, never()).getPathsToState(any(Set.class), anyLong());
        }
        
        @Test
        @DisplayName("Should handle empty state name")
        void testOpenStateEmptyName() {
            // Arrange
            when(stateService.getStateId("")).thenReturn(null);
            
            // Act
            boolean result = stateNavigator.openState("");
            
            // Assert
            assertFalse(result);
        }
    }
    
    @Nested
    @DisplayName("Open State by Enum")
    class OpenStateByEnum {
        
        enum TestStateEnum implements StateEnum {
            HOME_STATE("HomeState"),
            LOGIN_STATE("LoginState");
            
            private final String name;
            
            TestStateEnum(String name) {
                this.name = name;
            }
            
            @Override
            public String toString() {
                return name;
            }
        }
        
        @Test
        @DisplayName("Should navigate to state by enum successfully")
        void testOpenStateByEnumSuccess() {
            // Arrange
            TestStateEnum stateEnum = TestStateEnum.HOME_STATE;
            Long stateId = 3L;
            
            when(stateService.getStateId("HomeState")).thenReturn(stateId);
            when(stateService.getStateName(stateId)).thenReturn("HomeState");
            when(stateService.getState(stateId)).thenReturn(Optional.of(mockState));
            when(mockState.getName()).thenReturn("HomeState");
            
            when(pathFinder.getPathsToState(any(Set.class), eq(stateId))).thenReturn(mockPaths);
            when(mockPaths.isEmpty()).thenReturn(false);
            when(mockPaths.getPaths()).thenReturn(List.of(mockPath));
            when(pathTraverser.traverse(mockPath)).thenReturn(true);
            
            when(stateService.findSetById(any(Set.class))).thenReturn(new HashSet<>(Arrays.asList(mockState)));
            
            // Act
            boolean result = stateNavigator.openState(stateEnum);
            
            // Assert
            assertTrue(result);
            verify(stateService).getStateId("HomeState");
        }
    }
    
    @Nested
    @DisplayName("Open State by ID")
    class OpenStateById {
        
        @Test
        @DisplayName("Should navigate to state by ID successfully")
        void testOpenStateByIdSuccess() {
            // Arrange
            Long targetStateId = 5L;
            State targetState = mock(State.class);
            when(targetState.getName()).thenReturn("TargetState");
            
            when(stateService.getStateName(targetStateId)).thenReturn("TargetState");
            when(stateService.getState(targetStateId)).thenReturn(Optional.of(targetState));
            
            when(pathFinder.getPathsToState(any(Set.class), eq(targetStateId))).thenReturn(mockPaths);
            when(mockPaths.isEmpty()).thenReturn(false);
            when(mockPaths.getPaths()).thenReturn(List.of(mockPath));
            when(pathTraverser.traverse(mockPath)).thenReturn(true);
            
            when(stateService.findSetById(any(Set.class))).thenReturn(new HashSet<>(Arrays.asList(targetState)));
            
            // Act
            boolean result = stateNavigator.openState(targetStateId);
            
            // Assert
            assertTrue(result);
            verify(pathFinder).getPathsToState(any(Set.class), eq(targetStateId));
            verify(pathTraverser).traverse(mockPath);
            verify(actionLogger).logObservation(anyString(), anyString(), anyString(), eq("info"));
            verify(actionLogger).logStateTransition(anyString(), any(), any(), any(), eq(true), anyLong());
        }
        
        @Test
        @DisplayName("Should fail when target state not found")
        void testOpenStateByIdNotFound() {
            // Arrange
            Long targetStateId = 99L;
            when(stateService.getStateName(targetStateId)).thenReturn("UnknownState");
            when(stateService.getState(targetStateId)).thenReturn(Optional.empty());
            
            // Act
            boolean result = stateNavigator.openState(targetStateId);
            
            // Assert
            assertFalse(result);
            verify(pathFinder, never()).getPathsToState(any(Set.class), anyLong());
        }
        
        @Test
        @DisplayName("Should handle already at target state")
        void testAlreadyAtTargetState() {
            // Arrange
            Long targetStateId = 1L; // Same as current active state
            State targetState = mock(State.class);
            when(targetState.getName()).thenReturn("State1");
            
            when(stateService.getStateName(targetStateId)).thenReturn("State1");
            when(stateService.getState(targetStateId)).thenReturn(Optional.of(targetState));
            when(stateMemory.getActiveStates()).thenReturn(new HashSet<>(Arrays.asList(targetStateId)));
            
            when(pathFinder.getPathsToState(any(Set.class), eq(targetStateId))).thenReturn(mockPaths);
            when(mockPaths.isEmpty()).thenReturn(false);
            when(pathTraverser.finishTransition(targetStateId)).thenReturn(true);
            
            when(stateService.findSetById(any(Set.class))).thenReturn(new HashSet<>(Arrays.asList(targetState)));
            
            // Act
            boolean result = stateNavigator.openState(targetStateId);
            
            // Assert
            assertTrue(result);
            verify(pathTraverser).finishTransition(targetStateId);
            verify(pathTraverser, never()).traverse(any());
        }
        
        @Test
        @DisplayName("Should handle no paths available")
        void testNoPathsAvailable() {
            // Arrange
            Long targetStateId = 10L;
            State targetState = mock(State.class);
            when(targetState.getName()).thenReturn("IsolatedState");
            
            when(stateService.getStateName(targetStateId)).thenReturn("IsolatedState");
            when(stateService.getState(targetStateId)).thenReturn(Optional.of(targetState));
            
            Paths emptyPaths = mock(Paths.class);
            when(emptyPaths.isEmpty()).thenReturn(true);
            when(pathFinder.getPathsToState(any(Set.class), eq(targetStateId))).thenReturn(emptyPaths);
            
            when(stateService.findSetById(any(Set.class))).thenReturn(new HashSet<>());
            
            // Act
            boolean result = stateNavigator.openState(targetStateId);
            
            // Assert
            assertFalse(result);
            verify(pathTraverser, never()).traverse(any());
            verify(actionLogger).logStateTransition(anyString(), any(), any(), any(), eq(false), anyLong());
        }
    }
    
    @Nested
    @DisplayName("Path Traversal and Recovery")
    class PathTraversalAndRecovery {
        
        @Test
        @DisplayName("Should try alternative path on failure")
        void testAlternativePathOnFailure() {
            // Arrange
            Long targetStateId = 7L;
            State targetState = mock(State.class);
            when(targetState.getName()).thenReturn("TargetState");
            
            when(stateService.getStateName(targetStateId)).thenReturn("TargetState");
            when(stateService.getState(targetStateId)).thenReturn(Optional.of(targetState));
            
            Path path1 = mock(Path.class);
            Path path2 = mock(Path.class);
            
            Paths initialPaths = mock(Paths.class);
            when(initialPaths.isEmpty()).thenReturn(false);
            when(initialPaths.getPaths()).thenReturn(List.of(path1));
            
            Paths cleanedPaths = mock(Paths.class);
            when(cleanedPaths.isEmpty()).thenReturn(false);
            when(cleanedPaths.getPaths()).thenReturn(List.of(path2));
            
            when(pathFinder.getPathsToState(any(Set.class), eq(targetStateId))).thenReturn(initialPaths);
            
            // First path fails
            when(pathTraverser.traverse(path1)).thenReturn(false);
            when(pathTraverser.getFailedTransitionStartState()).thenReturn(1L);
            
            // Clean paths returns alternative
            when(pathManager.getCleanPaths(any(), eq(initialPaths), eq(1L))).thenReturn(cleanedPaths);
            
            // Second path succeeds
            when(pathTraverser.traverse(path2)).thenReturn(true);
            
            when(stateService.findSetById(any(Set.class))).thenReturn(new HashSet<>(Arrays.asList(targetState)));
            
            // Act
            boolean result = stateNavigator.openState(targetStateId);
            
            // Assert
            assertTrue(result);
            verify(pathTraverser).traverse(path1);
            verify(pathTraverser).traverse(path2);
            verify(pathManager).getCleanPaths(any(), eq(initialPaths), eq(1L));
        }
        
        @Test
        @DisplayName("Should fail after exhausting all paths")
        void testExhaustAllPaths() {
            // Arrange
            Long targetStateId = 8L;
            State targetState = mock(State.class);
            when(targetState.getName()).thenReturn("UnreachableState");
            
            when(stateService.getStateName(targetStateId)).thenReturn("UnreachableState");
            when(stateService.getState(targetStateId)).thenReturn(Optional.of(targetState));
            
            Path path1 = mock(Path.class);
            
            Paths initialPaths = mock(Paths.class);
            when(initialPaths.isEmpty()).thenReturn(false);
            when(initialPaths.getPaths()).thenReturn(List.of(path1));
            
            Paths emptyPaths = mock(Paths.class);
            when(emptyPaths.isEmpty()).thenReturn(true);
            
            when(pathFinder.getPathsToState(any(Set.class), eq(targetStateId))).thenReturn(initialPaths);
            
            // Path fails
            when(pathTraverser.traverse(path1)).thenReturn(false);
            when(pathTraverser.getFailedTransitionStartState()).thenReturn(1L);
            
            // No clean paths available
            when(pathManager.getCleanPaths(any(), eq(initialPaths), eq(1L))).thenReturn(emptyPaths);
            
            when(stateService.findSetById(any(Set.class))).thenReturn(new HashSet<>());
            
            // Act
            boolean result = stateNavigator.openState(targetStateId);
            
            // Assert
            assertFalse(result);
            verify(pathTraverser).traverse(path1);
            verify(pathManager).getCleanPaths(any(), eq(initialPaths), eq(1L));
            verify(actionLogger).logStateTransition(anyString(), any(), any(), any(), eq(false), anyLong());
        }
        
        @Test
        @DisplayName("Should update active states after partial traversal")
        void testUpdateActiveStatesAfterPartialTraversal() {
            // Arrange
            Long targetStateId = 9L;
            State targetState = mock(State.class);
            when(targetState.getName()).thenReturn("TargetState");
            
            when(stateService.getStateName(targetStateId)).thenReturn("TargetState");
            when(stateService.getState(targetStateId)).thenReturn(Optional.of(targetState));
            
            // Initial active states
            Set<Long> initialActiveStates = new HashSet<>(Arrays.asList(1L));
            // Active states after first failed traversal
            Set<Long> updatedActiveStates = new HashSet<>(Arrays.asList(2L, 3L));
            
            when(stateMemory.getActiveStates())
                .thenReturn(initialActiveStates)
                .thenReturn(updatedActiveStates);
            
            Path path1 = mock(Path.class);
            Path path2 = mock(Path.class);
            
            Paths initialPaths = mock(Paths.class);
            when(initialPaths.isEmpty()).thenReturn(false);
            when(initialPaths.getPaths()).thenReturn(List.of(path1));
            
            Paths cleanedPaths = mock(Paths.class);
            when(cleanedPaths.isEmpty()).thenReturn(false);
            when(cleanedPaths.getPaths()).thenReturn(List.of(path2));
            
            when(pathFinder.getPathsToState(eq(initialActiveStates), eq(targetStateId))).thenReturn(initialPaths);
            
            // First path fails but changes active states
            when(pathTraverser.traverse(path1)).thenReturn(false);
            when(pathTraverser.getFailedTransitionStartState()).thenReturn(2L);
            
            // Clean paths with updated active states
            when(pathManager.getCleanPaths(eq(updatedActiveStates), eq(initialPaths), eq(2L)))
                .thenReturn(cleanedPaths);
            
            // Second path succeeds
            when(pathTraverser.traverse(path2)).thenReturn(true);
            
            when(stateService.findSetById(any(Set.class))).thenReturn(new HashSet<>(Arrays.asList(targetState)));
            
            // Act
            boolean result = stateNavigator.openState(targetStateId);
            
            // Assert
            assertTrue(result);
            verify(stateMemory, atLeast(2)).getActiveStates();
            verify(pathManager).getCleanPaths(eq(updatedActiveStates), any(), any());
        }
    }
    
    @Nested
    @DisplayName("Logging and Session Management")
    class LoggingAndSessionManagement {
        
        @Test
        @DisplayName("Should log transition start and end")
        void testLoggingTransitionStartAndEnd() {
            // Arrange
            Long targetStateId = 6L;
            State targetState = mock(State.class);
            when(targetState.getName()).thenReturn("TargetState");
            
            when(stateService.getStateName(targetStateId)).thenReturn("TargetState");
            when(stateService.getState(targetStateId)).thenReturn(Optional.of(targetState));
            
            when(pathFinder.getPathsToState(any(Set.class), eq(targetStateId))).thenReturn(mockPaths);
            when(mockPaths.isEmpty()).thenReturn(false);
            when(mockPaths.getPaths()).thenReturn(List.of(mockPath));
            when(pathTraverser.traverse(mockPath)).thenReturn(true);
            
            when(stateService.findSetById(any(Set.class))).thenReturn(new HashSet<>(Arrays.asList(targetState)));
            
            // Act
            boolean result = stateNavigator.openState(targetStateId);
            
            // Assert
            assertTrue(result);
            
            // Verify transition start log
            verify(actionLogger).logObservation(
                eq("test-session-123"),
                eq("Transition start:"),
                contains("Transition from"),
                eq("info")
            );
            
            // Verify transition end log
            verify(actionLogger).logStateTransition(
                eq("test-session-123"),
                any(),
                any(),
                any(),
                eq(true),
                anyLong()
            );
        }
        
        @Test
        @DisplayName("Should use current session ID for logging")
        void testUseCurrentSessionId() {
            // Arrange
            String customSessionId = "custom-session-456";
            when(executionSession.getCurrentSessionId()).thenReturn(customSessionId);
            
            Long targetStateId = 4L;
            State targetState = mock(State.class);
            when(targetState.getName()).thenReturn("TargetState");
            
            when(stateService.getStateName(targetStateId)).thenReturn("TargetState");
            when(stateService.getState(targetStateId)).thenReturn(Optional.of(targetState));
            
            when(pathFinder.getPathsToState(any(Set.class), eq(targetStateId))).thenReturn(mockPaths);
            when(mockPaths.isEmpty()).thenReturn(false);
            when(mockPaths.getPaths()).thenReturn(List.of(mockPath));
            when(pathTraverser.traverse(mockPath)).thenReturn(true);
            
            when(stateService.findSetById(any(Set.class))).thenReturn(new HashSet<>(Arrays.asList(targetState)));
            
            // Act
            stateNavigator.openState(targetStateId);
            
            // Assert
            verify(actionLogger).logObservation(eq(customSessionId), anyString(), anyString(), anyString());
            verify(actionLogger).logStateTransition(eq(customSessionId), any(), any(), any(), anyBoolean(), anyLong());
        }
    }
}