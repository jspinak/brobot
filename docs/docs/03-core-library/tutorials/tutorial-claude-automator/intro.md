# Claude Automator Tutorial

:::info Version Note
This tutorial demonstrates the latest Brobot patterns and features for version 1.1.0. It uses modern ActionConfig classes, fluent API, and enhanced developer experience improvements.
:::

## Overview

This tutorial walks through creating a complete Brobot automation that monitors and interacts with Claude AI. It demonstrates:

- Modern state creation patterns with direct component access
- JavaStateTransition for code-based transitions
- Fluent API with action chaining
- Spring Boot dependency injection
- Continuous monitoring automation
- Enhanced developer experience features

## What You'll Build

The Claude Automator application:

1. **Monitors Claude AI Interface**: Watches for Claude's response icon to appear/disappear
2. **Manages Conversation Flow**: Automatically reopens the Working state when Claude finishes responding
3. **Handles Prompt Interaction**: Clicks on prompts and types "continue" to maintain conversation flow

## Key Concepts Demonstrated

- **State-based Architecture**: Two states representing different UI screens
- **Modern Transitions**: Using JavaStateTransition for flexible state navigation
- **Action Chaining**: Combining find, click, and type actions in a single fluent call
- **Active State Management**: Using StateMemory to track and manage active states
- **Convenience Methods**: Leveraging new API improvements for cleaner code

## Prerequisites

- Java 21 or higher
- Gradle
- Basic understanding of Brobot concepts (States, Transitions, Actions)
- Claude AI interface for testing

## Tutorial Structure

1. **Project Setup**: Configure Gradle with local Brobot library
2. **State Implementation**: Create Working and Prompt states with modern patterns
3. **Transitions**: Implement state transitions using JavaStateTransition
4. **Automation Logic**: Build continuous monitoring system
5. **Configuration**: Wire everything together with Spring Boot
6. **Running the Application**: Test the complete automation

Let's begin!