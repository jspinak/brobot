package io.github.jspinak.brobot.action;

import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.action.basic.mouse.ScrollOptions;
import io.github.jspinak.brobot.action.basic.type.KeyDownOptions;
import io.github.jspinak.brobot.action.basic.type.KeyUpOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.vanish.VanishOptions;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for ConditionalActionChain - conditional workflow execution.
 * Tests chaining, conditional logic, convenience methods, and control flow.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConditionalActionChainTest extends BrobotTestBase {
    
    @Mock private Action action;
    @Mock private ActionResult successResult;
    @Mock private ActionResult failureResult;
    @Mock private ObjectCollection defaultCollection;
    @Mock private StateImage stateImage;
    @Mock private Location location;
    @Mock private Match match;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        
        // Setup success result
        when(successResult.isSuccess()).thenReturn(true);
        when(successResult.getMatchList()).thenReturn(List.of(match));
        when(successResult.getMatchLocations()).thenReturn(List.of(location));
        
        // Setup failure result
        when(failureResult.isSuccess()).thenReturn(false);
        when(failureResult.getMatchList()).thenReturn(new ArrayList<>());
        when(failureResult.getMatchLocations()).thenReturn(new ArrayList<>());
        
        // Setup default action behavior - use the correct overload
        when(action.perform(any(ActionConfig.class), any(ObjectCollection[].class)))
            .thenReturn(successResult);
    }
    
    @Nested
    @DisplayName("Chain Creation Tests")
    class ChainCreationTests {
        
        @Test
        @DisplayName("Should create chain starting with find")
        void shouldCreateChainStartingWithFind() {
            // Given
            PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
            
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(findOptions);
            
            // Then
            assertNotNull(chain);
            ActionResult result = chain.perform(action, defaultCollection);
            verify(action).perform(eq(findOptions), any(ObjectCollection[].class));
        }
        
        @Test
        @DisplayName("Should create chain starting with StateImage")
        void shouldCreateChainStartingWithStateImage() {
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(stateImage);
            
            // Then
            assertNotNull(chain);
            ActionResult result = chain.perform(action, defaultCollection);
            
            ArgumentCaptor<ActionConfig> configCaptor = ArgumentCaptor.forClass(ActionConfig.class);
            ArgumentCaptor<ObjectCollection> collectionCaptor = ArgumentCaptor.forClass(ObjectCollection.class);
            verify(action).perform(configCaptor.capture(), collectionCaptor.capture());
            
            assertTrue(configCaptor.getValue() instanceof PatternFindOptions);
            assertEquals(stateImage, collectionCaptor.getValue().getStateImages().get(0));
        }
        
        @Test
        @DisplayName("Should create chain starting with any action")
        void shouldCreateChainStartingWithAnyAction() {
            // Given
            ClickOptions clickOptions = new ClickOptions.Builder().build();
            
            // When
            ConditionalActionChain chain = ConditionalActionChain.start(clickOptions);
            
            // Then
            assertNotNull(chain);
            ActionResult result = chain.perform(action, defaultCollection);
            verify(action).perform(eq(clickOptions), any(ObjectCollection[].class));
        }
    }
    
    @Nested
    @DisplayName("Sequential Chaining Tests")
    class SequentialChainingTests {
        
        @Test
        @DisplayName("Should execute sequential actions with then()")
        void shouldExecuteSequentialActionsWithThen() {
            // Given
            PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
            ClickOptions clickOptions = new ClickOptions.Builder().build();
            
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(findOptions)
                .then(clickOptions);
            
            chain.perform(action, defaultCollection);
            
            // Then
            verify(action).perform(eq(findOptions), any(ObjectCollection[].class));
            verify(action).perform(eq(clickOptions), any(ObjectCollection[].class));
        }
        
        @Test
        @DisplayName("Should chain multiple then() calls")
        void shouldChainMultipleThenCalls() {
            // Given
            PatternFindOptions find1 = new PatternFindOptions.Builder().build();
            ClickOptions click = new ClickOptions.Builder().build();
            PatternFindOptions find2 = new PatternFindOptions.Builder().build();
            
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(find1)
                .then(click)
                .then(find2);
            
            chain.perform(action, defaultCollection);
            
            // Then
            verify(action, times(2)).perform(any(PatternFindOptions.class), any(ObjectCollection[].class));
            verify(action).perform(eq(click), any(ObjectCollection[].class));
        }
        
        @Test
        @DisplayName("Should chain with StateImage using then()")
        void shouldChainWithStateImageUsingThen() {
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(stateImage)
                .then(stateImage);
            
            chain.perform(action, defaultCollection);
            
            // Then
            verify(action, times(2)).perform(any(PatternFindOptions.class), any(ObjectCollection[].class));
        }
    }
    
    @Nested
    @DisplayName("Conditional Execution Tests")
    class ConditionalExecutionTests {
        
        @Test
        @DisplayName("Should execute ifFound when previous succeeded")
        void shouldExecuteIfFoundWhenPreviousSucceeded() {
            // Given
            when(action.perform(any(PatternFindOptions.class), any(ObjectCollection[].class))).thenReturn(successResult);
            ClickOptions clickOptions = new ClickOptions.Builder().build();
            
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(stateImage)
                .ifFound(clickOptions);
            
            chain.perform(action, defaultCollection);
            
            // Then
            verify(action).perform(any(PatternFindOptions.class), any(ObjectCollection[].class));
            verify(action).perform(eq(clickOptions), any(ObjectCollection[].class));
        }
        
        @Test
        @DisplayName("Should skip ifFound when previous failed")
        void shouldSkipIfFoundWhenPreviousFailed() {
            // Given
            when(action.perform(any(PatternFindOptions.class), any(ObjectCollection[].class))).thenReturn(failureResult);
            ClickOptions clickOptions = new ClickOptions.Builder().build();
            
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(stateImage)
                .ifFound(clickOptions);
            
            chain.perform(action, defaultCollection);
            
            // Then
            verify(action).perform(any(PatternFindOptions.class), any(ObjectCollection[].class));
            verify(action, never()).perform(eq(clickOptions), any(ObjectCollection[].class));
        }
        
        @Test
        @DisplayName("Should execute ifNotFound when previous failed")
        void shouldExecuteIfNotFoundWhenPreviousFailed() {
            // Given
            when(action.perform(any(PatternFindOptions.class), any(ObjectCollection[].class))).thenReturn(failureResult);
            ClickOptions clickOptions = new ClickOptions.Builder().build();
            
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(stateImage)
                .ifNotFound(clickOptions);
            
            chain.perform(action, defaultCollection);
            
            // Then
            verify(action).perform(any(PatternFindOptions.class), any(ObjectCollection[].class));
            verify(action).perform(eq(clickOptions), any(ObjectCollection[].class));
        }
        
        @Test
        @DisplayName("Should skip ifNotFound when previous succeeded")
        void shouldSkipIfNotFoundWhenPreviousSucceeded() {
            // Given
            when(action.perform(any(PatternFindOptions.class), any(ObjectCollection[].class))).thenReturn(successResult);
            ClickOptions clickOptions = new ClickOptions.Builder().build();
            
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(stateImage)
                .ifNotFound(clickOptions);
            
            chain.perform(action, defaultCollection);
            
            // Then
            verify(action).perform(any(PatternFindOptions.class), any(ObjectCollection[].class));
            verify(action, never()).perform(eq(clickOptions), any(ObjectCollection[].class));
        }
        
        @Test
        @DisplayName("Should always execute with always()")
        void shouldAlwaysExecuteWithAlways() {
            // Given
            when(action.perform(any(PatternFindOptions.class), any(ObjectCollection[].class))).thenReturn(failureResult);
            ClickOptions clickOptions = new ClickOptions.Builder().build();
            
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(stateImage)
                .always(clickOptions);
            
            chain.perform(action, defaultCollection);
            
            // Then
            verify(action).perform(any(PatternFindOptions.class), any(ObjectCollection[].class));
            verify(action).perform(eq(clickOptions), any(ObjectCollection[].class));
        }
    }
    
    @Nested
    @DisplayName("Convenience Method Tests")
    class ConvenienceMethodTests {
        
        @Test
        @DisplayName("Should add click action")
        void shouldAddClickAction() {
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(stateImage)
                .click();
            
            chain.perform(action, defaultCollection);
            
            // Then
            verify(action).perform(any(ClickOptions.class), any(ObjectCollection[].class));
        }
        
        @Test
        @DisplayName("Should add conditional click with ifFoundClick()")
        void shouldAddConditionalClickWithIfFoundClick() {
            // Given
            when(action.perform(any(PatternFindOptions.class), any(ObjectCollection[].class))).thenReturn(successResult);
            
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(stateImage)
                .ifFoundClick();
            
            chain.perform(action, defaultCollection);
            
            // Then
            verify(action).perform(any(ClickOptions.class), any(ObjectCollection[].class));
        }
        
        @Test
        @DisplayName("Should type text")
        void shouldTypeText() {
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(stateImage)
                .type("test text");
            
            chain.perform(action, defaultCollection);
            
            // Then
            ArgumentCaptor<ActionConfig> configCaptor = ArgumentCaptor.forClass(ActionConfig.class);
            ArgumentCaptor<ObjectCollection> collectionCaptor = ArgumentCaptor.forClass(ObjectCollection.class);
            
            verify(action, atLeast(2)).perform(configCaptor.capture(), collectionCaptor.capture());
            
            // Verify TypeOptions was used
            boolean hasTypeOptions = configCaptor.getAllValues().stream()
                .anyMatch(config -> config instanceof TypeOptions);
            assertTrue(hasTypeOptions);
            
            // Verify text was included
            List<ObjectCollection> collections = collectionCaptor.getAllValues();
            boolean hasText = collections.stream()
                .filter(col -> col != null && col.getStateStrings() != null)
                .anyMatch(col -> !col.getStateStrings().isEmpty());
            assertTrue(hasText);
        }
        
        @Test
        @DisplayName("Should scroll down")
        void shouldScrollDown() {
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(stateImage)
                .scrollDown();
            
            chain.perform(action, defaultCollection);
            
            // Then
            ArgumentCaptor<ActionConfig> configCaptor = ArgumentCaptor.forClass(ActionConfig.class);
            verify(action, atLeast(2)).perform(configCaptor.capture(), any(ObjectCollection[].class));
            
            ScrollOptions scrollOptions = configCaptor.getAllValues().stream()
                .filter(config -> config instanceof ScrollOptions)
                .map(config -> (ScrollOptions) config)
                .findFirst()
                .orElse(null);
            
            assertNotNull(scrollOptions);
            assertEquals(ScrollOptions.Direction.DOWN, scrollOptions.getDirection());
        }
        
        @Test
        @DisplayName("Should scroll up")
        void shouldScrollUp() {
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(stateImage)
                .scrollUp();
            
            chain.perform(action, defaultCollection);
            
            // Then
            ArgumentCaptor<ActionConfig> configCaptor = ArgumentCaptor.forClass(ActionConfig.class);
            verify(action, atLeast(2)).perform(configCaptor.capture(), any(ObjectCollection[].class));
            
            ScrollOptions scrollOptions = configCaptor.getAllValues().stream()
                .filter(config -> config instanceof ScrollOptions)
                .map(config -> (ScrollOptions) config)
                .findFirst()
                .orElse(null);
            
            assertNotNull(scrollOptions);
            assertEquals(ScrollOptions.Direction.UP, scrollOptions.getDirection());
        }
        
        @Test
        @DisplayName("Should highlight element")
        void shouldHighlightElement() {
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(stateImage)
                .highlight();
            
            chain.perform(action, defaultCollection);
            
            // Then
            verify(action).perform(any(HighlightOptions.class), any(ObjectCollection[].class));
        }
        
        @Test
        @DisplayName("Should wait for vanish")
        void shouldWaitForVanish() {
            // Given
            StateImage vanishImage = mock(StateImage.class);
            
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(stateImage)
                .waitVanish(vanishImage);
            
            chain.perform(action, defaultCollection);
            
            // Then
            ArgumentCaptor<ActionConfig> configCaptor = ArgumentCaptor.forClass(ActionConfig.class);
            verify(action, atLeast(2)).perform(configCaptor.capture(), any(ObjectCollection[].class));
            
            boolean hasVanishOptions = configCaptor.getAllValues().stream()
                .anyMatch(config -> config instanceof VanishOptions);
            assertTrue(hasVanishOptions);
        }
    }
    
    @Nested
    @DisplayName("Keyboard Action Tests")
    class KeyboardActionTests {
        
        @Test
        @DisplayName("Should press Escape key")
        void shouldPressEscapeKey() {
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(stateImage)
                .pressEscape();
            
            chain.perform(action, defaultCollection);
            
            // Then
            verify(action, atLeast(1)).perform(any(KeyDownOptions.class), any(ObjectCollection[].class));
            verify(action, atLeast(1)).perform(any(KeyUpOptions.class), any(ObjectCollection[].class));
        }
        
        @Test
        @DisplayName("Should press Enter key")
        void shouldPressEnterKey() {
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(stateImage)
                .pressEnter();
            
            chain.perform(action, defaultCollection);
            
            // Then
            verify(action, atLeast(1)).perform(any(KeyDownOptions.class), any(ObjectCollection[].class));
            verify(action, atLeast(1)).perform(any(KeyUpOptions.class), any(ObjectCollection[].class));
        }
        
        @Test
        @DisplayName("Should press Tab key")
        void shouldPressTabKey() {
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(stateImage)
                .pressTab();
            
            chain.perform(action, defaultCollection);
            
            // Then
            verify(action, atLeast(1)).perform(any(KeyDownOptions.class), any(ObjectCollection[].class));
            verify(action, atLeast(1)).perform(any(KeyUpOptions.class), any(ObjectCollection[].class));
        }
        
        @Test
        @DisplayName("Should press specific key")
        void shouldPressSpecificKey() {
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(stateImage)
                .pressKey(KeyEvent.VK_SPACE);
            
            chain.perform(action, defaultCollection);
            
            // Then
            verify(action, atLeast(1)).perform(any(KeyDownOptions.class), any(ObjectCollection[].class));
            verify(action, atLeast(1)).perform(any(KeyUpOptions.class), any(ObjectCollection[].class));
        }
        
        @Test
        @DisplayName("Should press key combination")
        void shouldPressKeyCombination() {
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(stateImage)
                .pressKeyCombo(KeyEvent.VK_CONTROL, KeyEvent.VK_A);
            
            chain.perform(action, defaultCollection);
            
            // Then
            // Should have multiple key down/up operations for the combo
            verify(action, atLeast(2)).perform(any(KeyDownOptions.class), any(ObjectCollection[].class));
            verify(action, atLeast(2)).perform(any(KeyUpOptions.class), any(ObjectCollection[].class));
        }
        
        @Test
        @DisplayName("Should clear and type text")
        void shouldClearAndTypeText() {
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(stateImage)
                .clearAndType("new text");
            
            chain.perform(action, defaultCollection);
            
            // Then
            // Should have Ctrl+A, Delete, and then type
            verify(action, atLeast(2)).perform(any(KeyDownOptions.class), any(ObjectCollection[].class));
            verify(action, atLeast(2)).perform(any(KeyUpOptions.class), any(ObjectCollection[].class));
            verify(action).perform(any(TypeOptions.class), any(ObjectCollection[].class));
        }
    }
    
    @Nested
    @DisplayName("Custom Handler Tests")
    class CustomHandlerTests {
        
        @Test
        @DisplayName("Should execute custom handler on success")
        void shouldExecuteCustomHandlerOnSuccess() {
            // Given
            AtomicBoolean handlerExecuted = new AtomicBoolean(false);
            Consumer<ActionResult> handler = result -> handlerExecuted.set(true);
            
            when(action.perform(any(PatternFindOptions.class), any(ObjectCollection[].class))).thenReturn(successResult);
            
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(stateImage)
                .ifFoundDo(handler);
            
            chain.perform(action, defaultCollection);
            
            // Then
            assertTrue(handlerExecuted.get());
        }
        
        @Test
        @DisplayName("Should execute custom handler on failure")
        void shouldExecuteCustomHandlerOnFailure() {
            // Given
            AtomicBoolean handlerExecuted = new AtomicBoolean(false);
            Consumer<ActionResult> handler = result -> handlerExecuted.set(true);
            
            when(action.perform(any(PatternFindOptions.class), any(ObjectCollection[].class))).thenReturn(failureResult);
            
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(stateImage)
                .ifNotFoundDo(handler);
            
            chain.perform(action, defaultCollection);
            
            // Then
            assertTrue(handlerExecuted.get());
        }
        
        @Test
        @DisplayName("Should execute chain handler on success")
        void shouldExecuteChainHandlerOnSuccess() {
            // Given
            AtomicBoolean handlerExecuted = new AtomicBoolean(false);
            Consumer<ConditionalActionChain> chainHandler = c -> handlerExecuted.set(true);
            
            when(action.perform(any(PatternFindOptions.class), any(ObjectCollection[].class))).thenReturn(successResult);
            
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(stateImage)
                .ifFound(chainHandler);
            
            chain.perform(action, defaultCollection);
            
            // Then
            assertTrue(handlerExecuted.get());
        }
    }
    
    @Nested
    @DisplayName("Control Flow Tests")
    class ControlFlowTests {
        
        @Test
        @DisplayName("Should stop chain execution")
        void shouldStopChainExecution() {
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(stateImage)
                .stopChain()
                .click(); // This should not execute
            
            chain.perform(action, defaultCollection);
            
            // Then
            verify(action).perform(any(PatternFindOptions.class), any(ObjectCollection[].class));
            verify(action, never()).perform(any(ClickOptions.class), any(ObjectCollection[].class));
        }
        
        @Test
        @DisplayName("Should stop chain conditionally")
        void shouldStopChainConditionally() {
            // Given
            when(action.perform(any(PatternFindOptions.class), any(ObjectCollection[].class))).thenReturn(successResult);
            
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(stateImage)
                .stopIf(result -> result.isSuccess())
                .click(); // This should not execute
            
            chain.perform(action, defaultCollection);
            
            // Then
            verify(action).perform(any(PatternFindOptions.class), any(ObjectCollection[].class));
            verify(action, never()).perform(any(ClickOptions.class), any(ObjectCollection[].class));
        }
        
        @Test
        @DisplayName("Should throw error when configured")
        void shouldThrowErrorWhenConfigured() {
            // When/Then
            ConditionalActionChain chain = ConditionalActionChain.find(stateImage)
                .throwError("Test error");
            
            assertThrows(RuntimeException.class, 
                () -> chain.perform(action, defaultCollection));
        }
        
        @Test
        @DisplayName("Should throw error conditionally")
        void shouldThrowErrorConditionally() {
            // Given
            when(action.perform(any(PatternFindOptions.class), any(ObjectCollection[].class))).thenReturn(failureResult);
            
            // When/Then
            ConditionalActionChain chain = ConditionalActionChain.find(stateImage)
                .ifNotFoundDo(result -> { throw new RuntimeException("Element not found"); });
            
            assertThrows(RuntimeException.class, 
                () -> chain.perform(action, defaultCollection));
        }
    }
    
    @Nested
    @DisplayName("Complex Workflow Tests")
    class ComplexWorkflowTests {
        
        @Test
        @DisplayName("Should execute login workflow")
        void shouldExecuteLoginWorkflow() {
            // Given
            StateImage loginButton = mock(StateImage.class);
            StateImage usernameField = mock(StateImage.class);
            StateImage passwordField = mock(StateImage.class);
            StateImage submitButton = mock(StateImage.class);
            
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(loginButton)
                .ifFoundClick()
                .then(usernameField)
                .ifFoundClick()
                .type("testuser")
                .then(passwordField)
                .ifFoundClick()
                .type("password123")
                .then(submitButton)
                .ifFoundClick()
                .ifNotFoundLog("Login failed");
            
            chain.perform(action, defaultCollection);
            
            // Then
            verify(action, atLeast(4)).perform(any(PatternFindOptions.class), any(ObjectCollection[].class));
            verify(action, atLeast(3)).perform(any(ClickOptions.class), any(ObjectCollection[].class));
            verify(action, times(2)).perform(any(TypeOptions.class), any(ObjectCollection[].class));
        }
        
        @Test
        @DisplayName("Should handle retry pattern")
        void shouldHandleRetryPattern() {
            // Given
            AtomicInteger attemptCount = new AtomicInteger(0);
            
            when(action.perform(any(PatternFindOptions.class), any(ObjectCollection[].class)))
                .thenAnswer(inv -> {
                    int attempt = attemptCount.incrementAndGet();
                    return attempt <= 1 ? failureResult : successResult;
                });
            
            // When - simulate retry by adding multiple find attempts
            ConditionalActionChain chain = ConditionalActionChain.find(stateImage)
                .ifNotFound(new PatternFindOptions.Builder().build())  // Try again if first fails
                .ifFoundLog("Finally found after retries");
            
            chain.perform(action, defaultCollection);
            
            // Then - First find fails, ifNotFound executes another find which succeeds
            verify(action, times(2)).perform(any(PatternFindOptions.class), any(ObjectCollection[].class));
        }
        
        @Test
        @DisplayName("Should execute multi-branch workflow")
        void shouldExecuteMultiBranchWorkflow() {
            // Given
            StateImage primaryButton = mock(StateImage.class);
            StateImage fallbackButton = mock(StateImage.class);
            
            when(action.perform(any(PatternFindOptions.class), any(ObjectCollection[].class)))
                .thenReturn(failureResult);  // First find fails
            
            // When
            ConditionalActionChain chain = ConditionalActionChain.find(primaryButton)
                .ifFoundClick()
                .ifNotFoundLog("Primary button not found, trying fallback");
            // Note: API doesn't support chaining another find after ifNotFound directly
            
            chain.perform(action, defaultCollection);
            
            // Then - Only one find is executed since find fails and ifNotFoundLog doesn't add another find
            verify(action, times(1)).perform(any(PatternFindOptions.class), any(ObjectCollection[].class));
            verify(action, never()).perform(any(ClickOptions.class), any(ObjectCollection[].class));
        }
    }
}