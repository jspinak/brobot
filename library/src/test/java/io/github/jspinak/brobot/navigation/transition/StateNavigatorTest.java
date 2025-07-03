package io.github.jspinak.brobot.navigation.transition;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.path.*;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.tools.logging.ActionLogger;
import io.github.jspinak.brobot.tools.logging.ExecutionSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StateNavigatorTest {

    @Mock
    private PathFinder pathFinder;
    
    @Mock
    private StateService allStatesInProjectService;
    
    @Mock
    private StateMemory stateMemory;
    
    @Mock
    private PathTraverser pathTraverser;
    
    @Mock
    private PathManager pathManager;
    
    @Mock
    private ActionLogger actionLogger;
    
    @Mock
    private ExecutionSession automationSession;
    
    private StateNavigator stateNavigator;
    
    @BeforeEach
    void setUp() {
        stateNavigator = new StateNavigator(pathFinder, allStatesInProjectService, 
                stateMemory, pathTraverser, pathManager, actionLogger, automationSession);
    }
    
    @Test
    void testOpenStateByName_Success() {
        String stateName = "HomePage";
        Long stateId = 42L;
        State state = new State();
        state.setName(stateName);
        
        Set<Long> activeStates = new HashSet<>(Arrays.asList(1L));
        when(allStatesInProjectService.getStateId(stateName)).thenReturn(stateId);
        when(allStatesInProjectService.getState(stateId)).thenReturn(Optional.of(state));
        when(allStatesInProjectService.getStateName(stateId)).thenReturn(stateName);
        when(stateMemory.getActiveStates()).thenReturn(activeStates);
        when(stateMemory.getActiveStateNamesAsString()).thenReturn("LoginPage");
        
        Paths paths = new Paths();
        Path path = new Path();
        path.add(1L);
        path.add(stateId);
        paths.addPath(path);
        
        when(pathFinder.getPathsToState(activeStates, stateId)).thenReturn(paths);
        when(pathTraverser.traverse(any(Path.class))).thenReturn(true);
        when(allStatesInProjectService.findSetById(any(Set.class))).thenReturn(new HashSet<>());
        when(automationSession.getCurrentSessionId()).thenReturn("test-session-123");
        
        boolean result = stateNavigator.openState(stateName);
        
        assertTrue(result);
        verify(allStatesInProjectService).getStateId(stateName);
        verify(pathTraverser).traverse(path);
    }
    
    @Test
    void testOpenStateByName_StateNotFound() {
        String stateName = "NonExistentState";
        
        when(allStatesInProjectService.getStateId(stateName)).thenReturn(null);
        
        boolean result = stateNavigator.openState(stateName);
        
        assertFalse(result);
        verify(pathFinder, never()).getPathsToState(any(Set.class), anyLong());
    }
    
    @Test
    void testOpenStateById_TargetStateNotFound() {
        Long stateId = 42L;
        
        when(allStatesInProjectService.getState(stateId)).thenReturn(Optional.empty());
        when(allStatesInProjectService.getStateName(stateId)).thenReturn("UnknownState");
        
        boolean result = stateNavigator.openState(stateId);
        
        assertFalse(result);
        verify(pathFinder, never()).getPathsToState(any(Set.class), anyLong());
    }
    
    @Test
    void testOpenStateById_AlreadyAtTarget() {
        Long stateId = 42L;
        State state = new State();
        state.setName("HomePage");
        
        Set<Long> activeStates = new HashSet<>(Arrays.asList(stateId));
        when(allStatesInProjectService.getState(stateId)).thenReturn(Optional.of(state));
        when(allStatesInProjectService.getStateName(stateId)).thenReturn("HomePage");
        when(stateMemory.getActiveStates()).thenReturn(activeStates);
        when(stateMemory.getActiveStateNamesAsString()).thenReturn("HomePage");
        
        // Create a dummy path to pass the empty check
        Paths paths = new Paths();
        Path dummyPath = new Path();
        dummyPath.add(stateId);
        paths.addPath(dummyPath);
        
        when(pathFinder.getPathsToState(activeStates, stateId)).thenReturn(paths);
        when(pathTraverser.finishTransition(stateId)).thenReturn(true);
        when(allStatesInProjectService.findSetById(any(Set.class))).thenReturn(new HashSet<>());
        when(automationSession.getCurrentSessionId()).thenReturn("test-session-123");
        
        boolean result = stateNavigator.openState(stateId);
        
        assertTrue(result);
        verify(pathTraverser).finishTransition(stateId);
        verify(pathTraverser, never()).traverse(any());
    }
    
    @Test
    void testOpenStateById_PathTraversalFails_AlternativePathSucceeds() {
        Long stateId = 42L;
        State state = new State();
        state.setName("HomePage");
        
        Set<Long> activeStates = new HashSet<>(Arrays.asList(1L));
        Set<Long> newActiveStates = new HashSet<>(Arrays.asList(2L));
        
        when(allStatesInProjectService.getState(stateId)).thenReturn(Optional.of(state));
        when(allStatesInProjectService.getStateName(stateId)).thenReturn("HomePage");
        when(stateMemory.getActiveStates()).thenReturn(activeStates).thenReturn(newActiveStates);
        when(stateMemory.getActiveStateNamesAsString()).thenReturn("LoginPage");
        
        // First path fails
        Path path1 = new Path();
        path1.add(1L);
        path1.add(2L);
        path1.add(stateId);
        
        // Alternative path
        Path path2 = new Path();
        path2.add(2L);
        path2.add(stateId);
        
        Paths initialPaths = new Paths();
        initialPaths.addPath(path1);
        
        Paths cleanedPaths = new Paths();
        cleanedPaths.addPath(path2);
        
        when(pathFinder.getPathsToState(activeStates, stateId)).thenReturn(initialPaths);
        when(pathTraverser.traverse(path1)).thenReturn(false);
        when(pathTraverser.getFailedTransitionStartState()).thenReturn(1L);
        when(pathManager.getCleanPaths(newActiveStates, initialPaths, 1L)).thenReturn(cleanedPaths);
        when(pathTraverser.traverse(path2)).thenReturn(true);
        when(allStatesInProjectService.findSetById(any(Set.class))).thenReturn(new HashSet<>());
        when(automationSession.getCurrentSessionId()).thenReturn("test-session-123");
        
        boolean result = stateNavigator.openState(stateId);
        
        assertTrue(result);
        verify(pathTraverser).traverse(path1);
        verify(pathTraverser).traverse(path2);
        verify(pathManager).getCleanPaths(newActiveStates, initialPaths, 1L);
    }
    
    @Test
    void testOpenStateById_AllPathsFail() {
        Long stateId = 42L;
        State state = new State();
        state.setName("HomePage");
        
        Set<Long> activeStates = new HashSet<>(Arrays.asList(1L));
        
        when(allStatesInProjectService.getState(stateId)).thenReturn(Optional.of(state));
        when(allStatesInProjectService.getStateName(stateId)).thenReturn("HomePage");
        when(stateMemory.getActiveStates()).thenReturn(activeStates);
        when(stateMemory.getActiveStateNamesAsString()).thenReturn("LoginPage");
        
        Path path = new Path();
        path.add(1L);
        path.add(stateId);
        
        Paths initialPaths = new Paths();
        initialPaths.addPath(path);
        
        Paths emptyPaths = new Paths();
        
        when(pathFinder.getPathsToState(activeStates, stateId)).thenReturn(initialPaths);
        when(pathTraverser.traverse(path)).thenReturn(false);
        when(pathTraverser.getFailedTransitionStartState()).thenReturn(1L);
        when(pathManager.getCleanPaths(activeStates, initialPaths, 1L)).thenReturn(emptyPaths);
        when(allStatesInProjectService.findSetById(any(Set.class))).thenReturn(new HashSet<>());
        when(automationSession.getCurrentSessionId()).thenReturn("test-session-123");
        
        boolean result = stateNavigator.openState(stateId);
        
        assertFalse(result);
        verify(pathTraverser).traverse(path);
    }
    
    @Test
    void testOpenStateById_EmptyPaths() {
        Long stateId = 42L;
        State state = new State();
        state.setName("HomePage");
        
        Set<Long> activeStates = new HashSet<>(Arrays.asList(1L));
        
        when(allStatesInProjectService.getState(stateId)).thenReturn(Optional.of(state));
        when(allStatesInProjectService.getStateName(stateId)).thenReturn("HomePage");
        when(stateMemory.getActiveStates()).thenReturn(activeStates);
        when(stateMemory.getActiveStateNamesAsString()).thenReturn("LoginPage");
        
        Paths emptyPaths = new Paths();
        
        when(pathFinder.getPathsToState(activeStates, stateId)).thenReturn(emptyPaths);
        when(allStatesInProjectService.findSetById(any(Set.class))).thenReturn(new HashSet<>());
        when(automationSession.getCurrentSessionId()).thenReturn("test-session-123");
        
        boolean result = stateNavigator.openState(stateId);
        
        assertFalse(result);
        verify(pathTraverser, never()).traverse(any());
    }
    
    @Test
    void testOpenStateById_LoggingInteractions() {
        Long stateId = 42L;
        State state = new State();
        state.setName("HomePage");
        
        Set<Long> activeStates = new HashSet<>(Arrays.asList(1L));
        
        when(allStatesInProjectService.getState(stateId)).thenReturn(Optional.of(state));
        when(allStatesInProjectService.getStateName(stateId)).thenReturn("HomePage");
        when(stateMemory.getActiveStates()).thenReturn(activeStates);
        when(stateMemory.getActiveStateNamesAsString()).thenReturn("LoginPage");
        
        Path path = new Path();
        path.add(1L);
        path.add(stateId);
        
        Paths paths = new Paths();
        paths.addPath(path);
        
        when(pathFinder.getPathsToState(activeStates, stateId)).thenReturn(paths);
        when(pathTraverser.traverse(path)).thenReturn(true);
        
        Set<State> activeStatesSet = new HashSet<>();
        when(allStatesInProjectService.findSetById(activeStates)).thenReturn(activeStatesSet);
        when(automationSession.getCurrentSessionId()).thenReturn("test-session-123");
        
        boolean result = stateNavigator.openState(stateId);
        
        assertTrue(result);
        
        // Verify logging
        verify(actionLogger).logObservation(eq("test-session-123"), 
                eq("Transition start:"), 
                eq("Transition from LoginPage to HomePage"), 
                eq("info"));
        
        verify(actionLogger).logStateTransition(
                eq("test-session-123"),
                eq(activeStatesSet),
                argThat(set -> set.size() == 1 && set.contains(state)),
                eq(activeStatesSet),
                eq(true),
                anyLong()
        );
    }
    
    @Test
    void testRecursePaths_MultipleRecursionLevels() {
        Long stateId = 42L;
        State state = new State();
        state.setName("HomePage");
        
        Set<Long> activeStates1 = new HashSet<>(Arrays.asList(1L));
        Set<Long> activeStates2 = new HashSet<>(Arrays.asList(2L));
        Set<Long> activeStates3 = new HashSet<>(Arrays.asList(3L));
        
        when(allStatesInProjectService.getState(stateId)).thenReturn(Optional.of(state));
        when(allStatesInProjectService.getStateName(stateId)).thenReturn("HomePage");
        when(stateMemory.getActiveStates())
                .thenReturn(activeStates1)
                .thenReturn(activeStates2)
                .thenReturn(activeStates3);
        when(stateMemory.getActiveStateNamesAsString()).thenReturn("State1");
        
        // Set up three paths that fail in sequence
        Path path1 = new Path();
        path1.add(1L);
        path1.add(stateId);
        
        Path path2 = new Path();
        path2.add(2L);
        path2.add(stateId);
        
        Path path3 = new Path();
        path3.add(3L);
        path3.add(stateId);
        
        Paths paths1 = new Paths();
        paths1.addPath(path1);
        
        Paths paths2 = new Paths();
        paths2.addPath(path2);
        
        Paths paths3 = new Paths();
        paths3.addPath(path3);
        
        when(pathFinder.getPathsToState(activeStates1, stateId)).thenReturn(paths1);
        when(pathTraverser.traverse(path1)).thenReturn(false);
        when(pathTraverser.traverse(path2)).thenReturn(false);
        when(pathTraverser.traverse(path3)).thenReturn(true);
        
        when(pathTraverser.getFailedTransitionStartState()).thenReturn(1L).thenReturn(2L);
        when(pathManager.getCleanPaths(activeStates2, paths1, 1L)).thenReturn(paths2);
        when(pathManager.getCleanPaths(activeStates3, paths2, 2L)).thenReturn(paths3);
        
        when(allStatesInProjectService.findSetById(any(Set.class))).thenReturn(new HashSet<>());
        when(automationSession.getCurrentSessionId()).thenReturn("test-session-123");
        
        boolean result = stateNavigator.openState(stateId);
        
        assertTrue(result);
        verify(pathTraverser, times(3)).traverse(any());
        verify(pathManager, times(2)).getCleanPaths(any(), any(), anyLong());
    }
}