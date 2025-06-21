---
sidebar_position: 4
title: 'Transitions'
---

# Transitions

## Introduction

While States define "where you can be" in a GUI, **Transitions** define "how you get there." Every State that is reachable needs an associated `StateTransitions` class that defines the pathways to and from other states. 

Formally, a transition is a process or a sequence of actions that changes the GUI from one state to another.  They form the "edges" of the state graph and are the building blocks used by the framework's pathfinder to navigate the application. 

## The Brobot Implementation: FromTransition and ToTransition

Brobot implements this concept by splitting transitions into two types: **FromTransitions** and **ToTransitions**.  This two-part system provides a clear and reusable structure for managing navigation logic.

![Transition Diagram](/img/paper/transitions.png) 
_This diagram is based on Figure 8 from the research paper._ 

* **FromTransition**: This handles the process of leaving the *current* state to go to another state. For example, to go from `State A` to `State B`, the FromTransition `A -> B` is called. It contains the specific actions needed to initiate the move from State A.

* **ToTransition**: This handles the final steps of arriving at a new state, regardless of which state started the process. There can be multiple FromTransitions going to `State B` from different states, but there is only one ToTransition for `State B`. This `-> B` transition contains generic actions that must always run to confirm `State B` is active.

### Defining Transitions in Code

In the `StateTransitions` Builder, you define these two types of transitions using specific commands:
* `addTransitionFinish` creates the ToTransition. The method passed to it is typically named `finishTransition()`. 
* `addTransition` creates a FromTransition. The methods passed to it can have any name and contain the actions for that specific path. 

```java
// From the DoT test application in the paper 
public class WorldTransitions {
    // ...
    StateTransitions transitions =
        new StateTransitions.Builder(WORLD) // Defines transitions from the WORLD state
            .addTransitionFinish(this::finishTransition) // This is the ToTransition for WORLD
            .addTransition(new StateTransition.Builder() // This is a FromTransition
                .addToActivate(ISLAND) // The target state is ISLAND
                .setFunction(this::goToIsland) // The method with the actions
                .build())
            .build();
    // ...
}
```

## The Formal Model (Under the Hood)

The academic paper provides a formal definition for a transition as a tuple **t = (A, S<sub>t</sub><sup>def</sup>)**. 

* **A** is a **process**, which is a sequence of one or more actions `(a¹, a², ..., aⁿ)`.  This corresponds to the method you pass to the builder (e.g., `goToIsland`).
* **S<sub>t</sub><sup>def</sup>** is the **intended state information**.  This is an explicit definition of which states should become active or inactive if the transition succeeds. This makes state management more robust and predictable, as the framework doesn't have to guess the outcome.  This corresponds to builder methods like `.addToActivate(ISLAND)`. 

## Dynamic Transitions for Hidden States

In addition to these statically defined transitions, Brobot also supports dynamic transitions to handle common UI patterns like menus and pop-ups. When a state opens and covers another, the covered state is registered as "hidden."

You can then define a transition with a dynamic target called **`PREVIOUS`**. When this transition is executed (e.g., by closing the menu), the framework intelligently navigates back to whatever state was most recently hidden, without you needing to pre-define every possible combination.



