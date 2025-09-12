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
   - **ToTransitions**: Verify arrival AT any state (e.g., ToPricing)
5. Generate a Spring Boot application with proper navigation

## Important: Brobot's Model-Based Architecture

### ⚠️ CRITICAL: Only Use Brobot API - NO External Functions

**NEVER use these (they break model-based automation):**
- ❌ `Thread.sleep()` - Breaks mock testing completely
- ❌ `java.awt.Robot` - Circumvents Brobot's model
- ❌ Direct SikuliX calls - Bypasses wrapper functions
- ❌ Any system automation outside Brobot API

**ALWAYS use Brobot's API:**
- ✅ `action.pause(2.0)` instead of `Thread.sleep(2000)`
- ✅ `action.move(location)` instead of Robot.mouseMove()
- ✅ `action.find(stateImage)` instead of Screen.wait()

Using external functions **nullifies ALL benefits** of model-based GUI automation and makes mock testing impossible.

### Navigation System
- **NEVER** call transitions directly (don't do `transition.execute()`)
- Use `Navigation.goToState("StateName")` to navigate between states
- Brobot automatically finds the path from current state to target state
- Brobot executes all necessary transitions in the correct order

### State Naming Convention
- Classes should be named with "State" suffix: `MenuState`, `PricingState`, `HomepageState`
- @State annotation automatically removes "State" from the name
- When navigating, use the name WITHOUT "State":
  - Class: `PricingState` → Navigate with: `navigation.goToState("Pricing")`
  - Class: `HomepageState` → Navigate with: `navigation.goToState("Homepage")`
  - Class: `MenuState` → Navigate with: `navigation.goToState("Menu")`

### Transition Organization

Each state should have ONE TransitionSet class containing ALL its transitions:

```java
@TransitionSet(state = PricingState.class)
@RequiredArgsConstructor
public class PricingTransitions {
    private final MenuState menuState;
    private final PricingState pricingState;
    private final Action action;
    
    // FromTransition - How to get TO Pricing FROM Menu
    @FromTransition(from = MenuState.class)
    public boolean fromMenu() {
        return action.click(menuState.getPricingButton()).isSuccess();
    }
    
    // FromTransition - How to get TO Pricing FROM Homepage
    @FromTransition(from = HomepageState.class)
    public boolean fromHomepage() {
        return action.click(homepageState.getPricingLink()).isSuccess();
    }
    
    // ToTransition - Verify ARRIVAL at Pricing
    @ToTransition
    public boolean verifyArrival() {
        return action.find(pricingState.getUniqueElement()).isSuccess();
    }
}
```

### Navigation Example
```java
// WRONG - Don't call transitions directly
menuToPricingTransition.execute();

// CORRECT - Use Navigation (executes both transitions automatically)
navigation.goToState("Pricing");  // Note: "Pricing" not "PricingState"
```

For more details on navigation, see:
- [States Overview](states.md)
- [Transitions Overview](transitions.md)
- [State Management Architecture](../03-core-library/architecture/initial-state-handling.md)

## Tips

- **Clear Screenshots**: Crop images to show just the UI element
- **Descriptive Names**: Use names that describe what the element does
- **Multiple Versions**: Include different versions of the same element if they exist