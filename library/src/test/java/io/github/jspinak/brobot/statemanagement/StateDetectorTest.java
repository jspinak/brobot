package io.github.jspinak.brobot.statemanagement;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import io.github.jspinak.brobot.model.state.special.SpecialStateType;
import io.github.jspinak.brobot.navigation.service.StateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StateDetectorTest {

    @Mock
    private StateService stateService;

    @Mock
    private StateMemory stateMemory;

    @Mock
    private Action action;

    @Mock
    private State mockState1;

    @Mock
    private State mockState2;

    @Mock
    private StateImage mockStateImage1;

    @Mock
    private StateImage mockStateImage2;

    private StateDetector stateDetector;

    @BeforeEach
    void setUp() {
        stateDetector = new StateDetector(stateService, stateMemory, action);
        
        // Setup mock states
        when(mockState1.getId()).thenReturn(1L);
        when(mockState1.getName()).thenReturn("State1");
        when(mockState2.getId()).thenReturn(2L);
        when(mockState2.getName()).thenReturn("State2");
    }

    @Test
    void testCheckForActiveStates_RemovesInactiveStates() {
        // Arrange
        Set<Long> activeStates = new HashSet<>(Arrays.asList(1L, 2L));
        when(stateMemory.getActiveStates()).thenReturn(activeStates);
        
        // Mock findState calls
        when(stateService.getState(1L)).thenReturn(Optional.of(mockState1));
        when(stateService.getState(2L)).thenReturn(Optional.of(mockState2));
        
        Set<StateImage> stateImages1 = new HashSet<>(Collections.singletonList(mockStateImage1));
        Set<StateImage> stateImages2 = new HashSet<>(Collections.singletonList(mockStateImage2));
        when(mockState1.getStateImages()).thenReturn(stateImages1);
        when(mockState2.getStateImages()).thenReturn(stateImages2);
        
        // State1 is found, State2 is not
        ActionResult foundResult = new ActionResult();
        foundResult.setSuccess(true);
        ActionResult notFoundResult = new ActionResult();
        notFoundResult.setSuccess(false);
        
        // Mock action.find() which is what StateDetector actually calls
        when(action.find(any(ObjectCollection.class))).thenReturn(foundResult, notFoundResult);
        when(action.find(any(ObjectCollection[].class))).thenReturn(foundResult, notFoundResult);

        // Act
        stateDetector.checkForActiveStates();

        // Assert
        verify(stateMemory).removeInactiveState(2L);
        verify(stateMemory, never()).removeInactiveState(1L);
    }

    @Test
    void testRebuildActiveStates_SearchesAllStatesWhenNoActiveStates() {
        // Arrange
        when(stateMemory.getActiveStates()).thenReturn(new HashSet<>()).thenReturn(new HashSet<>());
        
        Set<String> allStateNames = new HashSet<>(Arrays.asList("State1", "State2"));
        when(stateService.getAllStateNames()).thenReturn(allStateNames);
        when(stateService.getState("State1")).thenReturn(Optional.of(mockState1));
        when(stateService.getState("State2")).thenReturn(Optional.of(mockState2));
        
        Set<StateImage> stateImages1 = new HashSet<>(Collections.singletonList(mockStateImage1));
        Set<StateImage> stateImages2 = new HashSet<>(Collections.singletonList(mockStateImage2));
        when(mockState1.getStateImages()).thenReturn(stateImages1);
        when(mockState2.getStateImages()).thenReturn(stateImages2);
        
        // No states found
        ActionResult notFoundResult = new ActionResult();
        notFoundResult.setSuccess(false);
        
        // Mock action.find() which is what StateDetector actually calls
        when(action.find(any(ObjectCollection.class))).thenReturn(notFoundResult);
        when(action.find(any(ObjectCollection[].class))).thenReturn(notFoundResult);

        // Act
        stateDetector.rebuildActiveStates();

        // Assert
        verify(stateService).getAllStateNames();
        // Verify action.find() was called twice (once for each state)
        verify(action, times(2)).find(any(ObjectCollection.class));
        verify(stateMemory).addActiveState(SpecialStateType.UNKNOWN.getId());
    }

    @Test
    void testRebuildActiveStates_DoesNotSearchWhenActiveStatesExist() {
        // Arrange
        Set<Long> activeStates = new HashSet<>(Collections.singletonList(1L));
        when(stateMemory.getActiveStates()).thenReturn(activeStates).thenReturn(activeStates);
        
        when(stateService.getState(1L)).thenReturn(Optional.of(mockState1));
        Set<StateImage> stateImages1 = new HashSet<>(Collections.singletonList(mockStateImage1));
        when(mockState1.getStateImages()).thenReturn(stateImages1);
        
        // State1 is found
        ActionResult foundResult = new ActionResult();
        foundResult.setSuccess(true);
        
        // Mock action.find() which is what StateDetector actually calls
        when(action.find(any(ObjectCollection.class))).thenReturn(foundResult);
        when(action.find(any(ObjectCollection[].class))).thenReturn(foundResult);

        // Act
        stateDetector.rebuildActiveStates();

        // Assert
        verify(stateService, never()).getAllStateNames(); // Should not search all states
    }

    @Test
    void testSearchAllImagesForCurrentStates_FindsMultipleStates() {
        // Arrange
        Set<String> allStateNames = new HashSet<>(Arrays.asList("State1", "State2"));
        when(stateService.getAllStateNames()).thenReturn(allStateNames);
        when(stateService.getState("State1")).thenReturn(Optional.of(mockState1));
        when(stateService.getState("State2")).thenReturn(Optional.of(mockState2));
        
        Set<StateImage> stateImages1 = new HashSet<>(Collections.singletonList(mockStateImage1));
        Set<StateImage> stateImages2 = new HashSet<>(Collections.singletonList(mockStateImage2));
        when(mockState1.getStateImages()).thenReturn(stateImages1);
        when(mockState2.getStateImages()).thenReturn(stateImages2);
        
        // Both states are found
        ActionResult foundResult1 = new ActionResult();
        foundResult1.setSuccess(true);
        
        ActionResult foundResult2 = new ActionResult();
        foundResult2.setSuccess(true);
        
        // Mock action.find() which is what StateDetector actually calls
        when(action.find(any(ObjectCollection.class))).thenReturn(foundResult1, foundResult2);
        when(action.find(any(ObjectCollection[].class))).thenReturn(foundResult1, foundResult2);

        // Act
        stateDetector.searchAllImagesForCurrentStates();

        // Assert
        // Verify action.find() was called twice (once for each state)
        verify(action, times(2)).find(any(ObjectCollection.class));
    }

    @Test
    void testFindState_ReturnsTrueWhenFound() {
        // Arrange
        String stateName = "State1";
        when(stateService.getState(stateName)).thenReturn(Optional.of(mockState1));
        
        Set<StateImage> stateImages = new HashSet<>(Collections.singletonList(mockStateImage1));
        when(mockState1.getStateImages()).thenReturn(stateImages);
        
        ActionResult foundResult = new ActionResult();
        foundResult.setSuccess(true);
        
        // Mock action.find() which is what StateDetector actually calls
        when(action.find(any(ObjectCollection.class))).thenReturn(foundResult);
        when(action.find(any(ObjectCollection[].class))).thenReturn(foundResult);

        // Act
        boolean result = stateDetector.findState(stateName);

        // Assert
        assertTrue(result);
    }

    @Test
    void testFindState_ReturnsFalseWhenNotFound() {
        // Arrange
        String stateName = "State1";
        when(stateService.getState(stateName)).thenReturn(Optional.of(mockState1));
        
        Set<StateImage> stateImages = new HashSet<>(Collections.singletonList(mockStateImage1));
        when(mockState1.getStateImages()).thenReturn(stateImages);
        
        ActionResult notFoundResult = new ActionResult();
        notFoundResult.setSuccess(false);
        
        // Mock action.find() which is what StateDetector actually calls
        when(action.find(any(ObjectCollection.class))).thenReturn(notFoundResult);
        when(action.find(any(ObjectCollection[].class))).thenReturn(notFoundResult);

        // Act
        boolean result = stateDetector.findState(stateName);

        // Assert
        assertFalse(result);
    }

    @Test
    void testFindState_ReturnsFalseWhenStateDoesNotExist() {
        // Arrange
        String stateName = "NonExistentState";
        when(stateService.getState(stateName)).thenReturn(Optional.empty());

        // Act
        boolean result = stateDetector.findState(stateName);

        // Assert
        assertFalse(result);
        // Verify action.find() was never called
        verify(action, never()).find(any(ObjectCollection.class));
    }

    @Test
    void testRefreshActiveStates_ClearsAndRebuilds() {
        // Arrange
        Set<String> allStateNames = new HashSet<>(Collections.singletonList("State1"));
        when(stateService.getAllStateNames()).thenReturn(allStateNames);
        when(stateService.getState("State1")).thenReturn(Optional.of(mockState1));
        
        Set<StateImage> stateImages = new HashSet<>(Collections.singletonList(mockStateImage1));
        when(mockState1.getStateImages()).thenReturn(stateImages);
        
        ActionResult foundResult = new ActionResult();
        foundResult.setSuccess(true);
        
        // Mock action.find() which is what StateDetector actually calls
        when(action.find(any(ObjectCollection.class))).thenReturn(foundResult);
        when(action.find(any(ObjectCollection[].class))).thenReturn(foundResult);
        
        Set<Long> returnedStates = new HashSet<>(Collections.singletonList(1L));
        when(stateMemory.getActiveStates()).thenReturn(returnedStates);

        // Act
        Set<Long> result = stateDetector.refreshActiveStates();

        // Assert
        verify(stateMemory).removeAllStates();
        verify(stateService).getAllStateNames();
        assertEquals(returnedStates, result);
    }

    @Test
    void testFindStateById_ReturnsTrueWhenFound() {
        // Arrange
        Long stateId = 1L;
        when(stateService.getState(stateId)).thenReturn(Optional.of(mockState1));
        when(stateService.getStateName(stateId)).thenReturn("State1");
        
        Set<StateImage> stateImages = new HashSet<>(Collections.singletonList(mockStateImage1));
        when(mockState1.getStateImages()).thenReturn(stateImages);
        
        ActionResult foundResult = new ActionResult();
        foundResult.setSuccess(true);
        
        // Mock action.find() which is what StateDetector actually calls
        when(action.find(any(ObjectCollection.class))).thenReturn(foundResult);
        when(action.find(any(ObjectCollection[].class))).thenReturn(foundResult);

        // Act
        boolean result = stateDetector.findState(stateId);

        // Assert
        assertTrue(result);
    }

    @Test
    void testFindStateById_ReturnsFalseWhenNotFound() {
        // Arrange
        Long stateId = 1L;
        when(stateService.getState(stateId)).thenReturn(Optional.of(mockState1));
        when(stateService.getStateName(stateId)).thenReturn("State1");
        
        Set<StateImage> stateImages = new HashSet<>(Collections.singletonList(mockStateImage1));
        when(mockState1.getStateImages()).thenReturn(stateImages);
        
        ActionResult notFoundResult = new ActionResult();
        notFoundResult.setSuccess(false);
        
        // Mock action.find() which is what StateDetector actually calls
        when(action.find(any(ObjectCollection.class))).thenReturn(notFoundResult);
        when(action.find(any(ObjectCollection[].class))).thenReturn(notFoundResult);

        // Act
        boolean result = stateDetector.findState(stateId);

        // Assert
        assertFalse(result);
    }

    @Test
    void testSearchAllImagesForCurrentStates_RemovesUnknownFromSearch() {
        // Arrange
        Set<String> allStateNames = new HashSet<>(Arrays.asList("State1", SpecialStateType.UNKNOWN.toString()));
        when(stateService.getAllStateNames()).thenReturn(allStateNames);
        when(stateService.getState("State1")).thenReturn(Optional.of(mockState1));
        
        Set<StateImage> stateImages = new HashSet<>(Collections.singletonList(mockStateImage1));
        when(mockState1.getStateImages()).thenReturn(stateImages);
        
        ActionResult foundResult = new ActionResult();
        foundResult.setSuccess(true);
        
        // Mock action.find() which is what StateDetector actually calls
        when(action.find(any(ObjectCollection.class))).thenReturn(foundResult);
        when(action.find(any(ObjectCollection[].class))).thenReturn(foundResult);

        // Act
        stateDetector.searchAllImagesForCurrentStates();

        // Assert
        verify(stateService, times(1)).getState("State1");
        verify(stateService, never()).getState(SpecialStateType.UNKNOWN.toString());
    }
}