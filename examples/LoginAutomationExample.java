package io.github.jspinak.brobot.examples;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Example: Login Automation with New ActionConfig API
 * 
 * This example demonstrates how to create a login automation
 * using the new type-safe ActionConfig API introduced in Brobot 2.0.
 */
@Component
public class LoginAutomationExample {
    
    @Autowired
    private ActionService actionService;
    
    // State definitions
    private final State loginState = new State.Builder("LOGIN")
            .withImages(
                new StateImage.Builder().setName("UsernameField").build(),
                new StateImage.Builder().setName("PasswordField").build(),
                new StateImage.Builder().setName("LoginButton").build()
            )
            .build();
    
    private final State dashboardState = new State.Builder("DASHBOARD")
            .withImages(
                new StateImage.Builder().setName("DashboardLogo").build(),
                new StateImage.Builder().setName("WelcomeMessage").build()
            )
            .build();
    
    /**
     * Example 1: Simple login with automatic logging
     * 
     * Demonstrates how automatic logging reduces boilerplate code
     * while providing better observability.
     */
    public boolean performSimpleLogin(String username, String password) {
        // All logging is now handled automatically by the action configurations
        
        // Find and click username field
        if (!findAndClickElement("UsernameField")) {
            return false;
        }
        
        // Type username
        if (!typeText(username)) {
            return false;
        }
        
        // Find and click password field  
        if (!findAndClickElement("PasswordField")) {
            return false;
        }
        
        // Type password (masked for security)
        if (!typeText("***")) {  // In real usage, still pass the actual password
            return false;
        }
        
        // Click login button
        if (!clickElement("LoginButton")) {
            return false;
        }
        
        // Verify we reached the dashboard
        return verifyDashboard();
    }
    
    /**
     * Example 2: Login with retry logic and automatic logging
     * 
     * Shows how logging helps track retry attempts and understand
     * what's happening during complex flows.
     */
    public boolean performLoginWithRetry(String username, String password, int maxRetries) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            // Try quick find first with attempt logging
            PatternFindOptions quickFind = PatternFindOptions.forQuickSearch()
                    .toBuilder()
                    .withBeforeActionLog("Login attempt " + attempt + " of " + maxRetries + " - checking for login page...")
                    .withSuccessLog("Login page detected")
                    .withFailureLog("Login page not found")
                    .build();
                    
            if (findElement("LoginButton", quickFind)) {
                // We're on login page, proceed with login
                if (performSimpleLogin(username, password)) {
                    return true;
                }
            }
            
            // Check if we're already logged in
            if (verifyDashboard()) {
                return true;
            }
            
            // Wait before retry with logging
            if (attempt < maxRetries) {
                PatternFindOptions waitOptions = new PatternFindOptions.Builder()
                    .setPauseAfterEnd(2.0)  // 2 second pause
                    .setMaxMatchesToActOn(0)     // Don't actually search
                    .withBeforeActionLog("Waiting 2 seconds before retry...")
                    .withAfterActionLog("Ready for attempt " + (attempt + 1))
                    .build();
                findElement("LoginButton", waitOptions);
            }
        }
        
        return false;
    }
    
    // Helper methods
    
    private boolean findAndClickElement(String imageName) {
        // Use precise find with automatic logging
        PatternFindOptions findOptions = PatternFindOptions.forPreciseSearch()
                .toBuilder()
                .withBeforeActionLog("Searching for " + imageName + "...")
                .withSuccessLog("Found " + imageName + " at location {target}")
                .withFailureLog("Failed to find " + imageName + " - element may not be visible")
                .withAfterActionLog("Search for " + imageName + " completed in {duration}ms")
                .build();
        
        ActionResult findResult = new ActionResult();
        findResult.setActionConfig(findOptions);
        
        ObjectCollection objects = new ObjectCollection.Builder()
                .withImages(getStateImage(imageName))
                .build();
        
        Optional<ActionInterface> findActionOpt = actionService.getAction(findOptions);
        if (findActionOpt.isEmpty()) {
            return false;
        }
        findActionOpt.get().perform(findResult, objects);
        
        if (!findResult.isSuccess()) {
            return false;
        }
        
        // Click the found element with logging
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .setPauseAfterEnd(0.5)
                .withBeforeActionLog("Clicking " + imageName + "...")
                .withSuccessLog("Successfully clicked " + imageName)
                .withFailureLog("Failed to click " + imageName + " - check if element is clickable")
                .build();
        
        ActionResult clickResult = new ActionResult();
        clickResult.setActionConfig(clickOptions);
        
        ObjectCollection clickObjects = new ObjectCollection.Builder()
                .withMatches(findResult)
                .build();
        
        Optional<ActionInterface> clickActionOpt = actionService.getAction(clickOptions);
        if (clickActionOpt.isEmpty()) {
            return false;
        }
        clickActionOpt.get().perform(clickResult, clickObjects);
        
        return clickResult.isSuccess();
    }
    
    private boolean clickElement(String imageName) {
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .setPauseAfterEnd(1.0) // Wait for page response
                .withBeforeActionLog("Attempting to click " + imageName + "...")
                .withSuccessLog(imageName + " clicked successfully")
                .withFailureLog("Click on " + imageName + " failed")
                .withAfterActionLog("Click operation completed")
                .build();
        
        ActionResult result = new ActionResult();
        result.setActionConfig(clickOptions);
        
        ObjectCollection objects = new ObjectCollection.Builder()
                .withImages(getStateImage(imageName))
                .build();
        
        Optional<ActionInterface> clickActionOpt = actionService.getAction(clickOptions);
        if (clickActionOpt.isEmpty()) {
            return false;
        }
        clickActionOpt.get().perform(result, objects);
        
        return result.isSuccess();
    }
    
    private boolean typeText(String text) {
        // Mask password in logs for security
        String displayText = text.equals("***") ? "password" : text;
        
        TypeOptions typeOptions = new TypeOptions.Builder()
                .setTypeDelay(0.05)
                .setPauseAfterEnd(0.3)
                .withBeforeActionLog("Typing " + displayText + "...")
                .withSuccessLog("Successfully typed " + displayText)
                .withFailureLog("Failed to type " + displayText)
                .build();
        
        ActionResult result = new ActionResult();
        result.setActionConfig(typeOptions);
        
        ObjectCollection objects = new ObjectCollection.Builder()
                .withStrings(text)
                .build();
        
        Optional<ActionInterface> typeActionOpt = actionService.getAction(typeOptions);
        if (typeActionOpt.isPresent()) {
            typeActionOpt.get().perform(result, objects);
        }
        
        return result.isSuccess();
    }
    
    private boolean findElement(String imageName, PatternFindOptions options) {
        ActionResult result = new ActionResult();
        result.setActionConfig(options);
        
        ObjectCollection objects = new ObjectCollection.Builder()
                .withImages(getStateImage(imageName))
                .build();
        
        Optional<ActionInterface> findActionOpt = actionService.getAction(options);
        if (findActionOpt.isPresent()) {
            findActionOpt.get().perform(result, objects);
        }
        
        return result.isSuccess();
    }
    
    private boolean verifyDashboard() {
        // Look for multiple dashboard elements to verify state
        PatternFindOptions verifyOptions = new PatternFindOptions.Builder()
                .setMaxMatchesToActOn(3)
                .setSimilarity(0.8)
                .withBeforeActionLog("Verifying dashboard state...")
                .withLogging(logging -> logging
                        .successMessage("Dashboard verified - found {matchCount} elements")
                        .failureMessage("Dashboard not detected - expected at least 2 elements")
                        .logOnSuccess(true)
                        .logOnFailure(true))
                .build();
        
        ActionResult result = new ActionResult();
        result.setActionConfig(verifyOptions);
        
        ObjectCollection objects = new ObjectCollection.Builder()
                .withAllStateImages(dashboardState)
                .build();
        
        Optional<ActionInterface> findActionOpt = actionService.getAction(verifyOptions);
        if (findActionOpt.isEmpty()) {
            return false;
        }
        findActionOpt.get().perform(result, objects);
        
        // Should find at least 2 dashboard elements
        return result.isSuccess() && result.getMatchList().size() >= 2;
    }
    
    private StateImage getStateImage(String name) {
        return new StateImage.Builder().setName(name).build();
    }
    
    /**
     * Main method to demonstrate usage
     */
    public static void main(String[] args) {
        // This would typically be run within a Spring context
        System.out.println("Login Automation Example with Automatic Logging");
        System.out.println("==============================================");
        System.out.println();
        System.out.println("This example demonstrates:");
        System.out.println("1. Finding and clicking GUI elements with automatic logging");
        System.out.println("2. Before/after action logging for better observability");
        System.out.println("3. Success/failure logging with contextual messages");
        System.out.println("4. Advanced logging configuration with withLogging()");
        System.out.println("5. Security considerations (password masking in logs)");
        System.out.println();
        System.out.println("Key benefits of automatic logging:");
        System.out.println("- No more manual System.out.println() statements");
        System.out.println("- Consistent log format across all actions");
        System.out.println("- Dynamic placeholders ({target}, {duration}, {matchCount})");
        System.out.println("- Integration with Brobot's unified logging system");
        System.out.println();
        System.out.println("Note: This example requires:");
        System.out.println("- Spring context to be initialized");
        System.out.println("- Image files for the StateImages (UsernameField.png, etc.)");
        System.out.println("- A running application to automate");
    }
}