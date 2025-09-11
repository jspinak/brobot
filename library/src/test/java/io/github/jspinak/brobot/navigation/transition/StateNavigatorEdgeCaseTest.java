package io.github.jspinak.brobot.navigation.transition;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateEnum;
import io.github.jspinak.brobot.navigation.path.Path;
import io.github.jspinak.brobot.navigation.path.PathFinder;
import io.github.jspinak.brobot.navigation.path.PathManager;
import io.github.jspinak.brobot.navigation.path.PathTraverser;
import io.github.jspinak.brobot.navigation.path.Paths;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.logging.ActionLogger;
import io.github.jspinak.brobot.tools.logging.ExecutionSession;

/**
 * Edge case and boundary tests for StateNavigator. Tests complex scenarios, error conditions, and
 * boundary behaviors.
 */
@DisplayName("StateNavigator Edge Case Tests")
class StateNavigatorEdgeCaseTest extends BrobotTestBase {

    @Mock private PathFinder pathFinder;

    @Mock private StateService stateService;

    @Mock private StateMemory stateMemory;

    @Mock private PathTraverser pathTraverser;

    @Mock private PathManager pathManager;

    @Mock private ActionLogger actionLogger;

    @Mock private ExecutionSession executionSession;

    @Mock private State mockState;

    @Mock private Paths mockPaths;

    @Mock private Path mockPath;

    private StateNavigator stateNavigator;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);

        stateNavigator =
                new StateNavigator(
                        pathFinder,
                        stateService,
                        stateMemory,
                        pathTraverser,
                        pathManager,
                        actionLogger,
                        executionSession);

        // Default setup
        when(executionSession.getCurrentSessionId()).thenReturn("test-session");
        when(stateMemory.getActiveStates()).thenReturn(new HashSet<>(Arrays.asList(1L)));
        when(stateMemory.getActiveStateNamesAsString()).thenReturn("State1");
    }

    @Nested
    @DisplayName("Multiple Active States Scenarios")
    class MultipleActiveStatesScenarios {

        @Test
        @DisplayName("Should handle multiple active states as starting points")
        void testMultipleActiveStates() {
            // Arrange
            Long targetId = 10L;
            Set<Long> activeStates = new HashSet<>(Arrays.asList(1L, 2L, 3L));

            when(stateMemory.getActiveStates()).thenReturn(activeStates);
            when(stateService.getState(targetId)).thenReturn(Optional.of(mockState));
            when(mockState.getName()).thenReturn("TargetState");
            when(pathFinder.getPathsToState(activeStates, targetId)).thenReturn(mockPaths);
            when(mockPaths.isEmpty()).thenReturn(false);
            when(mockPaths.getPaths()).thenReturn(Arrays.asList(mockPath));
            when(pathTraverser.traverse(mockPath)).thenReturn(true);

            // Act
            boolean result = stateNavigator.openState(targetId);

            // Assert
            assertTrue(result);
            verify(pathFinder).getPathsToState(activeStates, targetId);
        }

        @Test
        @DisplayName("Should handle state already partially active")
        void testPartiallyActiveTarget() {
            // Arrange
            Long targetId = 5L;
            Set<Long> activeStates = new HashSet<>(Arrays.asList(1L, 5L, 10L));

            when(stateMemory.getActiveStates()).thenReturn(activeStates);
            when(stateService.getState(targetId)).thenReturn(Optional.of(mockState));
            when(mockState.getName()).thenReturn("PartialTarget");
            when(pathFinder.getPathsToState(activeStates, targetId)).thenReturn(mockPaths);
            when(mockPaths.isEmpty()).thenReturn(false);
            when(pathTraverser.finishTransition(targetId)).thenReturn(true);

            // Act
            boolean result = stateNavigator.openState(targetId);

            // Assert
            assertTrue(result);
            verify(pathTraverser).finishTransition(targetId);
            verify(pathTraverser, never()).traverse(any());
        }
    }

    @Nested
    @DisplayName("Path Recovery Scenarios")
    class PathRecoveryScenarios {

        @Test
        @DisplayName("Should handle partial path success with state change")
        void testPartialPathSuccess() {
            // Arrange
            Long targetId = 20L;
            Set<Long> initialActive = new HashSet<>(Arrays.asList(1L));
            Set<Long> newActive = new HashSet<>(Arrays.asList(5L, 6L));
            Path path1 = mock(Path.class);
            Path path2 = mock(Path.class);
            Paths cleanPaths = mock(Paths.class);

            when(stateMemory.getActiveStates()).thenReturn(initialActive).thenReturn(newActive);
            when(stateService.getState(targetId)).thenReturn(Optional.of(mockState));
            when(mockState.getName()).thenReturn("Target");
            when(pathFinder.getPathsToState(initialActive, targetId)).thenReturn(mockPaths);
            when(mockPaths.isEmpty()).thenReturn(false).thenReturn(false);
            when(mockPaths.getPaths()).thenReturn(Arrays.asList(path1));
            when(pathTraverser.traverse(path1)).thenReturn(false);
            when(pathTraverser.getFailedTransitionStartState()).thenReturn(1L);
            when(pathManager.getCleanPaths(newActive, mockPaths, 1L)).thenReturn(cleanPaths);
            when(cleanPaths.isEmpty()).thenReturn(false);
            when(cleanPaths.getPaths()).thenReturn(Arrays.asList(path2));
            when(pathTraverser.traverse(path2)).thenReturn(true);

            // Act
            boolean result = stateNavigator.openState(targetId);

            // Assert
            assertTrue(result);
            verify(pathTraverser).traverse(path1);
            verify(pathTraverser).traverse(path2);
            verify(pathManager).getCleanPaths(newActive, mockPaths, 1L);
        }

        @Test
        @DisplayName("Should exhaust all recovery attempts")
        void testExhaustAllRecoveryAttempts() {
            // Arrange
            Long targetId = 30L;
            Path path1 = mock(Path.class);
            Path path2 = mock(Path.class);
            Path path3 = mock(Path.class);
            Paths paths1 = mock(Paths.class);
            Paths paths2 = mock(Paths.class);
            Paths emptyPaths = mock(Paths.class);

            when(stateService.getState(targetId)).thenReturn(Optional.of(mockState));
            when(mockState.getName()).thenReturn("Target");
            when(pathFinder.getPathsToState(any(Set.class), eq(targetId))).thenReturn(paths1);

            // First attempt
            when(paths1.isEmpty()).thenReturn(false);
            when(paths1.getPaths()).thenReturn(Arrays.asList(path1));
            when(pathTraverser.traverse(path1)).thenReturn(false);

            // Second attempt
            when(pathManager.getCleanPaths(any(), eq(paths1), any())).thenReturn(paths2);
            when(paths2.isEmpty()).thenReturn(false);
            when(paths2.getPaths()).thenReturn(Arrays.asList(path2));
            when(pathTraverser.traverse(path2)).thenReturn(false);

            // Third attempt - empty paths
            when(pathManager.getCleanPaths(any(), eq(paths2), any())).thenReturn(emptyPaths);
            when(emptyPaths.isEmpty()).thenReturn(true);

            // Act
            boolean result = stateNavigator.openState(targetId);

            // Assert
            assertFalse(result);
            verify(pathTraverser, times(2)).traverse(any(Path.class));
        }
    }

    @Nested
    @DisplayName("State Name Resolution")
    class StateNameResolution {

        @Test
        @DisplayName("Should handle null state name")
        void testNullStateName() {
            // Act
            boolean result = stateNavigator.openState((String) null);

            // Assert
            assertFalse(result);
            verify(stateService).getStateId(null);
        }

        @Test
        @DisplayName("Should handle empty state name")
        void testEmptyStateName() {
            // Arrange
            when(stateService.getStateId("")).thenReturn(null);

            // Act
            boolean result = stateNavigator.openState("");

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("Should handle special characters in state name")
        void testSpecialCharactersInStateName() {
            // Arrange
            String specialName = "State-Name_With.Special$Characters";
            Long stateId = 100L;

            when(stateService.getStateId(specialName)).thenReturn(stateId);
            when(stateService.getState(stateId)).thenReturn(Optional.of(mockState));
            when(mockState.getName()).thenReturn(specialName);
            when(pathFinder.getPathsToState(any(Set.class), eq(stateId))).thenReturn(mockPaths);
            when(mockPaths.isEmpty()).thenReturn(false);
            when(mockPaths.getPaths()).thenReturn(Arrays.asList(mockPath));
            when(pathTraverser.traverse(mockPath)).thenReturn(true);

            // Act
            boolean result = stateNavigator.openState(specialName);

            // Assert
            assertTrue(result);
            verify(stateService).getStateId(specialName);
        }

        @Test
        @DisplayName("Should handle very long state name")
        void testVeryLongStateName() {
            // Arrange
            String longName = "State" + "X".repeat(1000);
            Long stateId = 200L;

            when(stateService.getStateId(longName)).thenReturn(stateId);
            when(stateService.getState(stateId)).thenReturn(Optional.of(mockState));
            when(mockState.getName()).thenReturn(longName);
            when(pathFinder.getPathsToState(any(Set.class), eq(stateId))).thenReturn(mockPaths);
            when(mockPaths.isEmpty()).thenReturn(true);

            // Act
            boolean result = stateNavigator.openState(longName);

            // Assert
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("StateEnum Navigation")
    class StateEnumNavigation {

        @Test
        @DisplayName("Should handle custom StateEnum implementation")
        void testCustomStateEnum() {
            // Arrange
            StateEnum customEnum =
                    new StateEnum() {
                        @Override
                        public String toString() {
                            return "CustomState";
                        }
                    };
            Long stateId = 50L;

            when(stateService.getStateId("CustomState")).thenReturn(stateId);
            when(stateService.getState(stateId)).thenReturn(Optional.of(mockState));
            when(mockState.getName()).thenReturn("CustomState");
            when(pathFinder.getPathsToState(any(Set.class), eq(stateId))).thenReturn(mockPaths);
            when(mockPaths.isEmpty()).thenReturn(false);
            when(mockPaths.getPaths()).thenReturn(Arrays.asList(mockPath));
            when(pathTraverser.traverse(mockPath)).thenReturn(true);

            // Act
            boolean result = stateNavigator.openState(customEnum);

            // Assert
            assertTrue(result);
            verify(stateService).getStateId("CustomState");
        }

        @Test
        @DisplayName("Should handle StateEnum with null toString")
        void testStateEnumNullToString() {
            // Arrange
            StateEnum nullEnum =
                    new StateEnum() {
                        @Override
                        public String toString() {
                            return null;
                        }
                    };

            // Act
            boolean result = stateNavigator.openState(nullEnum);

            // Assert
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Logging and Session Management")
    class LoggingAndSessionManagement {

        @Test
        @DisplayName("Should handle null session ID")
        void testNullSessionId() {
            // Arrange
            when(executionSession.getCurrentSessionId()).thenReturn(null);
            Long targetId = 15L;

            when(stateService.getState(targetId)).thenReturn(Optional.of(mockState));
            when(mockState.getName()).thenReturn("Target");
            when(pathFinder.getPathsToState(any(Set.class), eq(targetId))).thenReturn(mockPaths);
            when(mockPaths.isEmpty()).thenReturn(true);

            // Act
            boolean result = stateNavigator.openState(targetId);

            // Assert
            assertFalse(result);
            verify(actionLogger)
                    .logStateTransition(isNull(), any(), any(), any(), eq(false), anyLong());
        }

        @Test
        @DisplayName("Should log with empty active state names")
        void testEmptyActiveStateNames() {
            // Arrange
            when(stateMemory.getActiveStateNamesAsString()).thenReturn("");
            Long targetId = 25L;

            when(stateService.getState(targetId)).thenReturn(Optional.of(mockState));
            when(mockState.getName()).thenReturn("Target");
            when(pathFinder.getPathsToState(any(Set.class), eq(targetId))).thenReturn(mockPaths);
            when(mockPaths.isEmpty()).thenReturn(true);

            // Act
            boolean result = stateNavigator.openState(targetId);

            // Assert
            assertFalse(result);
            verify(actionLogger)
                    .logObservation(
                            anyString(), eq("Transition start:"), contains("Target"), eq("info"));
        }
    }

    @Nested
    @DisplayName("Concurrent Navigation")
    class ConcurrentNavigation {

        @Test
        @DisplayName("Should handle concurrent state changes during navigation")
        void testConcurrentStateChanges() {
            // Arrange
            Long targetId = 40L;
            Set<Long> states1 = new HashSet<>(Arrays.asList(1L));
            Set<Long> states2 = new HashSet<>(Arrays.asList(2L));
            Set<Long> states3 = new HashSet<>(Arrays.asList(3L));

            // Simulate state changes during navigation
            when(stateMemory.getActiveStates())
                    .thenReturn(states1)
                    .thenReturn(states2)
                    .thenReturn(states3);

            when(stateService.getState(targetId)).thenReturn(Optional.of(mockState));
            when(mockState.getName()).thenReturn("Target");
            when(pathFinder.getPathsToState(states1, targetId)).thenReturn(mockPaths);
            when(mockPaths.isEmpty()).thenReturn(false);
            when(mockPaths.getPaths()).thenReturn(Arrays.asList(mockPath));
            when(pathTraverser.traverse(mockPath)).thenReturn(false);
            when(pathTraverser.getFailedTransitionStartState()).thenReturn(1L);

            Paths cleanPaths = mock(Paths.class);
            when(pathManager.getCleanPaths(states2, mockPaths, 1L)).thenReturn(cleanPaths);
            when(cleanPaths.isEmpty()).thenReturn(true);

            // Act
            boolean result = stateNavigator.openState(targetId);

            // Assert
            assertFalse(result);
            verify(stateMemory, atLeast(2)).getActiveStates();
        }
    }

    @Nested
    @DisplayName("Error Recovery")
    class ErrorRecovery {

        @Test
        @DisplayName("Should handle exception in path traversal")
        void testExceptionInPathTraversal() {
            // Arrange
            Long targetId = 50L;

            when(stateService.getState(targetId)).thenReturn(Optional.of(mockState));
            when(mockState.getName()).thenReturn("Target");
            when(pathFinder.getPathsToState(any(Set.class), eq(targetId))).thenReturn(mockPaths);
            when(mockPaths.isEmpty()).thenReturn(false);
            when(mockPaths.getPaths()).thenReturn(Arrays.asList(mockPath));
            when(pathTraverser.traverse(mockPath))
                    .thenThrow(new RuntimeException("Traversal error"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> stateNavigator.openState(targetId));
        }

        @Test
        @DisplayName("Should handle null path in paths list")
        void testNullPathInList() {
            // Arrange
            Long targetId = 60L;
            List<Path> pathsWithNull = new ArrayList<>();
            pathsWithNull.add(null);

            when(stateService.getState(targetId)).thenReturn(Optional.of(mockState));
            when(mockState.getName()).thenReturn("Target");
            when(pathFinder.getPathsToState(any(Set.class), eq(targetId))).thenReturn(mockPaths);
            when(mockPaths.isEmpty()).thenReturn(false);
            when(mockPaths.getPaths()).thenReturn(pathsWithNull);

            // Act & Assert
            assertThrows(NullPointerException.class, () -> stateNavigator.openState(targetId));
        }
    }
}
