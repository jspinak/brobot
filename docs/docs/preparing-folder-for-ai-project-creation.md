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
https://github.com/jspinak/brobot/docs/docs/ai-brobot-project-creation.md

## Example Prompt

```
Can you create a Brobot application in the folder web-scraper? 

I have these images:
- search-box.png - should be part of SearchState
- results-list.png - should be part of ResultsState  
- next-button.png - should be part of ResultsState

[Optional: Describe what the automation should do]
```

## What Happens Next

The AI will:
1. Create proper project structure
2. Move your images to organized folders
3. Create State classes for your UI screens
4. Generate a basic Spring Boot application

## Tips

- **Clear Screenshots**: Crop images to show just the UI element
- **Descriptive Names**: Use names that describe what the element does
- **Multiple Versions**: Include different versions of the same element if they exist