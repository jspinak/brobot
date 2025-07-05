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
     * Example 1: Simple login with direct actions
     */
    public boolean performSimpleLogin(String username, String password) {
        // Find and click username field
        if (!findAndClickElement("UsernameField")) {
            System.out.println("Failed to find username field");
            return false;
        }
        
        // Type username
        if (!typeText(username)) {
            System.out.println("Failed to type username");
            return false;
        }
        
        // Find and click password field  
        if (!findAndClickElement("PasswordField")) {
            System.out.println("Failed to find password field");
            return false;
        }
        
        // Type password
        if (!typeText(password)) {
            System.out.println("Failed to type password");
            return false;
        }
        
        // Click login button
        if (!clickElement("LoginButton")) {
            System.out.println("Failed to click login button");
            return false;
        }
        
        // Verify we reached the dashboard
        return verifyDashboard();
    }
    
    /**
     * Example 2: Login with retry logic
     */
    public boolean performLoginWithRetry(String username, String password, int maxRetries) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            System.out.println("Login attempt " + attempt + " of " + maxRetries);
            
            // Try quick find first
            PatternFindOptions quickFind = PatternFindOptions.forQuickSearch();
            if (findElement("LoginButton", quickFind)) {
                // We're on login page, proceed with login
                if (performSimpleLogin(username, password)) {
                    return true;
                }
            }
            
            // Check if we're already logged in
            if (verifyDashboard()) {
                System.out.println("Already logged in!");
                return true;
            }
            
            // Wait before retry using action options
            if (attempt < maxRetries) {
                PatternFindOptions waitOptions = new PatternFindOptions.Builder()
                    .setPauseAfterEnd(2.0)  // 2 second pause
                    .setMaxMatchesToActOn(0)     // Don't actually search
                    .build();
                findElement("LoginButton", waitOptions);  // Use existing element just for the pause
            }
        }
        
        return false;
    }
    
    // Helper methods
    
    private boolean findAndClickElement(String imageName) {
        // Use precise find for important elements
        PatternFindOptions findOptions = PatternFindOptions.forPreciseSearch();
        
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
        
        // Click the found element
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .setPauseAfterEnd(0.5)
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
        TypeOptions typeOptions = new TypeOptions.Builder()
                .setTypeDelay(0.05)
                .setPauseAfterEnd(0.3)
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
        System.out.println("Login Automation Example");
        System.out.println("=======================");
        System.out.println();
        System.out.println("This example demonstrates:");
        System.out.println("1. Finding and clicking GUI elements");
        System.out.println("2. Typing text with the new TypeOptions API");
        System.out.println("3. Implementing retry logic with proper pauses");
        System.out.println("4. State verification using multiple image matches");
        System.out.println();
        System.out.println("Note: This example requires:");
        System.out.println("- Spring context to be initialized");
        System.out.println("- Image files for the StateImages (UsernameField.png, etc.)");
        System.out.println("- A running application to automate");
    }
}