---
sidebar_position: 11
title: Reusable Patterns
description: Build a library of reusable automation patterns and components
---

# Reusable Automation Patterns

Creating reusable patterns is key to building maintainable and scalable automation solutions. This guide shows how to create modular, composable automation components that can be shared across projects.

## Pattern Library Architecture

### Base Pattern Interface

Start with a common interface for all patterns:

```java
public interface AutomationPattern {
    String getName();
    String getDescription();
    boolean execute(PatternContext context);
    boolean validate(PatternContext context);
}

public class PatternContext {
    private final Map<String, Object> parameters = new HashMap<>();
    private final Map<String, StateImage> images = new HashMap<>();
    private final Map<String, ObjectCollection> collections = new HashMap<>();
    
    public PatternContext withParameter(String key, Object value) {
        parameters.put(key, value);
        return this;
    }
    
    public PatternContext withImage(String key, StateImage image) {
        images.put(key, image);
        return this;
    }
    
    public PatternContext withCollection(String key, ObjectCollection collection) {
        collections.put(key, collection);
        return this;
    }
    
    // Getters with type safety
    @SuppressWarnings("unchecked")
    public <T> T getParameter(String key, Class<T> type) {
        return (T) parameters.get(key);
    }
    
    public StateImage getImage(String key) {
        return images.get(key);
    }
    
    public ObjectCollection getCollection(String key) {
        return collections.get(key);
    }
}
```

## Common UI Patterns

### Login Pattern

A reusable login pattern that works across different applications:

```java
public class LoginPattern implements AutomationPattern {
    private static final String USERNAME_FIELD = "usernameField";
    private static final String PASSWORD_FIELD = "passwordField";
    private static final String LOGIN_BUTTON = "loginButton";
    private static final String SUCCESS_INDICATOR = "successIndicator";
    
    @Override
    public String getName() {
        return "Standard Login Flow";
    }
    
    @Override
    public String getDescription() {
        return "Handles username/password login with success verification";
    }
    
    @Override
    public boolean validate(PatternContext context) {
        // Validate required elements
        return context.getImage(USERNAME_FIELD) != null &&
               context.getImage(PASSWORD_FIELD) != null &&
               context.getImage(LOGIN_BUTTON) != null &&
               context.getParameter("username", String.class) != null &&
               context.getParameter("password", String.class) != null;
    }
    
    @Override
    public boolean execute(PatternContext context) {
        if (!validate(context)) {
            throw new IllegalArgumentException("Invalid context for LoginPattern");
        }
        
        String username = context.getParameter("username", String.class);
        String password = context.getParameter("password", String.class);
        
        // Build the login chain
        ActionChainOptions loginChain = new ActionChainOptions.Builder(
            // Click username field
            new ClickOptions.Builder()
                .setPauseAfterEnd(0.3)
                .build())
            // Clear and type username
            .then(new TypeOptions.Builder()
                .setText(username)
                .setClearFirst(true)
                .build())
            // Tab to password field
            .then(new TypeOptions.Builder()
                .setText("\t")
                .build())
            // Type password
            .then(new TypeOptions.Builder()
                .setText(password)
                .build())
            // Click login button
            .then(new ClickOptions.Builder()
                .setPauseAfterEnd(1.0)
                .build())
            .build();
        
        // Execute the chain
        ActionResult result = executeChain(loginChain,
            context.getImage(USERNAME_FIELD).asObjectCollection(),
            new ObjectCollection.Builder().withStrings(username).build(),
            new ObjectCollection.Builder().withStrings("\t").build(),
            new ObjectCollection.Builder().withStrings(password).build(),
            context.getImage(LOGIN_BUTTON).asObjectCollection()
        );
        
        if (!result.isSuccess()) {
            return false;
        }
        
        // Verify success if indicator provided
        StateImage successIndicator = context.getImage(SUCCESS_INDICATOR);
        if (successIndicator != null) {
            return waitForImage(successIndicator, 5.0);
        }
        
        return true;
    }
}
```

### Search Pattern

A flexible search pattern for various search interfaces:

```java
public class SearchPattern implements AutomationPattern {
    
    @Override
    public boolean execute(PatternContext context) {
        StateImage searchField = context.getImage("searchField");
        StateImage searchButton = context.getImage("searchButton");
        String searchTerm = context.getParameter("searchTerm", String.class);
        boolean pressEnter = context.getParameter("pressEnter", Boolean.class);
        
        // Click search field
        if (!click(searchField)) {
            return false;
        }
        
        // Clear existing text
        clearField();
        
        // Type search term
        if (!type(searchTerm)) {
            return false;
        }
        
        // Submit search
        if (pressEnter) {
            return type("\n");
        } else if (searchButton != null) {
            return click(searchButton);
        }
        
        return false;
    }
    
    private void clearField() {
        // Ctrl+A and Delete
        ActionChainOptions clearChain = new ActionChainOptions.Builder(
            new KeyDownOptions.Builder()
                .setKey("ctrl")
                .build())
            .then(new TypeOptions.Builder()
                .setText("a")
                .build())
            .then(new KeyUpOptions.Builder()
                .setKey("ctrl")
                .build())
            .then(new TypeOptions.Builder()
                .setText("\b")
                .build())
            .build();
        
        executeChain(clearChain);
    }
}
```

### Menu Navigation Pattern

Navigate through hierarchical menus:

```java
public class MenuNavigationPattern implements AutomationPattern {
    
    @Override
    public boolean execute(PatternContext context) {
        List<String> menuPath = context.getParameter("menuPath", List.class);
        Double pauseBetween = context.getParameter("pauseBetween", Double.class);
        
        if (pauseBetween == null) {
            pauseBetween = 0.5;
        }
        
        // Build dynamic chain for menu navigation
        ActionChainOptions.Builder chainBuilder = null;
        
        for (int i = 0; i < menuPath.size(); i++) {
            String menuItem = menuPath.get(i);
            StateImage menuImage = context.getImage(menuItem);
            
            if (menuImage == null) {
                logger.error("Menu item not found: {}", menuItem);
                return false;
            }
            
            // First action
            if (chainBuilder == null) {
                chainBuilder = new ActionChainOptions.Builder(
                    new ClickOptions.Builder()
                        .setPauseAfterEnd(pauseBetween)
                        .build()
                );
            } else {
                // Subsequent actions
                chainBuilder.then(new ClickOptions.Builder()
                    .setPauseAfterEnd(pauseBetween)
                    .build());
            }
        }
        
        if (chainBuilder == null) {
            return false;
        }
        
        // Execute the navigation chain
        return executeMenuChain(chainBuilder.build(), menuPath, context);
    }
}
```

## Composite Patterns

### Form Submission Pattern

Combines multiple patterns for complex form handling:

```java
public class FormSubmissionPattern implements AutomationPattern {
    private final Map<String, FieldPattern> fieldPatterns = new HashMap<>();
    
    public FormSubmissionPattern() {
        // Register field type handlers
        fieldPatterns.put("text", new TextFieldPattern());
        fieldPatterns.put("dropdown", new DropdownPattern());
        fieldPatterns.put("checkbox", new CheckboxPattern());
        fieldPatterns.put("radio", new RadioButtonPattern());
    }
    
    @Override
    public boolean execute(PatternContext context) {
        List<FieldDefinition> fields = context.getParameter("fields", List.class);
        StateImage submitButton = context.getImage("submitButton");
        
        // Process each field
        for (FieldDefinition field : fields) {
            FieldPattern pattern = fieldPatterns.get(field.getType());
            
            if (pattern == null) {
                logger.error("Unknown field type: {}", field.getType());
                return false;
            }
            
            // Create field context
            PatternContext fieldContext = new PatternContext()
                .withImage("field", field.getImage())
                .withParameter("value", field.getValue())
                .withParameter("options", field.getOptions());
            
            if (!pattern.execute(fieldContext)) {
                logger.error("Failed to fill field: {}", field.getName());
                return false;
            }
        }
        
        // Submit form
        return click(submitButton);
    }
    
    public static class FieldDefinition {
        private final String name;
        private final String type;
        private final StateImage image;
        private final Object value;
        private final Map<String, Object> options;
        
        // Constructor and getters...
    }
}
```

## Pattern Factories

### Dynamic Pattern Creation

Create patterns dynamically based on configuration:

```java
public class PatternFactory {
    private final Map<String, Class<? extends AutomationPattern>> patternRegistry;
    
    public PatternFactory() {
        patternRegistry = new HashMap<>();
        registerDefaultPatterns();
    }
    
    private void registerDefaultPatterns() {
        register("login", LoginPattern.class);
        register("search", SearchPattern.class);
        register("menu", MenuNavigationPattern.class);
        register("form", FormSubmissionPattern.class);
    }
    
    public void register(String name, Class<? extends AutomationPattern> patternClass) {
        patternRegistry.put(name, patternClass);
    }
    
    public AutomationPattern create(String name) {
        Class<? extends AutomationPattern> patternClass = patternRegistry.get(name);
        
        if (patternClass == null) {
            throw new IllegalArgumentException("Unknown pattern: " + name);
        }
        
        try {
            return patternClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create pattern: " + name, e);
        }
    }
    
    public AutomationPattern createFromConfig(PatternConfig config) {
        AutomationPattern pattern = create(config.getType());
        
        // Apply any custom configuration
        if (pattern instanceof ConfigurablePattern) {
            ((ConfigurablePattern) pattern).configure(config);
        }
        
        return pattern;
    }
}
```

## Pattern Composition

### Sequential Pattern Executor

Execute multiple patterns in sequence:

```java
public class SequentialPatternExecutor {
    
    public boolean executeSequence(List<PatternExecution> executions) {
        for (PatternExecution execution : executions) {
            logger.info("Executing pattern: {}", execution.getPattern().getName());
            
            if (!execution.getPattern().execute(execution.getContext())) {
                logger.error("Pattern failed: {}", execution.getPattern().getName());
                
                if (!execution.isContinueOnFailure()) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    public static class PatternExecution {
        private final AutomationPattern pattern;
        private final PatternContext context;
        private final boolean continueOnFailure;
        
        // Constructor and getters...
    }
}
```

### Conditional Pattern Executor

Execute patterns based on conditions:

```java
public class ConditionalPatternExecutor {
    
    public boolean executeConditional(ConditionalExecution execution) {
        // Evaluate condition
        if (!execution.getCondition().evaluate()) {
            // Execute else pattern if provided
            if (execution.getElsePattern() != null) {
                return execution.getElsePattern().execute(execution.getContext());
            }
            return true; // No else pattern, consider success
        }
        
        // Execute main pattern
        return execution.getPattern().execute(execution.getContext());
    }
    
    public interface PatternCondition {
        boolean evaluate();
    }
    
    public static class ImageExistsCondition implements PatternCondition {
        private final StateImage image;
        
        @Override
        public boolean evaluate() {
            return find(image);
        }
    }
}
```

## Testing Patterns

### Pattern Test Framework

```java
public abstract class PatternTest {
    protected PatternFactory factory;
    protected MockActionExecutor mockExecutor;
    
    @Before
    public void setUp() {
        factory = new PatternFactory();
        mockExecutor = new MockActionExecutor();
    }
    
    protected void assertPatternSuccess(String patternName, PatternContext context) {
        AutomationPattern pattern = factory.create(patternName);
        assertTrue("Pattern validation failed", pattern.validate(context));
        assertTrue("Pattern execution failed", pattern.execute(context));
    }
    
    protected void assertPatternFailure(String patternName, PatternContext context) {
        AutomationPattern pattern = factory.create(patternName);
        assertFalse("Pattern should have failed", pattern.execute(context));
    }
}

public class LoginPatternTest extends PatternTest {
    
    @Test
    public void testSuccessfulLogin() {
        PatternContext context = new PatternContext()
            .withImage("usernameField", mockImage("username"))
            .withImage("passwordField", mockImage("password"))
            .withImage("loginButton", mockImage("login"))
            .withParameter("username", "testuser")
            .withParameter("password", "testpass");
        
        assertPatternSuccess("login", context);
    }
}
```

## Best Practices

### 1. Design for Reusability
- Use generic parameter names
- Provide sensible defaults
- Make patterns configurable

### 2. Implement Validation
```java
@Override
public boolean validate(PatternContext context) {
    // Check all required parameters
    // Verify image availability
    // Validate parameter types
    return true;
}
```

### 3. Handle Errors Gracefully
```java
try {
    return executePattern(context);
} catch (Exception e) {
    logger.error("Pattern execution failed", e);
    return handlePatternError(e);
}
```

### 4. Document Patterns
```java
@PatternDoc(
    name = "Login Pattern",
    description = "Handles standard username/password login flows",
    requiredImages = {"usernameField", "passwordField", "loginButton"},
    requiredParams = {"username", "password"},
    optionalImages = {"successIndicator"},
    example = "context.withParameter('username', 'user').withParameter('password', 'pass')"
)
public class LoginPattern implements AutomationPattern {
    // Implementation
}
```

### 5. Version Patterns
```java
public interface VersionedPattern extends AutomationPattern {
    String getVersion();
    boolean isCompatibleWith(String version);
}
```

## Next Steps

- Explore [Migration Guide](./12-migration-guide.md) for updating legacy patterns