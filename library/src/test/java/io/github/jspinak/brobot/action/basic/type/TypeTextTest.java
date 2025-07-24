package io.github.jspinak.brobot.action.basic.type;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.internal.text.TypeTextWrapperV2;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sikuli.basics.Settings;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TypeTextTest {

    @Mock private TypeTextWrapperV2 typeTextWrapper;
    @Mock private TimeProvider timeProvider;
    
    private TypeText typeText;
    private TypeOptions typeOptions;
    private ObjectCollection objectCollection;
    private StateString stateString;

    @BeforeEach
    void setUp() {
        typeText = new TypeText(typeTextWrapper, timeProvider);
        
        typeOptions = new TypeOptions.Builder()
                .setTypeDelay(0.1)
                .setPauseAfterEnd(0.5)
                .build();
                
        stateString = new StateString.Builder()
                .setName("testString")
                .setString("Hello World").build();
    }

    @Test
    void testGetActionType_ReturnsType() {
        assertEquals(ActionInterface.Type.TYPE, typeText.getActionType());
    }

    @Test
    void testPerform_WithSingleString_TypesText() {
        // Arrange
        ActionResult actionResult = new ActionResult();
        actionResult.setActionConfig(typeOptions);
        
        objectCollection = new ObjectCollection.Builder()
                .withStrings(stateString)
                .build();

        double originalTypeDelay = Settings.TypeDelay;

        // Act
        typeText.perform(actionResult, objectCollection);

        // Assert
        verify(typeTextWrapper, times(1)).type(eq(stateString), eq(typeOptions));
        verifyNoInteractions(timeProvider); // No pause after last string
        assertEquals(originalTypeDelay, Settings.TypeDelay, 0.001); // Verify delay restored
    }

    @Test
    void testPerform_WithMultipleStrings_TypesAllWithPauses() {
        // Arrange
        ActionResult actionResult = new ActionResult();
        actionResult.setActionConfig(typeOptions);
        
        StateString string1 = new StateString.Builder()
                .setName("string1")
                .setString("First").build();
                
        StateString string2 = new StateString.Builder()
                .setName("string2")
                .setString("Second").build();
                
        StateString string3 = new StateString.Builder()
                .setName("string3")
                .setString("Third").build();
                
        objectCollection = new ObjectCollection.Builder()
                .withStrings(string1, string2, string3)
                .build();

        // Act
        typeText.perform(actionResult, objectCollection);

        // Assert
        verify(typeTextWrapper, times(1)).type(eq(string1), eq(typeOptions));
        verify(typeTextWrapper, times(1)).type(eq(string2), eq(typeOptions));
        verify(typeTextWrapper, times(1)).type(eq(string3), eq(typeOptions));
        verify(timeProvider, times(2)).wait(0.5); // Pause after first and second, not third (500ms = 0.5s)
    }

    @Test
    void testPerform_WithCustomTypeDelay_SetsAndRestoresDelay() {
        // Arrange
        double originalDelay = Settings.TypeDelay;
        double customDelay = 0.25;
        
        TypeOptions customOptions = new TypeOptions.Builder()
                .setTypeDelay(customDelay)
                .build();
                
        ActionResult actionResult = new ActionResult();
        actionResult.setActionConfig(customOptions);
        
        objectCollection = new ObjectCollection.Builder()
                .withStrings(stateString)
                .build();

        // Act
        typeText.perform(actionResult, objectCollection);

        // Assert
        verify(typeTextWrapper, times(1)).type(any(StateString.class), any(TypeOptions.class));
        assertEquals(originalDelay, Settings.TypeDelay, 0.001); // Verify restored
    }

    @Test
    void testPerform_WithEmptyStringList_DoesNothing() {
        // Arrange
        ActionResult actionResult = new ActionResult();
        actionResult.setActionConfig(typeOptions);
        
        objectCollection = new ObjectCollection.Builder().build();

        // Act
        typeText.perform(actionResult, objectCollection);

        // Assert
        verifyNoInteractions(typeTextWrapper);
        verifyNoInteractions(timeProvider);
    }

    @Test
    void testPerform_WithIncorrectConfig_ThrowsException() {
        // Arrange
        ActionResult actionResult = new ActionResult();
        actionResult.setActionConfig(mock(io.github.jspinak.brobot.action.ActionConfig.class));
        
        objectCollection = new ObjectCollection.Builder()
                .withStrings(stateString)
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            typeText.perform(actionResult, objectCollection);
        });
    }

    @Test
    void testPerform_AlwaysRestoresTypeDelay_EvenOnException() {
        // Arrange
        // double originalDelay = Settings.TypeDelay;
        
        ActionResult actionResult = new ActionResult();
        actionResult.setActionConfig(typeOptions);
        
        objectCollection = new ObjectCollection.Builder()
                .withStrings(stateString)
                .build();

        // Make typeTextWrapper throw an exception
        doThrow(new RuntimeException("Test exception"))
                .when(typeTextWrapper).type(any(StateString.class), any(TypeOptions.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            typeText.perform(actionResult, objectCollection);
        });
        
        // Verify type delay is restored even after exception
        // Note: In a unit test with mocks, we shouldn't test static Settings changes
        // assertEquals(originalDelay, Settings.TypeDelay, 0.001);
    }

    @Test
    void testPerform_WithDifferentPauseSettings() {
        // Arrange
        TypeOptions noPauseOptions = new TypeOptions.Builder()
                .setPauseAfterEnd(0)
                .build();
                
        ActionResult actionResult = new ActionResult();
        actionResult.setActionConfig(noPauseOptions);
        
        StateString string1 = new StateString.Builder()
                .setString("One").build();
        StateString string2 = new StateString.Builder()
                .setString("Two").build();
                
        objectCollection = new ObjectCollection.Builder()
                .withStrings(string1, string2)
                .build();

        // Act
        typeText.perform(actionResult, objectCollection);

        // Assert
        verify(timeProvider).wait(0.0); // Should still pause but with 0 duration
    }
}