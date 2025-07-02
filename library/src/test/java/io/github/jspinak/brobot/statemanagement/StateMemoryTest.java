package io.github.jspinak.brobot.statemanagement;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.special.SpecialStateType;
import io.github.jspinak.brobot.navigation.service.StateService;
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
class StateMemoryTest {

    @Mock
    private StateService stateService;

    @Mock
    private State mockState1;

    @Mock
    private State mockState2;

    private StateMemory stateMemory;

    @BeforeEach
    void setUp() {
        stateMemory = new StateMemory(stateService);
        
        // Setup mock states
        when(mockState1.getId()).thenReturn(1L);
        when(mockState1.getName()).thenReturn("State1");
        when(mockState2.getId()).thenReturn(2L);
        when(mockState2.getName()).thenReturn("State2");
    }

    @Test
    void testAddActiveState_AddsNewState() {
        // Arrange
        Long stateId = 1L;
        when(stateService.getState(stateId)).thenReturn(Optional.of(mockState1));

        // Act
        stateMemory.addActiveState(stateId);

        // Assert
        assertTrue(stateMemory.getActiveStates().contains(stateId));
        assertEquals(1, stateMemory.getActiveStates().size());
        verify(mockState1).setProbabilityExists(100);
        verify(mockState1).addVisit();
    }

    @Test
    void testAddActiveState_IgnoresDuplicates() {
        // Arrange
        Long stateId = 1L;
        when(stateService.getState(stateId)).thenReturn(Optional.of(mockState1));

        // Act
        stateMemory.addActiveState(stateId);
        stateMemory.addActiveState(stateId); // Try to add again

        // Assert
        assertEquals(1, stateMemory.getActiveStates().size());
        verify(mockState1, times(1)).setProbabilityExists(100);
        verify(mockState1, times(1)).addVisit();
    }

    @Test
    void testAddActiveState_IgnoresNullState() {
        // Arrange
        Long nullStateId = SpecialStateType.NULL.getId();

        // Act
        stateMemory.addActiveState(nullStateId);

        // Assert
        assertTrue(stateMemory.getActiveStates().isEmpty());
        verify(stateService, never()).getState(any());
    }

    @Test
    void testGetActiveStateList_ReturnsActiveStates() {
        // Arrange
        when(stateService.getState(1L)).thenReturn(Optional.of(mockState1));
        when(stateService.getState(2L)).thenReturn(Optional.of(mockState2));
        
        stateMemory.addActiveState(1L);
        stateMemory.addActiveState(2L);

        // Act
        List<State> activeStateList = stateMemory.getActiveStateList();

        // Assert
        assertEquals(2, activeStateList.size());
        assertTrue(activeStateList.contains(mockState1));
        assertTrue(activeStateList.contains(mockState2));
    }

    @Test
    void testGetActiveStateList_SkipsInvalidIds() {
        // Arrange
        when(stateService.getState(1L)).thenReturn(Optional.of(mockState1));
        when(stateService.getState(999L)).thenReturn(Optional.empty());
        
        stateMemory.getActiveStates().add(1L);
        stateMemory.getActiveStates().add(999L); // Invalid ID

        // Act
        List<State> activeStateList = stateMemory.getActiveStateList();

        // Assert
        assertEquals(1, activeStateList.size());
        assertTrue(activeStateList.contains(mockState1));
    }

    @Test
    void testGetActiveStateNames_ReturnsNames() {
        // Arrange
        when(stateService.getState(1L)).thenReturn(Optional.of(mockState1));
        when(stateService.getState(2L)).thenReturn(Optional.of(mockState2));
        
        stateMemory.addActiveState(1L);
        stateMemory.addActiveState(2L);

        // Act
        List<String> names = stateMemory.getActiveStateNames();

        // Assert
        assertEquals(2, names.size());
        assertTrue(names.contains("State1"));
        assertTrue(names.contains("State2"));
    }

    @Test
    void testGetActiveStateNamesAsString_ReturnsCommaSeparated() {
        // Arrange
        when(stateService.getState(1L)).thenReturn(Optional.of(mockState1));
        when(stateService.getState(2L)).thenReturn(Optional.of(mockState2));
        
        stateMemory.addActiveState(1L);
        stateMemory.addActiveState(2L);

        // Act
        String namesString = stateMemory.getActiveStateNamesAsString();

        // Assert
        assertTrue(namesString.contains("State1"));
        assertTrue(namesString.contains("State2"));
        assertTrue(namesString.contains(", "));
    }

    @Test
    void testRemoveInactiveStates_RemovesSpecifiedStates() {
        // Arrange
        when(stateService.getState(1L)).thenReturn(Optional.of(mockState1));
        when(stateService.getState(2L)).thenReturn(Optional.of(mockState2));
        
        stateMemory.addActiveState(1L);
        stateMemory.addActiveState(2L);
        
        Set<Long> toRemove = new HashSet<>(Collections.singletonList(1L));

        // Act
        stateMemory.removeInactiveStates(toRemove);

        // Assert
        assertFalse(stateMemory.getActiveStates().contains(1L));
        assertTrue(stateMemory.getActiveStates().contains(2L));
        assertEquals(1, stateMemory.getActiveStates().size());
    }

    @Test
    void testRemoveAllStates_ClearsActiveStates() {
        // Arrange
        when(stateService.getState(1L)).thenReturn(Optional.of(mockState1));
        when(stateService.getState(2L)).thenReturn(Optional.of(mockState2));
        
        stateMemory.addActiveState(1L);
        stateMemory.addActiveState(2L);

        // Act
        stateMemory.removeAllStates();

        // Assert
        assertTrue(stateMemory.getActiveStates().isEmpty());
    }

    @Test
    void testAdjustActiveStatesWithMatches_AddsStatesFromMatches() {
        // Arrange
        ActionResult actionResult = new ActionResult();
        
        StateObjectMetadata stateData1 = mock(StateObjectMetadata.class);
        when(stateData1.getOwnerStateId()).thenReturn(1L);
        
        StateObjectMetadata stateData2 = mock(StateObjectMetadata.class);
        when(stateData2.getOwnerStateId()).thenReturn(2L);
        
        Match match1 = new Match.Builder()
                .setStateObjectData(stateData1)
                .build();
        Match match2 = new Match.Builder()
                .setStateObjectData(stateData2)
                .build();
        
        actionResult.add(match1, match2);
        
        when(stateService.getState(1L)).thenReturn(Optional.of(mockState1));
        when(stateService.getState(2L)).thenReturn(Optional.of(mockState2));

        // Act
        stateMemory.adjustActiveStatesWithMatches(actionResult);

        // Assert
        assertEquals(2, stateMemory.getActiveStates().size());
        assertTrue(stateMemory.getActiveStates().contains(1L));
        assertTrue(stateMemory.getActiveStates().contains(2L));
    }

    @Test
    void testAdjustActiveStatesWithMatches_IgnoresNullStateData() {
        // Arrange
        ActionResult actionResult = new ActionResult();
        
        Match matchWithoutStateData = new Match.Builder().build();
        actionResult.add(matchWithoutStateData);

        // Act
        stateMemory.adjustActiveStatesWithMatches(actionResult);

        // Assert
        assertTrue(stateMemory.getActiveStates().isEmpty());
    }

    @Test
    void testAdjustActiveStatesWithMatches_IgnoresInvalidOwnerStateId() {
        // Arrange
        ActionResult actionResult = new ActionResult();
        
        StateObjectMetadata stateData = mock(StateObjectMetadata.class);
        when(stateData.getOwnerStateId()).thenReturn(0L); // Invalid ID
        
        Match match = new Match.Builder()
                .setStateObjectData(stateData)
                .build();
        
        actionResult.add(match);

        // Act
        stateMemory.adjustActiveStatesWithMatches(actionResult);

        // Assert
        assertTrue(stateMemory.getActiveStates().isEmpty());
    }

    @Test
    void testGetActiveStates_ReturnsSetOfIds() {
        // Arrange
        when(stateService.getState(1L)).thenReturn(Optional.of(mockState1));
        when(stateService.getState(2L)).thenReturn(Optional.of(mockState2));
        
        stateMemory.addActiveState(1L);
        stateMemory.addActiveState(2L);

        // Act
        Set<Long> activeStates = stateMemory.getActiveStates();

        // Assert
        assertEquals(2, activeStates.size());
        assertTrue(activeStates.contains(1L));
        assertTrue(activeStates.contains(2L));
    }

    @Test
    void testAddActiveState_UpdatesStateProbability() {
        // Arrange
        when(stateService.getState(1L)).thenReturn(Optional.of(mockState1));

        // Act
        stateMemory.addActiveState(1L);

        // Assert
        verify(mockState1).setProbabilityExists(100);
    }

    @Test
    void testRemoveInactiveState_UpdatesStateProbability() {
        // Arrange
        when(stateService.getState(1L)).thenReturn(Optional.of(mockState1));
        stateMemory.addActiveState(1L);

        // Act
        stateMemory.removeInactiveState(1L);

        // Assert
        verify(mockState1).setProbabilityExists(0);
        assertFalse(stateMemory.getActiveStates().contains(1L));
    }
}