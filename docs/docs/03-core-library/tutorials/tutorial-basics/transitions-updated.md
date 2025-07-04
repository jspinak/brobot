---
sidebar_position: 5
---

# Transitions (Updated API)

Transitions allow Brobot to move from one state to another. Any state that 
will be accessed needs a StateTransitions class.  

This guide has been updated to use the new ActionConfig API introduced in Brobot 2.0.

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
        // NEW API: Use ClickOptions
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
        // NEW API: Use PatternFindOptions
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
        // NEW API: Use ClickOptions with multiple clicks
        ClickOptions clickTwice = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .setNumberOfClicks(2)
                .setPauseBetweenClicks(0.2)
                .build();
                
        ActionResult result = new ActionResult();
        result.setActionConfig(clickTwice);
        
        ObjectCollection searchButton = new ObjectCollection.Builder()
                .withImages(world.getSearchButton())
                .build();
                
        ActionInterface clickAction = actionService.getAction(clickTwice);
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
                .addTransition(this::goToHome, HOME)
                .build();
        stateTransitionsRepository.add(transitions);
    }
    
    private boolean goToHome() {
        // NEW API: Use ClickOptions
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .setPauseAfterEnd(1.0) // Wait for page to load
                .build();
                
        ActionResult result = new ActionResult();
        result.setActionConfig(clickOptions);
        
        ObjectCollection homeButton = new ObjectCollection.Builder()
                .withImages(island.getHomeButton())
                .build();
                
        ActionInterface clickAction = actionService.getAction(clickOptions);
        clickAction.perform(result, homeButton);
        
        return result.isSuccess();
    }
}
```

## Using ActionDefinitions for Complex Transitions

For more complex transitions, you can use TaskSequence (formerly ActionDefinition) to define multi-step actions:

```java
@Component
public class ComplexTransitions {
    
    private final StateTransitionsRepository stateTransitionsRepository;
    
    public ComplexTransitions(StateTransitionsRepository stateTransitionsRepository) {
        this.stateTransitionsRepository = stateTransitionsRepository;
        
        // Create a complex transition using TaskSequence
        TaskSequence loginSequence = createLoginSequence();
        
        TaskSequenceStateTransition transition = new TaskSequenceStateTransition();
        transition.setActionDefinition(loginSequence);
        transition.setActivate(Collections.singleton(MAIN_MENU_STATE_ID));
        
        StateTransitions stateTransitions = new StateTransitions.Builder(LOGIN_STATE)
                .addTransition(transition)
                .build();
                
        stateTransitionsRepository.add(stateTransitions);
    }
    
    private TaskSequence createLoginSequence() {
        TaskSequence sequence = new TaskSequence();
        
        // Step 1: Find username field
        PatternFindOptions findUsername = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setSimilarity(0.9)
                .build();
        sequence.addStep(findUsername, new ObjectCollection.Builder()
                .withImages(new StateImage.Builder().setName("UsernameField").build())
                .build());
        
        // Step 2: Click username field
        ClickOptions clickUsername = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .build();
        sequence.addStep(clickUsername, new ObjectCollection.Builder()
                .useMatchesFromPreviousAction()
                .build());
        
        // Step 3: Type username
        TypeOptions typeUsername = new TypeOptions.Builder()
                .setModifierDelay(0.05)
                .build();
        sequence.addStep(typeUsername, new ObjectCollection.Builder()
                .withStrings("myusername")
                .build());
        
        // Step 4: Find and click password field
        PatternFindOptions findPassword = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();
        sequence.addStep(findPassword, new ObjectCollection.Builder()
                .withImages(new StateImage.Builder().setName("PasswordField").build())
                .build());
        
        ClickOptions clickPassword = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .build();
        sequence.addStep(clickPassword, new ObjectCollection.Builder()
                .useMatchesFromPreviousAction()
                .build());
        
        // Step 5: Type password
        TypeOptions typePassword = new TypeOptions.Builder()
                .setModifierDelay(0.05)
                .build();
        sequence.addStep(typePassword, new ObjectCollection.Builder()
                .withStrings("mypassword")
                .build());
        
        // Step 6: Click login button
        ClickOptions clickLogin = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .setPauseAfterEnd(2.0) // Wait for login to complete
                .build();
        sequence.addStep(clickLogin, new ObjectCollection.Builder()
                .withImages(new StateImage.Builder().setName("LoginButton").build())
                .build());
        
        return sequence;
    }
}
```

## Key Changes in the New API

1. **ActionService instead of Action**: Use `ActionService` to get the appropriate action implementation
2. **Specific Config Classes**: Use `ClickOptions`, `PatternFindOptions`, etc. instead of generic `ActionOptions`
3. **ActionResult**: Create an `ActionResult` and set the config before performing actions
4. **Type Safety**: Each config class only exposes relevant options
5. **Builder Pattern**: Consistent builder pattern across all config classes

## Benefits of the New API

- **Type Safety**: Compile-time checking of configuration options
- **Better IDE Support**: Auto-completion shows only relevant options
- **Clearer Intent**: Config class names clearly indicate the action type
- **Easier Testing**: Mock specific action types more easily
- **Future Proof**: New action types can be added without breaking existing code

## Migration Tips

When migrating from the old API:

1. Replace `ActionOptions.Action.CLICK` → `ClickOptions`
2. Replace `ActionOptions.Action.FIND` → `PatternFindOptions` 
3. Replace `Action.perform()` → `ActionService.getAction().perform()`
4. Use `ActionResult.setActionConfig()` instead of passing options to perform()
5. For repeated actions, use config-specific options like `setNumberOfClicks()` instead of `setTimesToRepeatIndividualAction()`