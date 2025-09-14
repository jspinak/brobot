# Preparing a Folder for AI-Assisted Brobot Project Creation

## Overview
This guide helps you prepare a folder and prompt for an AI assistant to create a Brobot automation project.

## Folder Preparation

### 1. Create Project Directory
```bash
mkdir my-automation-project
cd my-automation-project
```

### 2. Add Your Screenshots
Place screenshots of UI elements you want to automate in the folder:
- Use descriptive names: `login-button.png`, `search-box.png`
- Ensure images clearly show the UI elements

## AI Instructions Location
The AI assistant has detailed instructions at:
https://jspinak.github.io/brobot/docs/getting-started/ai-brobot-project-creation

## Example Prompt

```
Can you create a Brobot application in the folder floranext? 

Here are instructions on creating a Brobot application: 
https://jspinak.github.io/brobot/docs/getting-started/ai-brobot-project-creation  

Images and states:
- all images starting with 'menu' are part of the menu state 
- pricing-start_for_free is part of the pricing state 
- start_for_free_big and enter_your_email are part of the homepage state

Transitions:
- the transition from menu to homepage happens when menu-floranext_icon is clicked and either start_for_free_big or enter_your_email is found
- the transition from menu to pricing happens when the image menu-pricing is clicked and the image pricing-start_for_free is found

The application should first go to the pricing page and click on pricing-start_for_free, and then go to the homepage and click on enter_your_email.
```

## What Happens Next

The AI will:
1. Create proper project structure
2. Move your images to organized folders
3. Create State classes for your UI screens
4. Create TWO types of Transition classes:
   - **FromTransitions**: Navigate FROM one state TO another (e.g., MenuToPricing)
   - **IncomingTransitions**: Verify arrival AT any state (e.g., ToPricing)
5. Generate a Spring Boot application with proper navigation

## Key Concepts for Your Brobot Application

The AI assistant will create a model-based GUI automation application following Brobot's architecture:

- **States**: Represent different screens/pages of your application
- **Transitions**: Define how to navigate between states
- **Navigation**: Brobot automatically finds paths between states
- **Mock Testing**: All actions can be tested without a real GUI

For technical details about Brobot's architecture, see:
- [States Overview](states.md)
- [Transitions Overview](transitions.md)
- [State Management Architecture](../03-core-library/architecture/initial-state-handling.md)

## Tips

- **Clear Screenshots**: Crop images to show just the UI element
- **Descriptive Names**: Use names that describe what the element does
- **Multiple Versions**: Include different versions of the same element if they exist