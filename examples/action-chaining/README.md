# Action Chaining Example

This example demonstrates the power of Brobot's fluent API for chaining actions together with automatic logging at each step.

## Key Features

### 1. Fluent Action Chains
Build complex action sequences that read like natural language:
```java
find(searchBox)
    .then(click())
    .then(type("search query"))
    .then(find(searchButton))
    .then(click())
```

### 2. Automatic Logging Throughout
Every action in the chain is automatically logged:
```
[ACTION] Looking for search box...
[ACTION] Found search box
[ACTION] Clicking search box...
[ACTION] Successfully clicked search box
[ACTION] Typing search query...
[ACTION] Successfully typed search query
[ACTION] Looking for search button...
[ACTION] Found search button
[ACTION] Clicking search button...
[ACTION] Successfully clicked search button
```

### 3. Execution Strategies

#### NESTED Strategy
Actions search within previous results:
```java
find(dialog)
    .then(find(okButton))  // Searches for button within dialog
    .then(click())
```

#### CONFIRM Strategy
Each action validates previous results:
```java
find(saveButton)
    .then(click())
    .then(waitToVanish())  // Confirms button disappeared
```

### 4. Advanced Patterns

- **Conditional Chains**: Execute different paths based on results
- **Error Recovery**: Handle failures gracefully
- **Parallel Chains**: Execute multiple chains concurrently
- **Dynamic Chains**: Build chains at runtime based on data

## Running the Example

1. Place UI element screenshots in `src/main/resources/images/`
2. Run: `./gradlew bootRun`
3. Watch the console for detailed chain execution logging

## Project Structure

```
action-chaining/
├── src/main/java/com/example/chaining/
│   ├── ActionChainingApplication.java    # Main application
│   ├── demos/
│   │   ├── BasicChainingDemo.java       # Simple chain examples
│   │   ├── AdvancedChainingDemo.java    # Complex patterns
│   │   └── ConditionalChainingDemo.java # Conditional execution
│   └── builders/
│       ├── ChainBuilder.java            # Custom chain builders
│       └── WorkflowBuilder.java         # Workflow patterns
└── src/main/resources/
    ├── application.yml                   # Configuration
    └── images/                          # UI element screenshots
```

## Example Chains

### 1. Search and Navigate
```java
// Each step is logged automatically
ActionConfig searchWorkflow = find(searchIcon)
    .withBeforeActionLog("Opening search...")
    .withSuccessLog("Search opened")
    .then(type(searchQuery))
    .withBeforeActionLog("Entering search term...")
    .withSuccessLog("Search term entered")
    .then(find(searchButton))
    .withBeforeActionLog("Looking for search button...")
    .then(click())
    .withSuccessLog("Search initiated")
    .then(waitFor(resultsPanel))
    .withSuccessLog("Results loaded")
    .build();
```

### 2. Form Submission Chain
```java
ActionConfig formChain = find(nameField)
    .then(click())
    .then(clearField())
    .then(type(userName))
    .then(find(emailField))
    .then(click())
    .then(clearField())
    .then(type(email))
    .then(find(submitButton))
    .then(click())
    .then(waitFor(successMessage))
    .build();
```

### 3. Menu Navigation Chain
```java
ActionConfig menuChain = find(menuButton)
    .then(click())
    .then(waitFor(menuPanel))
    .then(find(settingsItem))
    .then(hover())
    .then(waitFor(submenu))
    .then(find(profileOption))
    .then(click())
    .build();
```

## Logging Output Examples

### Successful Chain Execution
```
[INFO] Starting search workflow...
[ACTION] Opening search...
[ACTION] Search opened
[ACTION] Entering search term...
[ACTION] Successfully typed search query
[ACTION] Looking for search button...
[ACTION] Found search button
[ACTION] Clicking search button...
[ACTION] Search initiated
[ACTION] Waiting for results panel...
[ACTION] Results loaded
[INFO] Search workflow completed in 2.3s
```

### Chain with Recovery
```
[INFO] Executing form submission...
[ACTION] Looking for name field...
[ACTION] Name field not found - form may not be loaded
[INFO] Attempting recovery: waiting for form...
[ACTION] Waiting for form container...
[ACTION] Form container appeared
[ACTION] Looking for name field...
[ACTION] Found name field
[ACTION] Clicking name field...
[INFO] Recovery successful - continuing chain
```

## Customization

1. **Custom Chain Builders**: Create reusable chain patterns
2. **Dynamic Chains**: Build chains based on runtime conditions
3. **Parallel Execution**: Run multiple chains simultaneously
4. **Chain Composition**: Combine smaller chains into workflows