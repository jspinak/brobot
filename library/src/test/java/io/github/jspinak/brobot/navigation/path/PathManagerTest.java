package io.github.jspinak.brobot.navigation.path;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.transition.JavaStateTransition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PathManagerTest {

    @Mock
    private StateService allStatesInProjectService;
    
    private PathManager pathManager;
    
    @BeforeEach
    void setUp() {
        pathManager = new PathManager(allStatesInProjectService);
    }
    
    @Test
    void testUpdateScore_AllStatesFound() {
        Path path = new Path();
        path.add(1L);
        path.add(2L);
        path.add(3L);
        
        State state1 = new State();
        state1.setPathScore(10);
        State state2 = new State();
        state2.setPathScore(20);
        State state3 = new State();
        state3.setPathScore(30);
        
        when(allStatesInProjectService.getState(1L)).thenReturn(Optional.of(state1));
        when(allStatesInProjectService.getState(2L)).thenReturn(Optional.of(state2));
        when(allStatesInProjectService.getState(3L)).thenReturn(Optional.of(state3));
        
        pathManager.updateScore(path);
        
        assertEquals(60, path.getScore());
        verify(allStatesInProjectService).getState(1L);
        verify(allStatesInProjectService).getState(2L);
        verify(allStatesInProjectService).getState(3L);
    }
    
    @Test
    void testUpdateScore_SomeStatesNotFound() {
        Path path = new Path();
        path.add(1L);
        path.add(2L);
        path.add(3L);
        
        State state1 = new State();
        state1.setPathScore(10);
        State state3 = new State();
        state3.setPathScore(30);
        
        when(allStatesInProjectService.getState(1L)).thenReturn(Optional.of(state1));
        when(allStatesInProjectService.getState(2L)).thenReturn(Optional.empty());
        when(allStatesInProjectService.getState(3L)).thenReturn(Optional.of(state3));
        
        pathManager.updateScore(path);
        
        assertEquals(40, path.getScore());
    }
    
    @Test
    void testUpdateScore_EmptyPath() {
        Path path = new Path();
        
        pathManager.updateScore(path);
        
        assertEquals(0, path.getScore());
        verify(allStatesInProjectService, never()).getState(anyLong());
    }
    
    @Test
    void testUpdateScore_SingleStatePath() {
        Path path = new Path();
        path.add(5L);
        
        State state = new State();
        state.setPathScore(42);
        
        when(allStatesInProjectService.getState(5L)).thenReturn(Optional.of(state));
        
        pathManager.updateScore(path);
        
        assertEquals(42, path.getScore());
    }
    
    @Test
    void testUpdateScores_MultiplePaths() {
        Paths paths = new Paths();
        Path path1 = new Path();
        path1.add(1L);
        Path path2 = new Path();
        path2.add(2L);
        paths.addPath(path1);
        paths.addPath(path2);
        
        State state1 = new State();
        state1.setPathScore(30);
        State state2 = new State();
        state2.setPathScore(10);
        
        when(allStatesInProjectService.getState(1L)).thenReturn(Optional.of(state1));
        when(allStatesInProjectService.getState(2L)).thenReturn(Optional.of(state2));
        
        pathManager.updateScores(paths);
        
        assertEquals(30, path1.getScore());
        assertEquals(10, path2.getScore());
        
        // Verify paths are sorted (path2 should be first since it has lower score)
        assertEquals(path2, paths.getPaths().get(0));
        assertEquals(path1, paths.getPaths().get(1));
    }
    
    @Test
    void testUpdateScores_EmptyPaths() {
        Paths paths = new Paths();
        
        pathManager.updateScores(paths);
        
        assertTrue(paths.isEmpty());
        verify(allStatesInProjectService, never()).getState(anyLong());
    }
    
    @Test
    void testGetCleanPaths() {
        // Setup active states
        Set<Long> activeStates = new HashSet<>();
        activeStates.add(2L);
        
        // Setup paths - path1 contains failed transition, path2 doesn't
        Paths originalPaths = new Paths();
        Path path1 = new Path();
        path1.add(1L);
        path1.add(2L);
        path1.add(3L);
        // Add transitions for each state (required by trimPath)
        for (int i = 0; i < 3; i++) {
            path1.add(mock(JavaStateTransition.class));
        }
        
        Path path2 = new Path();
        path2.add(4L);
        path2.add(5L);
        // Add transitions
        for (int i = 0; i < 2; i++) {
            path2.add(mock(JavaStateTransition.class));
        }
        
        originalPaths.addPath(path1);
        originalPaths.addPath(path2);
        
        // No need to setup states since both paths will be filtered out
        
        Long failedTransitionStart = 1L;
        
        Paths cleanPaths = pathManager.getCleanPaths(activeStates, originalPaths, failedTransitionStart);
        
        // path1 should be removed because it contains failed transition
        // path2 should be removed because it doesn't contain any active states
        assertEquals(0, cleanPaths.getPaths().size());
    }
    
    @Test
    void testGetCleanPaths_NoActiveStates() {
        Set<Long> activeStates = new HashSet<>();
        Paths originalPaths = new Paths();
        Path path = new Path();
        path.add(1L);
        path.add(2L);
        originalPaths.addPath(path);
        
        Long failedTransitionStart = 10L;
        
        Paths cleanPaths = pathManager.getCleanPaths(activeStates, originalPaths, failedTransitionStart);
        
        assertTrue(cleanPaths.isEmpty());
    }
    
    @Test
    void testGetCleanPaths_MultipleActiveStates() {
        Set<Long> activeStates = new HashSet<>();
        activeStates.add(2L);
        activeStates.add(3L);
        
        Paths originalPaths = new Paths();
        Path path1 = new Path();
        path1.add(1L);
        path1.add(2L);
        path1.add(4L);
        // Add transitions for each state
        for (int i = 0; i < 3; i++) {
            path1.add(mock(JavaStateTransition.class));
        }
        
        Path path2 = new Path();
        path2.add(1L);
        path2.add(3L);
        path2.add(4L);
        // Add transitions for each state
        for (int i = 0; i < 3; i++) {
            path2.add(mock(JavaStateTransition.class));
        }
        
        originalPaths.addPath(path1);
        originalPaths.addPath(path2);
        
        // No need to setup states since both paths will be filtered out due to failed transition
        
        Long failedTransitionStart = 1L;
        
        Paths cleanPaths = pathManager.getCleanPaths(activeStates, originalPaths, failedTransitionStart);
        
        // Both paths should be removed because they contain the failed transition starting at state 1
        assertEquals(0, cleanPaths.getPaths().size());
    }
}