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
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.runner.dsl.model.TaskSequence;
import io.github.jspinak.brobot.navigation.transition.TaskSequenceStateTransition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        
        // Type password
        if (!typeText(password)) {
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
     * Example 2: Login using TaskSequence (formerly ActionDefinition)
     */
    public TaskSequence createLoginSequence(String username, String password) {
        TaskSequence loginSequence = new TaskSequence();
        
        // Step 1: Find username field
        PatternFindOptions findUsername = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setSimilarity(0.9)
                .setCaptureImage(true)
                .build();
        loginSequence.addStep(findUsername, new ObjectCollection.Builder()
                .withImages(getStateImage("UsernameField"))
                .build());
        
        // Step 2: Click username field
        ClickOptions clickUsername = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .build();
        loginSequence.addStep(clickUsername, new ObjectCollection.Builder()
                .useMatchesFromPreviousAction()
                .build());
        
        // Step 3: Type username
        TypeOptions typeUsername = new TypeOptions.Builder()
                .setModifierDelay(0.05)
                .setClearFieldFirst(true)
                .build();
        loginSequence.addStep(typeUsername, new ObjectCollection.Builder()
                .withStrings(username)
                .build());
        
        // Step 4: Tab to password field
        TypeOptions tabToPassword = new TypeOptions.Builder()
                .setUseKeyboard(true)
                .build();
        loginSequence.addStep(tabToPassword, new ObjectCollection.Builder()
                .withStrings("\t") // Tab key
                .build());
        
        // Step 5: Type password
        TypeOptions typePassword = new TypeOptions.Builder()
                .setModifierDelay(0.05)
                .setHideText(true) // Don't log password
                .build();
        loginSequence.addStep(typePassword, new ObjectCollection.Builder()
                .withStrings(password)
                .build());
        
        // Step 6: Press Enter to submit
        TypeOptions pressEnter = new TypeOptions.Builder()
                .setUseKeyboard(true)
                .build();
        loginSequence.addStep(pressEnter, new ObjectCollection.Builder()
                .withStrings("\n") // Enter key
                .build());
        
        return loginSequence;
    }
    
    /**
     * Example 3: Create state transition for automated login
     */
    public StateTransitions createLoginTransition() {
        TaskSequence loginSequence = createLoginSequence("demo_user", "demo_pass");
        
        TaskSequenceStateTransition loginTransition = new TaskSequenceStateTransition();
        loginTransition.setActionDefinition(loginSequence);
        loginTransition.getActivate().add(2L); // Dashboard state ID
        
        return new StateTransitions.Builder("LOGIN")
                .addTransition(loginTransition)
                .build();
    }
    
    /**
     * Example 4: Advanced login with retry logic
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
            
            // Wait before retry
            sleep(2000);
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
        
        ActionInterface findAction = actionService.getAction(findOptions);
        findAction.perform(findResult, objects);
        
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
                .withMatches(findResult.getMatchList())
                .build();
        
        ActionInterface clickAction = actionService.getAction(clickOptions);
        clickAction.perform(clickResult, clickObjects);
        
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
        
        ActionInterface clickAction = actionService.getAction(clickOptions);
        clickAction.perform(result, objects);
        
        return result.isSuccess();
    }
    
    private boolean typeText(String text) {
        TypeOptions typeOptions = new TypeOptions.Builder()
                .setModifierDelay(0.05)
                .setPauseAfterEnd(0.3)
                .build();
        
        ActionResult result = new ActionResult();
        result.setActionConfig(typeOptions);
        
        ObjectCollection objects = new ObjectCollection.Builder()
                .withStrings(text)
                .build();
        
        ActionInterface typeAction = actionService.getAction(typeOptions);
        typeAction.perform(result, objects);
        
        return result.isSuccess();
    }
    
    private boolean findElement(String imageName, PatternFindOptions options) {
        ActionResult result = new ActionResult();
        result.setActionConfig(options);
        
        ObjectCollection objects = new ObjectCollection.Builder()
                .withImages(getStateImage(imageName))
                .build();
        
        ActionInterface findAction = actionService.getAction(options);
        findAction.perform(result, objects);
        
        return result.isSuccess();
    }
    
    private boolean verifyDashboard() {
        // Use ALL strategy to ensure multiple dashboard elements are present
        PatternFindOptions verifyOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setSimilarity(0.8)
                .setMaxMatchesToActOn(2)
                .build();
        
        ActionResult result = new ActionResult();
        result.setActionConfig(verifyOptions);
        
        ObjectCollection objects = new ObjectCollection.Builder()
                .withAllStateImages(dashboardState)
                .build();
        
        ActionInterface findAction = actionService.getAction(verifyOptions);
        findAction.perform(result, objects);
        
        // Should find at least 2 dashboard elements
        return result.isSuccess() && result.getMatchList().size() >= 2;
    }
    
    private StateImage getStateImage(String name) {
        return new StateImage.Builder().setName(name).build();
    }
    
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Main method to demonstrate usage
     */
    public static void main(String[] args) {
        System.out.println("Login Automation Example - New ActionConfig API");
        System.out.println("===============================================");
        System.out.println();
        System.out.println("This example demonstrates:");
        System.out.println("1. Simple login with direct actions");
        System.out.println("2. Login using TaskSequence");
        System.out.println("3. State transitions for automated login");
        System.out.println("4. Advanced login with retry logic");
        System.out.println();
        System.out.println("Key features of the new API:");
        System.out.println("- Type-safe configuration builders");
        System.out.println("- Action-specific options");
        System.out.println("- Clear separation of concerns");
        System.out.println("- Better error handling");
    }
}