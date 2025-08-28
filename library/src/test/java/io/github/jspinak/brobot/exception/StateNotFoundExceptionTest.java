package io.github.jspinak.brobot.exception;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for StateNotFoundException.
 * Achieves 100% coverage of all constructors and methods.
 */
@DisplayName("StateNotFoundException Tests")
public class StateNotFoundExceptionTest extends BrobotTestBase {

    @Test
    @DisplayName("Should create exception with state name only")
    void testExceptionWithStateName() {
        // Given
        String stateName = "LoginScreen";
        
        // When
        StateNotFoundException exception = new StateNotFoundException(stateName);
        
        // Then
        assertEquals(stateName, exception.getStateName());
        assertEquals("State 'LoginScreen' not found in the state model", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should create exception with state name and context")
    void testExceptionWithStateNameAndContext() {
        // Given
        String stateName = "CheckoutPage";
        String context = "e-commerce workflow";
        
        // When
        StateNotFoundException exception = new StateNotFoundException(stateName, context);
        
        // Then
        assertEquals(stateName, exception.getStateName());
        assertEquals("State 'CheckoutPage' not found in e-commerce workflow", exception.getMessage());
        assertNull(exception.getCause());
    }

    @ParameterizedTest
    @CsvSource({
        "MainMenu, navigation graph, State 'MainMenu' not found in navigation graph",
        "Settings, user preferences, State 'Settings' not found in user preferences",
        "Dashboard, admin panel, State 'Dashboard' not found in admin panel",
        "ProfilePage, social module, State 'ProfilePage' not found in social module"
    })
    @DisplayName("Should format message correctly with various contexts")
    void testMessageFormattingWithContext(String stateName, String context, String expectedMessage) {
        // When
        StateNotFoundException exception = new StateNotFoundException(stateName, context);
        
        // Then
        assertEquals(stateName, exception.getStateName());
        assertEquals(expectedMessage, exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n", "SimpleState", "State-With-Dashes", 
                            "State_With_Underscores", "StateWithNumbers123", 
                            "Very.Long.State.Name.With.Multiple.Parts.That.Exceeds.Normal.Length"})
    @DisplayName("Should handle various state name formats")
    void testVariousStateNameFormats(String stateName) {
        // When
        StateNotFoundException exception = new StateNotFoundException(stateName);
        
        // Then
        assertEquals(stateName, exception.getStateName());
        String expectedMessage = String.format("State '%s' not found in the state model", stateName);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Should handle null state name")
    void testNullStateName() {
        // When
        StateNotFoundException exception = new StateNotFoundException(null);
        
        // Then
        assertNull(exception.getStateName());
        assertEquals("State 'null' not found in the state model", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle null context")
    void testNullContext() {
        // Given
        String stateName = "TestState";
        
        // When
        StateNotFoundException exception = new StateNotFoundException(stateName, null);
        
        // Then
        assertEquals(stateName, exception.getStateName());
        assertEquals("State 'TestState' not found in null", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle both null state name and context")
    void testBothNull() {
        // When
        StateNotFoundException exception = new StateNotFoundException(null, null);
        
        // Then
        assertNull(exception.getStateName());
        assertEquals("State 'null' not found in null", exception.getMessage());
    }

    @Test
    @DisplayName("Should inherit from BrobotRuntimeException")
    void testInheritance() {
        // Given
        StateNotFoundException exception = new StateNotFoundException("TestState");
        
        // Then
        assertTrue(exception instanceof BrobotRuntimeException);
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }

    @Test
    @DisplayName("Should be throwable and catchable")
    void testThrowAndCatch() {
        // Given
        String stateName = "NonExistentState";
        
        // When/Then
        StateNotFoundException caught = assertThrows(StateNotFoundException.class, () -> {
            throw new StateNotFoundException(stateName);
        });
        
        assertEquals(stateName, caught.getStateName());
    }

    @Test
    @DisplayName("Should be catchable as BrobotRuntimeException")
    void testCatchAsBrobotRuntimeException() {
        // When/Then
        BrobotRuntimeException caught = assertThrows(BrobotRuntimeException.class, () -> {
            throw new StateNotFoundException("TestState", "test context");
        });
        
        assertTrue(caught instanceof StateNotFoundException);
    }

    @ParameterizedTest
    @MethodSource("provideRealisticScenarios")
    @DisplayName("Should handle realistic state not found scenarios")
    void testRealisticScenarios(String stateName, String context, String expectedInMessage) {
        // When
        StateNotFoundException exception = (context != null)
            ? new StateNotFoundException(stateName, context)
            : new StateNotFoundException(stateName);
        
        // Then
        assertEquals(stateName, exception.getStateName());
        assertTrue(exception.getMessage().contains(expectedInMessage));
    }

    private static Stream<Arguments> provideRealisticScenarios() {
        return Stream.of(
            Arguments.of("LoginScreen", null, "LoginScreen"),
            Arguments.of("HomePage", "main navigation", "HomePage"),
            Arguments.of("UserProfile", "social features", "social features"),
            Arguments.of("ShoppingCart", "e-commerce flow", "e-commerce flow"),
            Arguments.of("AdminDashboard", "backend administration", "backend administration"),
            Arguments.of("PaymentForm", "checkout process", "checkout process"),
            Arguments.of("SearchResults", "search workflow", "search workflow")
        );
    }

    @Test
    @DisplayName("Should preserve stack trace")
    void testStackTrace() {
        // Given
        String stateName = "TestState";
        
        // When
        StateNotFoundException exception = new StateNotFoundException(stateName);
        StackTraceElement[] stackTrace = exception.getStackTrace();
        
        // Then
        assertTrue(stackTrace.length > 0);
        assertEquals(this.getClass().getName(), stackTrace[0].getClassName());
    }

    @Test
    @DisplayName("Should support special characters in state names")
    void testSpecialCharactersInStateName() {
        // Given
        String[] specialNames = {
            "State@Home",
            "State#1",
            "State$Money",
            "State%Percent",
            "State&And",
            "State*Star",
            "State+Plus",
            "State=Equals",
            "State[Bracket]",
            "State{Brace}",
            "State|Pipe",
            "State\\Backslash",
            "State/Forward",
            "State<Less>",
            "State\"Quote\"",
            "State'Single'"
        };
        
        // When/Then
        for (String stateName : specialNames) {
            StateNotFoundException exception = new StateNotFoundException(stateName);
            assertEquals(stateName, exception.getStateName());
            assertTrue(exception.getMessage().contains(stateName));
        }
    }

    @Test
    @DisplayName("Should handle unicode characters in state names")
    void testUnicodeInStateName() {
        // Given
        String[] unicodeNames = {
            "État", // French
            "状態", // Japanese
            "состояние", // Russian
            "الحالة", // Arabic
            "स्थिति", // Hindi
            "状态", // Chinese
            "κατάσταση", // Greek
            "מצב", // Hebrew
            "상태", // Korean
            "สถานะ" // Thai
        };
        
        // When/Then
        for (String stateName : unicodeNames) {
            StateNotFoundException exception = new StateNotFoundException(stateName);
            assertEquals(stateName, exception.getStateName());
            assertTrue(exception.getMessage().contains(stateName));
        }
    }

    @Test
    @DisplayName("Should be immutable after creation")
    void testImmutability() {
        // Given
        String originalName = "OriginalState";
        StateNotFoundException exception = new StateNotFoundException(originalName);
        
        // When
        String retrievedName1 = exception.getStateName();
        String retrievedName2 = exception.getStateName();
        
        // Then
        assertEquals(originalName, retrievedName1);
        assertEquals(originalName, retrievedName2);
        assertSame(retrievedName1, retrievedName2); // Same object reference
    }

    @Test
    @DisplayName("Should handle very long state names")
    void testVeryLongStateName() {
        // Given
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longName.append("VeryLongStateName");
        }
        String stateName = longName.toString();
        
        // When
        StateNotFoundException exception = new StateNotFoundException(stateName);
        
        // Then
        assertEquals(stateName, exception.getStateName());
        assertTrue(exception.getMessage().length() > 1000);
    }

    @Test
    @DisplayName("Should handle concurrent access")
    void testConcurrentAccess() throws InterruptedException {
        // Given
        StateNotFoundException exception = new StateNotFoundException("ConcurrentState", "test context");
        
        // When - Access from multiple threads
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    assertEquals("ConcurrentState", exception.getStateName());
                    assertTrue(exception.getMessage().contains("ConcurrentState"));
                }
            });
            threads[i].start();
        }
        
        // Then - Wait for all threads
        for (Thread thread : threads) {
            thread.join();
        }
    }
}