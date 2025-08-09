---
sidebar_position: 5
---

# Transitions

Transitions define how your automation moves between states. With Brobot's modern `@Transition` annotation, creating robust state transitions is simple and maintainable.

## Modern Transition Definition with @Transition Annotation

The `@Transition` annotation automatically registers transitions between states:

```java
@Transition(from = HomeState.class, to = WorldState.class)
@Component
@RequiredArgsConstructor
@Slf4j
public class HomeToWorldTransition {
    private final HomeState homeState;
    private final Action action;
    
    public boolean execute() {
        log.info("Transitioning from Home to World");
        return action.click(homeState.getToWorldButton()).isSuccess();
    }
}
```

## Key Features of Modern Transitions

### 1. Automatic Registration
No manual transition setup needed - the annotation handles everything:
```java
@Transition(from = LoginState.class, to = DashboardState.class)
public class LoginToDashboardTransition {
    // Automatically registered with state management
}
```

### 2. Dependency Injection
Transitions are Spring components with full DI support:
```java
@Transition(from = WorldState.class, to = IslandState.class)
@Component
@RequiredArgsConstructor
public class WorldToIslandTransition {
    private final WorldState worldState;
    private final IslandState islandState;
    private final Action action;
    private final DatabaseService databaseService;  // Any Spring bean
    
    public boolean execute() {
        // Access any injected dependencies
        return action.click(worldState.getCastle()).isSuccess();
    }
}
```

### 3. Action Convenience Methods
Use fluent action methods for clean, readable code:
```java
public boolean execute() {
    return action
        .click(loginState.getUsernameField())
        .type("admin")
        .click(loginState.getPasswordField())
        .type("password123")
        .click(loginState.getSubmitButton())
        .isSuccess();
}
```

## Robust Error Handling with Sequential Actions

Handle failures gracefully with sequential action patterns:

```java
@Transition(from = MainMenuState.class, to = GameState.class)
@Component
@RequiredArgsConstructor
@Slf4j
public class MainMenuToGameTransition {
    private final MainMenuState mainMenu;
    private final GameState gameState;
    private final Action action;
    
    public boolean execute() {
        log.info("Starting game from main menu");
        
        // Try primary action first
        ActionResult playResult = action.click(mainMenu.getPlayButton());
        
        if (!playResult.isSuccess()) {
            log.warn("Play button not found, trying alternative");
            playResult = action.click(mainMenu.getStartButton());
        }
        
        if (!playResult.isSuccess()) {
            log.error("Failed to start game");
            return false;
        }
        
        // Wait for game board to appear with timeout
        ActionResult waitResult = action.findWithTimeout(gameState.getGameBoard(), 10);
        
        if (!waitResult.isSuccess()) {
            log.error("Game failed to load");
            return false;
        }
        
        return true;
    }
}
```

## Transition Patterns

### Simple Click Transition
```java
@Transition(from = HomeState.class, to = SettingsState.class)
@Component
@RequiredArgsConstructor
public class HomeToSettingsTransition {
    private final HomeState homeState;
    private final Action action;
    
    public boolean execute() {
        return action.click(homeState.getSettingsIcon()).isSuccess();
    }
}
```

### Multi-Step Transition
```java
@Transition(from = LoginState.class, to = DashboardState.class)
@Component
@RequiredArgsConstructor
public class LoginTransition {
    private final LoginState loginState;
    private final Action action;
    
    public boolean execute() {
        ActionResult result = action
            .click(loginState.getUsernameField())
            .type("user@example.com")
            .click(loginState.getPasswordField())
            .type(System.getenv("APP_PASSWORD"))
            .click(loginState.getLoginButton());
            
        if (result.isSuccess()) {
            log.info("Login successful");
            return true;
        }
        
        log.error("Login failed: {}", result.getFailureReason());
        return false;
    }
}
```

### Conditional Navigation
```java
@Transition(from = ProductListState.class, to = ProductDetailsState.class)
@Component
@RequiredArgsConstructor
public class SelectProductTransition {
    private final ProductListState productList;
    private final Action action;
    
    public boolean execute() {
        // Try to find and click a specific product
        ActionResult specificProduct = action.click(
            productList.getProductByName("Premium Widget")
        );
        
        if (specificProduct.isSuccess()) {
            return true;
        }
        
        // Fallback: click the first available product
        log.info("Specific product not found, selecting first available");
        return action.click(productList.getFirstProduct()).isSuccess();
    }
}
```

## Working with ActionConfig

Use modern ActionConfig classes for precise control:

```java
@Transition(from = FormState.class, to = ConfirmationState.class)
@Component
@RequiredArgsConstructor
public class SubmitFormTransition {
    private final FormState formState;
    private final Action action;
    
    public boolean execute() {
        // Configure specific action behaviors
        ClickOptions doubleClick = new ClickOptions.Builder()
            .setNumberOfClicks(2)
            .setPauseAfterEnd(0.5)
            .build();
            
        TypeTextOptions secureType = new TypeTextOptions.Builder()
            .setPauseBeforeBegin(0.3)
            .setModifierKeys(KeyEvent.VK_CONTROL)
            .build();
            
        DragOptions preciseDrag = new DragOptions.Builder()
            .setDragDuration(1.5)
            .setPauseAfterEnd(0.2)
            .build();
        
        return action
            .click(formState.getNameField())
            .type("John Doe")
            .perform(formState.getSubmitButton(), doubleClick)
            .isSuccess();
    }
}
```

## Transition with State Validation

Ensure you've reached the correct state:

```java
@Transition(from = HomeState.class, to = WorldState.class)
@Component
@RequiredArgsConstructor
@Slf4j
public class HomeToWorldWithValidation {
    private final HomeState homeState;
    private final WorldState worldState;
    private final Action action;
    
    public boolean execute() {
        // Perform the transition
        ActionResult clickResult = action.click(homeState.getToWorldButton());
        
        if (!clickResult.isSuccess()) {
            log.error("Failed to click world button");
            return false;
        }
        
        // Validate we reached the target state
        ActionResult validation = action.findWithTimeout(worldState.getMinimap(), 5);
        
        if (validation.isSuccess()) {
            log.info("Successfully transitioned to World state");
            return true;
        }
        
        log.error("Transition executed but World state not reached");
        return false;
    }
}
```

## Complex Transition with Retry Logic

```java
@Transition(from = ConnectionState.class, to = ConnectedState.class)
@Component
@RequiredArgsConstructor
@Slf4j
public class ConnectWithRetryTransition {
    private final ConnectionState connectionState;
    private final ConnectedState connectedState;
    private final Action action;
    
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY = 2000;
    
    public boolean execute() {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            log.info("Connection attempt {} of {}", attempt, MAX_RETRIES);
            
            ActionResult connectResult = action.click(
                connectionState.getConnectButton()
            );
            
            if (!connectResult.isSuccess()) {
                log.warn("Connect button click failed");
                continue;
            }
            
            // Wait for connection with timeout
            ActionResult waitResult = action.findWithTimeout(
                connectedState.getConnectionIndicator(), 
                10
            );
            
            if (waitResult.isSuccess()) {
                log.info("Successfully connected on attempt {}", attempt);
                return true;
            }
            
            if (attempt < MAX_RETRIES) {
                log.info("Connection failed, retrying in {} ms", RETRY_DELAY);
                try {
                    Thread.sleep(RETRY_DELAY);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        
        log.error("Failed to connect after {} attempts", MAX_RETRIES);
        return false;
    }
}
```

## Best Practices

### 1. Single Responsibility
Each transition should handle one logical flow:
```java
// Good: Clear, single purpose
@Transition(from = LoginState.class, to = DashboardState.class)
public class LoginTransition { }

// Avoid: Multiple unrelated transitions
@Transition(from = LoginState.class, to = DashboardState.class)
public class LoginAndSetupAndNavigateTransition { }
```

### 2. Logging
Add meaningful logs for debugging:
```java
public boolean execute() {
    log.info("Starting transition from {} to {}", 
        homeState.getClass().getSimpleName(),
        worldState.getClass().getSimpleName());
    
    ActionResult result = action.click(homeState.getToWorldButton());
    
    if (result.isSuccess()) {
        log.info("Transition successful");
    } else {
        log.error("Transition failed: {}", result.getFailureReason());
    }
    
    return result.isSuccess();
}
```

### 3. State Validation
Always validate you've reached the target state:
```java
public boolean execute() {
    // Perform action
    action.click(sourceState.getNavigationButton());
    
    // Validate arrival
    return action.exists(targetState.getUniqueElement());
}
```

### 4. Error Recovery
Use sequential actions with error checking for robust handling:
```java
public boolean execute() {
    // Try primary button first
    ActionResult primaryResult = action.click(primaryButton);
    
    if (!primaryResult.isSuccess()) {
        primaryResult = action.click(fallbackButton);
    }
    
    if (!primaryResult.isSuccess()) {
        return false;
    }
    
    // Wait for target element to appear
    ActionResult waitResult = action.findWithTimeout(targetElement, 5);
    return waitResult.isSuccess();
}
```

## Complete Example

Here's a complete transition with all modern features:

```java
@Transition(from = ShoppingCartState.class, to = CheckoutState.class)
@Component
@RequiredArgsConstructor
@Slf4j
public class CartToCheckoutTransition {
    private final ShoppingCartState cartState;
    private final CheckoutState checkoutState;
    private final Action action;
    private final CartService cartService;
    
    public boolean execute() {
        log.info("Proceeding to checkout");
        
        // Validate cart is not empty
        if (cartService.isEmpty()) {
            log.error("Cannot checkout with empty cart");
            return false;
        }
        
        // Use sequential execution for robust handling
        // Try primary checkout button first
        ActionResult checkoutResult = action.click(cartState.getCheckoutButton());
        
        if (!checkoutResult.isSuccess()) {
            log.warn("Primary checkout button failed, trying alternative");
            checkoutResult = action.click(cartState.getProceedButton());
        }
        
        if (!checkoutResult.isSuccess()) {
            log.error("Failed to click checkout button");
            return false;
        }
        
        // Wait for checkout page to load
        ActionResult waitResult = action.findWithTimeout(checkoutState.getPaymentSection(), 10);
        
        if (!waitResult.isSuccess()) {
            log.error("Checkout page did not load, trying to recover by refreshing");
            action.keyPress(KeyEvent.VK_F5);
            waitResult = action.findWithTimeout(checkoutState.getPaymentSection(), 5);
        }
        
        ActionResult result = waitResult;
        
        if (result.isSuccess()) {
            log.info("Successfully reached checkout");
            cartService.markAsCheckedOut();
        } else {
            log.error("Failed to reach checkout: {}", result.getFailureReason());
        }
        
        return result.isSuccess();
    }
}
```

## Next Steps

With states and transitions defined, you're ready to build complete automation workflows. Check out:
- [Action Configuration](../../guides/action-config-factory) for advanced action options
- [Testing](../../../testing/testing-intro) to validate your automation
- [Live Automation](live-automation) to see everything in action