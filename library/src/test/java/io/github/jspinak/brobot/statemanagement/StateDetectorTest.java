package io.github.jspinak.brobot.statemanagement;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.special.SpecialStateType;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.test.BrobotTestBase;
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

@DisplayName("StateDetector Tests")
public class StateDetectorTest extends BrobotTestBase {
    
    @Mock
    private Action action;
    
    @Mock
    private StateService stateService;
    
    @Mock
    private StateMemory stateMemory;
    
    private StateDetector stateDetector;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        stateDetector = new StateDetector(stateService, stateMemory, action);
    }
    
    @Nested
    @DisplayName("Check Active States")
    class CheckActiveStates {
        
        @Test
        @DisplayName("Should verify active states are still visible")
        public void testCheckActiveStatesStillVisible() {
            State activeState1 = createMockState(1L, "ActiveState1");
            State activeState2 = createMockState(2L, "ActiveState2");
            
            when(stateMemory.getActiveStates())
                .thenReturn(new HashSet<>(Arrays.asList(1L, 2L)));
            when(stateService.getState(1L)).thenReturn(Optional.of(activeState1));
            when(stateService.getState(2L)).thenReturn(Optional.of(activeState2));
            
            ActionResult successResult = mock(ActionResult.class);
            when(successResult.isSuccess()).thenReturn(true);
            when(action.find(any(ObjectCollection.class))).thenReturn(successResult);
            
            stateDetector.checkForActiveStates();
            
            // Verify both states were checked and neither was removed
            verify(action, times(2)).find(any(ObjectCollection.class));
            verify(stateMemory, never()).removeInactiveState(anyLong());
        }
        
        @Test
        @DisplayName("Should remove states that are no longer visible")
        public void testCheckActiveStatesRemoveInvisible() {
            State activeState1 = createMockState(1L, "ActiveState1");
            State activeState2 = createMockState(2L, "ActiveState2");
            State activeState3 = createMockState(3L, "ActiveState3");
            
            when(stateMemory.getActiveStates())
                .thenReturn(new HashSet<>(Arrays.asList(1L, 2L, 3L)));
            when(stateService.getState(1L)).thenReturn(Optional.of(activeState1));
            when(stateService.getState(2L)).thenReturn(Optional.of(activeState2));
            when(stateService.getState(3L)).thenReturn(Optional.of(activeState3));
            
            ActionResult successResult = mock(ActionResult.class);
            ActionResult failureResult = mock(ActionResult.class);
            when(successResult.isSuccess()).thenReturn(true);
            when(failureResult.isSuccess()).thenReturn(false);
            
            // State1 and State3 are found, State2 is not
            when(action.find(any(ObjectCollection.class)))
                .thenReturn(successResult, failureResult, successResult);
            
            stateDetector.checkForActiveStates();
            
            // Verify state2 was removed since it wasn't found
            verify(stateMemory).removeInactiveState(2L);
        }
        
        @Test
        @DisplayName("Should handle empty active states")
        public void testCheckActiveStatesWhenEmpty() {
            when(stateMemory.getActiveStates()).thenReturn(new HashSet<>());
            
            stateDetector.checkForActiveStates();
            
            verify(action, never()).find(any(ObjectCollection.class));
        }
    }
    
    @Nested
    @DisplayName("Rebuild Active States")
    class RebuildActiveStates {
        
        @Test
        @DisplayName("Should rebuild active states from scratch")
        public void testRebuildActiveStates() {
            State state1 = createMockState(1L, "State1");
            State state2 = createMockState(2L, "State2");
            State state3 = createMockState(3L, "State3");
            
            // First check returns empty, triggering full search
            when(stateMemory.getActiveStates())
                .thenReturn(new HashSet<>())
                .thenReturn(new HashSet<>()) // Second call during searchAll
                .thenReturn(new HashSet<>(Arrays.asList(1L, 3L))); // After states found
                
            when(stateService.getAllStateNames()).thenReturn(new HashSet<>(Arrays.asList("State1", "State2", "State3")));
            when(stateService.getState("State1")).thenReturn(Optional.of(state1));
            when(stateService.getState("State2")).thenReturn(Optional.of(state2));
            when(stateService.getState("State3")).thenReturn(Optional.of(state3));
            
            ActionResult successResult = mock(ActionResult.class);
            ActionResult failureResult = mock(ActionResult.class);
            when(successResult.isSuccess()).thenReturn(true);
            when(failureResult.isSuccess()).thenReturn(false);
            
            // State1 and State3 are found, State2 is not
            when(action.find(any(ObjectCollection.class)))
                .thenReturn(successResult, failureResult, successResult);
            
            stateDetector.rebuildActiveStates();
            
            // Verify that finding occurred for all states
            verify(action, times(3)).find(any(ObjectCollection.class));
            // Note: addActiveState is called by the Action framework when patterns are found
        }
        
        @Test
        @DisplayName("Should handle no states found during rebuild")
        public void testRebuildActiveStatesNoneFound() {
            State state1 = createMockState(1L, "State1");
            State state2 = createMockState(2L, "State2");
            
            when(stateService.getAllStateNames()).thenReturn(new HashSet<>(Arrays.asList("State1", "State2")));
            when(stateService.getState("State1")).thenReturn(Optional.of(state1));
            when(stateService.getState("State2")).thenReturn(Optional.of(state2));
            when(stateMemory.getActiveStates()).thenReturn(new HashSet<>());
            
            ActionResult failureResult = mock(ActionResult.class);
            when(failureResult.isSuccess()).thenReturn(false);
            when(action.find(any(ObjectCollection.class))).thenReturn(failureResult);
            
            stateDetector.rebuildActiveStates();
            
            // Should add UNKNOWN state when nothing found
            verify(stateMemory).addActiveState(SpecialStateType.UNKNOWN.getId());
        }
        
        @Test
        @DisplayName("Should set UNKNOWN state if no states found")
        public void testRebuildSetsUnknownStateWhenNoneFound() {
            State state1 = createMockState(1L, "State1");
            
            when(stateService.getAllStateNames()).thenReturn(new HashSet<>(Arrays.asList("State1")));
            when(stateService.getState("State1")).thenReturn(Optional.of(state1));
            when(stateMemory.getActiveStates()).thenReturn(new HashSet<>());
            
            ActionResult failureResult = mock(ActionResult.class);
            when(failureResult.isSuccess()).thenReturn(false);
            when(action.find(any(ObjectCollection.class))).thenReturn(failureResult);
            
            stateDetector.rebuildActiveStates();
            
            // Should add UNKNOWN state when nothing found
            verify(stateMemory).addActiveState(SpecialStateType.UNKNOWN.getId());
        }
    }
    
    @Nested
    @DisplayName("Search All States")
    class SearchAllStates {
        
        @Test
        @DisplayName("Should search all defined states")
        public void testSearchAllStates() {
            State state1 = createMockState(1L, "State1");
            State state2 = createMockState(2L, "State2");
            State state3 = createMockState(3L, "State3");
            State state4 = createMockState(4L, "State4");
            
            when(stateService.getAllStateNames())
                .thenReturn(new HashSet<>(Arrays.asList("State1", "State2", "State3", "State4")));
            when(stateService.getState("State1")).thenReturn(Optional.of(state1));
            when(stateService.getState("State2")).thenReturn(Optional.of(state2));
            when(stateService.getState("State3")).thenReturn(Optional.of(state3));
            when(stateService.getState("State4")).thenReturn(Optional.of(state4));
            
            ActionResult successResult = mock(ActionResult.class);
            ActionResult failureResult = mock(ActionResult.class);
            when(successResult.isSuccess()).thenReturn(true);
            when(failureResult.isSuccess()).thenReturn(false);
            
            // States 1, 3, and 4 are found
            when(action.find(any(ObjectCollection.class)))
                .thenReturn(successResult, failureResult, successResult, successResult);
            
            stateDetector.searchAllImagesForCurrentStates();
            
            verify(action, times(4)).find(any(ObjectCollection.class));
        }
        
        @Test
        @DisplayName("Should exclude special states from search")
        public void testSearchAllStatesExcludesSpecial() {
            State normalState = createMockState(1L, "NormalState");
            
            when(stateService.getAllStateNames())
                .thenReturn(new HashSet<>(Arrays.asList("NormalState", SpecialStateType.UNKNOWN.toString())));
            when(stateService.getState("NormalState")).thenReturn(Optional.of(normalState));
            
            ActionResult successResult = mock(ActionResult.class);
            when(successResult.isSuccess()).thenReturn(true);
            when(action.find(any(ObjectCollection.class))).thenReturn(successResult);
            
            stateDetector.searchAllImagesForCurrentStates();
            
            // Only normal state should be searched (UNKNOWN is excluded)
            verify(action, times(1)).find(any(ObjectCollection.class));
        }
    }
    
    @Nested
    @DisplayName("Find Specific State")
    class FindSpecificState {
        
        @Test
        @DisplayName("Should find specific state by name")
        public void testFindStateByName() {
            State targetState = createMockState(1L, "TargetState");
            
            when(stateService.getState("TargetState")).thenReturn(Optional.of(targetState));
            
            ActionResult successResult = mock(ActionResult.class);
            when(successResult.isSuccess()).thenReturn(true);
            when(action.find(any(ObjectCollection.class))).thenReturn(successResult);
            
            boolean foundState = stateDetector.findState("TargetState");
            
            assertTrue(foundState);
            verify(action).find(any(ObjectCollection.class));
        }
        
        @Test
        @DisplayName("Should return false if state not found visually")
        public void testFindStateNotFoundVisually() {
            State targetState = createMockState(1L, "TargetState");
            
            when(stateService.getState("TargetState")).thenReturn(Optional.of(targetState));
            
            ActionResult failureResult = mock(ActionResult.class);
            when(failureResult.isSuccess()).thenReturn(false);
            when(action.find(any(ObjectCollection.class))).thenReturn(failureResult);
            
            boolean foundState = stateDetector.findState("TargetState");
            
            assertFalse(foundState);
        }
        
        @Test
        @DisplayName("Should return false if state doesn't exist")
        public void testFindStateDoesNotExist() {
            when(stateService.getState("NonExistentState")).thenReturn(Optional.empty());
            
            boolean foundState = stateDetector.findState("NonExistentState");
            
            assertFalse(foundState);
            verify(action, never()).find(any(ObjectCollection.class));
        }
        
        @Test
        @DisplayName("Should find state by ID")
        public void testFindStateById() {
            State targetState = createMockState(42L, "StateFortyTwo");
            
            when(stateService.getStateName(42L)).thenReturn("StateFortyTwo");
            when(stateService.getState(42L)).thenReturn(Optional.of(targetState));
            
            ActionResult successResult = mock(ActionResult.class);
            when(successResult.isSuccess()).thenReturn(true);
            when(action.find(any(ObjectCollection.class))).thenReturn(successResult);
            
            boolean foundState = stateDetector.findState(42L);
            
            assertTrue(foundState);
            verify(action).find(any(ObjectCollection.class));
        }
    }
    
    @Nested
    @DisplayName("Refresh States")
    class RefreshStates {
        
        @Test
        @DisplayName("Should completely refresh active states")
        public void testRefreshStates() {
            State newState1 = createMockState(2L, "NewState1");
            State newState2 = createMockState(3L, "NewState2");
            
            // All states in the system
            when(stateService.getAllStateNames())
                .thenReturn(new HashSet<>(Arrays.asList("NewState1", "NewState2")));
            when(stateService.getState("NewState1")).thenReturn(Optional.of(newState1));
            when(stateService.getState("NewState2")).thenReturn(Optional.of(newState2));
            when(stateService.getStateName(2L)).thenReturn("NewState1");
            when(stateService.getStateName(3L)).thenReturn("NewState2");
            
            ActionResult successResult = mock(ActionResult.class);
            when(successResult.isSuccess()).thenReturn(true);
            when(action.find(any(ObjectCollection.class))).thenReturn(successResult);
            
            // Mock the active states that would be set after finding
            when(stateMemory.getActiveStates()).thenReturn(new HashSet<>(Arrays.asList(2L, 3L)));
            
            Set<Long> refreshedStates = stateDetector.refreshActiveStates();
            
            assertEquals(2, refreshedStates.size());
            assertTrue(refreshedStates.contains(2L));
            assertTrue(refreshedStates.contains(3L));
            
            verify(stateMemory).removeAllStates();
            verify(action, times(2)).find(any(ObjectCollection.class));
        }
        
        @Test
        @DisplayName("Should handle refresh with no states found")
        public void testRefreshStatesNoneFound() {
            State state1 = createMockState(1L, "State1");
            
            when(stateService.getAllStateNames()).thenReturn(new HashSet<>(Arrays.asList("State1")));
            when(stateService.getState("State1")).thenReturn(Optional.of(state1));
            
            ActionResult failureResult = mock(ActionResult.class);
            when(failureResult.isSuccess()).thenReturn(false);
            when(action.find(any(ObjectCollection.class))).thenReturn(failureResult);
            
            when(stateMemory.getActiveStates()).thenReturn(new HashSet<>());
            
            Set<Long> refreshedStates = stateDetector.refreshActiveStates();
            
            assertTrue(refreshedStates.isEmpty());
            verify(stateMemory).removeAllStates();
        }
    }
    
    @Nested
    @DisplayName("State Detection Strategy")
    class StateDetectionStrategy {
        
        @Test
        @DisplayName("Should use efficient strategy for checking known states")
        public void testEfficientKnownStateCheck() {
            State knownState1 = createMockState(1L, "KnownState1");
            State knownState2 = createMockState(2L, "KnownState2");
            
            when(stateMemory.getActiveStates())
                .thenReturn(new HashSet<>(Arrays.asList(1L, 2L)));
            when(stateService.getState(1L)).thenReturn(Optional.of(knownState1));
            when(stateService.getState(2L)).thenReturn(Optional.of(knownState2));
            
            ActionResult successResult = mock(ActionResult.class);
            when(successResult.isSuccess()).thenReturn(true);
            when(action.find(any(ObjectCollection.class))).thenReturn(successResult);
            
            // Check known states first
            stateDetector.checkForActiveStates();
            
            // Should only check the 2 known states, not all states
            verify(action, times(2)).find(any(ObjectCollection.class));
        }
        
        @Test
        @DisplayName("Should fall back to full search when no active states")
        public void testFallbackToFullSearch() {
            State state1 = createMockState(1L, "State1");
            State state2 = createMockState(2L, "State2");
            
            // First call returns empty, triggering search
            when(stateMemory.getActiveStates()).thenReturn(new HashSet<>());
            when(stateService.getAllStateNames()).thenReturn(new HashSet<>(Arrays.asList("State1", "State2")));
            when(stateService.getState("State1")).thenReturn(Optional.of(state1));
            when(stateService.getState("State2")).thenReturn(Optional.of(state2));
            
            ActionResult successResult = mock(ActionResult.class);
            when(successResult.isSuccess()).thenReturn(true);
            when(action.find(any(ObjectCollection.class))).thenReturn(successResult);
            
            // When no active states, rebuild will search all
            stateDetector.rebuildActiveStates();
            
            verify(action, times(2)).find(any(ObjectCollection.class));
        }
    }
    
    @Nested
    @DisplayName("Concurrent State Detection")
    class ConcurrentStateDetection {
        
        @Test
        @DisplayName("Should handle multiple states being active simultaneously")
        public void testMultipleActiveStates() {
            State mainState = createMockState(1L, "MainState");
            State sidebarState = createMockState(2L, "SidebarState");
            State headerState = createMockState(3L, "HeaderState");
            
            when(stateService.getAllStateNames())
                .thenReturn(new HashSet<>(Arrays.asList("MainState", "SidebarState", "HeaderState")));
            when(stateService.getState("MainState")).thenReturn(Optional.of(mainState));
            when(stateService.getState("SidebarState")).thenReturn(Optional.of(sidebarState));
            when(stateService.getState("HeaderState")).thenReturn(Optional.of(headerState));
            
            ActionResult successResult = mock(ActionResult.class);
            when(successResult.isSuccess()).thenReturn(true);
            when(action.find(any(ObjectCollection.class))).thenReturn(successResult);
            
            stateDetector.searchAllImagesForCurrentStates();
            
            // All three states should be checked
            verify(action, times(3)).find(any(ObjectCollection.class));
        }
        
        @Test
        @DisplayName("Should handle blocking state detection")
        public void testBlockingStateDetection() {
            State normalState = createMockState(1L, "NormalState");
            State blockingState = createMockState(2L, "BlockingModal");
            blockingState.setBlocking(true);
            
            when(stateService.getAllStateNames())
                .thenReturn(new HashSet<>(Arrays.asList("NormalState", "BlockingModal")));
            when(stateService.getState("NormalState")).thenReturn(Optional.of(normalState));
            when(stateService.getState("BlockingModal")).thenReturn(Optional.of(blockingState));
            
            ActionResult successResult = mock(ActionResult.class);
            when(successResult.isSuccess()).thenReturn(true);
            when(action.find(any(ObjectCollection.class))).thenReturn(successResult);
            
            stateDetector.searchAllImagesForCurrentStates();
            
            // Both states should be checked
            verify(action, times(2)).find(any(ObjectCollection.class));
        }
    }
    
    // Helper methods
    private State createMockState(Long id, String name) {
        State state = mock(State.class);
        when(state.getId()).thenReturn(id);
        when(state.getName()).thenReturn(name);
        when(state.getStateImages()).thenReturn(new HashSet<>(Arrays.asList(mock(StateImage.class))));
        when(state.isBlocking()).thenReturn(false);
        when(state.getHiddenStateIds()).thenReturn(new HashSet<>());
        return state;
    }
}