---
sidebar_position: 11
title: Reusable Patterns
description: Build a library of reusable automation patterns using Brobot's State and Transition system
---

# Reusable Automation Patterns

Creating reusable patterns is key to building maintainable and scalable automation solutions. This guide shows how to create modular, composable automation components using Brobot's native State and Transition system combined with modern ActionConfig APIs.

:::info Brobot's Pattern Philosophy
Brobot patterns are built using **@State** classes for UI elements and **@TransitionSet** classes for reusable logic. This guide demonstrates how to compose these patterns using **ConditionalActionChain** for modern, idiomatic Brobot code.
:::

## Prerequisites

This guide assumes familiarity with:
- **[ActionConfig Overview](./01-overview.md)** - Understanding the ActionConfig system
- **[Action Chaining](./07-action-chaining.md)** - Composing multiple actions together
- **[Conditional Actions](./09-conditional-actions.md)** - Handling dynamic UI scenarios
- **[States in Brobot](../../01-getting-started/states.md)** - Brobot's state machine system

## Required Imports

All examples in this guide require these imports:

```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ConditionalActionChain;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.manageStates.State;
import io.github.jspinak.brobot.manageStates.StateManagementService;
import io.github.jspinak.brobot.manageStates.Transition;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.Getter;
```

## Pattern Architecture

### States as Reusable UI Containers

Define states to hold UI element patterns that can be reused across your automation:

```java
@Component
@State
@Getter
public class LoginFormState {
    private final StateImage usernameField;
    private final StateImage passwordField;
    private final StateImage loginButton;
    private final StateImage successIndicator;
    private final StateImage errorMessage;

    public LoginFormState() {
        this.usernameField = new StateImage.Builder()
            .setName("username-field")
            .addPattern("images/login/username.png")
            .build();

        this.passwordField = new StateImage.Builder()
            .setName("password-field")
            .addPattern("images/login/password.png")
            .build();

        this.loginButton = new StateImage.Builder()
            .setName("login-button")
            .addPattern("images/login/login-btn.png")
            .build();

        this.successIndicator = new StateImage.Builder()
            .setName("success-indicator")
            .addPattern("images/login/success.png")
            .build();

        this.errorMessage = new StateImage.Builder()
            .setName("error-message")
            .addPattern("images/login/error.png")
            .build();
    }
}
```

### Transitions as Reusable Action Logic

Create transition classes with reusable methods for common workflows:

```java
@Component
@Transition(
    from = LoginFormState.class,
    to = DashboardState.class,
    activatesOn = {DashboardState.class}
)
public class LoginTransitions {
    private static final Logger logger = LoggerFactory.getLogger(LoginTransitions.class);

    @Autowired
    private Action action;

    @Autowired
    private LoginFormState loginForm;

    /**
     * Reusable login pattern - can be called from anywhere
     */
    public boolean performLogin(String username, String password) {
        logger.info("Performing login for user: {}", username);

        return ConditionalActionChain
            .find(loginForm.getUsernameField())
            .ifFoundClick()
            .ifFoundClearAndType(username)
            .pressTab()
            .type(password)
            .then(loginForm.getLoginButton())
            .ifFoundClick()
            .then(loginForm.getSuccessIndicator())
            .ifFoundLog("Login successful for: " + username)
            .ifNotFoundLog("Login failed for: " + username)
            .perform(action)
            .isSuccess();
    }

    /**
     * Login with validation
     */
    public boolean performLoginWithValidation(String username, String password) {
        boolean result = performLogin(username, password);

        if (!result) {
            // Check for error message
            ActionResult errorCheck = action.find(loginForm.getErrorMessage());
            if (errorCheck.isSuccess()) {
                logger.error("Login error message displayed");
            }
        }

        return result;
    }

    /**
     * Login with retry
     */
    public boolean performLoginWithRetry(String username, String password, int maxAttempts) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            logger.info("Login attempt {} of {}", attempt, maxAttempts);

            if (performLogin(username, password)) {
                return true;
            }

            // Check if we should retry
            if (attempt < maxAttempts) {
                logger.warn("Login failed, retrying...");
            }
        }

        logger.error("Login failed after {} attempts", maxAttempts);
        return false;
    }
}
```

## Common UI Patterns

### Search Pattern

A reusable search pattern for various search interfaces:

```java
@Component
@State
@Getter
public class SearchBarState {
    private final StateImage searchField;
    private final StateImage searchButton;
    private final StateImage resultsContainer;

    public SearchBarState() {
        this.searchField = new StateImage.Builder()
            .setName("search-field")
            .addPattern("images/search/field.png")
            .build();

        this.searchButton = new StateImage.Builder()
            .setName("search-button")
            .addPattern("images/search/button.png")
            .build();

        this.resultsContainer = new StateImage.Builder()
            .setName("results-container")
            .addPattern("images/search/results.png")
            .build();
    }
}

@Component
@Transition(from = SearchBarState.class)
public class SearchTransitions {
    private static final Logger logger = LoggerFactory.getLogger(SearchTransitions.class);

    @Autowired
    private Action action;

    @Autowired
    private SearchBarState searchBar;

    /**
     * Perform search and wait for results
     */
    public boolean performSearch(String searchTerm) {
        logger.info("Searching for: {}", searchTerm);

        return ConditionalActionChain
            .find(searchBar.getSearchField())
            .ifFoundClick()
            .ifFoundClearAndType(searchTerm)
            .pressEnter()
            .then(searchBar.getResultsContainer())
            .ifFoundLog("Search results displayed")
            .ifNotFoundLog("No search results found")
            .perform(action)
            .isSuccess();
    }

    /**
     * Search using button instead of Enter
     */
    public boolean performSearchWithButton(String searchTerm) {
        return ConditionalActionChain
            .find(searchBar.getSearchField())
            .ifFoundClick()
            .ifFoundClearAndType(searchTerm)
            .then(searchBar.getSearchButton())
            .ifFoundClick()
            .then(searchBar.getResultsContainer())
            .perform(action)
            .isSuccess();
    }

    /**
     * Search with timeout
     */
    public boolean performSearchWithTimeout(String searchTerm, double timeoutSeconds) {
        PatternFindOptions findWithTimeout = new PatternFindOptions.Builder()
            .setPauseBeforeBegin(timeoutSeconds)
            .build();

        boolean searchExecuted = ConditionalActionChain
            .find(searchBar.getSearchField())
            .ifFoundClick()
            .ifFoundClearAndType(searchTerm)
            .pressEnter()
            .perform(action)
            .isSuccess();

        if (!searchExecuted) {
            return false;
        }

        // Wait for results with timeout
        ActionResult resultsCheck = action.perform(
            findWithTimeout,
            new ObjectCollection.Builder()
                .withImages(searchBar.getResultsContainer())
                .build()
        );

        return resultsCheck.isSuccess();
    }
}
```

### Menu Navigation Pattern

Navigate through hierarchical menus using state-based patterns:

```java
@Component
@State
@Getter
public class MainMenuState {
    private final StateImage fileMenu;
    private final StateImage editMenu;
    private final StateImage viewMenu;
    private final StateImage helpMenu;

    public MainMenuState() {
        this.fileMenu = new StateImage.Builder()
            .setName("file-menu")
            .addPattern("images/menus/file.png")
            .build();

        this.editMenu = new StateImage.Builder()
            .setName("edit-menu")
            .addPattern("images/menus/edit.png")
            .build();

        this.viewMenu = new StateImage.Builder()
            .setName("view-menu")
            .addPattern("images/menus/view.png")
            .build();

        this.helpMenu = new StateImage.Builder()
            .setName("help-menu")
            .addPattern("images/menus/help.png")
            .build();
    }
}

@Component
@State
@Getter
public class FileMenuState {
    private final StateImage newFile;
    private final StateImage openFile;
    private final StateImage saveFile;
    private final StateImage exit;

    public FileMenuState() {
        this.newFile = new StateImage.Builder()
            .setName("new-file")
            .addPattern("images/menus/file/new.png")
            .build();

        this.openFile = new StateImage.Builder()
            .setName("open-file")
            .addPattern("images/menus/file/open.png")
            .build();

        this.saveFile = new StateImage.Builder()
            .setName("save-file")
            .addPattern("images/menus/file/save.png")
            .build();

        this.exit = new StateImage.Builder()
            .setName("exit")
            .addPattern("images/menus/file/exit.png")
            .build();
    }
}

@Component
@Transition(from = MainMenuState.class, to = FileMenuState.class)
public class MenuNavigationTransitions {
    private static final Logger logger = LoggerFactory.getLogger(MenuNavigationTransitions.class);

    @Autowired
    private Action action;

    @Autowired
    private MainMenuState mainMenu;

    @Autowired
    private FileMenuState fileMenu;

    /**
     * Navigate to File > New
     */
    public boolean createNewFile() {
        ClickOptions clickWithPause = new ClickOptions.Builder()
            .setPauseAfterEnd(0.5)  // Wait for menu to appear
            .build();

        return ConditionalActionChain
            .find(mainMenu.getFileMenu())
            .ifFoundClick(clickWithPause)
            .then(fileMenu.getNewFile())
            .ifFoundClick()
            .perform(action)
            .isSuccess();
    }

    /**
     * Navigate to File > Open
     */
    public boolean openFile() {
        ClickOptions clickWithPause = new ClickOptions.Builder()
            .setPauseAfterEnd(0.5)
            .build();

        return ConditionalActionChain
            .find(mainMenu.getFileMenu())
            .ifFoundClick(clickWithPause)
            .then(fileMenu.getOpenFile())
            .ifFoundClick()
            .perform(action)
            .isSuccess();
    }

    /**
     * Navigate to File > Save
     */
    public boolean saveFile() {
        ClickOptions clickWithPause = new ClickOptions.Builder()
            .setPauseAfterEnd(0.5)
            .build();

        return ConditionalActionChain
            .find(mainMenu.getFileMenu())
            .ifFoundClick(clickWithPause)
            .then(fileMenu.getSaveFile())
            .ifFoundClick()
            .perform(action)
            .isSuccess();
    }
}
```

## Composite Patterns

### Form Submission Pattern

Combine multiple patterns for complex form handling:

```java
@Component
public class RegistrationFormHandler {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationFormHandler.class);

    @Autowired
    private Action action;

    @Autowired
    private RegistrationFormState formState;

    /**
     * Complete registration workflow using reusable patterns
     */
    public boolean completeRegistration(UserData userData) {
        logger.info("Starting registration for: {}", userData.getEmail());

        // Fill personal information
        if (!fillPersonalInfo(userData)) {
            logger.error("Failed to fill personal information");
            return false;
        }

        // Fill address
        if (!fillAddress(userData.getAddress())) {
            logger.error("Failed to fill address");
            return false;
        }

        // Set preferences
        if (!setPreferences(userData.getPreferences())) {
            logger.error("Failed to set preferences");
            return false;
        }

        // Submit form
        return submitRegistration();
    }

    /**
     * Reusable pattern: Fill personal information section
     */
    private boolean fillPersonalInfo(UserData userData) {
        return ConditionalActionChain
            .find(formState.getFirstNameField())
            .ifFoundClick()
            .ifFoundClearAndType(userData.getFirstName())
            .pressTab()
            .type(userData.getLastName())
            .pressTab()
            .type(userData.getEmail())
            .pressTab()  // Trigger email validation
            .then(formState.getEmailValidIndicator())
            .ifNotFoundLog("Email validation failed")
            .perform(action)
            .isSuccess();
    }

    /**
     * Reusable pattern: Fill address section
     */
    private boolean fillAddress(Address address) {
        return ConditionalActionChain
            .find(formState.getStreetField())
            .ifFoundClick()
            .ifFoundClearAndType(address.getStreet())
            .pressTab()
            .type(address.getCity())
            .pressTab()
            .type(address.getState())
            .pressTab()
            .type(address.getZipCode())
            .perform(action)
            .isSuccess();
    }

    /**
     * Reusable pattern: Set preferences
     */
    private boolean setPreferences(UserPreferences prefs) {
        ConditionalActionChain chain = ConditionalActionChain
            .find(formState.getNewsletterCheckbox());

        if (prefs.wantsNewsletter()) {
            chain.ifFoundClick();
        }

        return chain
            .then(formState.getTermsCheckbox())
            .ifFoundClick()
            .perform(action)
            .isSuccess();
    }

    /**
     * Reusable pattern: Submit form with validation
     */
    private boolean submitRegistration() {
        return ConditionalActionChain
            .find(formState.getSubmitButton())
            .ifFoundClick()
            .then(formState.getSuccessMessage())
            .ifFoundLog("Registration successful!")
            .ifNotFoundLog("Registration failed")
            .perform(action)
            .isSuccess();
    }
}
```

## Pattern Composition with Spring

### Composing Patterns with Dependency Injection

Use Spring's dependency injection to compose complex workflows from simple patterns:

```java
@Component
public class WorkflowOrchestrator {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowOrchestrator.class);

    @Autowired
    private LoginTransitions loginTransitions;

    @Autowired
    private SearchTransitions searchTransitions;

    @Autowired
    private MenuNavigationTransitions menuTransitions;

    @Autowired
    private StateManagementService stateManager;

    /**
     * Complete workflow: Login → Search → Navigate
     */
    public boolean executeCompleteWorkflow(String username, String password, String searchTerm) {
        logger.info("Starting complete workflow");

        // Step 1: Login
        if (!loginTransitions.performLogin(username, password)) {
            logger.error("Workflow failed at login step");
            return false;
        }

        // Step 2: Perform search
        if (!searchTransitions.performSearch(searchTerm)) {
            logger.error("Workflow failed at search step");
            return false;
        }

        // Step 3: Navigate to results
        if (!menuTransitions.openFile()) {
            logger.error("Workflow failed at navigation step");
            return false;
        }

        logger.info("Workflow completed successfully");
        return true;
    }

    /**
     * Workflow with error recovery
     */
    public boolean executeWorkflowWithRecovery(String username, String password, String searchTerm) {
        // Try login with retry
        if (!loginTransitions.performLoginWithRetry(username, password, 3)) {
            return false;
        }

        // Try search
        boolean searchSuccess = searchTransitions.performSearch(searchTerm);

        if (!searchSuccess) {
            logger.warn("Search failed, attempting alternative search method");
            searchSuccess = searchTransitions.performSearchWithButton(searchTerm);
        }

        return searchSuccess;
    }
}
```

## Conditional Pattern Execution

### Using ConditionalActionChain for Dynamic Patterns

Build patterns that adapt to different UI states:

```java
@Component
public class DynamicFormHandler {
    private static final Logger logger = LoggerFactory.getLogger(DynamicFormHandler.class);

    @Autowired
    private Action action;

    @Autowired
    private FormState formState;

    /**
     * Pattern that handles optional fields conditionally
     */
    public boolean fillFormWithOptionalFields(FormData data) {
        ConditionalActionChain chain = ConditionalActionChain
            .find(formState.getRequiredField())
            .ifFoundClick()
            .ifFoundClearAndType(data.getRequiredValue());

        // Conditionally fill optional field if it exists
        if (data.hasOptionalValue()) {
            chain = chain
                .then(formState.getOptionalField())
                .ifFoundClick()
                .ifFoundClearAndType(data.getOptionalValue())
                .ifNotFoundLog("Optional field not present, skipping");
        }

        return chain
            .then(formState.getSubmitButton())
            .ifFoundClick()
            .perform(action)
            .isSuccess();
    }

    /**
     * Pattern with conditional navigation based on UI state
     */
    public boolean navigateBasedOnState() {
        return ConditionalActionChain
            .find(formState.getPrimaryButton())
            .ifFoundClick()
            .ifFoundLog("Primary path taken")
            .ifNotFoundDo(chain -> {
                logger.info("Primary button not found, trying alternative");
                return chain
                    .find(formState.getAlternativeButton())
                    .ifFoundClick()
                    .ifFoundLog("Alternative path taken")
                    .perform(action);
            })
            .perform(action)
            .isSuccess();
    }
}
```

## Testing Reusable Patterns

### Pattern Test Framework

```java
@SpringBootTest
public class LoginTransitionsTest extends BrobotTestBase {
    private static final Logger logger = LoggerFactory.getLogger(LoginTransitionsTest.class);

    @Autowired
    private LoginTransitions loginTransitions;

    @Autowired
    private Action action;

    @Autowired
    private LoginFormState loginForm;

    @Test
    public void testSuccessfulLogin() {
        // Arrange
        String username = "testuser";
        String password = "testpass";

        // Act
        boolean result = loginTransitions.performLogin(username, password);

        // Assert
        assertTrue(result, "Login should succeed");

        // Verify success indicator appeared
        ActionResult successCheck = action.find(loginForm.getSuccessIndicator());
        assertTrue(successCheck.isSuccess(), "Success indicator should be visible");
    }

    @Test
    public void testFailedLogin() {
        // Arrange
        String username = "baduser";
        String password = "badpass";

        // Act
        boolean result = loginTransitions.performLogin(username, password);

        // Assert
        assertFalse(result, "Login should fail with bad credentials");

        // Verify error message appeared
        ActionResult errorCheck = action.find(loginForm.getErrorMessage());
        assertTrue(errorCheck.isSuccess(), "Error message should be visible");
    }

    @Test
    public void testLoginWithRetry() {
        // Arrange
        String username = "retryuser";
        String password = "retrypass";

        // Act
        boolean result = loginTransitions.performLoginWithRetry(username, password, 3);

        // Assert
        assertTrue(result, "Login should eventually succeed with retry");
    }
}
```

## Best Practices

### 1. Use States for Data, Transitions for Logic

```java
// ✅ GOOD - State holds data
@State
@Getter
public class FormState {
    private final StateImage usernameField;
    private final StateImage passwordField;
}

// ✅ GOOD - Transition contains reusable logic
@Transition(from = FormState.class)
public class FormTransitions {
    public boolean fillForm(String username, String password) {
        // Reusable logic here
    }
}

// ❌ BAD - Don't put logic in State classes
@State
public class FormState {
    public boolean fillForm() {  // Logic doesn't belong here
        // ...
    }
}
```

### 2. Use ConditionalActionChain for Modern Patterns

```java
// ✅ GOOD - Modern ConditionalActionChain
return ConditionalActionChain
    .find(field)
    .ifFoundClick()
    .ifFoundClearAndType(value)
    .perform(action)
    .isSuccess();

// ❌ AVOID - Low-level ActionChainOptions
ActionChainOptions chain = new ActionChainOptions.Builder(...)
    .then(...)
    .build();
chainExecutor.executeChain(chain, ...);
```

### 3. Leverage Spring Dependency Injection

```java
// ✅ GOOD - Use @Autowired for composition
@Component
public class WorkflowHandler {
    @Autowired
    private LoginTransitions loginTransitions;

    @Autowired
    private SearchTransitions searchTransitions;

    public boolean execute() {
        return loginTransitions.performLogin(...) &&
               searchTransitions.performSearch(...);
    }
}

// ❌ BAD - Manual pattern factory
PatternFactory factory = new PatternFactory();
AutomationPattern pattern = factory.create("login");
```

### 4. Create Small, Focused Transition Methods

```java
// ✅ GOOD - Small, reusable methods
public boolean clickLoginButton() {
    return ConditionalActionChain
        .find(loginButton)
        .ifFoundClick()
        .perform(action)
        .isSuccess();
}

public boolean typeUsername(String username) {
    return ConditionalActionChain
        .find(usernameField)
        .ifFoundClick()
        .ifFoundClearAndType(username)
        .perform(action)
        .isSuccess();
}

// ❌ AVOID - Monolithic methods that do everything
public boolean loginAndSearchAndNavigate(...) {
    // 100+ lines of code
}
```

### 5. Use Clear Logging

```java
@Component
@Transition(from = LoginFormState.class)
public class LoginTransitions {
    private static final Logger logger = LoggerFactory.getLogger(LoginTransitions.class);

    public boolean performLogin(String username, String password) {
        logger.info("Performing login for user: {}", username);

        boolean result = ConditionalActionChain
            .find(loginForm.getUsernameField())
            .ifFoundClick()
            .ifFoundClearAndType(username)
            .ifFoundLog("Username entered successfully")
            .ifNotFoundLog("Username field not found")
            .perform(action)
            .isSuccess();

        if (result) {
            logger.info("Login successful for: {}", username);
        } else {
            logger.error("Login failed for: {}", username);
        }

        return result;
    }
}
```

### 6. Handle Errors Gracefully

```java
public boolean performSearchWithErrorHandling(String searchTerm) {
    try {
        return ConditionalActionChain
            .find(searchBar.getSearchField())
            .ifFoundClick()
            .ifFoundClearAndType(searchTerm)
            .pressEnter()
            .then(searchBar.getResultsContainer())
            .ifNotFoundDo(chain -> {
                logger.error("Search results not found for: {}", searchTerm);
                return chain.throwError("Search failed");
            })
            .perform(action)
            .isSuccess();
    } catch (Exception e) {
        logger.error("Search execution failed", e);
        return false;
    }
}
```

### 7. Write Tests for Reusable Patterns

```java
@SpringBootTest
public class SearchTransitionsTest extends BrobotTestBase {

    @Autowired
    private SearchTransitions searchTransitions;

    @Test
    public void testSearchPattern() {
        // Test your reusable patterns
        boolean result = searchTransitions.performSearch("test query");
        assertTrue(result, "Search pattern should execute successfully");
    }

    @Test
    public void testSearchWithTimeout() {
        boolean result = searchTransitions.performSearchWithTimeout("test query", 5.0);
        assertTrue(result, "Search with timeout should complete");
    }
}
```

## Next Steps

### Related Guides
- **[Action Chaining](./07-action-chaining.md)** - Composing patterns with action chains
- **[Conditional Actions](./09-conditional-actions.md)** - Dynamic pattern execution with RepeatUntilConfig
- **[Complex Workflows](./08-complex-workflows.md)** - Building multi-step workflows from patterns
- **[Form Automation](./10-form-automation.md)** - Practical form automation patterns
- **[Conditional Action Chains](./15-conditional-chains-examples.md)** - Modern fluent API examples

### Reference Documentation
- **[ActionConfig Overview](./01-overview.md)** - Foundation of all patterns
- **[States in Brobot](../../01-getting-started/states.md)** - Understanding the state system
- **[Upgrading to Latest](../migration/upgrading-to-latest.md)** - Updating legacy patterns to modern API
