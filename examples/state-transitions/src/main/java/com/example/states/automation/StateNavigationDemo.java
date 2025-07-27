package com.example.states.automation;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Demonstrates various state navigation patterns with automatic logging.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StateNavigationDemo {
    
    private final Action action;
    private final StateNavigator stateNavigator;
    private final StateMemory stateMemory;
    private final BrobotLogger brobotLogger;
    
    /**
     * Example 1: Simple direct transition from Login to Dashboard
     */
    public void demonstrateDirectTransition() {
        log.info("\n--- Example 1: Direct State Transition ---");
        
        // Define states
        State loginState = createLoginState();
        State dashboardState = createDashboardState();
        
        // Perform login with automatic logging
        boolean success = performLogin("demo@example.com", "password123");
        
        if (success) {
            // Verify we reached the dashboard
            verifyState(dashboardState, "Dashboard");
        }
    }
    
    /**
     * Example 2: Multi-step navigation through multiple states
     */
    public void demonstrateMultiStepNavigation() {
        log.info("\n--- Example 2: Multi-Step Navigation ---");
        log.info("Navigation path: Dashboard → Settings → Profile");
        
        // Navigate to Settings with logging
        boolean settingsReached = navigateToSettings();
        
        if (settingsReached) {
            // Navigate to Profile from Settings
            boolean profileReached = navigateToProfile();
            
            if (profileReached) {
                log.info("✓ Multi-step navigation completed successfully");
            }
        }
    }
    
    /**
     * Example 3: Conditional navigation based on current state
     */
    public void demonstrateConditionalNavigation() {
        log.info("\n--- Example 3: Conditional Navigation ---");
        
        // Check current state and navigate accordingly
        State currentState = getCurrentState();
        
        if (currentState.getName().equals("Login")) {
            log.info("Currently on Login page - performing login first");
            performLogin("demo@example.com", "password123");
        }
        
        // Now navigate to target state
        PatternFindOptions checkDashboard = new PatternFindOptions.Builder()
            .withBeforeActionLog("Checking if we're on Dashboard...")
            .withSuccessLog("Already on Dashboard")
            .withFailureLog("Not on Dashboard - navigation needed")
            .build();
            
        StateImage dashboardIndicator = new StateImage.Builder()
            .setName("dashboard-indicator")
            .addPattern("images/dashboard-logo.png")
            .build();
            
        ActionResult dashboardCheck = action.perform(checkDashboard, dashboardIndicator);
        
        if (!dashboardCheck.isSuccess()) {
            navigateToDashboard();
        }
    }
    
    /**
     * Example 4: Error recovery during navigation
     */
    public void demonstrateErrorRecovery() {
        log.info("\n--- Example 4: Navigation with Error Recovery ---");
        
        // Try primary navigation path
        boolean success = navigateWithRetry("Profile", 3);
        
        if (success) {
            log.info("✓ Navigation succeeded with automatic retry");
        } else {
            log.error("✗ Navigation failed after all retries");
        }
    }
    
    /**
     * Example 5: Complex workflow with multiple transitions
     */
    public void demonstrateComplexWorkflow() {
        log.info("\n--- Example 5: Complex Workflow ---");
        log.info("Workflow: Login → Dashboard → Settings → Update → Save → Logout");
        
        // Execute workflow with comprehensive logging
        executeWorkflow();
    }
    
    // Helper methods
    
    private boolean performLogin(String username, String password) {
        // Find username field with logging
        PatternFindOptions findUsername = new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for username field...")
            .withSuccessLog("Found username field")
            .withFailureLog("Username field not found - check if on login page")
            .build();
            
        StateImage usernameField = new StateImage.Builder()
            .setName("username-field")
            .addPattern("images/username-field.png")
            .build();
            
        ActionResult usernameResult = action.perform(findUsername, usernameField);
        if (!usernameResult.isSuccess()) return false;
        
        // Click and type username
        ClickOptions clickUsername = new ClickOptions.Builder()
            .withBeforeActionLog("Clicking username field...")
            .withSuccessLog("Username field focused")
            .build();
            
        action.perform(clickUsername, usernameResult.getMatchList());
        
        TypeOptions typeUsername = new TypeOptions.Builder()
            .withBeforeActionLog("Typing username...")
            .withSuccessLog("Username entered")
            .build();
            
        action.perform(typeUsername, username);
        
        // Find password field
        PatternFindOptions findPassword = new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for password field...")
            .withSuccessLog("Found password field")
            .withFailureLog("Password field not found")
            .build();
            
        StateImage passwordField = new StateImage.Builder()
            .setName("password-field")
            .addPattern("images/password-field.png")
            .build();
            
        ActionResult passwordResult = action.perform(findPassword, passwordField);
        if (!passwordResult.isSuccess()) return false;
        
        // Click and type password
        ClickOptions clickPassword = new ClickOptions.Builder()
            .withBeforeActionLog("Clicking password field...")
            .withSuccessLog("Password field focused")
            .build();
            
        action.perform(clickPassword, passwordResult.getMatchList());
        
        TypeOptions typePassword = new TypeOptions.Builder()
            .withBeforeActionLog("Typing password...")
            .withSuccessLog("Password entered")
            .build();
            
        action.perform(typePassword, password);
        
        // Click login button
        PatternFindOptions findLogin = new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for login button...")
            .withSuccessLog("Found login button")
            .withFailureLog("Login button not found")
            .build();
            
        StateImage loginButton = new StateImage.Builder()
            .setName("login-button")
            .addPattern("images/login-button.png")
            .build();
            
        ActionResult loginResult = action.perform(findLogin, loginButton);
        if (!loginResult.isSuccess()) return false;
        
        ClickOptions clickLogin = new ClickOptions.Builder()
            .withBeforeActionLog("Clicking login button...")
            .withSuccessLog("Login submitted")
            .withAfterActionLog("Login process completed in {duration}ms")
            .setPauseAfterEnd(2.0) // Wait for login processing
            .build();
            
        return action.perform(clickLogin, loginResult.getMatchList()).isSuccess();
    }
    
    private boolean navigateToSettings() {
        // Log the transition intent
        brobotLogger.log()
            .type(io.github.jspinak.brobot.logging.unified.LogEvent.Type.TRANSITION)
            .observation("Navigating from Dashboard to Settings...")
            .log();
            
        PatternFindOptions findSettings = new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for Settings menu item...")
            .withSuccessLog("Found Settings menu item")
            .withFailureLog("Settings menu item not found - check if menu is visible")
            .build();
            
        StateImage settingsMenuItem = new StateImage.Builder()
            .setName("settings-menu")
            .addPattern("images/settings-menu-item.png")
            .build();
            
        ActionResult settingsResult = action.perform(findSettings, settingsMenuItem);
        if (!settingsResult.isSuccess()) return false;
        
        ClickOptions clickSettings = new ClickOptions.Builder()
            .withBeforeActionLog("Clicking Settings menu item...")
            .withSuccessLog("Settings clicked")
            .setPauseAfterEnd(1.0)
            .build();
            
        boolean clicked = action.perform(clickSettings, settingsResult.getMatchList()).isSuccess();
        
        if (clicked) {
            // Verify we reached settings
            State settingsState = createSettingsState();
            return verifyState(settingsState, "Settings");
        }
        
        return false;
    }
    
    private boolean navigateToProfile() {
        brobotLogger.log()
            .type(io.github.jspinak.brobot.logging.unified.LogEvent.Type.TRANSITION)
            .observation("Navigating from Settings to Profile...")
            .log();
            
        PatternFindOptions findProfile = new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for Profile tab...")
            .withSuccessLog("Found Profile tab")
            .withFailureLog("Profile tab not found")
            .build();
            
        StateImage profileTab = new StateImage.Builder()
            .setName("profile-tab")
            .addPattern("images/profile-tab.png")
            .build();
            
        ActionResult profileResult = action.perform(findProfile, profileTab);
        if (!profileResult.isSuccess()) return false;
        
        ClickOptions clickProfile = new ClickOptions.Builder()
            .withBeforeActionLog("Clicking Profile tab...")
            .withSuccessLog("Profile tab clicked")
            .withAfterActionLog("Navigation to Profile completed in {duration}ms")
            .build();
            
        return action.perform(clickProfile, profileResult.getMatchList()).isSuccess();
    }
    
    private boolean navigateToDashboard() {
        PatternFindOptions findDashboard = new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for Dashboard link...")
            .withSuccessLog("Found Dashboard link")
            .withFailureLog("Dashboard link not found")
            .build();
            
        StateImage dashboardLink = new StateImage.Builder()
            .setName("dashboard-link")
            .addPattern("images/dashboard-link.png")
            .build();
            
        ActionResult result = action.perform(findDashboard, dashboardLink);
        if (!result.isSuccess()) return false;
        
        ClickOptions clickDashboard = new ClickOptions.Builder()
            .withBeforeActionLog("Clicking Dashboard link...")
            .withSuccessLog("Navigated to Dashboard")
            .build();
            
        return action.perform(clickDashboard, result.getMatchList()).isSuccess();
    }
    
    private boolean navigateWithRetry(String targetState, int maxRetries) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            PatternFindOptions findTarget = new PatternFindOptions.Builder()
                .withBeforeActionLog("Attempt " + attempt + "/" + maxRetries + 
                                   ": Looking for " + targetState + " link...")
                .withSuccessLog("Found " + targetState + " link")
                .withFailureLog(targetState + " link not found")
                .build();
                
            StateImage targetLink = new StateImage.Builder()
                .setName(targetState.toLowerCase() + "-link")
                .addPattern("images/" + targetState.toLowerCase() + "-link.png")
                .build();
                
            ActionResult result = action.perform(findTarget, targetLink);
            
            if (result.isSuccess()) {
                ClickOptions clickTarget = new ClickOptions.Builder()
                    .withBeforeActionLog("Clicking " + targetState + " link...")
                    .withSuccessLog("Successfully navigated to " + targetState)
                    .build();
                    
                if (action.perform(clickTarget, result.getMatchList()).isSuccess()) {
                    return true;
                }
            }
            
            if (attempt < maxRetries) {
                // Try alternate approach
                log.info("Trying alternate navigation approach...");
                
                // Check if menu needs to be opened
                PatternFindOptions findMenu = new PatternFindOptions.Builder()
                    .withBeforeActionLog("Checking if menu needs to be opened...")
                    .withSuccessLog("Found menu toggle")
                    .withFailureLog("Menu toggle not found")
                    .build();
                    
                StateImage menuToggle = new StateImage.Builder()
                    .setName("menu-toggle")
                    .addPattern("images/menu-toggle.png")
                    .build();
                    
                ActionResult menuResult = action.perform(findMenu, menuToggle);
                if (menuResult.isSuccess()) {
                    ClickOptions clickMenu = new ClickOptions.Builder()
                        .withBeforeActionLog("Opening menu...")
                        .withSuccessLog("Menu opened")
                        .setPauseAfterEnd(0.5)
                        .build();
                        
                    action.perform(clickMenu, menuResult.getMatchList());
                }
            }
        }
        
        return false;
    }
    
    private void executeWorkflow() {
        // Each step is logged automatically
        performLogin("demo@example.com", "password123");
        navigateToSettings();
        updateSettings();
        saveSettings();
        logout();
    }
    
    private void updateSettings() {
        PatternFindOptions findSetting = new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for notification setting...")
            .withSuccessLog("Found notification setting")
            .build();
            
        StateImage notificationToggle = new StateImage.Builder()
            .setName("notification-toggle")
            .addPattern("images/notification-toggle.png")
            .build();
            
        ActionResult result = action.perform(findSetting, notificationToggle);
        if (result.isSuccess()) {
            ClickOptions toggleSetting = new ClickOptions.Builder()
                .withBeforeActionLog("Toggling notification setting...")
                .withSuccessLog("Setting updated")
                .build();
                
            action.perform(toggleSetting, result.getMatchList());
        }
    }
    
    private void saveSettings() {
        PatternFindOptions findSave = new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for Save button...")
            .withSuccessLog("Found Save button")
            .build();
            
        StateImage saveButton = new StateImage.Builder()
            .setName("save-button")
            .addPattern("images/save-button.png")
            .build();
            
        ActionResult result = action.perform(findSave, saveButton);
        if (result.isSuccess()) {
            ClickOptions clickSave = new ClickOptions.Builder()
                .withBeforeActionLog("Saving settings...")
                .withSuccessLog("Settings saved successfully")
                .setPauseAfterEnd(1.0)
                .build();
                
            action.perform(clickSave, result.getMatchList());
        }
    }
    
    private void logout() {
        PatternFindOptions findLogout = new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for Logout button...")
            .withSuccessLog("Found Logout button")
            .build();
            
        StateImage logoutButton = new StateImage.Builder()
            .setName("logout-button")
            .addPattern("images/logout-button.png")
            .build();
            
        ActionResult result = action.perform(findLogout, logoutButton);
        if (result.isSuccess()) {
            ClickOptions clickLogout = new ClickOptions.Builder()
                .withBeforeActionLog("Logging out...")
                .withSuccessLog("Logged out successfully")
                .withAfterActionLog("Session ended")
                .build();
                
            action.perform(clickLogout, result.getMatchList());
        }
    }
    
    private boolean verifyState(State state, String stateName) {
        PatternFindOptions verifyState = new PatternFindOptions.Builder()
            .withBeforeActionLog("Verifying " + stateName + " state...")
            .withLogging(logging -> logging
                .successMessage("✓ Successfully reached " + stateName + " state")
                .failureMessage("✗ Failed to reach " + stateName + " state")
                .logOnSuccess(true)
                .logOnFailure(true))
            .build();
            
        // Check multiple indicators for state verification
        for (StateImage indicator : state.getStateImages()) {
            ActionResult result = action.perform(verifyState, indicator);
            if (result.isSuccess()) {
                return true;
            }
        }
        
        return false;
    }
    
    private State getCurrentState() {
        // This would normally use StateMemory to get the actual current state
        // For demo purposes, we'll return a mock state
        return createLoginState();
    }
    
    // State creation methods
    
    private State createLoginState() {
        return new State.Builder("Login")
            .withImages(
                new StateImage.Builder()
                    .setName("login-logo")
                    .addPattern("images/login-logo.png")
                    .build(),
                new StateImage.Builder()
                    .setName("username-field")
                    .addPattern("images/username-field.png")
                    .build()
            )
            .build();
    }
    
    private State createDashboardState() {
        return new State.Builder("Dashboard")
            .withImages(
                new StateImage.Builder()
                    .setName("dashboard-logo")
                    .addPattern("images/dashboard-logo.png")
                    .build(),
                new StateImage.Builder()
                    .setName("welcome-message")
                    .addPattern("images/welcome-message.png")
                    .build()
            )
            .build();
    }
    
    private State createSettingsState() {
        return new State.Builder("Settings")
            .withImages(
                new StateImage.Builder()
                    .setName("settings-header")
                    .addPattern("images/settings-header.png")
                    .build()
            )
            .build();
    }
}