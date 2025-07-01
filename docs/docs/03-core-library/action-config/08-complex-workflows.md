---
sidebar_position: 8
title: Complex Workflows
description: Build sophisticated multi-step automation workflows using ActionChainOptions
---

# Complex Workflows with Action Chains

This guide demonstrates how to build sophisticated automation workflows by combining multiple actions into complex sequences. These patterns are essential for real-world automation scenarios.

## Form Automation Example

One of the most common automation tasks is filling out forms. Here's a comprehensive example:

```java
public ActionResult fillLoginForm(StateImage usernameField, String username,
                                  StateImage passwordField, String password,
                                  StateImage loginButton) {
    
    ActionChainOptions loginChain = new ActionChainOptions.Builder(
        // Step 1: Click username field
        new ClickOptions.Builder()
            .setPauseAfterEnd(0.2)
            .build())
        // Step 2: Type username
        .then(new TypeOptions.Builder()
            .setTypeDelay(0.05)
            .build())
        // Step 3: Click password field
        .then(new ClickOptions.Builder()
            .setPauseAfterEnd(0.2)
            .build())
        // Step 4: Type password
        .then(new TypeOptions.Builder()
            .setTypeDelay(0.05)
            .build())
        // Step 5: Click login button
        .then(new ClickOptions.Builder()
            .setPauseAfterEnd(1.0) // Wait for login to process
            .build())
        .build();
    
    // Execute with the appropriate ObjectCollections for each step
    return chainExecutor.executeChain(loginChain, new ActionResult(),
        usernameField.asObjectCollection(),
        new ObjectCollection.Builder().withStrings(username).build(),
        passwordField.asObjectCollection(),
        new ObjectCollection.Builder().withStrings(password).build(),
        loginButton.asObjectCollection()
    );
}
```

### Key Points:
- Each action in the chain corresponds to a specific UI interaction
- Delays are added to allow the UI to respond
- Object collections are passed in the same order as the actions

## Multi-Step Navigation

Navigate through complex UI hierarchies:

```java
public ActionResult navigateToSettings(StateImage menuButton, 
                                     StateImage settingsMenu,
                                     StateImage securityTab) {
    
    ActionChainOptions navigationChain = new ActionChainOptions.Builder(
        // Open main menu
        new ClickOptions.Builder()
            .setPauseAfterEnd(0.5)
            .build())
        // Find and click settings in menu
        .then(new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.FIRST)
            .build())
        .then(new ClickOptions.Builder()
            .setPauseAfterEnd(0.5)
            .build())
        // Find and click security tab
        .then(new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.FIRST)
            .build())
        .then(new ClickOptions.Builder()
            .setPauseAfterEnd(0.3)
            .build())
        .build();
    
    return chainExecutor.executeChain(navigationChain, new ActionResult(),
        menuButton.asObjectCollection(),
        settingsMenu.asObjectCollection(),
        settingsMenu.asObjectCollection(), // Click the found menu item
        securityTab.asObjectCollection(),
        securityTab.asObjectCollection()  // Click the found tab
    );
}
```

## Complex Interaction Workflow

This example shows a workflow that combines finding, hovering, clicking, and verification:

```java
public ActionResult complexWorkflow(StateImage target, StateImage expectedResult) {
    
    ActionChainOptions workflow = new ActionChainOptions.Builder(
        // Step 1: Find the target element
        new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .setSimilarity(0.8)
            .build())
        // Step 2: Move mouse to target (hover effect)
        .then(new MouseMoveOptions.Builder()
            .setPauseAfterEnd(0.5) // Hold hover
            .build())
        // Step 3: Click the target
        .then(new ClickOptions.Builder()
            .setPauseAfterEnd(0.3)
            .build())
        // Step 4: Verify the expected result appears
        .then(new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.FIRST)
            .setPauseBeforeBegin(2.0) // Wait for UI update
            .build())
        .build();
    
    // Note: Same target is used for find, move, and click
    return chainExecutor.executeChain(workflow, new ActionResult(),
        target.asObjectCollection(),
        target.asObjectCollection(), // For mouse move
        target.asObjectCollection(), // For click
        expectedResult.asObjectCollection()
    );
}
```

## Drag and Drop Operations

While Brobot has a dedicated Drag action, you can also build custom drag operations:

```java
public ActionResult customDragDrop(StateImage source, StateImage target) {
    
    ActionChainOptions dragChain = new ActionChainOptions.Builder(
        // Find source element
        new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .build())
        // Move to source
        .then(new MouseMoveOptions.Builder()
            .setPauseAfterEnd(0.1)
            .build())
        // Mouse down
        .then(new MouseDownOptions.Builder()
            .setPauseAfterEnd(0.2)
            .build())
        // Find target location
        .then(new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .build())
        // Move to target (while holding)
        .then(new MouseMoveOptions.Builder()
            .setPauseAfterEnd(0.1)
            .build())
        // Release mouse
        .then(new MouseUpOptions.Builder()
            .setPauseAfterEnd(0.5)
            .build())
        .build();
    
    return chainExecutor.executeChain(dragChain, new ActionResult(),
        source.asObjectCollection(),
        source.asObjectCollection(), // For mouse move
        source.asObjectCollection(), // For mouse down
        target.asObjectCollection(), // Find target
        target.asObjectCollection(), // Move to target
        target.asObjectCollection()  // Mouse up at target
    );
}
```

## Dynamic Workflow Building

Build workflows dynamically based on runtime conditions:

```java
public class DynamicWorkflowBuilder {
    
    public ActionChainOptions buildWorkflow(List<WorkflowStep> steps) {
        if (steps.isEmpty()) {
            throw new IllegalArgumentException("Workflow must have at least one step");
        }
        
        // Start with the first step
        ActionChainOptions.Builder builder = new ActionChainOptions.Builder(
            steps.get(0).toActionConfig()
        );
        
        // Add remaining steps
        for (int i = 1; i < steps.size(); i++) {
            builder.then(steps.get(i).toActionConfig());
        }
        
        return builder.build();
    }
    
    // Example workflow step
    public static class WorkflowStep {
        private final String actionType;
        private final Map<String, Object> parameters;
        
        public ActionConfig toActionConfig() {
            switch (actionType) {
                case "click":
                    return new ClickOptions.Builder()
                        .setPauseAfterEnd((Double) parameters.getOrDefault("pause", 0.3))
                        .build();
                case "type":
                    return new TypeOptions.Builder()
                        .setTypeDelay((Double) parameters.getOrDefault("delay", 0.05))
                        .build();
                case "find":
                    return new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.valueOf(
                            (String) parameters.getOrDefault("strategy", "FIRST")))
                        .build();
                default:
                    throw new IllegalArgumentException("Unknown action type: " + actionType);
            }
        }
    }
}
```

## Error Handling in Workflows

Handle errors gracefully in complex workflows:

```java
public ActionResult robustWorkflow(StateImage element1, StateImage element2) {
    
    // Build the main workflow
    ActionChainOptions mainWorkflow = new ActionChainOptions.Builder(
        new PatternFindOptions.Builder().build())
        .then(new ClickOptions.Builder().build())
        .then(new PatternFindOptions.Builder().build())
        .build();
    
    // Execute with error handling
    ActionResult result = chainExecutor.executeChain(
        mainWorkflow, new ActionResult(),
        element1.asObjectCollection(),
        element1.asObjectCollection(),
        element2.asObjectCollection()
    );
    
    // Check intermediate results if needed
    if (!result.isSuccess()) {
        // Try alternative workflow
        ActionChainOptions fallbackWorkflow = new ActionChainOptions.Builder(
            new ClickOptions.Builder()
                .setPauseAfterEnd(1.0) // Longer pause
                .build())
            .then(new PatternFindOptions.Builder()
                .setSimilarity(0.7) // Lower threshold
                .build())
            .build();
        
        result = chainExecutor.executeChain(
            fallbackWorkflow, new ActionResult(),
            element1.asObjectCollection(),
            element2.asObjectCollection()
        );
    }
    
    return result;
}
```

## Best Practices for Complex Workflows

1. **Break down complexity**: Divide large workflows into smaller, reusable chains
2. **Add appropriate delays**: UI elements need time to respond
3. **Use meaningful variable names**: Make your workflow self-documenting
4. **Consider failure points**: Build in fallback strategies
5. **Log intermediate results**: Helps with debugging complex chains
6. **Test incrementally**: Build and test workflows step by step

## Performance Considerations

- **Minimize waits**: Use smart waiting strategies instead of fixed delays
- **Reuse search results**: Use NESTED strategy when searching within found elements
- **Batch similar actions**: Group similar operations when possible
- **Profile your workflows**: Identify and optimize bottlenecks

## Next Steps

- Learn about [Conditional Actions](./09-conditional-actions) for dynamic workflows
- Explore [Form Automation](./10-form-automation) patterns
- See [Reusable Patterns](./11-reusable-patterns) for building a library of common workflows