---
sidebar_position: 4
---

# States

:::info Version Note
This tutorial was originally created for an earlier version of Brobot but has been updated for version 1.1.0. The original code examples are available in documentation versions 1.0.6 and 1.0.7.
:::

There are 3 states in the demo: HOME, WORLD, and ISLAND. Each state describes a collection
of static images. The images are static in that they don't change their appearance
and always appear when the state is active. A state image's location on-screen may or may not change.

A state has an enum called Name that is used throughout the application to refer to the state. Every state
starts with an enum declaration.

## Home

The snapshot defined with the StateImage toWorldButton gives the location we 
expect to find this object. Snapshots represent possible locations for the image, and 
when there is more than one a match will be chosen randomly from all snapshots.   

When a StateImage is fixed, it is expected to be found in the same location every time.  

```java
@Component
@Getter
public class Home {
    
    public enum Name implements StateEnum {
        HOME
    }
    
    private final StateImage toWorldButton = new StateImage.Builder()
            .addPatterns("toWorldButton")
            .setFixed(true)
            .addSnapshot(new MatchSnapshot(220, 600, 20, 20))
            .build();
    
    private final State state = new State.Builder(Name.HOME)
            .withImages(toWorldButton)
            .build();
    
    public Home(StateService stateService) { stateService.save(state); }
}
```

## World

If a StateImage has no snapshots, the locations of matches will be
determined using probabilities. 

```java
@Component
@Getter
public class World {
    
    public enum Name implements StateEnum {
        WORLD
    }
    
    private final StateImage searchButton = new StateImage.Builder()
            .addPatterns("searchButton")
            .setFixed(true)
            .build();
    
    private State state = new State.Builder(Name.WORLD)
            .withImages(searchButton)
            .build();
    
    public World(StateService stateService) { stateService.save(state); }
}
```

## Island

Images can be defined with multiple image files.  

The action GetText references only GetText snapshots. Every snapshot
has an action associated with it; default snapshots (without an explicitly defined
action) are built as the result of Find operations. A GetText snapshot for a StateRegion
gives the expected text to find when performing a GetText action on the StateRegion. 
In our GetText snapshot, we have included a few different expected text results. Some of
them are misspelled to simulate the stochasticity of real execution, in which text is 
not always found as it appears on-screen.  

```java
@Component
@Getter
public class Island {
    
    public enum Name implements StateEnum {
        ISLAND
    }
    
    private final StateImage islandName = new StateImage.Builder()
            .addPatterns("castle", "mines", "farms", "forest", "mountains", "lakes")
            .setName("island type text")
            .setFixed(true)
            .build();
    private StateRegion islandRegion = new StateRegion.Builder()
            .setName("island region")
            .addSnapshot(new MatchSnapshot.Builder()
                    .setActionConfig(new TextFindOptions.Builder()
                            .setLanguage("eng")
                            .build())
                    .addString("Mines")
                    .addString("Lakess")
                    .addString("Farmz")
                    .build())
            .build();
    
    private State state = new State.Builder(Name.ISLAND)
            .withImages(islandName)
            .withRegions(islandRegion)
            .build();
    
    public Island(StateService stateService) { stateService.save(state); }
}
```

## Key Changes in v1.1.0

The main change in this example is the migration from `ActionOptions` to the new type-safe `ActionConfig` API:

- **Old**: `.setActionOptions(ActionOptions.Action.FIND, ActionOptions.Find.ALL_WORDS)`
- **New**: `.setActionConfig(new TextFindOptions.Builder().build())`

The `TextFindOptions` class is specifically designed for text/OCR operations and automatically uses the appropriate strategy for finding text. This provides better type safety and clearer intent in your code.

For more information about the new ActionConfig API, see the [Migration Guide](/docs/core-library/guides/migration-guide).