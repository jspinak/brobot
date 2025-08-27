package io.github.jspinak.brobot.exception;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.EmptySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StateNotFoundException
 * Testing state-specific exception scenarios and navigation failures
 */
@DisplayName("StateNotFoundException Tests")
class StateNotFoundExceptionTest extends BrobotTestBase {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create exception with state name only")
        void shouldCreateExceptionWithStateName() {
            // Given
            String stateName = "LoginScreen";
            
            // When
            StateNotFoundException exception = new StateNotFoundException(stateName);
            
            // Then
            assertNotNull(exception);
            assertEquals(stateName, exception.getStateName());
            assertEquals("State 'LoginScreen' not found in the state model", exception.getMessage());
        }

        @Test
        @DisplayName("Should create exception with state name and context")
        void shouldCreateExceptionWithStateNameAndContext() {
            // Given
            String stateName = "SettingsScreen";
            String context = "transition from MainMenu";
            
            // When
            StateNotFoundException exception = new StateNotFoundException(stateName, context);
            
            // Then
            assertNotNull(exception);
            assertEquals(stateName, exception.getStateName());
            assertEquals("State 'SettingsScreen' not found in transition from MainMenu", 
                        exception.getMessage());
        }

        @Test
        @DisplayName("Should handle null state name")
        void shouldHandleNullStateName() {
            // When
            StateNotFoundException exception = new StateNotFoundException((String) null);
            
            // Then
            assertNotNull(exception);
            assertNull(exception.getStateName());
            assertTrue(exception.getMessage().contains("null"));
        }

        @Test
        @DisplayName("Should handle empty state name")
        void shouldHandleEmptyStateName() {
            // When
            StateNotFoundException exception = new StateNotFoundException("");
            
            // Then
            assertNotNull(exception);
            assertEquals("", exception.getStateName());
            assertEquals("State '' not found in the state model", exception.getMessage());
        }

        @Test
        @DisplayName("Should handle null context")
        void shouldHandleNullContext() {
            // When
            StateNotFoundException exception = new StateNotFoundException("TestState", null);
            
            // Then
            assertNotNull(exception);
            assertEquals("TestState", exception.getStateName());
            assertEquals("State 'TestState' not found in null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("State Name Format Tests")
    class StateNameFormatTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "LoginScreen",
            "MainMenu",
            "Settings_Screen",
            "user.profile.view",
            "state-with-dashes",
            "StateWithNumbers123",
            "UPPERCASE_STATE",
            "lowercase_state",
            "CamelCaseState",
            "snake_case_state",
            "kebab-case-state",
            "state.with.dots",
            "state::with::colons",
            "state/with/slashes"
        })
        @DisplayName("Should handle various state name formats")
        void shouldHandleVariousStateNameFormats(String stateName) {
            // When
            StateNotFoundException exception = new StateNotFoundException(stateName);
            
            // Then
            assertNotNull(exception);
            assertEquals(stateName, exception.getStateName());
            assertTrue(exception.getMessage().contains(stateName));
        }

        @Test
        @DisplayName("Should handle state names with special characters")
        void shouldHandleStateNamesWithSpecialCharacters() {
            // Given
            String stateName = "State@#$%^&*()";
            
            // When
            StateNotFoundException exception = new StateNotFoundException(stateName);
            
            // Then
            assertEquals(stateName, exception.getStateName());
            assertTrue(exception.getMessage().contains(stateName));
        }

        @Test
        @DisplayName("Should handle very long state names")
        void shouldHandleVeryLongStateNames() {
            // Given
            String stateName = "VeryLongStateName".repeat(100);
            
            // When
            StateNotFoundException exception = new StateNotFoundException(stateName);
            
            // Then
            assertEquals(stateName, exception.getStateName());
            assertTrue(exception.getMessage().contains(stateName));
        }
    }

    @Nested
    @DisplayName("Context Information Tests")
    class ContextInformationTests {

        @ParameterizedTest
        @CsvSource({
            "HomeScreen, the main navigation flow",
            "LoginScreen, authentication process",
            "SettingsPanel, user preferences section",
            "Dashboard, data visualization module",
            "ProfileView, user management system"
        })
        @DisplayName("Should include context in error message")
        void shouldIncludeContextInErrorMessage(String stateName, String context) {
            // When
            StateNotFoundException exception = new StateNotFoundException(stateName, context);
            
            // Then
            assertTrue(exception.getMessage().contains(stateName));
            assertTrue(exception.getMessage().contains(context));
        }

        @Test
        @DisplayName("Should handle transition context")
        void shouldHandleTransitionContext() {
            // Given
            String stateName = "AdminPanel";
            String context = "transition #5 from UserDashboard";
            
            // When
            StateNotFoundException exception = new StateNotFoundException(stateName, context);
            
            // Then
            assertEquals("State 'AdminPanel' not found in transition #5 from UserDashboard", 
                        exception.getMessage());
        }

        @Test
        @DisplayName("Should handle navigation path context")
        void shouldHandleNavigationPathContext() {
            // Given
            String stateName = "CheckoutScreen";
            String context = "navigation path: Home -> Products -> Cart -> Checkout";
            
            // When
            StateNotFoundException exception = new StateNotFoundException(stateName, context);
            
            // Then
            assertTrue(exception.getMessage().contains("navigation path"));
            assertTrue(exception.getMessage().contains("Home -> Products"));
        }

        @Test
        @DisplayName("Should handle state model context")
        void shouldHandleStateModelContext() {
            // Given
            String stateName = "ErrorState";
            String context = "the error recovery state model";
            
            // When
            StateNotFoundException exception = new StateNotFoundException(stateName, context);
            
            // Then
            assertTrue(exception.getMessage().contains("error recovery state model"));
        }
    }

    @Nested
    @DisplayName("Exception Behavior Tests")
    class ExceptionBehaviorTests {

        @Test
        @DisplayName("Should be a BrobotRuntimeException")
        void shouldBeBrobotRuntimeException() {
            // Given
            StateNotFoundException exception = new StateNotFoundException("TestState");
            
            // Then
            assertTrue(exception instanceof BrobotRuntimeException);
            assertTrue(exception instanceof RuntimeException);
        }

        @Test
        @DisplayName("Should be throwable as RuntimeException")
        void shouldBeThrowableAsRuntimeException() {
            assertThrows(RuntimeException.class, () -> {
                throw new StateNotFoundException("MissingState");
            });
        }

        @Test
        @DisplayName("Should preserve stack trace")
        void shouldPreserveStackTrace() {
            // When
            StateNotFoundException exception = new StateNotFoundException("TestState");
            
            // Then
            assertNotNull(exception.getStackTrace());
            assertTrue(exception.getStackTrace().length > 0);
            assertEquals(this.getClass().getName(), 
                exception.getStackTrace()[0].getClassName());
        }

        @Test
        @DisplayName("Should have meaningful toString")
        void shouldHaveMeaningfulToString() {
            // Given
            StateNotFoundException exception = new StateNotFoundException("LoginState");
            
            // When
            String str = exception.toString();
            
            // Then
            assertNotNull(str);
            assertTrue(str.contains("StateNotFoundException"));
            assertTrue(str.contains("LoginState"));
        }
    }

    @Nested
    @DisplayName("Use Case Scenarios")
    class UseCaseScenarios {

        @Test
        @DisplayName("Should handle missing state in transition")
        void shouldHandleMissingStateInTransition() {
            // Given
            String stateName = "NonExistentState";
            String context = "transition from LoginScreen to MainMenu";
            
            // When
            StateNotFoundException exception = new StateNotFoundException(stateName, context);
            
            // Then
            assertEquals(stateName, exception.getStateName());
            assertTrue(exception.getMessage().contains("transition from LoginScreen"));
        }

        @Test
        @DisplayName("Should handle state not found during initialization")
        void shouldHandleStateNotFoundDuringInitialization() {
            // Given
            String stateName = "InitialState";
            String context = "application startup";
            
            // When
            StateNotFoundException exception = new StateNotFoundException(stateName, context);
            
            // Then
            assertTrue(exception.getMessage().contains("application startup"));
        }

        @Test
        @DisplayName("Should handle state not found in conditional navigation")
        void shouldHandleStateNotFoundInConditionalNavigation() {
            // Given
            String stateName = "ConditionalState";
            String context = "conditional navigation based on user role";
            
            // When
            StateNotFoundException exception = new StateNotFoundException(stateName, context);
            
            // Then
            assertTrue(exception.getMessage().contains("conditional navigation"));
            assertTrue(exception.getMessage().contains("user role"));
        }

        @Test
        @DisplayName("Should handle cyclic state reference")
        void shouldHandleCyclicStateReference() {
            // Given
            String stateName = "StateA";
            String context = "cyclic reference: StateA -> StateB -> StateC -> StateA";
            
            // When
            StateNotFoundException exception = new StateNotFoundException(stateName, context);
            
            // Then
            assertTrue(exception.getMessage().contains("cyclic reference"));
        }

        @Test
        @DisplayName("Should handle parallel state not found")
        void shouldHandleParallelStateNotFound() {
            // Given
            String stateName = "ParallelSubState";
            String context = "parallel state region 2";
            
            // When
            StateNotFoundException exception = new StateNotFoundException(stateName, context);
            
            // Then
            assertTrue(exception.getMessage().contains("parallel state region"));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle unicode state names")
        void shouldHandleUnicodeStateNames() {
            // Given
            String stateName = "状態画面"; // Japanese characters
            
            // When
            StateNotFoundException exception = new StateNotFoundException(stateName);
            
            // Then
            assertEquals(stateName, exception.getStateName());
            assertTrue(exception.getMessage().contains(stateName));
        }

        @Test
        @DisplayName("Should handle emoji in state names")
        void shouldHandleEmojiInStateNames() {
            // Given
            String stateName = "Loading🔄State";
            
            // When
            StateNotFoundException exception = new StateNotFoundException(stateName);
            
            // Then
            assertEquals(stateName, exception.getStateName());
            assertTrue(exception.getMessage().contains("🔄"));
        }

        @Test
        @DisplayName("Should handle whitespace in state names")
        void shouldHandleWhitespaceInStateNames() {
            // Given
            String stateName = "  State With Spaces  ";
            
            // When
            StateNotFoundException exception = new StateNotFoundException(stateName);
            
            // Then
            assertEquals(stateName, exception.getStateName());
            assertEquals("State '  State With Spaces  ' not found in the state model", 
                        exception.getMessage());
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        @ValueSource(strings = {" ", "\t", "\n", "   "})
        @DisplayName("Should handle various empty/whitespace contexts")
        void shouldHandleVariousEmptyContexts(String context) {
            // When
            StateNotFoundException exception = new StateNotFoundException("TestState", context);
            
            // Then
            assertNotNull(exception);
            assertEquals("TestState", exception.getStateName());
            assertNotNull(exception.getMessage());
        }
    }
}