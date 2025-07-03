package io.github.jspinak.brobot.statemanagement;

import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.transition.JavaStateTransition;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StateIdResolverTest {

    @Mock
    private StateService stateService;
    
    private StateIdResolver stateIdResolver;
    
    @BeforeEach
    void setUp() {
        stateIdResolver = new StateIdResolver(stateService);
    }
    
    @Test
    void testConvertNamesToIds_WithNullStateId() {
        // Setup
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.setStateName("MainMenu");
        
        when(stateService.getStateId("MainMenu")).thenReturn(1L);
        
        // Execute
        stateIdResolver.convertNamesToIds(stateTransitions);
        
        // Verify
        assertEquals(1L, stateTransitions.getStateId());
        verify(stateService).getStateId("MainMenu");
    }
    
    @Test
    void testConvertNamesToIds_WithExistingStateId() {
        // Setup
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.setStateName("MainMenu");
        stateTransitions.setStateId(2L); // Already has ID
        
        when(stateService.getStateId("MainMenu")).thenReturn(1L);
        
        // Execute
        stateIdResolver.convertNamesToIds(stateTransitions);
        
        // Verify - should not override existing ID
        assertEquals(2L, stateTransitions.getStateId());
        verify(stateService).getStateId("MainMenu");
    }
    
    @Test
    void testConvertNamesToIds_WithJavaStateTransition() {
        // Setup
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.setStateName("HomePage");
        
        JavaStateTransition javaTransition = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .build();
        javaTransition.setActivateNames(new HashSet<>(Arrays.asList("Settings", "Profile", "Dashboard")));
        javaTransition.setActivate(new HashSet<>()); // Initialize empty set
        
        stateTransitions.addTransition(javaTransition);
        
        when(stateService.getStateId("HomePage")).thenReturn(1L);
        when(stateService.getStateId("Settings")).thenReturn(2L);
        when(stateService.getStateId("Profile")).thenReturn(3L);
        when(stateService.getStateId("Dashboard")).thenReturn(4L);
        
        // Execute
        stateIdResolver.convertNamesToIds(stateTransitions);
        
        // Verify
        assertEquals(1L, stateTransitions.getStateId());
        Set<Long> activateIds = javaTransition.getActivate();
        assertEquals(3, activateIds.size());
        assertTrue(activateIds.contains(2L));
        assertTrue(activateIds.contains(3L));
        assertTrue(activateIds.contains(4L));
    }
    
    @Test
    void testConvertNamesToIds_WithUnregisteredStateName() {
        // Setup
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.setStateName("UnknownState");
        
        JavaStateTransition javaTransition = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .build();
        javaTransition.setActivateNames(new HashSet<>(Arrays.asList("ValidState", "InvalidState")));
        javaTransition.setActivate(new HashSet<>());
        
        stateTransitions.addTransition(javaTransition);
        
        when(stateService.getStateId("UnknownState")).thenReturn(null);
        when(stateService.getStateId("ValidState")).thenReturn(5L);
        when(stateService.getStateId("InvalidState")).thenReturn(null);
        
        // Execute
        stateIdResolver.convertNamesToIds(stateTransitions);
        
        // Verify
        assertNull(stateTransitions.getStateId());
        Set<Long> activateIds = javaTransition.getActivate();
        assertEquals(1, activateIds.size()); // Only valid state should be added
        assertTrue(activateIds.contains(5L));
    }
    
    @Test
    void testConvertNamesToIds_EmptyTransitions() {
        // Setup
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.setStateName("EmptyState");
        
        when(stateService.getStateId("EmptyState")).thenReturn(10L);
        
        // Execute
        stateIdResolver.convertNamesToIds(stateTransitions);
        
        // Verify
        assertEquals(10L, stateTransitions.getStateId());
        assertTrue(stateTransitions.getTransitions().isEmpty());
    }
    
    @Test
    void testConvertAllStateTransitions_EmptyList() {
        // Setup
        List<StateTransitions> emptyList = Arrays.asList();
        
        // Execute
        stateIdResolver.convertAllStateTransitions(emptyList);
        
        // Verify - should not throw exception
        verifyNoInteractions(stateService);
    }
    
    @Test
    void testConvertAllStateTransitions_MultipleTransitions() {
        // Setup
        StateTransitions transitions1 = new StateTransitions();
        transitions1.setStateName("State1");
        
        StateTransitions transitions2 = new StateTransitions();
        transitions2.setStateName("State2");
        
        JavaStateTransition javaTransition1 = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .build();
        javaTransition1.setActivateNames(new HashSet<>(Arrays.asList("State3")));
        javaTransition1.setActivate(new HashSet<>());
        transitions1.addTransition(javaTransition1);
        
        JavaStateTransition javaTransition2 = new JavaStateTransition.Builder()
                .setFunction(() -> false)
                .build();
        javaTransition2.setActivateNames(new HashSet<>(Arrays.asList("State4", "State5")));
        javaTransition2.setActivate(new HashSet<>());
        transitions2.addTransition(javaTransition2);
        
        List<StateTransitions> allTransitions = Arrays.asList(transitions1, transitions2);
        
        when(stateService.getStateId("State1")).thenReturn(1L);
        when(stateService.getStateId("State2")).thenReturn(2L);
        when(stateService.getStateId("State3")).thenReturn(3L);
        when(stateService.getStateId("State4")).thenReturn(4L);
        when(stateService.getStateId("State5")).thenReturn(5L);
        
        // Execute
        stateIdResolver.convertAllStateTransitions(allTransitions);
        
        // Verify
        assertEquals(1L, transitions1.getStateId());
        assertEquals(2L, transitions2.getStateId());
        
        Set<Long> activateIds1 = javaTransition1.getActivate();
        assertEquals(1, activateIds1.size());
        assertTrue(activateIds1.contains(3L));
        
        Set<Long> activateIds2 = javaTransition2.getActivate();
        assertEquals(2, activateIds2.size());
        assertTrue(activateIds2.contains(4L));
        assertTrue(activateIds2.contains(5L));
    }
    
    @Test
    void testConvertNamesToIds_WithMultipleJavaTransitions() {
        // Setup
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.setStateName("ComplexState");
        
        JavaStateTransition transition1 = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .build();
        transition1.setActivateNames(new HashSet<>(Arrays.asList("Target1", "Target2")));
        transition1.setActivate(new HashSet<>());
        
        JavaStateTransition transition2 = new JavaStateTransition.Builder()
                .setFunction(() -> false)
                .build();
        transition2.setActivateNames(new HashSet<>(Arrays.asList("Target3")));
        transition2.setActivate(new HashSet<>());
        
        stateTransitions.addTransition(transition1);
        stateTransitions.addTransition(transition2);
        
        when(stateService.getStateId("ComplexState")).thenReturn(10L);
        when(stateService.getStateId("Target1")).thenReturn(11L);
        when(stateService.getStateId("Target2")).thenReturn(12L);
        when(stateService.getStateId("Target3")).thenReturn(13L);
        
        // Execute
        stateIdResolver.convertNamesToIds(stateTransitions);
        
        // Verify
        assertEquals(10L, stateTransitions.getStateId());
        
        Set<Long> activateIds1 = transition1.getActivate();
        assertEquals(2, activateIds1.size());
        assertTrue(activateIds1.contains(11L));
        assertTrue(activateIds1.contains(12L));
        
        Set<Long> activateIds2 = transition2.getActivate();
        assertEquals(1, activateIds2.size());
        assertTrue(activateIds2.contains(13L));
    }
    
    @Test
    void testConvertNamesToIds_PreservesExistingActivateIds() {
        // Setup
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.setStateName("State");
        
        JavaStateTransition transition = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .build();
        transition.setActivateNames(new HashSet<>(Arrays.asList("NewState")));
        
        // Pre-existing IDs
        Set<Long> existingIds = new HashSet<>(Arrays.asList(100L, 200L));
        transition.setActivate(existingIds);
        
        stateTransitions.addTransition(transition);
        
        when(stateService.getStateId("State")).thenReturn(1L);
        when(stateService.getStateId("NewState")).thenReturn(300L);
        
        // Execute
        stateIdResolver.convertNamesToIds(stateTransitions);
        
        // Verify - should add to existing IDs, not replace them
        Set<Long> activateIds = transition.getActivate();
        assertEquals(3, activateIds.size());
        assertTrue(activateIds.contains(100L));
        assertTrue(activateIds.contains(200L));
        assertTrue(activateIds.contains(300L));
    }
}