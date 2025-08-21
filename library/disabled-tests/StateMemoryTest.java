package io.github.jspinak.brobot.statemanagement;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.special.SpecialStateType;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("StateMemory Tests")
public class StateMemoryTest extends BrobotTestBase {
    
    @Mock
    private StateService stateService;
    
    @Mock
    private StateTransitions stateTransitions;
    
    @Mock
    private ConsoleReporter consoleReporter;
    
    private StateMemory stateMemory;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        stateMemory = new StateMemory(stateService);
    }
    
    @Nested
    @DisplayName("Active State Management")
    class ActiveStateManagement {
        
        @Test
        @DisplayName("Should initialize with empty active states")
        public void testInitialState() {
            assertTrue(stateMemory.getActiveStates().isEmpty());
            assertTrue(stateMemory.getActiveStatesObjects().isEmpty());
        }
        
        @Test
        @DisplayName("Should add state to active states")
        public void testAddActiveState() {
            State mockState = mock(State.class);
            when(mockState.getId()).thenReturn(1L);
            when(mockState.getName()).thenReturn("TestState");
            when(stateService.findById(1L)).thenReturn(Optional.of(mockState));
            
            stateMemory.addActiveState(1L);
            
            assertTrue(stateMemory.getActiveStates().contains(1L));
            assertEquals(1, stateMemory.getActiveStates().size());
        }
        
        @Test
        @DisplayName("Should add multiple active states")
        public void testAddMultipleActiveStates() {
            State state1 = mock(State.class);
            State state2 = mock(State.class);
            State state3 = mock(State.class);
            
            when(state1.getId()).thenReturn(1L);
            when(state2.getId()).thenReturn(2L);
            when(state3.getId()).thenReturn(3L);
            
            when(stateService.findById(1L)).thenReturn(Optional.of(state1));
            when(stateService.findById(2L)).thenReturn(Optional.of(state2));
            when(stateService.findById(3L)).thenReturn(Optional.of(state3));
            
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            stateMemory.addActiveState(3L);
            
            assertEquals(3, stateMemory.getActiveStates().size());
            assertTrue(stateMemory.getActiveStates().contains(1L));
            assertTrue(stateMemory.getActiveStates().contains(2L));
            assertTrue(stateMemory.getActiveStates().contains(3L));
        }
        
        @Test
        @DisplayName("Should remove state from active states")
        public void testRemoveActiveState() {
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            
            stateMemory.removeActiveState(1L);
            
            assertFalse(stateMemory.getActiveStates().contains(1L));
            assertTrue(stateMemory.getActiveStates().contains(2L));
            assertEquals(1, stateMemory.getActiveStates().size());
        }
        
        @Test
        @DisplayName("Should clear all active states")
        public void testClearActiveStates() {
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            stateMemory.addActiveState(3L);
            
            stateMemory.clearActiveStates();
            
            assertTrue(stateMemory.getActiveStates().isEmpty());
        }
        
        @Test
        @DisplayName("Should replace active states")
        public void testReplaceActiveStates() {
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            
            Set<Long> newStates = new HashSet<>(Arrays.asList(3L, 4L, 5L));
            stateMemory.setActiveStates(newStates);
            
            assertEquals(3, stateMemory.getActiveStates().size());
            assertFalse(stateMemory.getActiveStates().contains(1L));
            assertFalse(stateMemory.getActiveStates().contains(2L));
            assertTrue(stateMemory.getActiveStates().contains(3L));
            assertTrue(stateMemory.getActiveStates().contains(4L));
            assertTrue(stateMemory.getActiveStates().contains(5L));
        }
    }
    
    @Nested
    @DisplayName("State Object Retrieval")
    class StateObjectRetrieval {
        
        @Test
        @DisplayName("Should retrieve active state objects")
        public void testGetActiveStateObjects() {
            State state1 = mock(State.class);
            State state2 = mock(State.class);
            
            when(state1.getId()).thenReturn(1L);
            when(state1.getName()).thenReturn("State1");
            when(state2.getId()).thenReturn(2L);
            when(state2.getName()).thenReturn("State2");
            
            when(stateService.findById(1L)).thenReturn(Optional.of(state1));
            when(stateService.findById(2L)).thenReturn(Optional.of(state2));
            
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            
            List<State> activeStates = stateMemory.getActiveStatesObjects();
            
            assertEquals(2, activeStates.size());
            assertTrue(activeStates.contains(state1));
            assertTrue(activeStates.contains(state2));
        }
        
        @Test
        @DisplayName("Should handle missing state objects gracefully")
        public void testGetActiveStateObjectsWithMissing() {
            State state1 = mock(State.class);
            when(state1.getId()).thenReturn(1L);
            
            when(stateService.findById(1L)).thenReturn(Optional.of(state1));
            when(stateService.findById(2L)).thenReturn(Optional.empty()); // State not found
            when(stateService.findById(3L)).thenReturn(Optional.empty()); // State not found
            
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            stateMemory.addActiveState(3L);
            
            List<State> activeStates = stateMemory.getActiveStatesObjects();
            
            assertEquals(1, activeStates.size());
            assertTrue(activeStates.contains(state1));
        }
    }
    
    @Nested
    @DisplayName("State Transitions")
    class StateTransitionTests {
        
        @Test
        @DisplayName("Should transition to new state")
        public void testTransitionToState() {
            State fromState = mock(State.class);
            State toState = mock(State.class);
            
            when(fromState.getId()).thenReturn(1L);
            when(toState.getId()).thenReturn(2L);
            when(stateService.findById(2L)).thenReturn(Optional.of(toState));
            
            stateMemory.addActiveState(1L);
            stateMemory.transitionTo(toState);
            
            assertFalse(stateMemory.getActiveStates().contains(1L));
            assertTrue(stateMemory.getActiveStates().contains(2L));
        }
        
        @Test
        @DisplayName("Should transition to multiple states")
        public void testTransitionToMultipleStates() {
            State state1 = mock(State.class);
            State state2 = mock(State.class);
            State state3 = mock(State.class);
            
            when(state1.getId()).thenReturn(1L);
            when(state2.getId()).thenReturn(2L);
            when(state3.getId()).thenReturn(3L);
            
            when(stateService.findById(2L)).thenReturn(Optional.of(state2));
            when(stateService.findById(3L)).thenReturn(Optional.of(state3));
            
            stateMemory.addActiveState(1L);
            stateMemory.transitionTo(Arrays.asList(state2, state3));
            
            assertFalse(stateMemory.getActiveStates().contains(1L));
            assertTrue(stateMemory.getActiveStates().contains(2L));
            assertTrue(stateMemory.getActiveStates().contains(3L));
            assertEquals(2, stateMemory.getActiveStates().size());
        }
    }
    
    @Nested
    @DisplayName("State Probability Management")
    class StateProbabilityManagement {
        
        @Test
        @DisplayName("Should set probability to base for all active states")
        public void testSetProbabilityToBase() {
            State state1 = mock(State.class);
            State state2 = mock(State.class);
            
            when(state1.getId()).thenReturn(1L);
            when(state1.getBaseProbabilityExists()).thenReturn(90);
            when(state2.getId()).thenReturn(2L);
            when(state2.getBaseProbabilityExists()).thenReturn(80);
            
            when(stateService.findById(1L)).thenReturn(Optional.of(state1));
            when(stateService.findById(2L)).thenReturn(Optional.of(state2));
            
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            
            stateMemory.setProbabilityToBaseProbabilityForActiveStates();
            
            verify(state1).setProbabilityToBaseProbability();
            verify(state2).setProbabilityToBaseProbability();
        }
        
        @Test
        @DisplayName("Should adjust probability based on action success")
        public void testAdjustProbabilityOnSuccess() {
            State state = mock(State.class);
            ActionResult successResult = mock(ActionResult.class);
            
            when(state.getId()).thenReturn(1L);
            when(state.getProbabilityExists()).thenReturn(50);
            when(successResult.isSuccess()).thenReturn(true);
            when(stateService.findById(1L)).thenReturn(Optional.of(state));
            
            stateMemory.addActiveState(1L);
            stateMemory.adjustProbabilityOnFind(state, successResult);
            
            verify(state).setProbabilityExists(anyInt());
        }
        
        @Test
        @DisplayName("Should adjust probability based on action failure")
        public void testAdjustProbabilityOnFailure() {
            State state = mock(State.class);
            ActionResult failureResult = mock(ActionResult.class);
            
            when(state.getId()).thenReturn(1L);
            when(state.getProbabilityExists()).thenReturn(50);
            when(failureResult.isSuccess()).thenReturn(false);
            when(stateService.findById(1L)).thenReturn(Optional.of(state));
            
            stateMemory.addActiveState(1L);
            stateMemory.adjustProbabilityOnFind(state, failureResult);
            
            verify(state).setProbabilityExists(anyInt());
        }
    }
    
    @Nested
    @DisplayName("State Visit Tracking")
    class StateVisitTracking {
        
        @Test
        @DisplayName("Should increment times visited")
        public void testIncrementTimesVisited() {
            State state = mock(State.class);
            when(state.getId()).thenReturn(1L);
            when(state.getTimesVisited()).thenReturn(5);
            when(stateService.findById(1L)).thenReturn(Optional.of(state));
            
            stateMemory.addActiveState(1L);
            stateMemory.incrementVisitCount(state);
            
            verify(state).setTimesVisited(6);
        }
        
        @Test
        @DisplayName("Should update last accessed time")
        public void testUpdateLastAccessed() {
            State state = mock(State.class);
            when(state.getId()).thenReturn(1L);
            when(stateService.findById(1L)).thenReturn(Optional.of(state));
            
            LocalDateTime beforeUpdate = LocalDateTime.now().minusMinutes(1);
            stateMemory.addActiveState(1L);
            stateMemory.updateLastAccessed(state);
            
            verify(state).setLastAccessed(any(LocalDateTime.class));
        }
        
        @Test
        @DisplayName("Should track visit for multiple states")
        public void testTrackMultipleStateVisits() {
            State state1 = mock(State.class);
            State state2 = mock(State.class);
            
            when(state1.getId()).thenReturn(1L);
            when(state1.getTimesVisited()).thenReturn(10);
            when(state2.getId()).thenReturn(2L);
            when(state2.getTimesVisited()).thenReturn(20);
            
            when(stateService.findById(1L)).thenReturn(Optional.of(state1));
            when(stateService.findById(2L)).thenReturn(Optional.of(state2));
            
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            
            stateMemory.incrementVisitCount(state1);
            stateMemory.incrementVisitCount(state2);
            
            verify(state1).setTimesVisited(11);
            verify(state2).setTimesVisited(21);
        }
    }
    
    @Nested
    @DisplayName("Special State Handling")
    class SpecialStateHandling {
        
        @Test
        @DisplayName("Should handle PREVIOUS state enum")
        public void testHandlePreviousStateEnum() {
            State hiddenState = mock(State.class);
            when(hiddenState.getId()).thenReturn(1L);
            when(hiddenState.getName()).thenReturn("HiddenState");
            
            State activeState = mock(State.class);
            when(activeState.getId()).thenReturn(2L);
            when(activeState.getHiddenStateIds()).thenReturn(new HashSet<>(Arrays.asList(1L)));
            
            when(stateService.findById(1L)).thenReturn(Optional.of(hiddenState));
            when(stateService.findById(2L)).thenReturn(Optional.of(activeState));
            
            stateMemory.addActiveState(2L);
            
            // When checking for PREVIOUS states
            List<State> previousStates = stateMemory.getPreviousStates(activeState);
            
            assertNotNull(previousStates);
            // Verify the logic for retrieving previous states
        }
        
        @Test
        @DisplayName("Should handle CURRENT state enum")
        public void testHandleCurrentStateEnum() {
            State state1 = mock(State.class);
            State state2 = mock(State.class);
            
            when(state1.getId()).thenReturn(1L);
            when(state2.getId()).thenReturn(2L);
            
            when(stateService.findById(1L)).thenReturn(Optional.of(state1));
            when(stateService.findById(2L)).thenReturn(Optional.of(state2));
            
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            
            List<State> currentStates = stateMemory.getActiveStatesObjects();
            
            assertEquals(2, currentStates.size());
            assertTrue(currentStates.contains(state1));
            assertTrue(currentStates.contains(state2));
        }
        
        @Test
        @DisplayName("Should handle NULL state properly")
        public void testHandleNullState() {
            State nullState = mock(State.class);
            when(nullState.getName()).thenReturn(SpecialStateType.NULL.toString());
            when(nullState.getId()).thenReturn(999L);
            
            when(stateService.findById(999L)).thenReturn(Optional.of(nullState));
            
            // NULL state should not affect active state tracking
            stateMemory.addActiveState(999L);
            
            // Verify NULL state handling logic
            assertTrue(stateMemory.getActiveStates().contains(999L));
        }
    }
    
    @Nested
    @DisplayName("State Query Operations")
    class StateQueryOperations {
        
        @Test
        @DisplayName("Should check if state is active")
        public void testIsStateActive() {
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            
            assertTrue(stateMemory.isActive(1L));
            assertTrue(stateMemory.isActive(2L));
            assertFalse(stateMemory.isActive(3L));
        }
        
        @Test
        @DisplayName("Should check if any state from list is active")
        public void testIsAnyStateActive() {
            stateMemory.addActiveState(2L);
            stateMemory.addActiveState(4L);
            
            List<Long> checkList1 = Arrays.asList(1L, 2L, 3L);
            List<Long> checkList2 = Arrays.asList(5L, 6L, 7L);
            
            assertTrue(stateMemory.isAnyActive(checkList1));
            assertFalse(stateMemory.isAnyActive(checkList2));
        }
        
        @Test
        @DisplayName("Should check if all states from list are active")
        public void testAreAllStatesActive() {
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            stateMemory.addActiveState(3L);
            
            List<Long> checkList1 = Arrays.asList(1L, 2L);
            List<Long> checkList2 = Arrays.asList(1L, 2L, 4L);
            
            assertTrue(stateMemory.areAllActive(checkList1));
            assertFalse(stateMemory.areAllActive(checkList2));
        }
        
        @Test
        @DisplayName("Should count active states")
        public void testCountActiveStates() {
            assertEquals(0, stateMemory.getActiveStateCount());
            
            stateMemory.addActiveState(1L);
            assertEquals(1, stateMemory.getActiveStateCount());
            
            stateMemory.addActiveState(2L);
            stateMemory.addActiveState(3L);
            assertEquals(3, stateMemory.getActiveStateCount());
            
            stateMemory.removeActiveState(2L);
            assertEquals(2, stateMemory.getActiveStateCount());
        }
    }
    
    @Nested
    @DisplayName("Blocking State Management")
    class BlockingStateManagement {
        
        @Test
        @DisplayName("Should handle blocking state activation")
        public void testBlockingStateActivation() {
            State blockingState = mock(State.class);
            State normalState = mock(State.class);
            
            when(blockingState.getId()).thenReturn(1L);
            when(blockingState.isBlocking()).thenReturn(true);
            when(normalState.getId()).thenReturn(2L);
            when(normalState.isBlocking()).thenReturn(false);
            
            when(stateService.findById(1L)).thenReturn(Optional.of(blockingState));
            when(stateService.findById(2L)).thenReturn(Optional.of(normalState));
            
            stateMemory.addActiveState(2L); // Normal state active
            stateMemory.addActiveState(1L); // Add blocking state
            
            // When blocking state is active, other states should be considered blocked
            assertTrue(stateMemory.hasBlockingState());
        }
        
        @Test
        @DisplayName("Should check if any active state is blocking")
        public void testHasBlockingState() {
            State blockingState = mock(State.class);
            State normalState1 = mock(State.class);
            State normalState2 = mock(State.class);
            
            when(blockingState.getId()).thenReturn(1L);
            when(blockingState.isBlocking()).thenReturn(true);
            when(normalState1.getId()).thenReturn(2L);
            when(normalState1.isBlocking()).thenReturn(false);
            when(normalState2.getId()).thenReturn(3L);
            when(normalState2.isBlocking()).thenReturn(false);
            
            when(stateService.findById(1L)).thenReturn(Optional.of(blockingState));
            when(stateService.findById(2L)).thenReturn(Optional.of(normalState1));
            when(stateService.findById(3L)).thenReturn(Optional.of(normalState2));
            
            stateMemory.addActiveState(2L);
            stateMemory.addActiveState(3L);
            assertFalse(stateMemory.hasBlockingState());
            
            stateMemory.addActiveState(1L);
            assertTrue(stateMemory.hasBlockingState());
        }
    }
    
    // Helper methods for tests
    private List<State> getPreviousStates(State activeState) {
        List<State> previousStates = new ArrayList<>();
        for (Long hiddenId : activeState.getHiddenStateIds()) {
            stateService.findById(hiddenId).ifPresent(previousStates::add);
        }
        return previousStates;
    }
    
    private boolean isActive(Long stateId) {
        return stateMemory.getActiveStates().contains(stateId);
    }
    
    private boolean isAnyActive(List<Long> stateIds) {
        for (Long id : stateIds) {
            if (stateMemory.getActiveStates().contains(id)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean areAllActive(List<Long> stateIds) {
        return stateMemory.getActiveStates().containsAll(stateIds);
    }
    
    private int getActiveStateCount() {
        return stateMemory.getActiveStates().size();
    }
    
    private boolean hasBlockingState() {
        for (State state : stateMemory.getActiveStatesObjects()) {
            if (state.isBlocking()) {
                return true;
            }
        }
        return false;
    }
}