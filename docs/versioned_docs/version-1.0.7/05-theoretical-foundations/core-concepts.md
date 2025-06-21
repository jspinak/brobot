---
sidebar_position: 2
title: 'Core Concepts of the Model-Based Approach'
---

# Core Concepts of the Model-Based Approach

Model-based GUI automation addresses the limitations of traditional methods by redefining the problem. Instead of creating sequential scripts, the developer builds an explicit model of the GUI environment itself. This approach is inspired by principles from robotics, human cognition, and graph theory.

## The Explicit Model: A Digital Twin

At its core, model-based GUI automation transforms the developer's implicit mental model of the GUI into an explicit, machine-readable format. This explicit model, called the **state structure (Ω)**, acts as a map of the problem space.

This applied model functions as a **digital twin** of the GUI environment. It is a virtual representation that can be used to predict and interact with the digital environment in the same way a traditional digital twin represents a physical system.

## Domain vs. Strategic Knowledge

The framework is built on a separation of knowledge into two distinct categories, a concept adapted from studies of human problem-solving:

1.  **Domain Knowledge (Ω)**: This is specific to the problem and represents the GUI environment being automated. It is the "what" and "where"—what states and elements exist, and what transitions are possible between them. In the Brobot framework, the user provides this knowledge by defining states and transitions for their specific application.

2.  **Strategic Knowledge (F)**: This is problem-agnostic knowledge used to understand and manipulate the environment. It includes the "how"—pathfinding, path traversal, state management, and action execution. This strategic logic is handled by the framework itself, not the user's application code.

This separation allows the automation developer to focus on defining their application's GUI (the domain) and business logic, while the framework handles the complex strategies for navigation and interaction.

## The Visual API

The combination of the user-defined state structure (Ω) and the framework (F) creates what the paper calls a **Visual API**. This API abstracts away the complexities of visual automation, allowing the user's application to interact with the GUI at a high level, similar to how a programmatic API works. The automation instructions can simply request a navigation to a specific state, and the Visual API handles the low-level recognition and execution required to get there.