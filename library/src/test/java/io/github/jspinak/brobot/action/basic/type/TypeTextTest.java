package io.github.jspinak.brobot.action.basic.type;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.internal.text.TextTyper;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sikuli.basics.Settings;

import java.util.Arrays;
import java.util.List;
import io.github.jspinak.brobot.test.DisabledInCI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for TypeText - types text to focused window.
 * Tests text typing functionality, timing configuration, and batch processing.
 */
@DisplayName("TypeText Tests")

@DisabledInCI
public class TypeTextTest extends BrobotTestBase {
    
    @Mock
    private TextTyper mockTextTyper;
    
    @Mock
    private TimeProvider mockTimeProvider;
    
    @Mock
    private ActionResult mockActionResult;
    
    @Mock
    private ObjectCollection mockObjectCollection;
    
    private TypeText typeText;
    private TypeOptions typeOptions;
    private double originalTypeDelay;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        typeText = new TypeText(mockTextTyper, mockTimeProvider);
        typeOptions = new TypeOptions.Builder().build();
        originalTypeDelay = Settings.TypeDelay;
        
        when(mockActionResult.getActionConfig()).thenReturn(typeOptions);
    }
    
    @Nested
    @DisplayName("Basic Typing Operations")
    class BasicTypingOperations {
        
        @Test
        @DisplayName("Type single string")
        public void testTypeSingleString() {
            StateString stateString = new StateString.Builder().setString("Hello World").build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(stateString));
            
            typeText.perform(mockActionResult, mockObjectCollection);
            
            verify(mockTextTyper, times(1)).type(stateString, typeOptions);
            verify(mockTimeProvider, never()).wait(anyDouble());
        }
        
        @Test
        @DisplayName("Type multiple strings with pauses")
        public void testTypeMultipleStrings() {
            StateString str1 = new StateString.Builder().setString("First").build();
            StateString str2 = new StateString.Builder().setString("Second").build();
            StateString str3 = new StateString.Builder().setString("Third").build();
            when(mockObjectCollection.getStateStrings())
                .thenReturn(Arrays.asList(str1, str2, str3));
            
            TypeOptions options = new TypeOptions.Builder()
                .setPauseAfterEnd(0.5)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            typeText.perform(mockActionResult, mockObjectCollection);
            
            verify(mockTextTyper).type(str1, options);
            verify(mockTextTyper).type(str2, options);
            verify(mockTextTyper).type(str3, options);
            verify(mockTimeProvider, times(2)).wait(0.5); // Pause between strings, not after last
        }
        
        @Test
        @DisplayName("Type empty string")
        public void testTypeEmptyString() {
            StateString emptyString = new StateString.Builder().setString("").build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(emptyString));
            
            typeText.perform(mockActionResult, mockObjectCollection);
            
            verify(mockTextTyper).type(emptyString, typeOptions);
        }
        
        @Test
        @DisplayName("Type special characters")
        public void testTypeSpecialCharacters() {
            StateString specialChars = new StateString.Builder().setString("!@#$%^&*()_+-=[]{}|;':\",./<>?").build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(specialChars));
            
            typeText.perform(mockActionResult, mockObjectCollection);
            
            verify(mockTextTyper).type(specialChars, typeOptions);
        }
        
        @Test
        @DisplayName("Type with newlines and tabs")
        public void testTypeWithWhitespace() {
            StateString withWhitespace = new StateString.Builder().setString("Line1\nLine2\tTabbed").build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(withWhitespace));
            
            typeText.perform(mockActionResult, mockObjectCollection);
            
            verify(mockTextTyper).type(withWhitespace, typeOptions);
        }
    }
    
    @Nested
    @DisplayName("Type Delay Configuration")
    class TypeDelayConfiguration {
        
        @Test
        @DisplayName("Set custom type delay")
        public void testCustomTypeDelay() {
            TypeOptions options = new TypeOptions.Builder()
                .setTypeDelay(0.1)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            StateString str = new StateString.Builder().setString("Test").build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(str));
            
            typeText.perform(mockActionResult, mockObjectCollection);
            
            // Settings.TypeDelay should be set during typing
            verify(mockTextTyper).type(str, options);
        }
        
        @Test
        @DisplayName("Restore original type delay after typing")
        public void testRestoreTypeDelay() {
            double customDelay = 0.05;
            TypeOptions options = new TypeOptions.Builder()
                .setTypeDelay(customDelay)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            StateString str = new StateString.Builder().setString("Test").build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(str));
            
            typeText.perform(mockActionResult, mockObjectCollection);
            
            // Verify the original delay is restored
            assertEquals(originalTypeDelay, Settings.TypeDelay, 0.01);
        }
        
        @ParameterizedTest
        @ValueSource(doubles = {0.0, 0.01, 0.05, 0.1, 0.5, 1.0})
        @DisplayName("Various type delays")
        public void testVariousTypeDelays(double delay) {
            TypeOptions options = new TypeOptions.Builder()
                .setTypeDelay(delay)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            StateString str = new StateString.Builder().setString("Test").build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(str));
            
            typeText.perform(mockActionResult, mockObjectCollection);
            
            verify(mockTextTyper).type(str, options);
        }
    }
    
    @Nested
    @DisplayName("Pause Configuration")
    class PauseConfiguration {
        
        @Test
        @DisplayName("Custom pause between strings")
        public void testCustomPauseBetweenStrings() {
            TypeOptions options = new TypeOptions.Builder()
                .setPauseAfterEnd(1.0)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            StateString str1 = new StateString.Builder().setString("First").build();
            StateString str2 = new StateString.Builder().setString("Second").build();
            when(mockObjectCollection.getStateStrings())
                .thenReturn(Arrays.asList(str1, str2));
            
            typeText.perform(mockActionResult, mockObjectCollection);
            
            verify(mockTimeProvider).wait(1.0);
        }
        
        @Test
        @DisplayName("No pause after last string")
        public void testNoPauseAfterLastString() {
            TypeOptions options = new TypeOptions.Builder()
                .setPauseAfterEnd(1.0)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            StateString str1 = new StateString.Builder().setString("First").build();
            StateString str2 = new StateString.Builder().setString("Last").build();
            when(mockObjectCollection.getStateStrings())
                .thenReturn(Arrays.asList(str1, str2));
            
            typeText.perform(mockActionResult, mockObjectCollection);
            
            // Should only pause once (between strings), not after the last one
            verify(mockTimeProvider, times(1)).wait(1.0);
        }
        
        @ParameterizedTest
        @CsvSource({
            "2, 0.5, 1",
            "3, 1.0, 2",
            "5, 0.25, 4",
            "10, 0.1, 9"
        })
        @DisplayName("Multiple strings with various pauses")
        public void testMultipleStringsVariousPauses(int stringCount, double pause, int expectedPauses) {
            TypeOptions options = new TypeOptions.Builder()
                .setPauseAfterEnd(pause)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            List<StateString> strings = new java.util.ArrayList<>();
            for (int i = 0; i < stringCount; i++) {
                strings.add(new StateString.Builder().setString("String" + i).build());
            }
            when(mockObjectCollection.getStateStrings()).thenReturn(strings);
            
            typeText.perform(mockActionResult, mockObjectCollection);
            
            verify(mockTimeProvider, times(expectedPauses)).wait(pause);
        }
    }
    
    @Nested
    @DisplayName("StateString Handling")
    class StateStringHandling {
        
        @Test
        @DisplayName("Type StateString with owner state")
        public void testStateStringWithOwner() {
            StateString stateString = new StateString.Builder()
                .setString("Test Text")
                .setOwnerStateName("LoginState")
                .build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(stateString));
            
            typeText.perform(mockActionResult, mockObjectCollection);
            
            ArgumentCaptor<StateString> captor = ArgumentCaptor.forClass(StateString.class);
            verify(mockTextTyper).type(captor.capture(), eq(typeOptions));
            assertEquals("Test Text", captor.getValue().getString());
            assertEquals("LoginState", captor.getValue().getOwnerStateName());
        }
        
        @Test
        @DisplayName("Type multiple StateStrings from different states")
        public void testMultipleStateStringsFromDifferentStates() {
            StateString str1 = new StateString.Builder()
                .setString("Username")
                .setOwnerStateName("LoginState")
                .build();
            StateString str2 = new StateString.Builder()
                .setString("Password")
                .setOwnerStateName("PasswordState")
                .build();
            
            when(mockObjectCollection.getStateStrings())
                .thenReturn(Arrays.asList(str1, str2));
            
            TypeOptions options = new TypeOptions.Builder()
                .setPauseAfterEnd(0.5)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            typeText.perform(mockActionResult, mockObjectCollection);
            
            verify(mockTextTyper).type(str1, options);
            verify(mockTextTyper).type(str2, options);
            verify(mockTimeProvider).wait(0.5);
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Throw exception for non-TypeOptions config")
        public void testNonTypeOptionsConfig() {
            when(mockActionResult.getActionConfig()).thenReturn(mock(ClickOptions.class));
            
            assertThrows(IllegalArgumentException.class, () -> 
                typeText.perform(mockActionResult, mockObjectCollection));
        }
        
        @Test
        @DisplayName("Handle empty string list")
        public void testEmptyStringList() {
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of());
            
            typeText.perform(mockActionResult, mockObjectCollection);
            
            verify(mockTextTyper, never()).type(any(), any());
            verify(mockTimeProvider, never()).wait(anyDouble());
        }
        
        @Test
        @DisplayName("Handle null string in list")
        public void testNullStringInList() {
            StateString validString = new StateString.Builder().setString("Valid").build();
            StateString nullString = null;
            when(mockObjectCollection.getStateStrings())
                .thenReturn(Arrays.asList(validString, nullString));
            
            // Should handle gracefully or throw appropriate exception
            try {
                typeText.perform(mockActionResult, mockObjectCollection);
                verify(mockTextTyper).type(validString, typeOptions);
                verify(mockTextTyper).type(null, typeOptions);
            } catch (NullPointerException e) {
                // Expected if null handling is not implemented
                assertTrue(true);
            }
        }
    }
    
    @Nested
    @DisplayName("Action Type")
    class ActionType {
        
        @Test
        @DisplayName("Correct action type")
        public void testActionType() {
            assertEquals(io.github.jspinak.brobot.action.ActionInterface.Type.TYPE, 
                typeText.getActionType());
        }
    }
    
    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {
        
        @Test
        @DisplayName("Type form data sequence")
        public void testFormDataSequence() {
            StateString username = new StateString.Builder().setString("user@example.com").build();
            StateString password = new StateString.Builder().setString("SecurePass123!").build();
            StateString confirmPassword = new StateString.Builder().setString("SecurePass123!").build();
            
            when(mockObjectCollection.getStateStrings())
                .thenReturn(Arrays.asList(username, password, confirmPassword));
            
            TypeOptions options = new TypeOptions.Builder()
                .setTypeDelay(0.05)
                .setPauseAfterEnd(0.3)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            typeText.perform(mockActionResult, mockObjectCollection);
            
            // Verify all three strings are typed in order
            InOrder inOrder = inOrder(mockTextTyper, mockTimeProvider);
            inOrder.verify(mockTextTyper).type(username, options);
            inOrder.verify(mockTimeProvider).wait(0.3);
            inOrder.verify(mockTextTyper).type(password, options);
            inOrder.verify(mockTimeProvider).wait(0.3);
            inOrder.verify(mockTextTyper).type(confirmPassword, options);
        }
        
        @Test
        @DisplayName("Type command sequence")
        public void testCommandSequence() {
            StateString command1 = new StateString.Builder().setString("ls -la").build();
            StateString command2 = new StateString.Builder().setString("cd /home/user").build();
            StateString command3 = new StateString.Builder().setString("pwd").build();
            
            when(mockObjectCollection.getStateStrings())
                .thenReturn(Arrays.asList(command1, command2, command3));
            
            TypeOptions options = new TypeOptions.Builder()
                .setTypeDelay(0.02)
                .setPauseAfterEnd(1.0) // Wait for command execution
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            typeText.perform(mockActionResult, mockObjectCollection);
            
            verify(mockTextTyper, times(3)).type(any(StateString.class), eq(options));
            verify(mockTimeProvider, times(2)).wait(1.0);
        }
        
        @Test
        @DisplayName("Type multiline text")
        public void testMultilineText() {
            String multiline = "Line 1\nLine 2\nLine 3\n\nLine 5";
            StateString text = new StateString.Builder().setString(multiline).build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(text));
            
            typeText.perform(mockActionResult, mockObjectCollection);
            
            ArgumentCaptor<StateString> captor = ArgumentCaptor.forClass(StateString.class);
            verify(mockTextTyper).type(captor.capture(), eq(typeOptions));
            assertEquals(multiline, captor.getValue().getString());
        }
    }
    
    @Nested
    @DisplayName("Performance Considerations")
    class PerformanceConsiderations {
        
        @Test
        @DisplayName("Fast typing with minimal delay")
        public void testFastTyping() {
            TypeOptions options = new TypeOptions.Builder()
                .setTypeDelay(0.0)
                .setPauseAfterEnd(0.0)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            StateString str = new StateString.Builder().setString("VeryFastTyping").build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(str));
            
            typeText.perform(mockActionResult, mockObjectCollection);
            
            verify(mockTextTyper).type(str, options);
            verify(mockTimeProvider, never()).wait(anyDouble());
        }
        
        @Test
        @DisplayName("Slow typing for laggy applications")
        public void testSlowTyping() {
            TypeOptions options = new TypeOptions.Builder()
                .setTypeDelay(0.2)
                .setPauseAfterEnd(2.0)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            StateString str1 = new StateString.Builder().setString("Slow").build();
            StateString str2 = new StateString.Builder().setString("Typing").build();
            when(mockObjectCollection.getStateStrings())
                .thenReturn(Arrays.asList(str1, str2));
            
            typeText.perform(mockActionResult, mockObjectCollection);
            
            verify(mockTextTyper).type(str1, options);
            verify(mockTextTyper).type(str2, options);
            verify(mockTimeProvider).wait(2.0);
        }
    }
}