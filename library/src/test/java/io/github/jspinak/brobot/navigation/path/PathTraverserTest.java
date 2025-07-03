package io.github.jspinak.brobot.navigation.path;

import io.github.jspinak.brobot.model.state.special.SpecialStateType;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.navigation.transition.JavaStateTransition;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.navigation.transition.TransitionConditionPackager;
import io.github.jspinak.brobot.navigation.transition.TransitionExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PathTraverserTest {

    @Mock
    private TransitionExecutor doTransition;
    
    @Mock
    private StateTransitionService stateTransitionsInProjectService;
    
    @Mock
    private TransitionConditionPackager transitionBooleanSupplierPackager;
    
    private PathTraverser pathTraverser;
    
    @BeforeEach
    void setUp() {
        pathTraverser = new PathTraverser(doTransition, stateTransitionsInProjectService, transitionBooleanSupplierPackager);
    }
    
    @Test
    void testConstructor() {
        assertNotNull(pathTraverser);
        assertEquals(SpecialStateType.NULL.getId(), pathTraverser.getFailedTransitionStartState());
    }
    
    @Test
    void testTraverse_EmptyPath() {
        Path emptyPath = new Path();
        
        boolean result = pathTraverser.traverse(emptyPath);
        
        assertTrue(result);
        verify(doTransition, never()).go(anyLong(), anyLong());
    }
    
    @Test
    void testTraverse_SingleStatePath() {
        Path singleStatePath = new Path();
        singleStatePath.add(1L);
        
        boolean result = pathTraverser.traverse(singleStatePath);
        
        assertTrue(result);
        verify(doTransition, never()).go(anyLong(), anyLong());
    }
    
    @Test
    void testTraverse_SuccessfulPath() {
        Path path = new Path();
        path.add(1L);
        path.add(2L);
        path.add(3L);
        
        when(doTransition.go(1L, 2L)).thenReturn(true);
        when(doTransition.go(2L, 3L)).thenReturn(true);
        
        boolean result = pathTraverser.traverse(path);
        
        assertTrue(result);
        verify(doTransition).go(1L, 2L);
        verify(doTransition).go(2L, 3L);
        assertEquals(SpecialStateType.NULL.getId(), pathTraverser.getFailedTransitionStartState());
    }
    
    @Test
    void testTraverse_FirstTransitionFails() {
        Path path = new Path();
        path.add(1L);
        path.add(2L);
        path.add(3L);
        
        when(doTransition.go(1L, 2L)).thenReturn(false);
        
        boolean result = pathTraverser.traverse(path);
        
        assertFalse(result);
        verify(doTransition).go(1L, 2L);
        verify(doTransition, never()).go(2L, 3L);
        assertEquals(1L, pathTraverser.getFailedTransitionStartState());
    }
    
    @Test
    void testTraverse_SecondTransitionFails() {
        Path path = new Path();
        path.add(1L);
        path.add(2L);
        path.add(3L);
        
        when(doTransition.go(1L, 2L)).thenReturn(true);
        when(doTransition.go(2L, 3L)).thenReturn(false);
        
        boolean result = pathTraverser.traverse(path);
        
        assertFalse(result);
        verify(doTransition).go(1L, 2L);
        verify(doTransition).go(2L, 3L);
        assertEquals(2L, pathTraverser.getFailedTransitionStartState());
    }
    
    @Test
    void testTraverse_LongPathWithFailure() {
        Path path = new Path();
        for (long i = 1; i <= 10; i++) {
            path.add(i);
        }
        
        // Set up only the transitions that should be called
        when(doTransition.go(1L, 2L)).thenReturn(true);
        when(doTransition.go(2L, 3L)).thenReturn(true);
        when(doTransition.go(3L, 4L)).thenReturn(true);
        when(doTransition.go(4L, 5L)).thenReturn(true);
        when(doTransition.go(5L, 6L)).thenReturn(false);
        
        boolean result = pathTraverser.traverse(path);
        
        assertFalse(result);
        assertEquals(5L, pathTraverser.getFailedTransitionStartState());
        
        // Verify transitions were called up to the failure point
        for (long i = 1; i <= 5; i++) {
            verify(doTransition).go(i, i + 1);
        }
        // Verify no transitions after failure
        for (long i = 6; i < 10; i++) {
            verify(doTransition, never()).go(i, i + 1);
        }
    }
    
    @Test
    void testFinishTransition_Success() {
        Long stateToOpen = 42L;
        StateTransitions stateTransitions = mock(StateTransitions.class);
        JavaStateTransition finishTransition = mock(JavaStateTransition.class);
        
        when(stateTransitionsInProjectService.getTransitions(stateToOpen)).thenReturn(Optional.of(stateTransitions));
        when(stateTransitions.getTransitionFinish()).thenReturn(finishTransition);
        when(transitionBooleanSupplierPackager.getAsBoolean(finishTransition)).thenReturn(true);
        
        boolean result = pathTraverser.finishTransition(stateToOpen);
        
        assertTrue(result);
        verify(stateTransitionsInProjectService).getTransitions(stateToOpen);
        verify(stateTransitions).getTransitionFinish();
        verify(transitionBooleanSupplierPackager).getAsBoolean(finishTransition);
    }
    
    @Test
    void testFinishTransition_NoTransitions() {
        Long stateToOpen = 42L;
        
        when(stateTransitionsInProjectService.getTransitions(stateToOpen)).thenReturn(Optional.empty());
        
        boolean result = pathTraverser.finishTransition(stateToOpen);
        
        assertFalse(result);
        verify(stateTransitionsInProjectService).getTransitions(stateToOpen);
        verify(transitionBooleanSupplierPackager, never()).getAsBoolean(any());
    }
    
    @Test
    void testFinishTransition_TransitionFails() {
        Long stateToOpen = 42L;
        StateTransitions stateTransitions = mock(StateTransitions.class);
        JavaStateTransition finishTransition = mock(JavaStateTransition.class);
        
        when(stateTransitionsInProjectService.getTransitions(stateToOpen)).thenReturn(Optional.of(stateTransitions));
        when(stateTransitions.getTransitionFinish()).thenReturn(finishTransition);
        when(transitionBooleanSupplierPackager.getAsBoolean(finishTransition)).thenReturn(false);
        
        boolean result = pathTraverser.finishTransition(stateToOpen);
        
        assertFalse(result);
        verify(stateTransitionsInProjectService).getTransitions(stateToOpen);
        verify(stateTransitions).getTransitionFinish();
        verify(transitionBooleanSupplierPackager).getAsBoolean(finishTransition);
    }
    
    @Test
    void testGetters() {
        assertSame(doTransition, pathTraverser.getDoTransition());
        assertSame(stateTransitionsInProjectService, pathTraverser.getStateTransitionsInProjectService());
        assertSame(transitionBooleanSupplierPackager, pathTraverser.getTransitionBooleanSupplierPackager());
    }
}