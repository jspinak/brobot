---
sidebar_position: 4
---

# States (Updated API)

There are 3 states in the demo: HOME, WORLD, and ISLAND. Each state describes a collection
of static images. The images are static in that they don't change their appearance
and always appear when the state is active. A state image's location on-screen may or may not change.

A state has an enum called Name that is used throughout the application to refer to the state. Every state
starts with an enum declaration.

This guide has been updated to use the new ActionConfig API introduced in Brobot 2.0.

## Home

The snapshot defined with the StateImageObject toWorldButton gives the location we 
expect to find this object. Snapshots represent possible locations for the image, and 
when there is more than one a match will be chosen randomly from all snapshots.   

When a StateImageObject is fixed, it is expected to be found in the same location every time.  

```java
@Component
@Getter
public class Home {
    
    public enum Name implements StateEnum {
        HOME
    }
    
    private StateImageObject toWorldButton = new StateImageObject.Builder()
            .withImage("toWorldButton")
            .isFixed()
            .addSnapshot(new MatchSnapshot(220, 600, 20, 20))
            .build();
    
    private State state = new State.Builder(HOME)
            .withImages(toWorldButton)
            .build();
    
    public Home(StateService stateService) { stateService.save(state); }
}
```

## World

If a StateImageObject has no snapshots, the locations of matches will be
determined using probabilities. 

```java
@Component
@Getter
public class World {
    
    public enum Name implements StateEnum {
        WORLD
    }
    
    private StateImageObject searchButton = new StateImageObject.Builder()
            .withImage("searchButton")
            .isFixed()
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
    
    private StateImageObject islandName = new StateImageObject.Builder()
            .withImage("castle", "mines", "farms", "forest", "mountains", "lakes")
            .called("island type text")
            .isFixed()
            .build();
            
    private StateRegion islandRegion = new StateRegion.Builder()
            .called("island region")
            .addSnapshot(new MatchSnapshot.Builder()
                    // NEW API: Use TextFindOptions for text finding
                    .setActionConfig(new TextFindOptions.Builder().build())
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

## Using Different ActionConfigs in Snapshots

With the new API, you can associate different types of action configurations with snapshots:

```java
@Component
@Getter
public class AdvancedState {
    
    public enum Name implements StateEnum {
        ADVANCED_STATE
    }
    
    // Example: Button with different click behaviors
    private StateImageObject multiActionButton = new StateImageObject.Builder()
            .withImage("button")
            .isFixed()
            // Snapshot for single click
            .addSnapshot(new MatchSnapshot.Builder()
                    .setActionConfig(new ClickOptions.Builder()
                            .setClickType(ClickOptions.Type.LEFT)
                            .build())
                    .setRegion(new Region(100, 100, 50, 30))
                    .build())
            // Snapshot for double click
            .addSnapshot(new MatchSnapshot.Builder()
                    .setActionConfig(new ClickOptions.Builder()
                            .setClickType(ClickOptions.Type.DOUBLE)
                            .setNumberOfClicks(2)
                            .build())
                    .setRegion(new Region(100, 100, 50, 30))
                    .build())
            .build();
    
    // Example: Text field with pattern finding
    private StateImageObject textField = new StateImageObject.Builder()
            .withImage("textField")
            .addSnapshot(new MatchSnapshot.Builder()
                    .setActionConfig(new PatternFindOptions.Builder()
                            .setStrategy(PatternFindOptions.Strategy.BEST)
                            .setSimilarity(0.95)
                            .build())
                    .setRegion(new Region(200, 200, 150, 25))
                    .build())
            .build();
    
    // Example: Region for text extraction with OCR
    private StateRegion textRegion = new StateRegion.Builder()
            .called("text extraction region")
            .setSearchRegion(new Region(300, 300, 200, 50))
            .addSnapshot(new MatchSnapshot.Builder()
                    .setActionConfig(new TextFindOptions.Builder()
                            .setLanguage("eng")
                            .setMaxMatchRetries(3)
                            .build())
                    .addString("Expected Text")
                    .addString("Alternative Text")
                    .build())
            .build();
    
    // Example: Draggable element
    private StateImageObject draggableItem = new StateImageObject.Builder()
            .withImage("draggable")
            .addSnapshot(new MatchSnapshot.Builder()
                    .setActionConfig(new DragOptions.Builder()
                            .setFromIndex(0)
                            .setToIndex(1)
                            .setPauseAfterEnd(1.0)
                            .build())
                    .setRegion(new Region(400, 400, 40, 40))
                    .build())
            .build();
    
    private State state = new State.Builder(Name.ADVANCED_STATE)
            .withImages(multiActionButton, textField, draggableItem)
            .withRegions(textRegion)
            .build();
    
    public AdvancedState(StateService stateService) { stateService.save(state); }
}
```

## Key Changes in the New API

1. **Specific Config Classes**: Use `TextFindOptions`, `ClickOptions`, `PatternFindOptions` instead of generic `ActionOptions`
2. **Type Safety**: Each config class only exposes relevant options
3. **Builder Pattern**: Consistent builder pattern across all config classes
4. **setActionConfig()**: Use this method instead of `setActionOptions()`

## Benefits for State Definition

- **Clearer Intent**: The config class name indicates what action the snapshot is for
- **Better Validation**: Config-specific validation ensures correct usage
- **Enhanced Documentation**: IDE can show relevant documentation for each config type
- **Future Extensibility**: New action types can be added without affecting existing states

## Migration Tips

When migrating state definitions:

1. Replace `setActionOptions(ActionOptions.Action.FIND, ActionOptions.Find.ALL_WORDS)` with `setActionConfig(new TextFindOptions.Builder().build())`
2. For click snapshots, use `ClickOptions` with appropriate click type
3. For pattern matching, use `PatternFindOptions` with desired strategy
4. For drag operations, use `DragOptions` with from/to indices