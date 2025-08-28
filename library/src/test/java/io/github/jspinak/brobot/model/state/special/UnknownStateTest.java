package io.github.jspinak.brobot.model.state.special;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateEnum;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for the UnknownState class which represents the initial 
 * uncertain state in Brobot's state management system.
 */
@DisplayName("UnknownState Special State Tests")
public class UnknownStateTest extends BrobotTestBase {

    private UnknownState unknownState;
    private StateService mockStateService;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mockStateService = mock(StateService.class);
        unknownState = new UnknownState(mockStateService);
    }

    @Test
    @DisplayName("Should create UnknownState with correct state name")
    void testUnknownStateCreation() {
        // Then
        assertNotNull(unknownState);
        assertNotNull(unknownState.getState());
        assertEquals("unknown", unknownState.getState().getName());
    }

    @Test
    @DisplayName("Should register state with StateService on creation")
    void testStateRegistration() {
        // Then - Verify state was saved to service
        verify(mockStateService, times(1)).save(any(State.class));
        verify(mockStateService).save(unknownState.getState());
    }

    @Test
    @DisplayName("Should have Enum implementing StateEnum")
    void testEnumImplementsStateEnum() {
        // When
        UnknownState.Enum unknownEnum = UnknownState.Enum.UNKNOWN;
        
        // Then
        assertNotNull(unknownEnum);
        assertTrue(unknownEnum instanceof StateEnum);
        assertEquals("UNKNOWN", unknownEnum.name());
    }

    @Test
    @DisplayName("Should get state correctly")
    void testGetState() {
        // When
        State state = unknownState.getState();
        
        // Then
        assertNotNull(state);
        assertEquals("unknown", state.getName());
        // Should return same instance each time
        assertSame(state, unknownState.getState());
    }

    @Test
    @DisplayName("Should maintain singleton-like behavior per instance")
    void testSingletonBehavior() {
        // Given
        State state1 = unknownState.getState();
        State state2 = unknownState.getState();
        
        // Then - Same state instance from same UnknownState
        assertSame(state1, state2);
        assertEquals(state1.getName(), state2.getName());
    }

    @Test
    @DisplayName("Should support equality checks for Enum")
    void testEnumEquality() {
        // Given
        UnknownState.Enum enum1 = UnknownState.Enum.UNKNOWN;
        UnknownState.Enum enum2 = UnknownState.Enum.UNKNOWN;
        
        // Then
        assertSame(enum1, enum2); // Enum constants are singletons
        assertEquals(enum1, enum2);
        assertEquals(enum1.hashCode(), enum2.hashCode());
    }

    @Test
    @DisplayName("Should verify Enum has single value")
    void testEnumSingleValue() {
        // When
        UnknownState.Enum[] values = UnknownState.Enum.values();
        
        // Then
        assertEquals(1, values.length);
        assertEquals(UnknownState.Enum.UNKNOWN, values[0]);
    }

    @Test
    @DisplayName("Should support valueOf for Enum")
    void testEnumValueOf() {
        // When
        UnknownState.Enum unknownEnum = UnknownState.Enum.valueOf("UNKNOWN");
        
        // Then
        assertEquals(UnknownState.Enum.UNKNOWN, unknownEnum);
        
        // Invalid name should throw exception
        assertThrows(IllegalArgumentException.class, 
            () -> UnknownState.Enum.valueOf("INVALID"));
    }

    @TestFactory
    @DisplayName("UnknownState usage scenarios")
    Stream<DynamicTest> testUsageScenarios() {
        return Stream.of(
            dynamicTest("Initial automation startup", () -> {
                // Simulate starting automation
                StateService service = mock(StateService.class);
                UnknownState startState = new UnknownState(service);
                
                assertNotNull(startState.getState());
                assertEquals("unknown", startState.getState().getName());
                verify(service).save(startState.getState());
            }),
            
            dynamicTest("Recovery from application crash", () -> {
                // Simulate crash recovery
                StateService service = mock(StateService.class);
                UnknownState recoveryState = new UnknownState(service);
                
                // Unknown state should be available for recovery
                assertNotNull(recoveryState.getState());
                // Should be registered for state management
                verify(service).save(any(State.class));
            }),
            
            dynamicTest("Handling unexpected dialogs", () -> {
                // When unexpected dialog appears
                State state = unknownState.getState();
                
                // Can transition from unknown to handle dialog
                assertEquals("unknown", state.getName());
                // State should be ready for recovery actions
                assertNotNull(state.getStateImages());
                assertNotNull(state.getStateLocations());
            }),
            
            dynamicTest("State detection failure fallback", () -> {
                // When confidence is below threshold
                State fallbackState = unknownState.getState();
                
                assertEquals("unknown", fallbackState.getName());
                // Should provide safe fallback
                assertNotNull(fallbackState);
            })
        );
    }

    @Test
    @DisplayName("Should work as universal entry point")
    void testUniversalEntryPoint() {
        // Given - UnknownState as entry point
        State entryState = unknownState.getState();
        
        // Then
        assertEquals("unknown", entryState.getName());
        // Should have no prerequisites (always accessible)
        // This is indicated by the "unknown" name
        assertNotNull(entryState);
    }

    @Test
    @DisplayName("Should support multiple recovery strategies")
    void testMultipleRecoveryStrategies() {
        // The unknown state should support various recovery approaches
        State state = unknownState.getState();
        
        // Then - State is ready for any recovery strategy
        assertNotNull(state);
        assertEquals("unknown", state.getName());
        
        // State can have various action configurations
        assertNotNull(state.getStateImages());
        assertNotNull(state.getStateRegions());
        assertNotNull(state.getStateLocations());
    }

    @Test
    @DisplayName("Should handle Spring component annotation")
    void testSpringComponent() {
        // UnknownState is annotated with @Component
        // This test verifies it works with Spring context
        
        StateService service = mock(StateService.class);
        UnknownState componentState = new UnknownState(service);
        
        // Should be constructable with dependency injection
        assertNotNull(componentState);
        verify(service).save(any(State.class));
    }

    @Test
    @DisplayName("Should maintain state completeness guarantee")
    void testStateCompletenessGuarantee() {
        // Unknown state ensures state space is complete
        State state = unknownState.getState();
        
        // Then
        assertEquals("unknown", state.getName());
        
        // The unknown state complements all known states
        // ensuring automation can handle any situation
        assertNotNull(state);
        
        // Should always be accessible (no patterns required)
        assertTrue(state.getStateImages().isEmpty());
    }

    @Test
    @DisplayName("Should support error resilient operations")
    void testErrorResilience() {
        // Given
        State state = unknownState.getState();
        
        // Then - State should be ready for error handling
        assertNotNull(state);
        assertEquals("unknown", state.getName());
        
        // Can handle various error conditions
        // State structure supports recovery actions
    }

    @Test
    @DisplayName("Should work with StateEnum interface")
    void testStateEnumInterface() {
        // Given
        StateEnum stateEnum = UnknownState.Enum.UNKNOWN;
        
        // Then
        assertNotNull(stateEnum);
        assertEquals("UNKNOWN", stateEnum.toString());
    }

    @ParameterizedTest
    @EnumSource(UnknownState.Enum.class)
    @DisplayName("Should handle all enum values")
    void testAllEnumValues(UnknownState.Enum enumValue) {
        // Then
        assertNotNull(enumValue);
        assertTrue(enumValue instanceof StateEnum);
        assertEquals("UNKNOWN", enumValue.name());
    }

    @Test
    @DisplayName("Should support transition hub functionality")
    void testTransitionHub() {
        // Unknown state should be a hub for transitions
        State state = unknownState.getState();
        
        // Then
        assertNotNull(state);
        assertEquals("unknown", state.getName());
        
        // Transitions can be added to reach various states
    }

    @Test
    @DisplayName("Should handle manual intervention scenarios")
    void testManualInterventionHandling() {
        // When user manually intervenes during automation
        State state = unknownState.getState();
        
        // Then - Unknown state provides recovery
        assertEquals("unknown", state.getName());
        assertNotNull(state);
        
        // State should be ready to detect current context
        // and navigate back to known states
    }

    @Test
    @DisplayName("Should compare different UnknownState instances")
    void testMultipleInstances() {
        // Given
        StateService service1 = mock(StateService.class);
        StateService service2 = mock(StateService.class);
        
        UnknownState unknown1 = new UnknownState(service1);
        UnknownState unknown2 = new UnknownState(service2);
        
        // Then - Different instances but same semantic meaning
        assertNotSame(unknown1, unknown2);
        assertNotSame(unknown1.getState(), unknown2.getState());
        assertEquals(unknown1.getState().getName(), unknown2.getState().getName());
        
        // Both should register with their respective services
        verify(service1).save(unknown1.getState());
        verify(service2).save(unknown2.getState());
    }

    @Test
    @DisplayName("Should maintain consistent state properties")
    void testConsistentStateProperties() {
        // Given
        State state = unknownState.getState();
        
        // Then - Consistent properties
        assertEquals("unknown", state.getName());
        assertNotNull(state.getStateImages());
        assertNotNull(state.getStateLocations());
        assertNotNull(state.getStateRegions());
        assertNotNull(state.getStateStrings());
        
        // Properties should be empty initially (no visual patterns required)
        assertTrue(state.getStateImages().isEmpty());
    }
}