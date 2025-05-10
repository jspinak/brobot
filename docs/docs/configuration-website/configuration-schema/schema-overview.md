---
sidebar_position: 3
---

# Schema Overview

Brobot Runner uses two complementary JSON schemas to define your automation projects. Understanding these schemas is 
essential for creating and modifying automation configurations effectively.

## Schema Types

Brobot Runner configuration is defined by two distinct but related schemas:

1. **Project Schema** - Defines the structural components of automation (states, transitions, UI elements)
2. **Automation DSL Schema** - Defines the programming language aspects (functions, statements, expressions)

### Relationship Between Schemas

The Project Schema defines "what" exists in your automation environment, while the Automation DSL Schema defines "how" 
your automation behaves. Together, they provide a complete definition of your automation project:

Project Schema (structure) + Automation DSL (behavior) = Complete Automation Solution

### Key Concepts

- **States** represent distinct screens or conditions in your application
- **Transitions** define how to move between states using actions
- **Actions** are operations performed on the GUI (clicking, typing, etc.)
- **Functions** contain custom automation logic using the DSL

## Working with the Schemas

### Project Creation Workflow

1. Define states with their visual elements (images, regions, etc.)
2. Create state transitions that define how to move between states
3. Define automation functions with custom logic
4. Configure UI elements (buttons) that trigger automation functions

### Best Practices

1. **Organize by feature**: Group related states and transitions
2. **Use descriptive names**: Make state and function names self-explanatory
3. **Reuse components**: Define common patterns and elements once
4. **Keep functions focused**: Each function should do one thing well
5. **Test incrementally**: Validate small changes before making larger ones

## Integration with Brobot Runner

The defined schemas are used by both the configuration website and desktop runner application:

1. **Configuration Website**: Used to create and edit your automation project
2. **Desktop Runner**: Executes the automation based on the configuration