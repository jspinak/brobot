package io.github.jspinak.brobot.statemanagement;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.special.SpecialStateType;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.stream.Collectors;

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
    
    @Mock
    private ConsoleReporter consoleReporter;
    
    private StateDetector stateDetector;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        stateDetector = new StateDetector(action, stateService, stateMemory, consoleReporter);
    }
    
    @Nested
    @DisplayName("Check Active States")
    class CheckActiveStates {
        
        @Test
        @DisplayName("Should verify active states are still visible")
        public void testCheckActiveStatesStillVisible() {
            State activeState1 = createMockState(1L, "ActiveState1");
            State activeState2 = createMockState(2L, "ActiveState2");
            
            when(stateMemory.getActiveStatesObjects())
                .thenReturn(Arrays.asList(activeState1, activeState2));
            
            ActionResult successResult = mock(ActionResult.class);
            when(successResult.isSuccess()).thenReturn(true);
            when(action.find(any(ObjectCollection.class))).thenReturn(successResult);
            
            Set<State> stillActive = stateDetector.checkActiveStates();
            
            assertEquals(2, stillActive.size());
            assertTrue(stillActive.contains(activeState1));
            assertTrue(stillActive.contains(activeState2));
            verify(action, times(2)).find(any(ObjectCollection.class));
        }
        
        @Test
        @DisplayName("Should remove states that are no longer visible")
        public void testCheckActiveStatesRemoveInvisible() {
            State activeState1 = createMockState(1L, "ActiveState1");
            State activeState2 = createMockState(2L, "ActiveState2");
            State activeState3 = createMockState(3L, "ActiveState3");
            
            when(stateMemory.getActiveStatesObjects())
                .thenReturn(Arrays.asList(activeState1, activeState2, activeState3));
            
            ActionResult successResult = mock(ActionResult.class);
            ActionResult failureResult = mock(ActionResult.class);
            when(successResult.isSuccess()).thenReturn(true);
            when(failureResult.isSuccess()).thenReturn(false);
            
            // State1 and State3 are found, State2 is not
            when(action.find(any(ObjectCollection.class)))
                .thenReturn(successResult, failureResult, successResult);
            
            Set<State> stillActive = stateDetector.checkActiveStates();
            
            assertEquals(2, stillActive.size());
            assertTrue(stillActive.contains(activeState1));
            assertFalse(stillActive.contains(activeState2));
            assertTrue(stillActive.contains(activeState3));
            
            verify(stateMemory).removeActiveState(2L);
        }
        
        @Test
        @DisplayName("Should handle empty active states")
        public void testCheckActiveStatesWhenEmpty() {
            when(stateMemory.getActiveStatesObjects()).thenReturn(new ArrayList<>());
            
            Set<State> stillActive = stateDetector.checkActiveStates();
            
            assertTrue(stillActive.isEmpty());
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
            
            when(stateService.findAll()).thenReturn(Arrays.asList(state1, state2, state3));
            
            ActionResult successResult = mock(ActionResult.class);
            ActionResult failureResult = mock(ActionResult.class);
            when(successResult.isSuccess()).thenReturn(true);
            when(failureResult.isSuccess()).thenReturn(false);
            
            // State1 and State3 are found, State2 is not
            when(action.find(any(ObjectCollection.class)))
                .thenReturn(successResult, failureResult, successResult);
            
            Set<State> foundStates = stateDetector.rebuildActiveStates();
            
            assertEquals(2, foundStates.size());
            assertTrue(foundStates.contains(state1));
            assertFalse(foundStates.contains(state2));
            assertTrue(foundStates.contains(state3));
            
            verify(stateMemory).clearActiveStates();
            verify(stateMemory).addActiveState(1L);
            verify(stateMemory).addActiveState(3L);
        }
        
        @Test
        @DisplayName("Should handle no states found during rebuild")
        public void testRebuildActiveStatesNoneFound() {
            State state1 = createMockState(1L, "State1");
            State state2 = createMockState(2L, "State2");
            
            when(stateService.findAll()).thenReturn(Arrays.asList(state1, state2));
            
            ActionResult failureResult = mock(ActionResult.class);
            when(failureResult.isSuccess()).thenReturn(false);
            when(action.find(any(ObjectCollection.class))).thenReturn(failureResult);
            
            Set<State> foundStates = stateDetector.rebuildActiveStates();
            
            assertTrue(foundStates.isEmpty());
            verify(stateMemory).clearActiveStates();
            verify(stateMemory, never()).addActiveState(anyLong());
        }
        
        @Test
        @DisplayName("Should set UNKNOWN state if no states found")
        public void testRebuildSetsUnknownStateWhenNoneFound() {
            State state1 = createMockState(1L, "State1");
            State unknownState = createMockState(999L, SpecialStateType.UNKNOWN.toString());
            
            when(stateService.findAll()).thenReturn(Arrays.asList(state1));
            when(stateService.findByName(SpecialStateType.UNKNOWN.toString()))
                .thenReturn(Optional.of(unknownState));
            
            ActionResult failureResult = mock(ActionResult.class);
            when(failureResult.isSuccess()).thenReturn(false);
            when(action.find(any(ObjectCollection.class))).thenReturn(failureResult);
            
            Set<State> foundStates = stateDetector.rebuildActiveStates();
            
            assertTrue(foundStates.isEmpty());
            verify(stateMemory).addActiveState(999L);
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
            
            when(stateService.findAll()).thenReturn(Arrays.asList(state1, state2, state3, state4));
            
            ActionResult successResult = mock(ActionResult.class);
            ActionResult failureResult = mock(ActionResult.class);
            when(successResult.isSuccess()).thenReturn(true);
            when(failureResult.isSuccess()).thenReturn(false);
            
            // States 1, 3, and 4 are found
            when(action.find(any(ObjectCollection.class)))
                .thenReturn(successResult, failureResult, successResult, successResult);
            
            Set<State> foundStates = stateDetector.searchAllStates();
            
            assertEquals(3, foundStates.size());
            assertTrue(foundStates.contains(state1));
            assertFalse(foundStates.contains(state2));
            assertTrue(foundStates.contains(state3));
            assertTrue(foundStates.contains(state4));
            
            verify(action, times(4)).find(any(ObjectCollection.class));
        }
        
        @Test
        @DisplayName("Should exclude special states from search")
        public void testSearchAllStatesExcludesSpecial() {
            State normalState = createMockState(1L, "NormalState");
            State nullState = createMockState(2L, SpecialStateType.NULL.toString());
            State unknownState = createMockState(3L, SpecialStateType.UNKNOWN.toString());
            
            when(stateService.findAll()).thenReturn(Arrays.asList(normalState, nullState, unknownState));
            
            ActionResult successResult = mock(ActionResult.class);
            when(successResult.isSuccess()).thenReturn(true);
            when(action.find(any(ObjectCollection.class))).thenReturn(successResult);
            
            Set<State> foundStates = stateDetector.searchAllStates();
            
            // Only normal state should be searched
            verify(action, times(1)).find(any(ObjectCollection.class));
            assertTrue(foundStates.contains(normalState));
        }
    }
    
    @Nested
    @DisplayName("Find Specific State")
    class FindSpecificState {
        
        @Test
        @DisplayName("Should find specific state by name")
        public void testFindStateByName() {
            State targetState = createMockState(1L, "TargetState");
            
            when(stateService.findByName("TargetState")).thenReturn(Optional.of(targetState));
            
            ActionResult successResult = mock(ActionResult.class);
            when(successResult.isSuccess()).thenReturn(true);
            when(action.find(any(ObjectCollection.class))).thenReturn(successResult);
            
            Optional<State> foundState = stateDetector.findState("TargetState");
            
            assertTrue(foundState.isPresent());
            assertEquals(targetState, foundState.get());
            verify(action).find(any(ObjectCollection.class));
        }
        
        @Test
        @DisplayName("Should return empty if state not found visually")
        public void testFindStateNotFoundVisually() {
            State targetState = createMockState(1L, "TargetState");
            
            when(stateService.findByName("TargetState")).thenReturn(Optional.of(targetState));
            
            ActionResult failureResult = mock(ActionResult.class);
            when(failureResult.isSuccess()).thenReturn(false);
            when(action.find(any(ObjectCollection.class))).thenReturn(failureResult);
            
            Optional<State> foundState = stateDetector.findState("TargetState");
            
            assertFalse(foundState.isPresent());
        }
        
        @Test
        @DisplayName("Should return empty if state doesn't exist")
        public void testFindStateDoesNotExist() {
            when(stateService.findByName("NonExistentState")).thenReturn(Optional.empty());
            
            Optional<State> foundState = stateDetector.findState("NonExistentState");
            
            assertFalse(foundState.isPresent());
            verify(action, never()).find(any(ObjectCollection.class));
        }
        
        @Test
        @DisplayName("Should find state by ID")
        public void testFindStateById() {
            State targetState = createMockState(42L, "StateFortyTwo");
            
            when(stateService.findById(42L)).thenReturn(Optional.of(targetState));
            
            ActionResult successResult = mock(ActionResult.class);
            when(successResult.isSuccess()).thenReturn(true);
            when(action.find(any(ObjectCollection.class))).thenReturn(successResult);
            
            Optional<State> foundState = stateDetector.findStateById(42L);
            
            assertTrue(foundState.isPresent());
            assertEquals(targetState, foundState.get());
        }
    }
    
    @Nested
    @DisplayName("Refresh States")
    class RefreshStates {
        
        @Test
        @DisplayName("Should completely refresh active states")
        public void testRefreshStates() {
            State oldState = createMockState(1L, "OldState");
            State newState1 = createMockState(2L, "NewState1");
            State newState2 = createMockState(3L, "NewState2");
            
            // Initially has old state
            when(stateMemory.getActiveStatesObjects()).thenReturn(Arrays.asList(oldState));
            
            // All states in the system
            when(stateService.findAll()).thenReturn(Arrays.asList(oldState, newState1, newState2));
            
            ActionResult failureResult = mock(ActionResult.class);
            ActionResult successResult = mock(ActionResult.class);
            when(failureResult.isSuccess()).thenReturn(false);
            when(successResult.isSuccess()).thenReturn(true);
            
            // Old state no longer visible, new states are
            when(action.find(any(ObjectCollection.class)))
                .thenReturn(failureResult, successResult, successResult);
            
            Set<State> refreshedStates = stateDetector.refreshStates();
            
            assertEquals(2, refreshedStates.size());
            assertFalse(refreshedStates.contains(oldState));
            assertTrue(refreshedStates.contains(newState1));
            assertTrue(refreshedStates.contains(newState2));
            
            verify(stateMemory).clearActiveStates();
            verify(stateMemory).addActiveState(2L);
            verify(stateMemory).addActiveState(3L);
        }
        
        @Test
        @DisplayName("Should handle refresh with no states found")
        public void testRefreshStatesNoneFound() {
            State state1 = createMockState(1L, "State1");
            State unknownState = createMockState(999L, SpecialStateType.UNKNOWN.toString());
            
            when(stateMemory.getActiveStatesObjects()).thenReturn(Arrays.asList(state1));
            when(stateService.findAll()).thenReturn(Arrays.asList(state1));
            when(stateService.findByName(SpecialStateType.UNKNOWN.toString()))
                .thenReturn(Optional.of(unknownState));
            
            ActionResult failureResult = mock(ActionResult.class);
            when(failureResult.isSuccess()).thenReturn(false);
            when(action.find(any(ObjectCollection.class))).thenReturn(failureResult);
            
            Set<State> refreshedStates = stateDetector.refreshStates();
            
            assertTrue(refreshedStates.isEmpty());
            verify(stateMemory).clearActiveStates();
            verify(stateMemory).addActiveState(999L); // UNKNOWN state
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
            
            when(stateMemory.getActiveStatesObjects())
                .thenReturn(Arrays.asList(knownState1, knownState2));
            
            ActionResult successResult = mock(ActionResult.class);
            when(successResult.isSuccess()).thenReturn(true);
            when(action.find(any(ObjectCollection.class))).thenReturn(successResult);
            
            // Check known states first
            Set<State> activeStates = stateDetector.checkActiveStates();
            
            // Should only check the 2 known states, not all states
            verify(action, times(2)).find(any(ObjectCollection.class));
            assertEquals(2, activeStates.size());
        }
        
        @Test
        @DisplayName("Should fall back to full search when no active states")
        public void testFallbackToFullSearch() {
            when(stateMemory.getActiveStatesObjects()).thenReturn(new ArrayList<>());
            
            State state1 = createMockState(1L, "State1");
            State state2 = createMockState(2L, "State2");
            when(stateService.findAll()).thenReturn(Arrays.asList(state1, state2));
            
            ActionResult successResult = mock(ActionResult.class);
            when(successResult.isSuccess()).thenReturn(true);
            when(action.find(any(ObjectCollection.class))).thenReturn(successResult);
            
            // When no active states, should search all
            Set<State> foundStates = stateDetector.detectCurrentStates();
            
            verify(action, times(2)).find(any(ObjectCollection.class));
            assertEquals(2, foundStates.size());
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
            
            when(stateService.findAll()).thenReturn(Arrays.asList(mainState, sidebarState, headerState));
            
            ActionResult successResult = mock(ActionResult.class);
            when(successResult.isSuccess()).thenReturn(true);
            when(action.find(any(ObjectCollection.class))).thenReturn(successResult);
            
            Set<State> foundStates = stateDetector.searchAllStates();
            
            assertEquals(3, foundStates.size());
            assertTrue(foundStates.contains(mainState));
            assertTrue(foundStates.contains(sidebarState));
            assertTrue(foundStates.contains(headerState));
            
            // All three states should be added to active states
            verify(stateMemory).addActiveState(1L);
            verify(stateMemory).addActiveState(2L);
            verify(stateMemory).addActiveState(3L);
        }
        
        @Test
        @DisplayName("Should handle blocking state detection")
        public void testBlockingStateDetection() {
            State normalState = createMockState(1L, "NormalState");
            State blockingState = createMockState(2L, "BlockingModal");
            blockingState.setBlocking(true);
            
            when(stateService.findAll()).thenReturn(Arrays.asList(normalState, blockingState));
            
            ActionResult successResult = mock(ActionResult.class);
            when(successResult.isSuccess()).thenReturn(true);
            when(action.find(any(ObjectCollection.class))).thenReturn(successResult);
            
            Set<State> foundStates = stateDetector.searchAllStates();
            
            assertTrue(foundStates.contains(normalState));
            assertTrue(foundStates.contains(blockingState));
            
            // Both states found but blocking state should be noted
            State blocking = foundStates.stream()
                .filter(State::isBlocking)
                .findFirst()
                .orElse(null);
            
            assertNotNull(blocking);
            assertEquals("BlockingModal", blocking.getName());
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
    
    private Set<State> detectCurrentStates() {
        Set<State> activeStates = stateDetector.checkActiveStates();
        if (activeStates.isEmpty()) {
            activeStates = stateDetector.searchAllStates();
        }
        return activeStates;
    }
    
    private Optional<State> findStateById(Long stateId) {
        Optional<State> state = stateService.findById(stateId);
        if (state.isPresent()) {
            ObjectCollection collection = new ObjectCollection.Builder()
                .withStateImages(state.get().getStateImages())
                .build();
            ActionResult result = action.find(collection);
            if (result.isSuccess()) {
                return state;
            }
        }
        return Optional.empty();
    }
}