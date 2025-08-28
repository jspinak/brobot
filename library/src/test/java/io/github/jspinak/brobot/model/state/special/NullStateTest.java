package io.github.jspinak.brobot.model.state.special;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateEnum;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.DynamicTest;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Comprehensive tests for the NullState class which provides a container 
 * for stateless objects in the Brobot framework.
 */
@DisplayName("NullState Special State Tests")
public class NullStateTest extends BrobotTestBase {

    private NullState nullState;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        nullState = new NullState();
    }

    @Test
    @DisplayName("Should create NullState with correct state name")
    void testNullStateCreation() {
        // Then
        assertNotNull(nullState);
        assertNotNull(nullState.getState());
        assertEquals("null", nullState.getState().getName());
    }

    @Test
    @DisplayName("Should have Name enum implementing StateEnum")
    void testNameEnumImplementsStateEnum() {
        // When
        NullState.Name nullName = NullState.Name.NULL;
        
        // Then
        assertNotNull(nullName);
        assertTrue(nullName instanceof StateEnum);
        assertEquals("NULL", nullName.name());
    }

    @Test
    @DisplayName("Should get state correctly")
    void testGetState() {
        // When
        State state = nullState.getState();
        
        // Then
        assertNotNull(state);
        assertEquals("null", state.getName());
        // State should be the same instance each time
        assertSame(state, nullState.getState());
    }

    @Test
    @DisplayName("Should maintain state immutability")
    void testStateImmutability() {
        // Given
        State originalState = nullState.getState();
        String originalName = originalState.getName();
        
        // When - Try to modify state (if setter exists)
        // The state is final, so we can't replace it
        
        // Then
        assertSame(originalState, nullState.getState());
        assertEquals(originalName, nullState.getState().getName());
    }

    @Test
    @DisplayName("Should have consistent state across instances")
    void testConsistentStateAcrossInstances() {
        // Given
        NullState nullState1 = new NullState();
        NullState nullState2 = new NullState();
        
        // Then - Different instances but same state name
        assertNotSame(nullState1, nullState2);
        assertNotSame(nullState1.getState(), nullState2.getState());
        assertEquals(nullState1.getState().getName(), nullState2.getState().getName());
    }

    @Test
    @DisplayName("Should support equality checks for Name enum")
    void testNameEnumEquality() {
        // Given
        NullState.Name name1 = NullState.Name.NULL;
        NullState.Name name2 = NullState.Name.NULL;
        
        // Then
        assertSame(name1, name2); // Enum constants are singletons
        assertEquals(name1, name2);
        assertEquals(name1.hashCode(), name2.hashCode());
    }

    @TestFactory
    @DisplayName("NullState usage scenarios")
    Stream<DynamicTest> testUsageScenarios() {
        return Stream.of(
            dynamicTest("Use for temporary dialog handling", () -> {
                // Simulate handling a temporary dialog
                NullState tempState = new NullState();
                State dialogState = tempState.getState();
                
                assertNotNull(dialogState);
                assertEquals("null", dialogState.getName());
                // Can be used for stateless dialog operations
            }),
            
            dynamicTest("Use for utility objects", () -> {
                // Simulate utility objects that appear across states
                NullState utilityState = new NullState();
                
                assertNotNull(utilityState.getState());
                // Utility objects can be processed without state context
            }),
            
            dynamicTest("Use during state transitions", () -> {
                // Objects that appear during transitions
                NullState transitionState = new NullState();
                State state = transitionState.getState();
                
                assertEquals("null", state.getName());
                // Can handle objects that don't belong to source or target state
            }),
            
            dynamicTest("Use for testing individual patterns", () -> {
                // Test patterns without state infrastructure
                NullState testState = new NullState();
                
                assertNotNull(testState.getState());
                // Patterns can be tested in isolation
            })
        );
    }

    @Test
    @DisplayName("Should work with StateEnum interface")
    void testStateEnumInterface() {
        // Given
        StateEnum stateEnum = NullState.Name.NULL;
        
        // Then
        assertNotNull(stateEnum);
        assertEquals("NULL", stateEnum.toString());
    }

    @Test
    @DisplayName("Should handle toString correctly")
    void testToString() {
        // When
        String nullStateString = nullState.toString();
        String stateString = nullState.getState().toString();
        
        // Then
        assertNotNull(nullStateString);
        assertNotNull(stateString);
        // ToString should provide meaningful output
    }

    @Test
    @DisplayName("Should verify Name enum has single value")
    void testNameEnumSingleValue() {
        // When
        NullState.Name[] values = NullState.Name.values();
        
        // Then
        assertEquals(1, values.length);
        assertEquals(NullState.Name.NULL, values[0]);
    }

    @Test
    @DisplayName("Should support valueOf for Name enum")
    void testNameEnumValueOf() {
        // When
        NullState.Name name = NullState.Name.valueOf("NULL");
        
        // Then
        assertEquals(NullState.Name.NULL, name);
        
        // Invalid name should throw exception
        assertThrows(IllegalArgumentException.class, 
            () -> NullState.Name.valueOf("INVALID"));
    }

    @Test
    @DisplayName("Should maintain null state semantics")
    void testNullStateSemantics() {
        // Given
        State state = nullState.getState();
        
        // Then
        // The state should represent "no state" or "null state"
        assertEquals("null", state.getName());
        
        // Should not have complex state properties
        // (assuming State has default values for other properties)
        assertNotNull(state.getStateImages());
        assertNotNull(state.getStateLocations());
        assertNotNull(state.getStateRegions());
    }

    @Test
    @DisplayName("Should support multiple null state contexts")
    void testMultipleNullStateContexts() {
        // Given - Multiple contexts might need different null states
        NullState context1 = new NullState();
        NullState context2 = new NullState();
        
        // Then - Not singletons, allowing multiple contexts
        assertNotSame(context1, context2);
        assertNotSame(context1.getState(), context2.getState());
        
        // But semantically equivalent
        assertEquals(context1.getState().getName(), context2.getState().getName());
    }

    @Test
    @DisplayName("Should not be stored in state repository")
    void testRepositoryExclusion() {
        // This test documents the expected behavior
        // NullState should not be added to the state repository
        
        State state = nullState.getState();
        
        // The state name "null" should indicate it's not for repository storage
        assertEquals("null", state.getName());
        
        // This is a documentation test - actual repository behavior 
        // would be tested in repository tests
    }

    @Test
    @DisplayName("Should provide type safety in state system")
    void testTypeSafety() {
        // Given
        State regularState = new State.Builder("regular").build();
        State nullStateInstance = nullState.getState();
        
        // Then - Both are State types
        assertTrue(regularState instanceof State);
        assertTrue(nullStateInstance instanceof State);
        
        // Can be used interchangeably where State is expected
        State[] states = {regularState, nullStateInstance};
        assertEquals(2, states.length);
    }

    @Test
    @DisplayName("Should handle edge cases properly")
    void testEdgeCases() {
        // Test that NullState handles edge cases gracefully
        
        // Multiple gets return same instance
        State state1 = nullState.getState();
        State state2 = nullState.getState();
        assertSame(state1, state2);
        
        // State name is always "null"
        assertEquals("null", state1.getName());
        assertEquals("null", state2.getName());
    }
}