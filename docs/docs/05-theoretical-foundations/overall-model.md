---
sidebar_position: 3
title: 'The Overall Model'
---

# The Overall Model

The paper presents a formal mathematical model of GUI automation that provides a rigorous foundation for the framework. This model unifies the core components into a cohesive system, specifying how the system perceives the GUI, manages state, performs actions, and navigates the interface.

## Formal Definition

The overall model of GUI automation is defined as a tuple **(Ξ, Ω, a, M, τ, §)**.

The components are:

* **Ξ**: The **visible GUI**, representing the current pixel output of the screen. This is the real-time data the system uses to perceive and interact with the interface.
* **Ω**: The **state structure**, which is an abstraction of the GUI environment. This is the developer-defined map of all relevant elements, states, and transitions.
* **a**: The **action model**, for performing individual, atomic operations on the GUI like clicking or typing.
* **M**: The **state management system**, responsible for maintaining the set of currently active states.
* **τ**: The **transition model**, for executing sequences of actions (processes) that lead to changes between states.
* **§**: The **path traversal model**, used for finding and executing a path through the state graph to reach a target state.

## Architecture and Data Flow

The architecture separates environment representation (Domain Knowledge) from interaction mechanisms (Strategic Knowledge).

* The **Action Model (a)** bridges both domains. It receives input from the user's defined state elements, acts on the visible GUI (Ξ), and provides results that inform the State Management System (M).
* The **State Management System (M)** tracks the set of active states based on information from actions and transitions.
* The **Path Traversal Model (§)** takes the current active states from M and uses the map provided by the State Structure (Ω) to find and execute a path to a target state.

This architecture allows the framework to handle the complex, reusable strategic logic while the user's application focuses only on domain knowledge and business logic.