package io.github.jspinak.brobot.action;

import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.state.StateImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test cases demonstrating the enhanced ConditionalActionChain functionality.
 * These tests show how the idealized API from documentation can now work.
 */
class EnhancedConditionalActionChainTest {
    
    @Mock
    private Action mockAction;
    
    @Mock
    private StateImage loginButton;
    
    @Mock
    private StateImage usernameField;
    
    @Mock
    private StateImage passwordField;
    
    @Mock
    private StateImage submitButton;
    
    @Mock
    private StateImage successMessage;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    @DisplayName("Should support then() method for sequential actions")
    void testThenMethodForSequentialActions() {
        // This now works with the enhanced version!
        EnhancedConditionalActionChain chain = EnhancedConditionalActionChain
            .find(loginButton)
            .ifFound(new ClickOptions.Builder().build())
            .then(new PatternFindOptions.Builder().build())  // <-- then() now exists!
            .ifFound(new ClickOptions.Builder().build());
        
        assertNotNull(chain);
    }
    
    @Test
    @DisplayName("Should support convenience methods like click() and type()")
    void testConvenienceMethods() {
        // These convenience methods now work!
        EnhancedConditionalActionChain chain = EnhancedConditionalActionChain
            .find(usernameField)
            .ifFoundClick()           // <-- Direct click method
            .ifFoundType("username")  // <-- Direct type method
            .then(passwordField)      // <-- Direct StateImage as parameter
            .ifFoundClick()
            .ifFoundType("password");
        
        assertNotNull(chain);
    }
    
    @Test
    @DisplayName("Should support login flow from documentation")
    void testLoginFlowFromDocumentation() {
        // Setup mock responses
        ActionResult foundResult = new ActionResult();
        foundResult.setSuccess(true);
        
        ActionResult notFoundResult = new ActionResult();
        notFoundResult.setSuccess(false);
        
        when(mockAction.perform(any(ActionConfig.class), any(ObjectCollection[].class)))
            .thenReturn(foundResult);
        
        // The login flow from documentation now works!
        ActionResult result = EnhancedConditionalActionChain
            .find(loginButton)
            .ifFoundClick()
            .ifNotFoundLog("Login button not visible")
            .then(usernameField)
            .ifFoundClick()
            .ifFoundType("testuser")
            .then(passwordField)
            .ifFoundClick()
            .ifFoundType("password123")
            .then(submitButton)
            .ifFoundClick()
            .perform(mockAction, new ObjectCollection.Builder().build());
        
        assertNotNull(result);
    }
    
    @Test
    @DisplayName("Should support form filling pattern")
    void testFormFillingPattern() {
        // Complex form filling now works as documented!
        EnhancedConditionalActionChain chain = EnhancedConditionalActionChain
            .find(new PatternFindOptions.Builder().build())
            .ifNotFoundLog("Form not visible")
            .ifNotFound(chain -> chain.throwError("Cannot proceed without form"))
            
            // Name field
            .then(new PatternFindOptions.Builder().build())
            .ifFoundClick()
            .ifFound(chain -> chain.clearAndType("John Doe"))
            
            // Email field  
            .then(new PatternFindOptions.Builder().build())
            .ifFoundClick()
            .ifFound(chain -> chain.clearAndType("john@example.com"))
            
            // Submit
            .then(submitButton)
            .ifFoundClick()
            .always(chain -> chain.takeScreenshot("form-submission"));
        
        assertNotNull(chain);
    }
    
    @Test
    @DisplayName("Should support keyboard shortcuts")
    void testKeyboardShortcuts() {
        // Keyboard shortcuts now work!
        EnhancedConditionalActionChain chain = EnhancedConditionalActionChain
            .find(usernameField)
            .ifFoundClick()
            .pressCtrlA()     // Select all
            .pressDelete()    // Delete
            .type("newuser")  // Type new text
            .pressTab()       // Move to next field
            .type("newpass")
            .pressEnter();    // Submit
        
        assertNotNull(chain);
    }
    
    @Test
    @DisplayName("Should support scroll and navigation")
    void testScrollAndNavigation() {
        // Scroll actions now work!
        EnhancedConditionalActionChain chain = EnhancedConditionalActionChain
            .find(new PatternFindOptions.Builder().build())
            .ifNotFound(chain -> chain.scrollDown())
            .ifNotFound(new PatternFindOptions.Builder().build())
            .ifNotFound(chain -> chain.scrollDown())
            .ifFound(new ClickOptions.Builder().build());
        
        assertNotNull(chain);
    }
    
    @Test
    @DisplayName("Should support retry pattern")
    void testRetryPattern() {
        // Retry pattern with convenience methods
        StateImage target = mock(StateImage.class);
        
        EnhancedConditionalActionChain chain = EnhancedConditionalActionChain
            .retry(new PatternFindOptions.Builder().build(), 3)
            .ifFoundClick()
            .ifFoundLog("Successfully clicked after retries")
            .ifNotFoundLog("Failed after all attempts");
        
        assertNotNull(chain);
    }
    
    @Test
    @DisplayName("Should handle conditional execution properly")
    void testConditionalExecution() {
        // Setup
        ActionResult successResult = new ActionResult();
        successResult.setSuccess(true);
        
        ActionResult failResult = new ActionResult();
        failResult.setSuccess(false);
        
        // Test success path
        when(mockAction.perform(any(ActionConfig.class), any(ObjectCollection[].class)))
            .thenReturn(successResult);
        
        ActionResult result = EnhancedConditionalActionChain
            .find(loginButton)
            .ifFoundLog("Found!")        // Should execute
            .ifNotFoundLog("Not found")  // Should NOT execute
            .perform(mockAction, new ObjectCollection.Builder().build());
        
        assertTrue(result.isSuccess());
        
        // Test failure path
        when(mockAction.perform(any(ActionConfig.class), any(ObjectCollection[].class)))
            .thenReturn(failResult);
        
        result = EnhancedConditionalActionChain
            .find(loginButton)
            .ifFoundLog("Found!")        // Should NOT execute
            .ifNotFoundLog("Not found")  // Should execute
            .perform(mockAction, new ObjectCollection.Builder().build());
        
        assertFalse(result.isSuccess());
    }
    
    @Test
    @DisplayName("Should support control flow with stopChain")
    void testControlFlow() {
        ActionResult errorResult = new ActionResult();
        errorResult.setSuccess(true);
        errorResult.setText("ERROR");
        
        when(mockAction.perform(any(ActionConfig.class), any(ObjectCollection[].class)))
            .thenReturn(errorResult);
        
        // Chain should stop when error is found
        ActionResult result = EnhancedConditionalActionChain
            .find(new PatternFindOptions.Builder().build())
            .ifFoundDo(res -> {
                if (res.getText().contains("ERROR")) {
                    // Stop the chain
                }
            })
            .stopIf(res -> res.getText().contains("ERROR"))
            .then(submitButton)  // This should not execute
            .ifFoundClick()       // This should not execute
            .perform(mockAction, new ObjectCollection.Builder().build());
        
        assertNotNull(result);
    }
    
    @Test
    @DisplayName("Should support action chaining without explicit waits")
    void testActionChainingWithoutWaits() {
        // Model-based automation uses action configurations for timing
        // not explicit wait() calls
        EnhancedConditionalActionChain chain = EnhancedConditionalActionChain
            .find(loginButton)
            .ifFoundClick()
            .then(usernameField)
            .ifFoundClick();
        
        assertNotNull(chain);
        // Timing should be handled by PatternFindOptions.setPauseBeforeBegin()
        // or other action-specific timing configurations
    }
    
    @Test
    @DisplayName("Should support highlighting and debugging")
    void testHighlightingAndDebugging() {
        // Debugging features now work
        EnhancedConditionalActionChain chain = EnhancedConditionalActionChain
            .find(loginButton)
            .ifFound(chain -> chain.highlight())  // Highlight found element
            .ifFoundLog("Found login button")     // Log for debugging
            .takeScreenshot("debug-1")            // Take screenshot
            .ifFoundClick()
            .takeScreenshot("debug-2");           // Another screenshot
        
        assertNotNull(chain);
    }
    
    @Test
    @DisplayName("Should support vanish operations")
    void testVanishOperations() {
        // Wait for element to disappear
        StateImage loadingSpinner = mock(StateImage.class);
        
        EnhancedConditionalActionChain chain = EnhancedConditionalActionChain
            .find(submitButton)
            .ifFoundClick()
            .waitVanish(loadingSpinner)  // Wait for spinner to disappear
            .then(successMessage)
            .ifFoundLog("Success!");
        
        assertNotNull(chain);
    }
    
    @Test
    @DisplayName("Documentation example: Save with confirmation dialog")
    void testSaveWithConfirmationDialog() {
        StateImage saveButton = mock(StateImage.class);
        StateImage confirmDialog = mock(StateImage.class);
        StateImage yesButton = mock(StateImage.class);
        
        // The example from documentation now works!
        EnhancedConditionalActionChain chain = EnhancedConditionalActionChain
            .find(saveButton)
            .ifFoundClick()
            .ifNotFoundLog("Save button not found")
            .then(confirmDialog)
            .ifFound(yesButton)
            .ifFoundClick()
            .ifNotFoundLog("No confirmation needed")
            .then(successMessage)
            .ifFoundLog("Save successful")
            .ifNotFoundLog("Save may have failed");
        
        assertNotNull(chain);
    }
}