---
sidebar_position: 5
---

# Processes as Objects

Interacting with the GUI is not as simple as 'find' and 'click'. There are a lot of parameters involved. For example, a 'find' operation depends on settings such as the minimum similarity required for the match to be accepted, the max amount of time to search before giving up, and the region in which to search, as well as other parameters. Building a library such as Brobot while using processes as functions would create either an exploding web of methods or methods with extremely long parameter lists. It would be unwieldy to use and complicated to develop.

Treating processes as objects has an additional advantage in that it allows an interactive process to be treated as the combination of building blocks. This is important because Brobot allows for the development of complex applications that react to their environments, in which the sequences of paths and actions taken are not known before the application is run. 

A good example of how this has affected the design choices taken is how pauses are used. Brobot makes a set of actions available for creating applications. Pause, or wait, is not one of them. Instead, pauses can be selected as part of the action's options. Including pauses as options and not as a separate action was a deliberate design choice. Having a pause method allows the programmer to think in a more procedural manner: for example, do A and then wait a bit and then do B. Brobot's design incentivizes the programmer to think about the process as discrete process objects that can be combined and recombined in different configurations. Brobot provides a framework for a semi-intelligent automation and not just for automating a static process flow.

Treating processes as objects also simplifies the creation of new composite actions out of the more basic process building blocks. For example, the composite action ClickUntil, which performs clicks until a condition is reached, is much simpler to create with the standard process objects in Brobot.

## Core Classes in Brobot 1.1.0

The main classes enabling this object-oriented approach are:

- **ActionConfig Classes**: Type-safe configuration objects for specific actions (e.g., `PatternFindOptions`, `ClickOptions`, `DragOptions`). Each class contains only the options relevant to its action type.
- **ObjectCollection**: A collection of state objects that are used with the action.
- **ActionService**: Provides the appropriate action implementation based on the ActionConfig type.
- **ActionInterface**: The common interface for all actions, which take ActionConfig and ObjectCollection as parameters.
- **ActionResult**: Contains all the results of the action linked to the corresponding objects in the ObjectCollection.

## Example: Defining a Region

Often in GUI automation, you need to define a region dynamically based on where elements appear on screen. For example, you might want to define a capture region around a minimap that appears in different positions depending on the game or application state.

Here's a complete example that defines a region around a match using the modern ActionConfig API:

```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.region.DefineRegionOptions;
import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.model.ObjectCollection;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MinimapRegionDefiner {
    private final Action action;

    @Autowired
    public MinimapRegionDefiner(Action action) {
        this.action = action;
    }

    /**
     * Defines a region around the minimap with specific dimensions.
     * This is useful for capturing or searching within a consistent area
     * relative to a dynamic UI element.
     *
     * @return The defined region, or null if the minimap wasn't found
     */
    public Region defineMinimapRegion() {
        // Create the minimap image to search for
        StateImage minimapImage = new StateImage.Builder()
            .addPatterns("minimap.png")
            .setName("Minimap")
            .build();

        // Define options for finding the minimap
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setSimilarity(0.6)
            .setStrategy(PatternFindOptions.Strategy.FIRST)
            .build();

        // Create object collection with the minimap image
        ObjectCollection mapImage = new ObjectCollection.Builder()
            .withImages(minimapImage)
            .build();

        // Find the minimap
        ActionResult findResult = action.perform(findOptions, mapImage);

        if (!findResult.isSuccess()) {
            log.error("Could not find minimap");
            return null;
        }

        // Define a region around the found match with adjustments
        DefineRegionOptions defineOptions = new DefineRegionOptions.Builder()
            .setDefineAs(DefineRegionOptions.DefineAs.MATCH)
            .setMatchAdjustment(MatchAdjustmentOptions.builder()
                .setAddX(-5)        // Offset x by -5 pixels
                .setAddY(-1)        // Offset y by -1 pixel
                .setAbsoluteW(296)  // Set absolute width to 296 pixels
                .setAbsoluteH(255)  // Set absolute height to 255 pixels
                .build())
            .build();

        // Use the matches from the previous find operation
        ObjectCollection matchCollection = new ObjectCollection.Builder()
            .withMatches(findResult.getMatchList())
            .build();

        // Define the region
        ActionResult defineResult = action.perform(defineOptions, matchCollection);

        if (defineResult.isSuccess()) {
            Region definedRegion = defineResult.getDefinedRegion();
            log.info("Successfully defined minimap region: {}", definedRegion);
            return definedRegion;
        } else {
            log.error("Failed to define region around minimap");
            return null;
        }
    }
}
```

> **Note on Builder Conventions**: Different Brobot classes use different builder prefixes:
> - `ObjectCollection.Builder()` uses `with-` prefix: `.withImages()`, `.withMatches()`, `.withRegions()`
> - `ActionConfig` subclasses use `set-` prefix: `.setSimilarity()`, `.setDefineAs()`, `.setStrategy()`
> - Lombok-generated builders use `set-` prefix: `.setAddX()`, `.setAbsoluteW()`

## Benefits of the New Type-Safe Approach

1. **Compile-Time Safety**: The compiler ensures you're using the right options for each action type.
2. **Better IDE Support**: Auto-completion shows only relevant options for each action.
3. **Clearer Intent**: The configuration class name immediately tells you what action will be performed.
4. **Easier Composition**: Different action configurations can be easily combined to create complex behaviors.

## Creating Complex Actions

The object-oriented approach makes it easy to create complex, reusable actions:

```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.model.ObjectCollection;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.state.StateImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SmartClick {
    private final Action action;

    @Autowired
    public SmartClick(Action action) {
        this.action = action;
    }

    public boolean clickWithRetry(StateImage target, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            // Configure find options with pause if this is a retry
            PatternFindOptions.Builder findBuilder = new PatternFindOptions.Builder()
                .setSimilarity(0.85);

            if (i > 0) {
                findBuilder.setPauseBeforeBegin(1.0);  // 1 second pause before retry
            }

            PatternFindOptions findOptions = findBuilder.build();

            ObjectCollection targetCollection = new ObjectCollection.Builder()
                .withImages(target)
                .build();

            ActionResult findResult = action.perform(findOptions, targetCollection);

            if (findResult.isSuccess()) {
                // Click on the found target
                ClickOptions clickOptions = new ClickOptions.Builder()
                    .setPauseAfterEnd(0.5)
                    .build();

                ObjectCollection matchCollection = new ObjectCollection.Builder()
                    .withMatches(findResult.getMatchList())
                    .build();

                ActionResult clickResult = action.perform(clickOptions, matchCollection);

                if (clickResult.isSuccess()) {
                    return true;
                }
            }
        }
        return false;
    }
}
```

This design philosophy allows Brobot to provide a framework for semi-intelligent automation, not just for automating static process flows. The type-safe ActionConfig classes in version 1.1.0 make this approach even more powerful and easier to use.

## Related Documentation

- **[Migration Guide](../../action-config/migration-guide)** - Migrating from ActionOptions to ActionConfig
- **[Search Regions and Fixed Locations](./search-regions-and-fixed-locations.md)** - Comprehensive guide to region definition
- **[Declarative Region Definition](./declarative-region-definition.md)** - Define regions relative to other elements
- **[ActionConfig API Reference](../../action-config/overview.md)** - Complete ActionConfig documentation
- **[Pausing in Brobot](/docs/getting-started/quick-start#important-pausing-in-brobot)** - Best practices for pauses