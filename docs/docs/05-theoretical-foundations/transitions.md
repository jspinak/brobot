---
sidebar_position: 5
title: 'Transitions'
---

# Transitions

## Introduction

In model-based GUI automation, a transition represents the pathway from one state to another. Unlike states, which represent a static configuration of the GUI, transitions define the dynamic sequences of actions that cause the GUI to change. They are a fundamental part of the state structure, forming the edges in the graph of the GUI environment.

## Formal Definition

The paper's formal model defines a transition as a tuple t = (A, S&lt;sub>t&lt;/sub>&lt;sup>def&lt;/sup>) where:

A is a process or sequence of actions (a&lt;sup>1&lt;/sup>, a&lt;sup>2&lt;/sup>, ..., a&lt;sup>n&lt;/sup>) that are executed as part of the transition. These are the concrete steps, like clicks and keyboard inputs, that manipulate the GUI.
S&lt;sub>t&lt;/sub>&lt;sup>def&lt;/sup> is the intended state information that is applied if the transition succeeds. This explicitly defines which states should become active or inactive upon the successful completion of the action sequence.
The transition function, f&lt;sub>τ&lt;/sub>, processes this tuple to produce a new GUI state and a result indicating success or failure. By including defined state changes (S&lt;sub>t&lt;/sub>&lt;sup>def&lt;/sup>), transitions can significantly streamline state management, eliminating the need for extra verification steps.

## Transitions in Brobot

### Implementation

Brobot implements the transition model by separating it into two parts: a **FromTransition** and a **ToTransition**.

* A **FromTransition** contains the initial actions required to move from a specific starting state towards a target state.
* A **ToTransition** contains the actions that always occur to finalize the entry into the target state, regardless of where the transition began.

For example, to go from `State 1` to `State 6`, the framework first executes the `FromTransition` for `State 1 to State 6` and then completes it by executing the `ToTransition` for `to State 6`.

### Code Example

The DoT application demonstrates how transitions are defined in the framework. A transition is built by specifying its target state and the function that performs the necessary actions.

```java
// From the DoT test application in the paper
public class WorldTransitions {
    // ...
    StateTransitions transitions =
        new StateTransitions.Builder(WORLD) // Defines transitions from the WORLD state
            .addTransition(new StateTransition.Builder()
                .addToActivate(ISLAND) // The target state is ISLAND
                .setFunction(this::goToIsland) // The method to execute
                .setStaysVisibleAfterTransition(TRUE) // The WORLD state remains active
                .build())
            .build(); 
    // ...
    public boolean goToIsland() {
        // The action sequence for this transition is a single click
        return action.perform(
            CLICK, world.getSearchButton()
        ).isSuccess(); 
    }
}
```

## The Role of Transitions in Path Traversal

Transitions are the building blocks of paths in the state graph. The path traversal model is responsible for finding and executing a sequence of transitions to move the GUI to a desired target state.

If a transition in the selected path fails, the framework can recalculate to find a new path from the current set of active states, providing robustness against automation failures. The framework can also use a cost assigned to each transition as part of a heuristic to select the most efficient or reliable path.