---
sidebar_position: 5
---

# Transitions

Transitions define how your automation moves between states. With Brobot's modern `@TransitionSet` annotation system (1.2.0+), all transitions for a state are grouped together in a single class, providing better organization and clearer intent.

## Modern Transition Definition with @TransitionSet

The `@TransitionSet` annotation groups all transitions for a state in one class. Each class contains:
- `@FromTransition` methods that define how to navigate TO this state FROM other states
- `@ToTransition` method that verifies arrival at this state

```java
@TransitionSet(state = WorldState.class, description = "World state transitions")
@Component
@RequiredArgsConstructor
@Slf4j
public class WorldTransitions {
    
    private final HomeState homeState;
    private final WorldState worldState;
    private final IslandState islandState;
    private final Action action;
    
    /**
     * Navigate from Home to World by clicking the world button.
     */
    @FromTransition(from = HomeState.class, priority = 1, description = "Navigate from Home to World")
    public boolean fromHome() {
        log.info("Navigating from Home to World");
        
        // Mock mode support for testing
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful navigation");
            return true;
        }
        
        return action.click(homeState.getToWorldButton()).isSuccess();
    }
    
    /**
     * Navigate from Island back to World.
     */
    @FromTransition(from = IslandState.class, priority = 2, description = "Return from Island to World")
    public boolean fromIsland() {
        log.info("Navigating from Island to World");
        
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            return true;
        }
        
        return action.click(islandState.getBackToWorldButton()).isSuccess();
    }
    
    /**
     * Verify that we have successfully arrived at the World state.
     */
    @ToTransition(description = "Verify arrival at World state", required = true)
    public boolean verifyArrival() {
        log.info("Verifying arrival at World state");
        
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            return true;
        }
        
        // Check for world-specific elements
        boolean foundMinimap = action.find(worldState.getMinimap()).isSuccess();
        
        if (foundMinimap) {
            log.info("Successfully confirmed World state is active");
            return true;
        } else {
            log.error("Failed to confirm World state - minimap not found");
            return false;
        }
    }
}
```

## Key Features of @TransitionSet

### 1. Unified Class Structure
All transitions for a state are in ONE class:
- Better organization - easy to find all paths to/from a state
- Clear separation of concerns - navigation vs verification
- Natural file structure that mirrors state structure

### 2. Method-Level Annotations

#### @FromTransition
Defines how to navigate TO this state FROM another state:
```java
@FromTransition(
    from = SourceState.class,     // Required: source state
    priority = 1,                  // Optional: higher = preferred path
    description = "Navigation logic" // Optional: documentation
)
public boolean fromSource() {
    // Navigation logic
}
```

#### @ToTransition
Verifies successful arrival at the state:
```java
@ToTransition(
    description = "Verification logic",  // Optional: documentation
    required = true                       // Optional: must succeed (default: false)
)
public boolean verifyArrival() {
    // Verification logic
}
```

### 3. Automatic Registration
No manual transition setup needed - the framework handles everything automatically.

### 4. Dependency Injection
Transitions are Spring components with full DI support:
```java
@TransitionSet(state = DashboardState.class)
@Component
@RequiredArgsConstructor
public class DashboardTransitions {
    private final LoginState loginState;
    private final DashboardState dashboardState;
    private final Action action;
    private final DatabaseService databaseService;  // Any Spring bean
    
    @FromTransition(from = LoginState.class, priority = 1)
    public boolean fromLogin() {
        // Access any injected dependencies
        return action.click(loginState.getSubmitButton()).isSuccess();
    }
}
```

## Complete Example: Island Transitions

```java
@TransitionSet(state = IslandState.class, description = "Island state transitions")
@Component
@RequiredArgsConstructor
@Slf4j
public class IslandTransitions {
    
    private final IslandState islandState;
    private final WorldState worldState;
    private final Action action;
    
    /**
     * Navigate from World to Island by clicking on an island.
     */
    @FromTransition(from = WorldState.class, priority = 1)
    public boolean fromWorld() {
        log.info("Navigating from World to Island");
        
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            return true;
        }
        
        // Try clicking different islands
        ActionResult result = action.click(worldState.getCastle());
        if (!result.isSuccess()) {
            result = action.click(worldState.getFarms());
        }
        if (!result.isSuccess()) {
            result = action.click(worldState.getMines());
        }
        
        return result.isSuccess();
    }
    
    /**
     * Verify arrival at Island state.
     */
    @ToTransition(required = true)
    public boolean verifyArrival() {
        log.info("Verifying arrival at Island state");
        
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            return true;
        }
        
        return action.find(islandState.getIslandName()).isSuccess();
    }
}
```

## Transition Patterns

### Simple Navigation
```java
@TransitionSet(state = SettingsState.class)
@Component
@RequiredArgsConstructor
public class SettingsTransitions {
    
    private final HomeState homeState;
    private final SettingsState settingsState;
    private final Action action;
    
    @FromTransition(from = HomeState.class, priority = 1)
    public boolean fromHome() {
        if (FrameworkSettings.mock) return true;
        return action.click(homeState.getSettingsIcon()).isSuccess();
    }
    
    @ToTransition(required = true)
    public boolean verifyArrival() {
        if (FrameworkSettings.mock) return true;
        return action.find(settingsState.getSettingsHeader()).isSuccess();
    }
}
```

### Multi-Step Navigation
```java
@TransitionSet(state = DashboardState.class)
@Component
@RequiredArgsConstructor
@Slf4j
public class DashboardTransitions {
    
    private final LoginState loginState;
    private final DashboardState dashboardState;
    private final Action action;
    
    @FromTransition(from = LoginState.class, priority = 1)
    public boolean fromLogin() {
        log.info("Navigating from Login to Dashboard");
        
        if (FrameworkSettings.mock) return true;
        
        // Multi-step login process
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
    
    @ToTransition(required = true)
    public boolean verifyArrival() {
        if (FrameworkSettings.mock) return true;
        
        // Wait for dashboard to load
        ActionResult validation = action.findWithTimeout(
            dashboardState.getDashboardHeader(), 10
        );
        
        return validation.isSuccess();
    }
}
```

### Conditional Navigation with Multiple Paths
```java
@TransitionSet(state = GameState.class)
@Component
@RequiredArgsConstructor
@Slf4j
public class GameTransitions {
    
    private final MainMenuState mainMenu;
    private final PauseMenuState pauseMenu;
    private final GameState gameState;
    private final Action action;
    
    @FromTransition(from = MainMenuState.class, priority = 1)
    public boolean fromMainMenu() {
        log.info("Starting game from main menu");
        
        if (FrameworkSettings.mock) return true;
        
        // Try primary action first
        ActionResult playResult = action.click(mainMenu.getPlayButton());
        
        if (!playResult.isSuccess()) {
            log.warn("Play button not found, trying alternative");
            playResult = action.click(mainMenu.getStartButton());
        }
        
        return playResult.isSuccess();
    }
    
    @FromTransition(from = PauseMenuState.class, priority = 2)
    public boolean fromPauseMenu() {
        log.info("Resuming game from pause menu");
        
        if (FrameworkSettings.mock) return true;
        
        return action.click(pauseMenu.getResumeButton()).isSuccess();
    }
    
    @ToTransition(required = true)
    public boolean verifyArrival() {
        if (FrameworkSettings.mock) return true;
        
        // Wait for game board to appear
        ActionResult waitResult = action.findWithTimeout(
            gameState.getGameBoard(), 10
        );
        
        if (!waitResult.isSuccess()) {
            log.error("Game failed to load");
            return false;
        }
        
        log.info("Game state confirmed active");
        return true;
    }
}
```

## Working with ActionConfig

Use modern ActionConfig classes for precise control:

```java
@TransitionSet(state = ConfirmationState.class)
@Component
@RequiredArgsConstructor
public class ConfirmationTransitions {
    
    private final FormState formState;
    private final ConfirmationState confirmationState;
    private final Action action;
    
    @FromTransition(from = FormState.class, priority = 1)
    public boolean fromForm() {
        if (FrameworkSettings.mock) return true;
        
        // Configure specific action behaviors
        ClickOptions doubleClick = new ClickOptions.Builder()
            .setNumberOfClicks(2)
            .setPauseAfterEnd(0.5)
            .build();
            
        TypeOptions secureType = new TypeOptions.Builder()
            .setPauseBeforeBegin(0.3)
            .build();
            
        // Chain actions with specific configurations
        PatternFindOptions chainedAction = new PatternFindOptions.Builder()
            .then(doubleClick)
            .then(secureType)
            .build();
        
        ObjectCollection targets = new ObjectCollection.Builder()
            .withImages(formState.getSubmitButton())
            .withStrings("John Doe")
            .build();
        
        return action.perform(chainedAction, targets).isSuccess();
    }
    
    @ToTransition(required = true)
    public boolean verifyArrival() {
        if (FrameworkSettings.mock) return true;
        return action.find(confirmationState.getSuccessMessage()).isSuccess();
    }
}
```

## File Organization

Organize transitions alongside states for clarity:

```
src/main/java/com/example/basics/
├── states/
│   ├── HomeState.java
│   ├── WorldState.java
│   ├── IslandState.java
│   └── SettingsState.java
└── transitions/
    ├── HomeTransitions.java      # All transitions for Home state
    ├── WorldTransitions.java     # All transitions for World state
    ├── IslandTransitions.java    # All transitions for Island state
    └── SettingsTransitions.java  # All transitions for Settings state
```

## Best Practices

1. **Always Include Mock Mode Support**
   ```java
   @FromTransition(from = SourceState.class)
   public boolean fromSource() {
       if (FrameworkSettings.mock) return true;
       // Real navigation logic
   }
   ```

2. **Use Descriptive Method Names**
   - `fromHome()`, `fromLogin()`, `fromMenu()` - clear source indication
   - `verifyArrival()` - standard verification method name

3. **Add Comprehensive Logging**
   ```java
   @FromTransition(from = SourceState.class)
   public boolean fromSource() {
       log.info("Navigating from Source to Target");
       if (FrameworkSettings.mock) {
           log.info("Mock mode: simulating successful navigation");
           return true;
       }
       // Navigation logic
   }
   ```

4. **Handle Failures Gracefully**
   ```java
   @FromTransition(from = SourceState.class)
   public boolean fromSource() {
       try {
           if (FrameworkSettings.mock) return true;
           return action.click(element).isSuccess();
       } catch (Exception e) {
           log.error("Transition failed", e);
           return false;
       }
   }
   ```

5. **Verify Critical Elements in ToTransition**
   ```java
   @ToTransition(required = true)
   public boolean verifyArrival() {
       if (FrameworkSettings.mock) return true;
       
       // Check multiple elements for robust verification
       boolean hasHeader = action.find(state.getHeader()).isSuccess();
       boolean hasContent = action.find(state.getMainContent()).isSuccess();
       
       return hasHeader && hasContent;
   }
   ```

## Testing Transitions

The unified structure makes testing straightforward:

```java
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfiguration.class})
public class WorldTransitionsTest {
    
    @Autowired
    private WorldTransitions worldTransitions;
    
    @MockBean
    private Action action;
    
    @MockBean
    private HomeState homeState;
    
    @Test
    public void testFromHomeTransition() {
        // Given
        when(action.click(any())).thenReturn(
            new ActionResult.Builder().setSuccess(true).build()
        );
        
        // When
        boolean result = worldTransitions.fromHome();
        
        // Then
        assertTrue(result);
        verify(action).click(homeState.getToWorldButton());
    }
    
    @Test
    public void testVerifyArrival() {
        // Given
        when(action.find(any())).thenReturn(
            new ActionResult.Builder().setSuccess(true).build()
        );
        
        // When
        boolean arrived = worldTransitions.verifyArrival();
        
        // Then
        assertTrue(arrived);
    }
}
```

## Migration from Old Format

If migrating from the old `@Transition` annotation:

### Old Format (Pre-1.2.0)
```java
// Separate class for each transition
@Transition(from = HomeState.class, to = WorldState.class)
public class HomeToWorldTransition {
    public boolean execute() {
        return action.click(homeState.getToWorldButton()).isSuccess();
    }
}
```

### New Format (1.2.0+)
```java
// All transitions for a state in one class
@TransitionSet(state = WorldState.class)
@Component
public class WorldTransitions {
    
    @FromTransition(from = HomeState.class, priority = 1)
    public boolean fromHome() {
        if (FrameworkSettings.mock) return true;
        return action.click(homeState.getToWorldButton()).isSuccess();
    }
    
    @ToTransition(required = true)
    public boolean verifyArrival() {
        if (FrameworkSettings.mock) return true;
        return action.find(worldState.getMinimap()).isSuccess();
    }
}
```

## Benefits of @TransitionSet

1. **Better Organization** - All transitions for a state in ONE place
2. **Clearer Intent** - FromTransitions vs ToTransition makes flow obvious
3. **Less Boilerplate** - No manual StateTransitions builders
4. **Natural Structure** - File organization mirrors state structure
5. **Easier Testing** - Each method can be tested independently
6. **Mock Mode Ready** - Easy to add testing support

## Next Steps

With states and transitions defined using the @TransitionSet system, your entire state machine is automatically configured. The framework handles all registration and wiring - you just focus on your automation logic!