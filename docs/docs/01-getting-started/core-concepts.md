---
sidebar_position: 2
title: 'Core Concepts'
---

# Modeling the GUI: States & Core Concepts

## States: Modeling the GUI

In model-based GUI automation, a State represents a distinct configuration of the user interface, such as a login screen, a main dashboard, or a pop-up dialog. A state is formally defined by a collection of GUI elements (like images or regions) that identify it. This approach allows you to model your application as a map of unique conditions rather than a simple sequence of actions.

[Learn more on the detailed States page...](states.md)

## The State Structure (Ω)

The GUI environment is organized into manageable parts, similar to how a website is structured with pages and links. Formally, this is called the **State Structure (Ω)** and is defined as a tuple `(E, S, T)`:

* **E**: The set of all GUI **Elements** selected to model the environment (images, regions, locations, and strings).
* **S**: The set of all **States** defined in the model.
* **T**: The set of all **Transitions** between states.

### Project Organization

Brobot projects typically use one of two organizational patterns:

1. **Organized by State** - Each state has its own package containing the state class, transitions class, and related automation code
2. **Organized by Type** - Separate packages for states, transitions, and automation instructions

For detailed examples and best practices on organizing your Brobot project, see the **[Project File Structure Guide](./file-structure.md)**.

## Actions and Transitions: Controlling the GUI

To move between states, you use **Transitions**. A transition is a defined sequence of actions that changes the GUI from one state to another.

* An **Action** is a single, atomic operation performed on the GUI, such as clicking a location, finding an image, or typing text.
* A **Transition** is a process composed of one or more actions. For example, a "Login" transition might consist of `type` and `click` actions.

You define these transitions for your states, essentially creating a map of how to navigate your application.

## Pathfinding: Intelligent Navigation

A key advantage of the model-based approach is that you do not need to manually code every possible path through your application. You only need to define the states and the direct transitions between them.

Brobot's **Path Traversal Model (§)** then acts as an intelligent navigator. When you request to go to a target state, the framework automatically finds the most efficient path and executes the necessary transitions. If a transition fails, the framework dynamically recalculates a new path from its current position. This makes the automation resilient to unexpected errors.

## State Management: Knowing Where You Are

To enable intelligent pathfinding, the framework must always have an accurate understanding of the GUI's current condition. This is handled by the **State Management System (M)**.

This system continuously observes the screen to determine which states are currently active. A crucial rule in Brobot's state management is how states are deactivated: a state is *only* marked as inactive after a successful transition explicitly deactivates it. A state is not considered inactive simply because its defining images temporarily disappear. Individual images can reliably signal a state's existence but not its absence; therefore, Brobot relies on transitions to deactivate states.

## State Finder and the Unknown State

In case Brobot gets lost and cannot find any of the active states, there is a State Finder that will search for active states. This is a costly operation
but will rebuild the list of active states in State Memory and allow the automation to continue.

There is also a possibility that something really unexpected happens and that
no states are active. In this case the UNKNOWN state will become active. The UNKNOWN state also has transitions and will attempt these transitions in order to find its way back to the target state. Any code meant to deal with unexpected situations in which no states are visible should go into the UNKNOWN state's transitions. 

## Handling Dynamic Overlays (Hidden States)

A common challenge in GUI automation is dealing with dynamic elements like menus or pop-up dialogs. Brobot handles this through a special mechanism called **Hidden States**.

When a state opens and obscures another (e.g., a menu opening over the main window), the covered state is registered as "hidden". You can then define a special dynamic transition to return to the previously visible state. Instead of a fixed target like `(menu -> main_window)`, you can define a transition like `(menu -> PREVIOUS)`.

When this transition is executed (e.g., by closing the menu), the framework knows to return to whatever state was registered as hidden, making your automation robust against dynamic overlays.

## The Action Hierarchy

All operations in Brobot, from a simple click to a complex drag-and-drop, are built on a three-level abstraction. At the lowest level, Sikuli Wrappers interface directly with the Sikuli library to control the mouse and keyboard. These are used to build Basic Actions, which are simple operations like finding an image. Finally, Composite Actions combine these basic blocks to create more complex behaviors like clicking until a specific condition is met.

[Learn more on the detailed Action Hierarchy page...](action-hierarchy.md)

## Intelligent Pathfinding

When navigating between states, Brobot uses a cost-based pathfinding system to automatically select the most efficient route. Each state and transition has an associated cost (default: 1), and the framework calculates the total cost of all possible paths to find the optimal route.

Key concepts:
- **Path Cost** = Sum of all state costs + Sum of all transition costs
- **Lower costs are preferred** - The path with the lowest total cost is automatically selected
- **Configurable costs** - Set custom costs to prefer certain routes or discourage expensive operations

This intelligent pathfinding means you can simply request to navigate to a target state, and Brobot will automatically find and execute the best path to get there.

[Learn more about pathfinding and path costs...](../03-core-library/user-guides/pathfinding-and-costs.md)

## State-Aware Scheduling

For continuous monitoring and background automation tasks, Brobot provides state-aware scheduling capabilities. This feature automatically validates and manages active states before executing scheduled tasks, ensuring your automation runs with the correct GUI context.

The `StateAwareScheduler` component wraps standard Java scheduling with intelligent state validation, allowing you to:
- Define required states that must be active before task execution
- Automatically rebuild states when they're missing
- Configure different behaviors for different automation scenarios

This is particularly useful for long-running automation, periodic maintenance tasks, and error recovery scenarios where maintaining state integrity is critical.

[Learn more about state-aware scheduling...](../03-core-library/user-guides/state-aware-scheduling.md)

## Benefits: Cleaner and Simpler Code

By modeling the environment with states and transitions, the underlying framework handles the complex logic of navigation, state tracking, and error recovery. Once your state structure is created, moving to any state is often a single line of code, such as `stateNavigator.openState("state-name");`. This allows you to concentrate on business logic rather than the complexities of dealing with an unpredictable GUI environment.