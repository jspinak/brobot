package io.github.jspinak.brobot.statemanagement;

import io.github.jspinak.brobot.model.state.StateEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ActiveStateSetTest {

    private ActiveStateSet activeStateSet;
    
    // Test StateEnum implementation
    private enum TestStates implements StateEnum {
        HOME,
        SETTINGS,
        PROFILE,
        DASHBOARD,
        LOGIN
    }
    
    @BeforeEach
    void setUp() {
        activeStateSet = new ActiveStateSet();
    }
    
    @Test
    void testInitialState() {
        // Verify
        assertNotNull(activeStateSet.getActiveStates());
        assertTrue(activeStateSet.getActiveStates().isEmpty());
    }
    
    @Test
    void testAddState_SingleState() {
        // Execute
        activeStateSet.addState(TestStates.HOME);
        
        // Verify
        Set<StateEnum> states = activeStateSet.getActiveStates();
        assertEquals(1, states.size());
        assertTrue(states.contains(TestStates.HOME));
    }
    
    @Test
    void testAddState_MultipleStates() {
        // Execute
        activeStateSet.addState(TestStates.HOME);
        activeStateSet.addState(TestStates.SETTINGS);
        activeStateSet.addState(TestStates.PROFILE);
        
        // Verify
        Set<StateEnum> states = activeStateSet.getActiveStates();
        assertEquals(3, states.size());
        assertTrue(states.contains(TestStates.HOME));
        assertTrue(states.contains(TestStates.SETTINGS));
        assertTrue(states.contains(TestStates.PROFILE));
    }
    
    @Test
    void testAddState_DuplicateState() {
        // Execute - Add same state multiple times
        activeStateSet.addState(TestStates.DASHBOARD);
        activeStateSet.addState(TestStates.DASHBOARD);
        activeStateSet.addState(TestStates.DASHBOARD);
        
        // Verify - Should only contain one instance
        Set<StateEnum> states = activeStateSet.getActiveStates();
        assertEquals(1, states.size());
        assertTrue(states.contains(TestStates.DASHBOARD));
    }
    
    @Test
    void testAddStates_FromSet() {
        // Setup
        Set<StateEnum> statesToAdd = new HashSet<>();
        statesToAdd.add(TestStates.HOME);
        statesToAdd.add(TestStates.SETTINGS);
        statesToAdd.add(TestStates.PROFILE);
        
        // Execute
        activeStateSet.addStates(statesToAdd);
        
        // Verify
        Set<StateEnum> states = activeStateSet.getActiveStates();
        assertEquals(3, states.size());
        assertTrue(states.contains(TestStates.HOME));
        assertTrue(states.contains(TestStates.SETTINGS));
        assertTrue(states.contains(TestStates.PROFILE));
    }
    
    @Test
    void testAddStates_EmptySet() {
        // Setup
        activeStateSet.addState(TestStates.LOGIN);
        Set<StateEnum> emptySet = new HashSet<>();
        
        // Execute
        activeStateSet.addStates(emptySet);
        
        // Verify - Should not change existing states
        Set<StateEnum> states = activeStateSet.getActiveStates();
        assertEquals(1, states.size());
        assertTrue(states.contains(TestStates.LOGIN));
    }
    
    @Test
    void testAddStates_FromActiveStateSet() {
        // Setup
        ActiveStateSet otherSet = new ActiveStateSet();
        otherSet.addState(TestStates.DASHBOARD);
        otherSet.addState(TestStates.PROFILE);
        
        activeStateSet.addState(TestStates.HOME);
        
        // Execute
        activeStateSet.addStates(otherSet);
        
        // Verify
        Set<StateEnum> states = activeStateSet.getActiveStates();
        assertEquals(3, states.size());
        assertTrue(states.contains(TestStates.HOME));
        assertTrue(states.contains(TestStates.DASHBOARD));
        assertTrue(states.contains(TestStates.PROFILE));
    }
    
    @Test
    void testAddStates_MergeWithDuplicates() {
        // Setup
        activeStateSet.addState(TestStates.HOME);
        activeStateSet.addState(TestStates.SETTINGS);
        
        Set<StateEnum> overlappingSet = new HashSet<>();
        overlappingSet.add(TestStates.SETTINGS); // Duplicate
        overlappingSet.add(TestStates.PROFILE);  // New
        overlappingSet.add(TestStates.DASHBOARD); // New
        
        // Execute
        activeStateSet.addStates(overlappingSet);
        
        // Verify - Should have 4 unique states
        Set<StateEnum> states = activeStateSet.getActiveStates();
        assertEquals(4, states.size());
        assertTrue(states.contains(TestStates.HOME));
        assertTrue(states.contains(TestStates.SETTINGS));
        assertTrue(states.contains(TestStates.PROFILE));
        assertTrue(states.contains(TestStates.DASHBOARD));
    }
    
    @Test
    void testGetActiveStates_ReturnsActualSet() {
        // Setup
        activeStateSet.addState(TestStates.HOME);
        
        // Execute
        Set<StateEnum> states = activeStateSet.getActiveStates();
        states.add(TestStates.SETTINGS); // Modify returned set
        
        // Verify - Changes should be reflected in original
        assertEquals(2, activeStateSet.getActiveStates().size());
        assertTrue(activeStateSet.getActiveStates().contains(TestStates.SETTINGS));
    }
    
    @Test
    void testComplexScenario_MultipleOperations() {
        // Initial states
        activeStateSet.addState(TestStates.LOGIN);
        
        // User logs in, transitions to HOME
        activeStateSet.addState(TestStates.HOME);
        
        // Open multiple sections
        Set<StateEnum> openSections = new HashSet<>();
        openSections.add(TestStates.DASHBOARD);
        openSections.add(TestStates.PROFILE);
        activeStateSet.addStates(openSections);
        
        // Merge with another active state set (e.g., from another component)
        ActiveStateSet otherComponent = new ActiveStateSet();
        otherComponent.addState(TestStates.SETTINGS);
        otherComponent.addState(TestStates.HOME); // Duplicate
        activeStateSet.addStates(otherComponent);
        
        // Verify final state
        Set<StateEnum> finalStates = activeStateSet.getActiveStates();
        assertEquals(5, finalStates.size());
        assertTrue(finalStates.contains(TestStates.LOGIN));
        assertTrue(finalStates.contains(TestStates.HOME));
        assertTrue(finalStates.contains(TestStates.DASHBOARD));
        assertTrue(finalStates.contains(TestStates.PROFILE));
        assertTrue(finalStates.contains(TestStates.SETTINGS));
    }
    
    @Test
    void testAddStates_NullHandling() {
        // This test documents current behavior with null
        // HashSet allows null values, so no exception is thrown
        
        // Execute
        activeStateSet.addState(null);
        
        // Verify - null is added to the set
        assertTrue(activeStateSet.getActiveStates().contains(null));
        assertEquals(1, activeStateSet.getActiveStates().size());
    }
}