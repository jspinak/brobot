---
sidebar_position: 5
---

# Processes as Objects

Interacting with the GUI is not as simple as 'find' and 'click'. There are a lot of parameters involved. For example, a 'find' operation depends on settings such as the minimum similarity required for the match to be accepted, the max amount of time to search before giving up, and the region in which to search, as well as other parameters. Building a library such as Brobot while using processes as functions would create either an exploding web of methods or methods with extremely long parameter lists. It would be unwieldy to use and complicated to develop.

Processes as objects has an additional advantage in that it allows a interactive process to be treated as the combination of building blocks. This is important because brobot allows for the development of complex applications that react to their environments, in which the sequences of paths and actions taken are not known before the application is run. 

A good example of how this has affected the design choices taken is how pauses are used. Brobot makes a set of actions available to be used to create  applications. Pause, or wait, is not one of them. Instead, pauses can be selected as part of the action's options. Including pauses as options and not as a separate action was a deliberate design choice. Having a pause method allows the programmer to think in a more procedural manner: for example, do A and then wait a bit and then do B. Brobot's design incentivizes the programmer to think about the process as discrete process objects that can be combined and recombined in different configurations. Brobot provides a framework for a semi-intelligent automation and not just for automating a static process flow.

Treating processes as objects also simplifies the creation of new composite actions out of the more basic process building blocks. For example, the composite action ClickUntil, which performs clicks until a condition is reached, is much simpler to create with the standard process objects in Brobot.

## Core Classes in Brobot 1.1.0

The main classes enabling this object-oriented approach are:

- **ActionConfig Classes**: Type-safe configuration objects for specific actions (e.g., `PatternFindOptions`, `ClickOptions`, `DragOptions`). Each class contains only the options relevant to its action type.
- **ObjectCollection**: A collection of state objects that are used with the action.
- **ActionService**: Provides the appropriate action implementation based on the ActionConfig type.
- **ActionInterface**: The common interface for all actions, which take ActionConfig and ObjectCollection as parameters.
- **ActionResult**: Contains all the results of the action linked to the corresponding objects in the ObjectCollection.

## Example: Defining a Region

Here's an example that defines a region around a match using the new ActionConfig API:

```java
// Define options for finding the minimap
PatternFindOptions findOptions = new PatternFindOptions.Builder()
    .setSimilarity(0.6)
    .setStrategy(PatternFindOptions.Strategy.FIRST)
    .build();

// Create object collection with the minimap image
ObjectCollection mapImage = new ObjectCollection.Builder()
    .withImages(miniMap.getMap())
    .build();

// Find the minimap
ActionResult findResult = new ActionResult();
findResult.setActionConfig(findOptions);
ActionInterface findAction = actionService.getAction(findOptions);
findAction.perform(findResult, mapImage);

// Define a region around the found match
DefineRegionOptions defineOptions = new DefineRegionOptions.Builder()
    .setDefineAs(DefineRegionOptions.DefineAs.MATCH)
    .setOffsetX(-5)
    .setOffsetY(-1)
    .setWidth(296)
    .setHeight(255)
    .build();

// Use the matches from the previous find operation
ObjectCollection matchCollection = new ObjectCollection.Builder()
    .withMatches(findResult.getMatchList())
    .build();

// Define the region
ActionResult defineResult = new ActionResult();
defineResult.setActionConfig(defineOptions);
ActionInterface defineAction = actionService.getAction(defineOptions);
defineAction.perform(defineResult, matchCollection);
```

## Benefits of the New Type-Safe Approach

1. **Compile-Time Safety**: The compiler ensures you're using the right options for each action type.
2. **Better IDE Support**: Auto-completion shows only relevant options for each action.
3. **Clearer Intent**: The configuration class name immediately tells you what action will be performed.
4. **Easier Composition**: Different action configurations can be easily combined to create complex behaviors.

## Creating Complex Actions

The object-oriented approach makes it easy to create complex, reusable actions:

```java
public class SmartClick {
    private final ActionService actionService;
    
    public boolean clickWithRetry(StateImageObject target, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            // First, find the target
            PatternFindOptions findOptions = PatternFindOptions.forPreciseSearch();
            ActionResult findResult = performAction(findOptions, target);
            
            if (findResult.isSuccess()) {
                // Click on the found target
                ClickOptions clickOptions = new ClickOptions.Builder()
                    .setClickType(ClickOptions.Type.LEFT)
                    .setPauseAfterEnd(0.5)
                    .build();
                    
                ActionResult clickResult = performAction(clickOptions, 
                    findResult.getMatchList());
                
                if (clickResult.isSuccess()) {
                    return true;
                }
            }
            
            // If this isn't the last attempt, add a pause
            if (i < maxAttempts - 1) {
                // Use a find action with pause to wait between attempts
                PatternFindOptions waitOptions = new PatternFindOptions.Builder()
                    .setPauseAfterAction(1.0)  // 1 second pause
                    .setMaxMatchesToFind(0)     // Don't actually search
                    .build();
                performAction(waitOptions, target);
            }
        }
        return false;
    }
    
    private ActionResult performAction(ActionConfig config, Object data) {
        ActionResult result = new ActionResult();
        result.setActionConfig(config);
        
        ObjectCollection objects = createObjectCollection(data);
        ActionInterface action = actionService.getAction(config);
        action.perform(result, objects);
        
        return result;
    }
}
```

This design philosophy allows Brobot to provide a framework for semi-intelligent automation, not just for automating static process flows. The type-safe ActionConfig classes in version 2.0 make this approach even more powerful and easier to use.

For more information on migrating from the old ActionOptions API to the new ActionConfig API, see the [Migration Guide](/docs/core-library/guides/migration-guide).