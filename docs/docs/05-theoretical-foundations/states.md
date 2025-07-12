---
sidebar_position: 4
title: 'States'
---

# States

## Introduction

In model-based GUI automation, the environment is represented by a **state structure**, which provides a complete map of the problem space. States are a fundamental component of this structure, representing conceptually cohesive sections of the graphical user interface. This approach moves away from fragile, sequential scripts and toward a more robust, explicit model of the GUI environment.

## The State Structure (Ω)

The formal model of the GUI environment is defined by the state structure **Ω = (E, S, T)**.

* **E** is the set of all GUI elements chosen to model the environment, such as images, regions, or locations.
* **S** is the set of all GUI states. Each state is defined as a collection of related GUI elements (a subset of E). Multiple states can be active at the same time.
* **T** is the set of all transitions between states. Transitions are sequences of actions that change the set of active states.

## States in Brobot

### State Definition
In Brobot, a state is a collection of related GUI elements. These elements are often grouped spatially or appear together, and what constitutes a state is subjective and should make sense for the specific automation task.

A state is defined by its collection of elements, such as images or regions. For example, the `Home` state in the DoT test application is defined with a single image:

```java
// From the DoT test application in the paper
private StateImage toWorldButton = new StateImage.Builder()
    .addPatterns("toWorldButton")
    .setFixed(true)
    .addSnapshot(new MatchSnapshot(220, 600, 20, 20))
    .build(); 

private State state = new State.Builder(Name.HOME)
    .withImages(toWorldButton)
    .build(); 
```

### State Identification and Verification

A state is considered **active** if one or more of its defining elements are found in the visible GUI. The set of all currently active states is represented by S<sub>Ξ</sub>.

* **Activation**: An observation action that successfully finds an element will mark that element's parent state as active.
* **Deactivation**: States are not marked as inactive simply because an element is not found. Brobot relies on explicit **transitions** to deactivate states, which provides a more reliable mechanism for state management.

## Best Practices for State Design

* **Cohesiveness**: Group elements into a state that are logically related, appear together on the screen, or are used together in a process.
* **Context-Driven**: The definition of a state is subjective and should be tailored to the goals of the automation application.
* **Modularity**: The state-based approach allows states and transitions to be built, tested, and debugged independently. This modular design helps manage complexity and localize troubleshooting efforts.
* **Granularity**: Balancing the number of states is key. For instance, the paper notes that the DoT app's simple design was chosen for the example, but that a more granular `NewIsland` state might have been better for the specific automation task.