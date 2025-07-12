---
sidebar_position: 5
---

# Transitions

:::info Version Note
This tutorial was originally created for an earlier version of Brobot but has been updated for version 1.1.0. The original code examples are available in documentation versions 1.0.6 and 1.0.7.
:::

Transitions allow Brobot to move from one state to another. Any state that 
will be accessed needs a StateTransitions class.  

## Home Transitions

```java
@Component
@RequiredArgsConstructor
public class HomeTransitions {
    
    private final Action action;
    private final Home home;
    
    public StateTransitions getStateTransitions() {
        return new StateTransitions.Builder(Home.Name.HOME.toString())
                .addTransition(createWorldTransition())
                .build();
    }
    
    private JavaStateTransition createWorldTransition() {
        return new JavaStateTransition.Builder()
                .setFunction(this::goToWorld)
                .addToActivate(World.Name.WORLD.toString())
                .build();
    }
    
    private boolean goToWorld() {
        return action.click(home.getToWorldButton()).isSuccess();
    }

}
```


## World Transitions

We use the builder for the transition from World to Island since it requires
a special option: in this transition the World state stays visible.  

```java
@Component
@RequiredArgsConstructor
public class WorldTransitions {
    
    private final Action action;
    private final World world;
    
    public StateTransitions getStateTransitions() {
        return new StateTransitions.Builder(World.Name.WORLD.toString())
                .addTransitionFinish(this::finishTransition)
                .addTransition(createIslandTransition())
                .build();
    }
    
    private JavaStateTransition createIslandTransition() {
        return new JavaStateTransition.Builder()
                .setFunction(this::goToIsland)
                .addToActivate(Island.Name.ISLAND.toString())
                .setStaysVisibleAfterTransition(true)
                .build();
    }
    
    private boolean finishTransition() {
        // Using convenience method for find
        return action.find(world.getSearchButton()).isSuccess();
    }
    
    public boolean goToIsland() {
        // Using fluent API for action chaining
        ClickOptions clickTwice = new ClickOptions.Builder()
                .setClicks(2)
                .setPauseBetweenClicks(0.2)
                .build();
        return action.perform(clickTwice, world.getSearchButton()).isSuccess();
    }

}
```

## Island Transitions

```java
@Component
@RequiredArgsConstructor
public class IslandTransitions {
    
    private final Action action;
    private final Island island;
    
    public StateTransitions getStateTransitions() {
        return new StateTransitions.Builder(Island.Name.ISLAND.toString())
                .addTransitionFinish(this::finishTransition)
                .build();
    }
    
    private boolean finishTransition() {
        // Using convenience method to verify state
        return action.find(island.getIslandName()).isSuccess();
    }

}
```
