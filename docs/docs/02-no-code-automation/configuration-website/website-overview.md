---
sidebar_position: 2
---

# Brobot Configuration Website

The Brobot Configuration Website is a web-based interface that allows users to visually create, edit, and manage 
automation configurations for the Brobot framework. It serves as the design studio for your automation projects, 
with the Brobot Runner application handling the execution of these configurations.

This document outlines features that are a work in progress and may not be fully implemented yet.

## Overview

The Configuration Website provides an intuitive, visual approach to building model-based GUI automation without 
writing code. It allows you to:

- Design state structures visually
- Create and manage state transitions
- Define automation instructions through a graphical interface
- Export configurations for use with Brobot Runner
- Collaborate with team members on automation projects

## Relationship with Brobot Runner

The Configuration Website and Brobot Runner form a complementary system:

1. **Configuration Website**: Where you design and build your automation projects
2. **Brobot Runner**: Where you import, execute, and monitor those projects

This separation of concerns allows for:
- Specialized interfaces optimized for both design and execution
- Web-based collaboration during the design phase
- Secure, offline execution of sensitive automation tasks
- Clear versioning and deployment of automation configurations

## Key Features

### State Structure Builder

- **Visual state mapping**: Create states by adding visual elements through a drag-and-drop interface
- **Image management**: Upload, crop, and organize the images used to identify GUI states
- **Region definition**: Visually define regions of interest within states
- **State property editor**: Configure state properties, behaviors, and metadata

### Transition Designer

- **Visual transition mapping**: Connect states through a graphical network diagram
- **Transition condition builder**: Define when and how transitions should occur
- **Action sequence definition**: Specify the actions performed during transitions
- **Transition testing**: Validate transition logic with simulations

### Automation Instructions

- **Task builder**: Create sequences of automation instructions using visual components
- **Decision logic**: Add conditional branches and loops to automation flows
- **Parameterization**: Define variables and parameters for flexible automation
- **Function library**: Create and reuse custom functions across automation tasks

### Project Management

- **Multi-project support**: Organize automation configurations into separate projects
- **Version control**: Track changes and maintain version history of configurations
- **Collaboration tools**: Share projects and coordinate with team members
- **Export options**: Package configurations for use with Brobot Runner

### Configuration Validation

- **Real-time validation**: Immediate feedback on configuration correctness
- **Schema compliance**: Ensure that all configurations adhere to the required JSON schema
- **Reference checking**: Verify that all referenced states and transitions exist
- **Optimization suggestions**: Identify potential improvements for robustness and efficiency

## Configuration Format

The Configuration Website generates structured JSON files that conform to the Brobot framework's schema requirements:

### Project Metadata

```json
{
  "projectId": "example-project",
  "name": "Example Automation Project",
  "description": "Demonstrates automation capabilities",
  "version": "1.0.0",
  "createdAt": "2025-04-01T12:00:00Z",
  "updatedAt": "2025-04-10T15:30:00Z"
}
```

### States Configuration

```json
{
  "states": [
    {
      "id": "home-screen",
      "name": "Home Screen",
      "elements": [
        {
          "type": "image",
          "id": "home-logo",
          "imagePath": "images/home-logo.png",
          "searchRegion": {"x": 10, "y": 20, "width": 200, "height": 100}
        }
      ]
    },
    {
      "id": "login-form",
      "name": "Login Form",
      "elements": [
        {
          "type": "image",
          "id": "username-field",
          "imagePath": "images/username-field.png"
        },
        {
          "type": "image",
          "id": "password-field",
          "imagePath": "images/password-field.png"
        }
      ]
    }
  ]
}
```

### Transitions Configuration

```json
{
  "transitions": [
    {
      "id": "home-to-login",
      "sourceStateId": "home-screen",
      "targetStateId": "login-form",
      "action": {
        "type": "click",
        "targetElement": "login-button"
      }
    },
    {
      "id": "login-submit",
      "sourceStateId": "login-form",
      "targetStateId": "dashboard",
      "action": {
        "type": "sequence",
        "actions": [
          {
            "type": "type",
            "targetElement": "username-field",
            "text": "${username}"
          },
          {
            "type": "type",
            "targetElement": "password-field",
            "text": "${password}"
          },
          {
            "type": "click",
            "targetElement": "submit-button"
          }
        ]
      }
    }
  ]
}
```

### Automation Instructions

```json
{
  "automationInstructions": [
    {
      "id": "login-workflow",
      "name": "User Login",
      "description": "Logs in with provided credentials",
      "parameters": [
        {
          "name": "username",
          "type": "string",
          "required": true
        },
        {
          "name": "password",
          "type": "string",
          "required": true
        }
      ],
      "steps": [
        {
          "type": "navigateToState",
          "targetState": "login-form"
        },
        {
          "type": "executeTransition",
          "transitionId": "login-submit"
        },
        {
          "type": "verifyState",
          "expectedState": "dashboard",
          "timeoutSeconds": 10
        }
      ]
    }
  ]
}
```

## Workflow Between Configuration Website and Runner

The typical workflow between the Configuration Website and Brobot Runner follows these steps:

1. **Design Phase** (Configuration Website)
    - Create and define states based on application GUI
    - Establish transitions between states
    - Build automation instructions that utilize the state structure
    - Validate configurations for correctness
    - Export the complete configuration package

2. **Execution Phase** (Brobot Runner)
    - Import the configuration package
    - Set up execution parameters
    - Run automation tasks
    - Monitor progress and results
    - Generate reports and logs

3. **Refinement Phase** (Iterative)
    - Review execution results and identify issues
    - Return to Configuration Website to adjust states, transitions, or instructions
    - Export updated configurations
    - Re-test in the Runner

## Benefits of the Web-Based Configuration Approach

### Accessibility

- **No installation required**: Access the configuration tools through any modern web browser
- **Cross-platform compatibility**: Design on any OS without compatibility concerns
- **Centralized resource management**: Store and share images and configurations from a central location

### Collaboration

- **Team access**: Multiple team members can work on automation projects
- **Version control integration**: Track changes and manage versions
- **Role-based permissions**: Control who can view, edit, or export configurations

### Visualization

- **Interactive diagrams**: See the relationships between states and transitions
- **Visual feedback**: Immediate visual representation of your automation structure
- **Simulation capabilities**: Test logic without running actual automation

### Simplified Complexity

- **Abstraction of technical details**: Focus on automation logic rather than implementation
- **Guided configuration**: Templates and wizards for common automation patterns
- **Error prevention**: Built-in validation catches issues before they reach execution

## Use Cases

The Configuration Website is particularly valuable for:

- **Automation teams**: Collaborative development of automation solutions
- **Business analysts**: Defining automation requirements without coding
- **QA professionals**: Creating visual test cases for GUI testing
- **Consultants**: Designing automation solutions for clients

## Getting Started

To begin using the Configuration Website:

1. Access the website through your organization's URL or the public instance
2. Create a new project or open an existing one
3. Design your state structure by defining states and their visual elements
4. Create transitions to connect states
5. Build automation instructions that leverage your state structure
6. Export your configuration for use with Brobot Runner

## Integration with Existing Systems

The Configuration Website can be integrated with:

- **Version control systems**: GitHub, GitLab, Bitbucket
- **CI/CD pipelines**: Jenkins, GitHub Actions, GitLab CI
- **Test management tools**: TestRail, Zephyr, qTest
- **Issue tracking systems**: Jira, Azure DevOps

These integrations allow for automated exports, synchronization with test plans, and tracking of automation assets 
within your existing toolchain.

---

The Brobot Configuration Website transforms the process of creating model-based GUI automation from a coding exercise 
to a visual design activity. Combined with Brobot Runner for execution, it provides a complete solution for developing, 
deploying, and maintaining robust automation that can adapt to complex and changing GUI environments.