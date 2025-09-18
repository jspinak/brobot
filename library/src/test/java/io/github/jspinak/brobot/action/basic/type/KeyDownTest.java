package io.github.jspinak.brobot.action.basic.type;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.text.KeyDownWrapper;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.DisabledInCI;
import io.github.jspinak.brobot.tools.testing.wrapper.TimeWrapper;

/**
 * Comprehensive test suite for KeyDown - presses and holds keyboard keys. Tests key press
 * operations, modifier handling, and timing control.
 */
@DisplayName("KeyDown Tests")
@DisabledInCI
public class KeyDownTest extends BrobotTestBase {

    @Mock private KeyDownWrapper mockKeyDownWrapper;

    @Mock private TimeWrapper mockTimeWrapper;

    @Mock private ActionResult mockActionResult;

    @Mock private ObjectCollection mockObjectCollection;

    private KeyDown keyDown;
    private KeyDownOptions keyDownOptions;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);

        keyDown = new KeyDown(mockKeyDownWrapper, mockTimeWrapper);
        keyDownOptions = new KeyDownOptions.Builder().build();

        when(mockActionResult.getActionConfig()).thenReturn(keyDownOptions);
    }

    @Test
    @DisplayName("Should return KEY_DOWN action type")
    public void testGetActionType() {
        assertEquals(ActionInterface.Type.KEY_DOWN, keyDown.getActionType());
    }

    @Nested
    @DisplayName("Basic Key Press Operations")
    class BasicKeyPressOperations {

        @Test
        @DisplayName("Should press single key")
        public void testPressSingleKey() {
            StateString key = new StateString.Builder().setString("a").build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(key));

            keyDown.perform(mockActionResult, mockObjectCollection);

            verify(mockKeyDownWrapper).press("a", "");
            verify(mockTimeWrapper, never()).wait(anyDouble());
        }

        @Test
        @DisplayName("Should press multiple keys in sequence")
        public void testPressMultipleKeys() {
            StateString key1 = new StateString.Builder().setString("ctrl").build();
            StateString key2 = new StateString.Builder().setString("c").build();
            when(mockObjectCollection.getStateStrings()).thenReturn(Arrays.asList(key1, key2));

            KeyDownOptions options = new KeyDownOptions.Builder().setPauseBetweenKeys(0.1).build();
            when(mockActionResult.getActionConfig()).thenReturn(options);

            keyDown.perform(mockActionResult, mockObjectCollection);

            InOrder inOrder = inOrder(mockKeyDownWrapper, mockTimeWrapper);
            inOrder.verify(mockKeyDownWrapper).press("ctrl", "");
            inOrder.verify(mockTimeWrapper).wait(0.1);
            inOrder.verify(mockKeyDownWrapper).press("c", "");
        }

        @Test
        @DisplayName("Should handle empty key list")
        public void testEmptyKeyList() {
            when(mockObjectCollection.getStateStrings()).thenReturn(Collections.emptyList());

            keyDown.perform(mockActionResult, mockObjectCollection);

            verify(mockKeyDownWrapper, never()).press(anyString(), anyString());
            verify(mockTimeWrapper, never()).wait(anyDouble());
        }
    }

    @Nested
    @DisplayName("Modifier Key Handling")
    class ModifierKeyHandling {

        @Test
        @DisplayName("Should apply single modifier")
        public void testSingleModifier() {
            StateString key = new StateString.Builder().setString("a").build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(key));

            KeyDownOptions options =
                    new KeyDownOptions.Builder().setModifiers(List.of("CTRL")).build();
            when(mockActionResult.getActionConfig()).thenReturn(options);

            keyDown.perform(mockActionResult, mockObjectCollection);

            verify(mockKeyDownWrapper).press("a", "CTRL");
        }

        @Test
        @DisplayName("Should apply multiple modifiers")
        public void testMultipleModifiers() {
            StateString key = new StateString.Builder().setString("s").build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(key));

            KeyDownOptions options =
                    new KeyDownOptions.Builder()
                            .setModifiers(Arrays.asList("CTRL", "SHIFT"))
                            .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);

            keyDown.perform(mockActionResult, mockObjectCollection);

            verify(mockKeyDownWrapper).press("s", "CTRL+SHIFT");
        }

        @Test
        @DisplayName("Should handle three modifiers")
        public void testThreeModifiers() {
            StateString key = new StateString.Builder().setString("delete").build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(key));

            KeyDownOptions options =
                    new KeyDownOptions.Builder()
                            .setModifiers(Arrays.asList("CTRL", "ALT", "SHIFT"))
                            .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);

            keyDown.perform(mockActionResult, mockObjectCollection);

            verify(mockKeyDownWrapper).press("delete", "CTRL+ALT+SHIFT");
        }

        @Test
        @DisplayName("Should handle empty modifiers list")
        public void testEmptyModifiers() {
            StateString key = new StateString.Builder().setString("enter").build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(key));

            KeyDownOptions options =
                    new KeyDownOptions.Builder().setModifiers(Collections.emptyList()).build();
            when(mockActionResult.getActionConfig()).thenReturn(options);

            keyDown.perform(mockActionResult, mockObjectCollection);

            verify(mockKeyDownWrapper).press("enter", "");
        }
    }

    @Nested
    @DisplayName("Special Keys")
    class SpecialKeys {

        @ParameterizedTest
        @ValueSource(strings = {"TAB", "ENTER", "ESC", "SPACE", "BACKSPACE", "DELETE"})
        @DisplayName("Should handle special keys")
        public void testSpecialKeys(String specialKey) {
            StateString key = new StateString.Builder().setString(specialKey).build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(key));

            keyDown.perform(mockActionResult, mockObjectCollection);

            verify(mockKeyDownWrapper).press(specialKey, "");
        }

        @ParameterizedTest
        @ValueSource(strings = {"F1", "F2", "F3", "F10", "F11", "F12"})
        @DisplayName("Should handle function keys")
        public void testFunctionKeys(String functionKey) {
            StateString key = new StateString.Builder().setString(functionKey).build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(key));

            keyDown.perform(mockActionResult, mockObjectCollection);

            verify(mockKeyDownWrapper).press(functionKey, "");
        }

        @ParameterizedTest
        @ValueSource(
                strings = {"UP", "DOWN", "LEFT", "RIGHT", "HOME", "END", "PAGE_UP", "PAGE_DOWN"})
        @DisplayName("Should handle navigation keys")
        public void testNavigationKeys(String navKey) {
            StateString key = new StateString.Builder().setString(navKey).build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(key));

            keyDown.perform(mockActionResult, mockObjectCollection);

            verify(mockKeyDownWrapper).press(navKey, "");
        }
    }

    @Nested
    @DisplayName("Timing Control")
    class TimingControl {

        @ParameterizedTest
        @CsvSource({"0.0, 2", "0.1, 2", "0.5, 2", "1.0, 2"})
        @DisplayName("Should pause between keys with custom timing")
        public void testCustomPauseBetweenKeys(double pause, int keyCount) {
            List<StateString> keys =
                    Arrays.asList(
                            new StateString.Builder().setString("key1").build(),
                            new StateString.Builder().setString("key2").build());
            when(mockObjectCollection.getStateStrings()).thenReturn(keys);

            KeyDownOptions options =
                    new KeyDownOptions.Builder().setPauseBetweenKeys(pause).build();
            when(mockActionResult.getActionConfig()).thenReturn(options);

            keyDown.perform(mockActionResult, mockObjectCollection);

            verify(mockTimeWrapper, times(keyCount - 1)).wait(pause);
        }

        @Test
        @DisplayName("Should not pause after last key")
        public void testNoPauseAfterLastKey() {
            List<StateString> keys =
                    Arrays.asList(
                            new StateString.Builder().setString("a").build(),
                            new StateString.Builder().setString("b").build(),
                            new StateString.Builder().setString("c").build());
            when(mockObjectCollection.getStateStrings()).thenReturn(keys);

            KeyDownOptions options = new KeyDownOptions.Builder().setPauseBetweenKeys(0.2).build();
            when(mockActionResult.getActionConfig()).thenReturn(options);

            keyDown.perform(mockActionResult, mockObjectCollection);

            // Should pause only between keys, not after the last one
            verify(mockTimeWrapper, times(2)).wait(0.2);
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should throw exception for invalid configuration")
        public void testInvalidConfiguration() {
            when(mockActionResult.getActionConfig()).thenReturn(new TypeOptions.Builder().build());

            assertThrows(
                    IllegalArgumentException.class,
                    () -> keyDown.perform(mockActionResult, mockObjectCollection));
        }

        @Test
        @DisplayName("Should throw exception for null configuration")
        public void testNullConfiguration() {
            when(mockActionResult.getActionConfig()).thenReturn(null);

            assertThrows(
                    IllegalArgumentException.class,
                    () -> keyDown.perform(mockActionResult, mockObjectCollection));
        }

        @Test
        @DisplayName("Should handle null object collections")
        public void testNullObjectCollections() {
            assertDoesNotThrow(() -> keyDown.perform(mockActionResult, (ObjectCollection[]) null));

            verify(mockKeyDownWrapper, never()).press(anyString(), anyString());
        }

        @Test
        @DisplayName("Should handle empty object collections array")
        public void testEmptyObjectCollectionsArray() {
            assertDoesNotThrow(() -> keyDown.perform(mockActionResult));

            verify(mockKeyDownWrapper, never()).press(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Complex Key Combinations")
    class ComplexKeyCombinations {

        @Test
        @DisplayName("Should handle Ctrl+C combination")
        public void testCtrlC() {
            List<StateString> keys =
                    Arrays.asList(
                            new StateString.Builder().setString("ctrl").build(),
                            new StateString.Builder().setString("c").build());
            when(mockObjectCollection.getStateStrings()).thenReturn(keys);

            keyDown.perform(mockActionResult, mockObjectCollection);

            InOrder inOrder = inOrder(mockKeyDownWrapper);
            inOrder.verify(mockKeyDownWrapper).press("ctrl", "");
            inOrder.verify(mockKeyDownWrapper).press("c", "");
        }

        @Test
        @DisplayName("Should handle Alt+Tab combination")
        public void testAltTab() {
            List<StateString> keys =
                    Arrays.asList(
                            new StateString.Builder().setString("alt").build(),
                            new StateString.Builder().setString("tab").build());
            when(mockObjectCollection.getStateStrings()).thenReturn(keys);

            keyDown.perform(mockActionResult, mockObjectCollection);

            InOrder inOrder = inOrder(mockKeyDownWrapper);
            inOrder.verify(mockKeyDownWrapper).press("alt", "");
            inOrder.verify(mockKeyDownWrapper).press("tab", "");
        }

        @Test
        @DisplayName("Should handle Ctrl+Shift+Esc combination")
        public void testCtrlShiftEsc() {
            List<StateString> keys =
                    Arrays.asList(
                            new StateString.Builder().setString("ctrl").build(),
                            new StateString.Builder().setString("shift").build(),
                            new StateString.Builder().setString("esc").build());
            when(mockObjectCollection.getStateStrings()).thenReturn(keys);

            keyDown.perform(mockActionResult, mockObjectCollection);

            InOrder inOrder = inOrder(mockKeyDownWrapper);
            inOrder.verify(mockKeyDownWrapper).press("ctrl", "");
            inOrder.verify(mockKeyDownWrapper).press("shift", "");
            inOrder.verify(mockKeyDownWrapper).press("esc", "");
        }
    }

    @Nested
    @DisplayName("Multiple ObjectCollections")
    class MultipleObjectCollections {

        @Test
        @DisplayName("Should only use first ObjectCollection")
        public void testOnlyFirstCollectionUsed() {
            ObjectCollection collection1 = mock(ObjectCollection.class);
            ObjectCollection collection2 = mock(ObjectCollection.class);

            StateString key1 = new StateString.Builder().setString("a").build();
            StateString key2 = new StateString.Builder().setString("b").build();

            when(collection1.getStateStrings()).thenReturn(List.of(key1));
            when(collection2.getStateStrings()).thenReturn(List.of(key2));

            keyDown.perform(mockActionResult, collection1, collection2);

            verify(mockKeyDownWrapper).press("a", "");
            verify(mockKeyDownWrapper, never()).press("b", "");
            verify(collection1).getStateStrings();
            verify(collection2, never()).getStateStrings();
        }
    }

    @Nested
    @DisplayName("StateString Properties")
    class StateStringProperties {

        @Test
        @DisplayName("Should handle StateString with owner state")
        public void testStateStringWithOwner() {
            StateString key =
                    new StateString.Builder()
                            .setString("enter")
                            .setOwnerStateName("LoginState")
                            .build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(key));

            keyDown.perform(mockActionResult, mockObjectCollection);

            verify(mockKeyDownWrapper).press("enter", "");
        }

        @Test
        @DisplayName("Should handle multiple StateStrings from different states")
        public void testMultipleStatesStrings() {
            StateString key1 =
                    new StateString.Builder()
                            .setString("tab")
                            .setOwnerStateName("FormState")
                            .build();
            StateString key2 =
                    new StateString.Builder()
                            .setString("enter")
                            .setOwnerStateName("SubmitState")
                            .build();

            when(mockObjectCollection.getStateStrings()).thenReturn(Arrays.asList(key1, key2));

            keyDown.perform(mockActionResult, mockObjectCollection);

            verify(mockKeyDownWrapper).press("tab", "");
            verify(mockKeyDownWrapper).press("enter", "");
        }
    }
}
