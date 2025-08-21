package io.github.jspinak.brobot.action.basic.type;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.text.KeyUpWrapper;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for KeyUp - releases previously pressed keyboard keys.
 * Tests key release operations, modifier handling, and batch processing.
 */
@DisplayName("KeyUp Tests")
public class KeyUpTest extends BrobotTestBase {
    
    @Mock
    private KeyUpWrapper mockKeyUpWrapper;
    
    @Mock
    private ActionResult mockActionResult;
    
    @Mock
    private ObjectCollection mockObjectCollection;
    
    private KeyUp keyUp;
    private KeyUpOptions keyUpOptions;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        
        keyUp = new KeyUp(mockKeyUpWrapper);
        keyUpOptions = new KeyUpOptions.Builder().build();
        
        when(mockActionResult.getActionConfig()).thenReturn(keyUpOptions);
    }
    
    @Test
    @DisplayName("Should return KEY_UP action type")
    public void testGetActionType() {
        assertEquals(ActionInterface.Type.KEY_UP, keyUp.getActionType());
    }
    
    @Nested
    @DisplayName("Basic Key Release Operations")
    class BasicKeyReleaseOperations {
        
        @Test
        @DisplayName("Should release single key")
        public void testReleaseSingleKey() {
            StateString key = new StateString.Builder().setString("a").build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(key));
            
            keyUp.perform(mockActionResult, mockObjectCollection);
            
            verify(mockKeyUpWrapper).release("a");
            verify(mockKeyUpWrapper, never()).release(); // Should not release all
        }
        
        @Test
        @DisplayName("Should release multiple keys in sequence")
        public void testReleaseMultipleKeys() {
            StateString key1 = new StateString.Builder().setString("ctrl").build();
            StateString key2 = new StateString.Builder().setString("c").build();
            when(mockObjectCollection.getStateStrings())
                .thenReturn(Arrays.asList(key1, key2));
            
            keyUp.perform(mockActionResult, mockObjectCollection);
            
            InOrder inOrder = inOrder(mockKeyUpWrapper);
            inOrder.verify(mockKeyUpWrapper).release("ctrl");
            inOrder.verify(mockKeyUpWrapper).release("c");
            verify(mockKeyUpWrapper, never()).release(); // Should not release all
        }
        
        @Test
        @DisplayName("Should release all keys when no specific keys provided")
        public void testReleaseAllKeys() {
            when(mockObjectCollection.getStateStrings()).thenReturn(Collections.emptyList());
            
            keyUp.perform(mockActionResult, mockObjectCollection);
            
            verify(mockKeyUpWrapper).release(); // Release all keys
            verify(mockKeyUpWrapper, never()).release(anyString());
        }
    }
    
    @Nested
    @DisplayName("Modifier Key Handling")
    class ModifierKeyHandling {
        
        @Test
        @DisplayName("Should release modifier after regular keys")
        public void testReleaseModifierAfterKeys() {
            StateString key = new StateString.Builder().setString("a").build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(key));
            
            KeyUpOptions options = new KeyUpOptions.Builder()
                .setModifiers(List.of("CTRL"))
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            keyUp.perform(mockActionResult, mockObjectCollection);
            
            InOrder inOrder = inOrder(mockKeyUpWrapper);
            inOrder.verify(mockKeyUpWrapper).release("a");
            inOrder.verify(mockKeyUpWrapper).release("CTRL");
        }
        
        @Test
        @DisplayName("Should release multiple modifiers as single string")
        public void testReleaseMultipleModifiers() {
            StateString key = new StateString.Builder().setString("s").build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(key));
            
            KeyUpOptions options = new KeyUpOptions.Builder()
                .setModifiers(Arrays.asList("CTRL", "SHIFT"))
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            keyUp.perform(mockActionResult, mockObjectCollection);
            
            InOrder inOrder = inOrder(mockKeyUpWrapper);
            inOrder.verify(mockKeyUpWrapper).release("s");
            inOrder.verify(mockKeyUpWrapper).release("CTRL+SHIFT");
        }
        
        @Test
        @DisplayName("Should release modifiers even without regular keys")
        public void testReleaseOnlyModifiers() {
            when(mockObjectCollection.getStateStrings()).thenReturn(Collections.emptyList());
            
            KeyUpOptions options = new KeyUpOptions.Builder()
                .setModifiers(Arrays.asList("CTRL", "ALT"))
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            keyUp.perform(mockActionResult, mockObjectCollection);
            
            verify(mockKeyUpWrapper).release("CTRL+ALT");
            verify(mockKeyUpWrapper, never()).release(); // Should not release all
        }
        
        @Test
        @DisplayName("Should handle three modifiers")
        public void testThreeModifiers() {
            StateString key = new StateString.Builder().setString("delete").build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(key));
            
            KeyUpOptions options = new KeyUpOptions.Builder()
                .setModifiers(Arrays.asList("CTRL", "ALT", "SHIFT"))
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            keyUp.perform(mockActionResult, mockObjectCollection);
            
            InOrder inOrder = inOrder(mockKeyUpWrapper);
            inOrder.verify(mockKeyUpWrapper).release("delete");
            inOrder.verify(mockKeyUpWrapper).release("CTRL+ALT+SHIFT");
        }
    }
    
    @Nested
    @DisplayName("Release All Behavior")
    class ReleaseAllBehavior {
        
        @Test
        @DisplayName("Should release all when null collections")
        public void testReleaseAllWithNullCollections() {
            keyUp.perform(mockActionResult, (ObjectCollection[]) null);
            
            verify(mockKeyUpWrapper).release(); // Release all
            verify(mockKeyUpWrapper, never()).release(anyString());
        }
        
        @Test
        @DisplayName("Should release all when empty collections array")
        public void testReleaseAllWithEmptyArray() {
            keyUp.perform(mockActionResult);
            
            verify(mockKeyUpWrapper).release(); // Release all
            verify(mockKeyUpWrapper, never()).release(anyString());
        }
        
        @Test
        @DisplayName("Should release all when collection has empty strings list")
        public void testReleaseAllWithEmptyStringsList() {
            when(mockObjectCollection.getStateStrings()).thenReturn(Collections.emptyList());
            
            keyUp.perform(mockActionResult, mockObjectCollection);
            
            verify(mockKeyUpWrapper).release(); // Release all
            verify(mockKeyUpWrapper, never()).release(anyString());
        }
        
        @Test
        @DisplayName("Should not release all when has keys or modifiers")
        public void testNotReleaseAllWithContent() {
            StateString key = new StateString.Builder().setString("x").build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(key));
            
            keyUp.perform(mockActionResult, mockObjectCollection);
            
            verify(mockKeyUpWrapper).release("x");
            verify(mockKeyUpWrapper, never()).release(); // Should not release all
        }
    }
    
    @Nested
    @DisplayName("Special Keys")
    class SpecialKeys {
        
        @ParameterizedTest
        @ValueSource(strings = {"TAB", "ENTER", "ESC", "SPACE", "BACKSPACE", "DELETE"})
        @DisplayName("Should release special keys")
        public void testReleaseSpecialKeys(String specialKey) {
            StateString key = new StateString.Builder().setString(specialKey).build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(key));
            
            keyUp.perform(mockActionResult, mockObjectCollection);
            
            verify(mockKeyUpWrapper).release(specialKey);
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"F1", "F2", "F3", "F10", "F11", "F12"})
        @DisplayName("Should release function keys")
        public void testReleaseFunctionKeys(String functionKey) {
            StateString key = new StateString.Builder().setString(functionKey).build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(key));
            
            keyUp.perform(mockActionResult, mockObjectCollection);
            
            verify(mockKeyUpWrapper).release(functionKey);
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"UP", "DOWN", "LEFT", "RIGHT", "HOME", "END", "PAGE_UP", "PAGE_DOWN"})
        @DisplayName("Should release navigation keys")
        public void testReleaseNavigationKeys(String navKey) {
            StateString key = new StateString.Builder().setString(navKey).build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(key));
            
            keyUp.perform(mockActionResult, mockObjectCollection);
            
            verify(mockKeyUpWrapper).release(navKey);
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Should throw exception for invalid configuration")
        public void testInvalidConfiguration() {
            when(mockActionResult.getActionConfig()).thenReturn(new TypeOptions.Builder().build());
            
            assertThrows(IllegalArgumentException.class, () ->
                keyUp.perform(mockActionResult, mockObjectCollection));
        }
        
        @Test
        @DisplayName("Should throw exception for null configuration")
        public void testNullConfiguration() {
            when(mockActionResult.getActionConfig()).thenReturn(null);
            
            assertThrows(IllegalArgumentException.class, () ->
                keyUp.perform(mockActionResult, mockObjectCollection));
        }
    }
    
    @Nested
    @DisplayName("Complex Key Combinations")
    class ComplexKeyCombinations {
        
        @Test
        @DisplayName("Should release Ctrl+C combination")
        public void testReleaseCtrlC() {
            List<StateString> keys = Arrays.asList(
                new StateString.Builder().setString("c").build(),
                new StateString.Builder().setString("ctrl").build()
            );
            when(mockObjectCollection.getStateStrings()).thenReturn(keys);
            
            keyUp.perform(mockActionResult, mockObjectCollection);
            
            InOrder inOrder = inOrder(mockKeyUpWrapper);
            inOrder.verify(mockKeyUpWrapper).release("c");
            inOrder.verify(mockKeyUpWrapper).release("ctrl");
        }
        
        @Test
        @DisplayName("Should release Alt+Tab combination")
        public void testReleaseAltTab() {
            List<StateString> keys = Arrays.asList(
                new StateString.Builder().setString("tab").build(),
                new StateString.Builder().setString("alt").build()
            );
            when(mockObjectCollection.getStateStrings()).thenReturn(keys);
            
            keyUp.perform(mockActionResult, mockObjectCollection);
            
            InOrder inOrder = inOrder(mockKeyUpWrapper);
            inOrder.verify(mockKeyUpWrapper).release("tab");
            inOrder.verify(mockKeyUpWrapper).release("alt");
        }
        
        @Test
        @DisplayName("Should release Ctrl+Shift+Esc combination in reverse")
        public void testReleaseCtrlShiftEsc() {
            List<StateString> keys = Arrays.asList(
                new StateString.Builder().setString("esc").build(),
                new StateString.Builder().setString("shift").build(),
                new StateString.Builder().setString("ctrl").build()
            );
            when(mockObjectCollection.getStateStrings()).thenReturn(keys);
            
            keyUp.perform(mockActionResult, mockObjectCollection);
            
            InOrder inOrder = inOrder(mockKeyUpWrapper);
            inOrder.verify(mockKeyUpWrapper).release("esc");
            inOrder.verify(mockKeyUpWrapper).release("shift");
            inOrder.verify(mockKeyUpWrapper).release("ctrl");
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
            
            // Setup KeyUpOptions for the test
            KeyUpOptions options = new KeyUpOptions.Builder().build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            keyUp.perform(mockActionResult, collection1, collection2);
            
            verify(mockKeyUpWrapper).release("a");
            verify(mockKeyUpWrapper, never()).release("b");
            // collection1.getStateStrings() is called twice: once in nothingToRelease() and once in perform()
            verify(collection1, times(2)).getStateStrings();
            verify(collection2, never()).getStateStrings();
        }
    }
    
    @Nested
    @DisplayName("StateString Properties")
    class StateStringProperties {
        
        @Test
        @DisplayName("Should handle StateString with owner state")
        public void testStateStringWithOwner() {
            StateString key = new StateString.Builder()
                .setString("enter")
                .setOwnerStateName("LoginState")
                .build();
            when(mockObjectCollection.getStateStrings()).thenReturn(List.of(key));
            
            keyUp.perform(mockActionResult, mockObjectCollection);
            
            verify(mockKeyUpWrapper).release("enter");
        }
        
        @Test
        @DisplayName("Should handle multiple StateStrings from different states")
        public void testMultipleStatesStrings() {
            StateString key1 = new StateString.Builder()
                .setString("tab")
                .setOwnerStateName("FormState")
                .build();
            StateString key2 = new StateString.Builder()
                .setString("enter")
                .setOwnerStateName("SubmitState")
                .build();
            
            when(mockObjectCollection.getStateStrings())
                .thenReturn(Arrays.asList(key1, key2));
            
            keyUp.perform(mockActionResult, mockObjectCollection);
            
            verify(mockKeyUpWrapper).release("tab");
            verify(mockKeyUpWrapper).release("enter");
        }
    }
    
    @Nested
    @DisplayName("Release Order")
    class ReleaseOrder {
        
        @Test
        @DisplayName("Should release keys in collection order then modifiers")
        public void testReleaseOrder() {
            List<StateString> keys = Arrays.asList(
                new StateString.Builder().setString("x").build(),
                new StateString.Builder().setString("y").build(),
                new StateString.Builder().setString("z").build()
            );
            when(mockObjectCollection.getStateStrings()).thenReturn(keys);
            
            KeyUpOptions options = new KeyUpOptions.Builder()
                .setModifiers(Arrays.asList("CTRL", "ALT"))
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            keyUp.perform(mockActionResult, mockObjectCollection);
            
            InOrder inOrder = inOrder(mockKeyUpWrapper);
            inOrder.verify(mockKeyUpWrapper).release("x");
            inOrder.verify(mockKeyUpWrapper).release("y");
            inOrder.verify(mockKeyUpWrapper).release("z");
            inOrder.verify(mockKeyUpWrapper).release("CTRL+ALT");
        }
    }
}