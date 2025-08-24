package io.github.jspinak.brobot.action.basic.type;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.text.TextTyper;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Fixed test suite for TypeText functionality with proper mock configuration.
 * Extends BrobotTestBase for proper mock mode and uses lenient mocking.
 */
@DisplayName("TypeText Tests - Fixed")
@MockitoSettings(strictness = Strictness.LENIENT)
public class TypeTextTestFixed extends BrobotTestBase {
    
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
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest(); // Enable mock mode
        MockitoAnnotations.openMocks(this);
        
        typeText = new TypeText(mockTextTyper, mockTimeProvider);
        typeOptions = new TypeOptions.Builder().build();
        when(mockActionResult.getActionConfig()).thenReturn(typeOptions);
    }
    
    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {
        
        @Test
        @DisplayName("Type form data sequence - Fixed")
        public void testFormDataSequence() {
            // Given
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
            
            // When
            typeText.perform(mockActionResult, mockObjectCollection);
            
            // Then - verify each text is typed (3 times total)
            verify(mockTextTyper, times(3)).type(any(StateString.class), eq(options));
            
            // Verify specific texts were typed using ArgumentCaptor
            ArgumentCaptor<StateString> stringCaptor = ArgumentCaptor.forClass(StateString.class);
            verify(mockTextTyper, times(3)).type(stringCaptor.capture(), eq(options));
            
            List<StateString> capturedStrings = stringCaptor.getAllValues();
            assertEquals(3, capturedStrings.size());
            assertEquals("user@example.com", capturedStrings.get(0).getString());
            assertEquals("SecurePass123!", capturedStrings.get(1).getString());
            assertEquals("SecurePass123!", capturedStrings.get(2).getString());
            
            // There should be 3 texts, so 2 pauses between them (not after the last one)
            verify(mockTimeProvider, times(2)).wait(0.3);
        }
        
        @Test
        @DisplayName("Type command sequence - Fixed")
        public void testCommandSequence() {
            // Given
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
            
            // When
            typeText.perform(mockActionResult, mockObjectCollection);
            
            // Then
            verify(mockTextTyper, times(3)).type(any(StateString.class), eq(options));
            // 3 commands means 2 pauses (between commands, not after the last one)
            verify(mockTimeProvider, times(2)).wait(1.0);
        }
        
        @Test
        @DisplayName("Type single text")
        public void testSingleText() {
            // Given
            StateString text = new StateString.Builder().setString("Hello World").build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(text));
            
            TypeOptions options = new TypeOptions.Builder()
                .setPauseAfterEnd(0.5)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            // When
            typeText.perform(mockActionResult, mockObjectCollection);
            
            // Then
            verify(mockTextTyper).type(text, options);
            // Only one text, so no pause after (pause only happens between texts)
            verify(mockTimeProvider, never()).wait(anyDouble());
        }
        
        @Test
        @DisplayName("Type with no pause")
        public void testNoPause() {
            // Given
            StateString text = new StateString.Builder().setString("NoPause").build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(text));
            
            TypeOptions options = new TypeOptions.Builder()
                .setPauseAfterEnd(0.0) // No pause
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            // When
            typeText.perform(mockActionResult, mockObjectCollection);
            
            // Then
            verify(mockTextTyper).type(text, options);
            // No pause when pauseAfterEnd is 0
            verify(mockTimeProvider, never()).wait(anyDouble());
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Handle empty text list")
        public void testEmptyTextList() {
            // Given
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of());
            
            // When
            typeText.perform(mockActionResult, mockObjectCollection);
            
            // Then
            verify(mockTextTyper, never()).type(any(), any());
        }
        
        @Test
        @DisplayName("Handle null text")
        public void testNullText() {
            // Given
            when(mockObjectCollection.getStateStrings()).thenReturn(null);
            
            // When
            typeText.perform(mockActionResult, mockObjectCollection);
            
            // Then
            verify(mockTextTyper, never()).type(any(), any());
        }
    }
}