---
sidebar_position: 5
---

# Transitions

Transitions allow Brobot to move from one state to another. Any state that 
will be accessed needs a StateTransitions class.  

## Home Transitions

```java
@Component
public class HomeTransitions {
    
    private final ActionService actionService;
    private final Home home;
    
    private StateTransitions transitions;
    
    public HomeTransitions(StateTransitionsRepository stateTransitionsRepository,
                           ActionService actionService, Home home) {
        this.actionService = actionService;
        this.home = home;
        transitions = new StateTransitions.Builder(HOME)
                .addTransition(this::goToWorld, WORLD)
                .build();
        stateTransitionsRepository.add(transitions);
    }
    
    private boolean goToWorld() {
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .build();
        
        ActionResult result = new ActionResult();
        result.setActionConfig(clickOptions);
        
        ObjectCollection objects = new ObjectCollection.Builder()
                .withImages(home.getToWorldButton())
                .build();
        
        ActionInterface clickAction = actionService.getAction(clickOptions);
        clickAction.perform(result, objects);
        
        return result.isSuccess();
    }
}
```


## World Transitions

We use the builder for the transition from World to Island since it requires
a special option: in this transition the World state stays visible.  

```java
@Component
public class WorldTransitions {
    
    private final ActionService actionService;
    private final World world;
    
    private StateTransitions transitions;
    
    public WorldTransitions(StateTransitionsRepository stateTransitionsRepository,
                            ActionService actionService, World world) {
        this.actionService = actionService;
        this.world = world;
        transitions = new StateTransitions.Builder(WORLD)
                .addTransitionFinish(this::finishTransition)
                .addTransition(new StateTransition.Builder()
                        .addToActivate(ISLAND)
                        .setFunction(this::goToIsland)
                        .setStaysVisibleAfterTransition(TRUE)
                        .build())
                .build();
        stateTransitionsRepository.add(transitions);
    }
    
    private boolean finishTransition() {
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();
        
        ActionResult result = new ActionResult();
        result.setActionConfig(findOptions);
        
        ObjectCollection worldImages = new ObjectCollection.Builder()
                .withAllStateImages(world.getState())
                .build();
        
        ActionInterface findAction = actionService.getAction(findOptions);
        findAction.perform(result, worldImages);
        
        return result.isSuccess();
    }
    
    public boolean goToIsland() {
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .setNumberOfClicks(2)
                .setPauseBetweenClicks(0.2)
                .build();
        
        ActionResult result = new ActionResult();
        result.setActionConfig(clickOptions);
        
        ObjectCollection searchButton = new ObjectCollection.Builder()
                .withImages(world.getSearchButton())
                .build();
        
        ActionInterface clickAction = actionService.getAction(clickOptions);
        clickAction.perform(result, searchButton);
        
        return result.isSuccess();
    }
}
```

## Island Transitions

```java
@Component
public class IslandTransitions {
    
    private final ActionService actionService;
    private final Island island;
    
    private StateTransitions transitions;
    
    public IslandTransitions(StateTransitionsRepository stateTransitionsRepository,
                             ActionService actionService, Island island) {
        this.actionService = actionService;
        this.island = island;
        transitions = new StateTransitions.Builder(ISLAND)
                .addTransitionFinish(this::finishTransition)
                .build();
        stateTransitionsRepository.add(transitions);
    }
    
    private boolean finishTransition() {
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();
        
        ActionResult result = new ActionResult();
        result.setActionConfig(findOptions);
        
        ObjectCollection worldImages = new ObjectCollection.Builder()
                .withAllStateImages(island.getState())
                .build();
        
        ActionInterface findAction = actionService.getAction(findOptions);
        findAction.perform(result, worldImages);
        
        return result.isSuccess();
    }
}
```

## Key Changes in v1.1.0

The transition examples have been updated to use the new ActionConfig API:

1. **Dependency Injection**: Use `ActionService` instead of `Action` class
2. **Type-Safe Configurations**: 
   - `ClickOptions` for click actions
   - `PatternFindOptions` for find operations
3. **Clear Action Intent**: 
   - `setNumberOfClicks(2)` instead of `setTimesToRepeatIndividualAction(2)`
   - `setPauseBetweenClicks(0.2)` instead of `setPauseBetweenActions(.2)`
4. **Explicit Action Creation**: Use `actionService.getAction()` to get the appropriate action implementation

For more details on migrating from ActionOptions to ActionConfig, see the [Migration Guide](/docs/03-core-library/guides/migration-guide).