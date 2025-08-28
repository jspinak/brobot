package io.github.jspinak.brobot.model.state;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Comprehensive tests for the StateEnum interface which serves as a marker
 * interface for state enumerations in the Brobot framework.
 */
@DisplayName("StateEnum Interface Tests")
public class StateEnumTest extends BrobotTestBase {

    // Test enums implementing StateEnum
    private enum ApplicationStates implements StateEnum {
        LOGIN,
        MAIN_MENU,
        SETTINGS,
        PROFILE,
        LOGOUT
    }

    private enum DialogStates implements StateEnum {
        CONFIRM_DIALOG,
        ERROR_DIALOG,
        INFO_DIALOG,
        WARNING_DIALOG
    }

    private enum EmptyStateEnum implements StateEnum {
        // Empty enum to test edge cases
    }

    private enum SingleStateEnum implements StateEnum {
        ONLY_STATE
    }

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
    }

    @Test
    @DisplayName("Should implement StateEnum interface correctly")
    void testImplementsInterface() {
        // Then
        assertTrue(StateEnum.class.isAssignableFrom(ApplicationStates.class));
        assertTrue(StateEnum.class.isAssignableFrom(DialogStates.class));
        assertTrue(StateEnum.class.isAssignableFrom(EmptyStateEnum.class));
        assertTrue(StateEnum.class.isAssignableFrom(SingleStateEnum.class));
    }

    @Test
    @DisplayName("Should use enum features with StateEnum implementation")
    void testEnumFeatures() {
        // Test name()
        assertEquals("LOGIN", ApplicationStates.LOGIN.name());
        assertEquals("MAIN_MENU", ApplicationStates.MAIN_MENU.name());
        
        // Test ordinal()
        assertEquals(0, ApplicationStates.LOGIN.ordinal());
        assertEquals(1, ApplicationStates.MAIN_MENU.ordinal());
        assertEquals(2, ApplicationStates.SETTINGS.ordinal());
        
        // Test values()
        ApplicationStates[] states = ApplicationStates.values();
        assertEquals(5, states.length);
        assertEquals(ApplicationStates.LOGIN, states[0]);
        assertEquals(ApplicationStates.LOGOUT, states[4]);
        
        // Test valueOf()
        assertEquals(ApplicationStates.SETTINGS, ApplicationStates.valueOf("SETTINGS"));
        assertThrows(IllegalArgumentException.class, 
            () -> ApplicationStates.valueOf("INVALID"));
    }

    @ParameterizedTest
    @EnumSource(ApplicationStates.class)
    @DisplayName("Should handle all ApplicationStates enum values")
    void testAllApplicationStates(ApplicationStates state) {
        // Then
        assertNotNull(state);
        assertTrue(state instanceof StateEnum);
        assertNotNull(state.name());
        assertTrue(state.ordinal() >= 0);
        assertTrue(state.ordinal() < ApplicationStates.values().length);
    }

    @ParameterizedTest
    @EnumSource(DialogStates.class)
    @DisplayName("Should handle all DialogStates enum values")
    void testAllDialogStates(DialogStates state) {
        // Then
        assertNotNull(state);
        assertTrue(state instanceof StateEnum);
        assertNotNull(state.name());
        assertTrue(state.name().endsWith("_DIALOG"));
    }

    @Test
    @DisplayName("Should work in switch statements")
    void testSwitchStatements() {
        // Given
        ApplicationStates currentState = ApplicationStates.MAIN_MENU;
        
        // When
        String result = switch (currentState) {
            case LOGIN -> "User needs to log in";
            case MAIN_MENU -> "User is in main menu";
            case SETTINGS -> "User is in settings";
            case PROFILE -> "User is viewing profile";
            case LOGOUT -> "User is logging out";
        };
        
        // Then
        assertEquals("User is in main menu", result);
    }

    @Test
    @DisplayName("Should support type-safe state collections")
    void testTypeSafeCollections() {
        // Given
        Set<ApplicationStates> visitedStates = new HashSet<>();
        
        // When
        visitedStates.add(ApplicationStates.LOGIN);
        visitedStates.add(ApplicationStates.MAIN_MENU);
        visitedStates.add(ApplicationStates.SETTINGS);
        
        // Then
        assertEquals(3, visitedStates.size());
        assertTrue(visitedStates.contains(ApplicationStates.LOGIN));
        assertTrue(visitedStates.contains(ApplicationStates.MAIN_MENU));
        assertTrue(visitedStates.contains(ApplicationStates.SETTINGS));
        assertFalse(visitedStates.contains(ApplicationStates.PROFILE));
    }

    @Test
    @DisplayName("Should support polymorphic StateEnum handling")
    void testPolymorphicHandling() {
        // Given
        StateEnum[] states = {
            ApplicationStates.LOGIN,
            ApplicationStates.MAIN_MENU,
            DialogStates.CONFIRM_DIALOG,
            DialogStates.ERROR_DIALOG
        };
        
        // Then
        assertEquals(4, states.length);
        for (StateEnum state : states) {
            assertNotNull(state);
            assertTrue(state instanceof StateEnum);
            assertTrue(state instanceof Enum);
        }
    }

    @Test
    @DisplayName("Should handle empty enum implementation")
    void testEmptyEnum() {
        // When
        EmptyStateEnum[] values = EmptyStateEnum.values();
        
        // Then
        assertNotNull(values);
        assertEquals(0, values.length);
    }

    @Test
    @DisplayName("Should handle single value enum")
    void testSingleValueEnum() {
        // When
        SingleStateEnum[] values = SingleStateEnum.values();
        
        // Then
        assertEquals(1, values.length);
        assertEquals(SingleStateEnum.ONLY_STATE, values[0]);
        assertEquals(0, SingleStateEnum.ONLY_STATE.ordinal());
    }

    @Test
    @DisplayName("Should support enum comparison")
    void testEnumComparison() {
        // Given
        ApplicationStates state1 = ApplicationStates.LOGIN;
        ApplicationStates state2 = ApplicationStates.LOGIN;
        ApplicationStates state3 = ApplicationStates.MAIN_MENU;
        
        // Then - Reference equality works for enums
        assertSame(state1, state2);
        assertNotSame(state1, state3);
        
        // Equality comparison
        assertEquals(state1, state2);
        assertNotEquals(state1, state3);
        
        // Comparable
        assertTrue(state1.compareTo(state3) < 0);
        assertTrue(state3.compareTo(state1) > 0);
        assertEquals(0, state1.compareTo(state2));
    }

    @TestFactory
    @DisplayName("StateEnum usage patterns")
    Stream<DynamicTest> testUsagePatterns() {
        return Stream.of(
            dynamicTest("State machine implementation", () -> {
                // Simulate state transitions
                ApplicationStates currentState = ApplicationStates.LOGIN;
                
                // Transition to main menu after login
                if (currentState == ApplicationStates.LOGIN) {
                    currentState = ApplicationStates.MAIN_MENU;
                }
                
                assertEquals(ApplicationStates.MAIN_MENU, currentState);
            }),
            
            dynamicTest("State validation", () -> {
                // Check if state is valid for operation
                Set<ApplicationStates> navigableStates = Set.of(
                    ApplicationStates.MAIN_MENU,
                    ApplicationStates.SETTINGS,
                    ApplicationStates.PROFILE
                );
                
                assertTrue(navigableStates.contains(ApplicationStates.MAIN_MENU));
                assertFalse(navigableStates.contains(ApplicationStates.LOGIN));
            }),
            
            dynamicTest("State history tracking", () -> {
                // Track state history
                ApplicationStates[] history = {
                    ApplicationStates.LOGIN,
                    ApplicationStates.MAIN_MENU,
                    ApplicationStates.SETTINGS,
                    ApplicationStates.MAIN_MENU,
                    ApplicationStates.LOGOUT
                };
                
                // Verify history
                assertEquals(5, history.length);
                assertEquals(ApplicationStates.LOGIN, history[0]);
                assertEquals(ApplicationStates.LOGOUT, history[history.length - 1]);
            }),
            
            dynamicTest("Multi-enum state handling", () -> {
                // Handle different types of states
                StateEnum currentMainState = ApplicationStates.SETTINGS;
                StateEnum currentDialogState = null;
                
                // Show dialog
                if (currentMainState == ApplicationStates.SETTINGS) {
                    currentDialogState = DialogStates.CONFIRM_DIALOG;
                }
                
                assertNotNull(currentDialogState);
                assertEquals(DialogStates.CONFIRM_DIALOG, currentDialogState);
            })
        );
    }

    @Test
    @DisplayName("Should handle toString for debugging")
    void testToString() {
        // Given
        ApplicationStates state = ApplicationStates.MAIN_MENU;
        DialogStates dialog = DialogStates.ERROR_DIALOG;
        
        // When
        String stateString = state.toString();
        String dialogString = dialog.toString();
        
        // Then
        assertEquals("MAIN_MENU", stateString);
        assertEquals("ERROR_DIALOG", dialogString);
    }

    @Test
    @DisplayName("Should work with instanceof checks")
    void testInstanceOf() {
        // Given
        StateEnum state = ApplicationStates.LOGIN;
        
        // Then
        assertTrue(state instanceof StateEnum);
        assertTrue(state instanceof ApplicationStates);
        assertTrue(state instanceof Enum);
        assertFalse(state instanceof DialogStates);
    }

    @Test
    @DisplayName("Should handle class references")
    void testClassReferences() {
        // Given
        Class<? extends StateEnum> appStatesClass = ApplicationStates.class;
        Class<? extends StateEnum> dialogStatesClass = DialogStates.class;
        
        // Then
        assertTrue(StateEnum.class.isAssignableFrom(appStatesClass));
        assertTrue(StateEnum.class.isAssignableFrom(dialogStatesClass));
        assertTrue(Enum.class.isAssignableFrom(appStatesClass));
        
        // Check enum constants
        assertEquals(5, appStatesClass.getEnumConstants().length);
        assertEquals(4, dialogStatesClass.getEnumConstants().length);
    }

    @Test
    @DisplayName("Should support generic state handling")
    void testGenericHandling() {
        // Given
        class StateHandler<T extends Enum<T> & StateEnum> {
            private T currentState;
            
            void setState(T state) {
                this.currentState = state;
            }
            
            T getState() {
                return currentState;
            }
            
            boolean isInState(T state) {
                return currentState == state;
            }
        }
        
        // When
        StateHandler<ApplicationStates> handler = new StateHandler<>();
        handler.setState(ApplicationStates.SETTINGS);
        
        // Then
        assertEquals(ApplicationStates.SETTINGS, handler.getState());
        assertTrue(handler.isInState(ApplicationStates.SETTINGS));
        assertFalse(handler.isInState(ApplicationStates.LOGIN));
    }

    @Test
    @DisplayName("Should demonstrate marker interface pattern")
    void testMarkerInterfacePattern() {
        // StateEnum is a marker interface (no methods)
        // Similar to Serializable, Cloneable, etc.
        
        // Verify no declared methods (marker interface)
        assertEquals(0, StateEnum.class.getDeclaredMethods().length);
        
        // Can still be used for type constraints
        StateEnum[] states = {
            ApplicationStates.LOGIN,
            DialogStates.CONFIRM_DIALOG,
            SingleStateEnum.ONLY_STATE
        };
        
        // All can be treated as StateEnum
        for (StateEnum state : states) {
            assertNotNull(state);
            assertTrue(state instanceof StateEnum);
        }
    }

    @Test
    @DisplayName("Should support state grouping")
    void testStateGrouping() {
        // Given - Group states by category
        Set<ApplicationStates> authStates = Set.of(
            ApplicationStates.LOGIN,
            ApplicationStates.LOGOUT
        );
        
        Set<ApplicationStates> navigationStates = Set.of(
            ApplicationStates.MAIN_MENU,
            ApplicationStates.SETTINGS,
            ApplicationStates.PROFILE
        );
        
        // Then
        assertTrue(authStates.contains(ApplicationStates.LOGIN));
        assertFalse(authStates.contains(ApplicationStates.SETTINGS));
        assertTrue(navigationStates.contains(ApplicationStates.SETTINGS));
        assertFalse(navigationStates.contains(ApplicationStates.LOGIN));
        
        // No overlap
        assertTrue(authStates.stream().noneMatch(navigationStates::contains));
    }

    @Test
    @DisplayName("Should demonstrate enum constant caching")
    void testEnumConstantCaching() {
        // Enum constants are cached/singleton
        ApplicationStates state1 = ApplicationStates.valueOf("LOGIN");
        ApplicationStates state2 = ApplicationStates.valueOf("LOGIN");
        ApplicationStates state3 = ApplicationStates.LOGIN;
        
        // All references point to same instance
        assertSame(state1, state2);
        assertSame(state2, state3);
        assertSame(state1, state3);
        
        // Same for array access
        assertSame(ApplicationStates.LOGIN, ApplicationStates.values()[0]);
    }

    @Test
    @DisplayName("Should handle enum iteration")
    void testEnumIteration() {
        // Given
        int stateCount = 0;
        Set<String> stateNames = new HashSet<>();
        
        // When - Iterate over all enum values
        for (ApplicationStates state : ApplicationStates.values()) {
            stateCount++;
            stateNames.add(state.name());
        }
        
        // Then
        assertEquals(5, stateCount);
        assertEquals(5, stateNames.size());
        assertTrue(stateNames.contains("LOGIN"));
        assertTrue(stateNames.contains("LOGOUT"));
    }
}